/*
 * Copyright 2010-2015 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.kotlin.service

import org.jetbrains.kotlin.cli.common.CLICompiler
import org.jetbrains.kotlin.config.Services
import org.jetbrains.kotlin.incremental.components.LookupTracker
import org.jetbrains.kotlin.load.kotlin.incremental.components.IncrementalCache
import org.jetbrains.kotlin.load.kotlin.incremental.components.IncrementalCompilationComponents
import org.jetbrains.kotlin.rmi.COMPILER_SERVICE_RMI_NAME
import org.jetbrains.kotlin.rmi.CompileService
import org.jetbrains.kotlin.rmi.CompilerId
import org.jetbrains.kotlin.rmi.RemoteOutputStream
import org.jetbrains.kotlin.rmi.service.RemoteIncrementalCacheClient
import org.jetbrains.kotlin.rmi.service.RemoteOutputStreamClient
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.io.PrintStream
import java.net.URLClassLoader
import java.rmi.registry.Registry
import java.rmi.server.UnicastRemoteObject
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantReadWriteLock
import java.util.jar.Manifest
import java.util.logging.Logger
import kotlin.concurrent.read
import kotlin.concurrent.write


class CompileServiceImpl<Compiler: CLICompiler<*>>(val registry: Registry, val compiler: Compiler) : CompileService, UnicastRemoteObject() {

    private val rwlock = ReentrantReadWriteLock()
    private var alive = false
    private val selfCompilerId by lazy {
        // TODO: add classpath checksum calculated on init, add it to the interface and use to decide whether it was changed during the lifetime of the daemon
        CompilerId(
                compilerClasspath = System.getProperty("java.class.path")
                                            ?.split(File.pathSeparator)
                                            ?.map { File(it) }
                                            ?.filter { it.exists() }
                                            ?.map { it.absolutePath }
                                    ?: listOf(),
                compilerVersion = loadKotlinVersionFromResource()
        )
    }

    init {
        // assuming logically synchronized
        try {
            // cleanup for the case of incorrect restart
            UnicastRemoteObject.unexportObject(this, false)
        }
        catch (e: java.rmi.NoSuchObjectException) {
            // ignoring if object already exported
        }

        val stub = UnicastRemoteObject.exportObject(this, 0) as CompileService
        // TODO: use version-specific name
        registry.rebind (COMPILER_SERVICE_RMI_NAME, stub);
        alive = true
    }

    public class IncrementalCompilationComponentsImpl(val idToCache: Map<String, CompileService.RemoteIncrementalCache>): IncrementalCompilationComponents {
        // perf: cheap object, but still the pattern may be costly if there are too many calls to cache with the same id (which seems not to be the case now)
        override fun getIncrementalCache(moduleId: String): IncrementalCache = RemoteIncrementalCacheClient(idToCache[moduleId]!!)
        override fun getLookupTracker(): LookupTracker = LookupTracker.DO_NOTHING
    }

    private fun createCompileServices(incrementalCaches: Map<String, CompileService.RemoteIncrementalCache>): Services =
        Services.Builder()
                .register(javaClass<IncrementalCompilationComponents>(), IncrementalCompilationComponentsImpl(incrementalCaches))
//                .register(javaClass<CompilationCanceledStatus>(), object: CompilationCanceledStatus {
//                    override fun checkCanceled(): Unit = if (context.getCancelStatus().isCanceled()) throw CompilationCanceledException()
//                })
                .build()


    fun usedMemory(): Long {
        System.gc()
        val rt = Runtime.getRuntime()
        return (rt.totalMemory() - rt.freeMemory())
    }

    private fun loadKotlinVersionFromResource(): String {
        (javaClass.classLoader as? URLClassLoader)
        ?.findResource("META-INF/MANIFEST.MF")
        ?.let {
            try {
                return Manifest(it.openStream()).mainAttributes.getValue("Implementation-Version") ?: ""
            }
            catch (e: IOException) {}
        }
        return ""
    }


    fun<R> checkedCompile(args: Array<out String>, body: () -> R): R {
        try {
            if (args.none())
                throw IllegalArgumentException("Error: empty arguments list.")
            println("Starting compilation with args: " + args.joinToString(" "))
            val startMem = usedMemory() / 1024
            val startTime = System.nanoTime()
            val res = body()
            val endTime = System.nanoTime()
            val endMem = usedMemory() / 1024
            println("Done")
            println("Elapsed time: " + TimeUnit.NANOSECONDS.toMillis(endTime - startTime) + " ms")
            println("Used memory: $endMem kb (${"%+d".format(endMem - startMem)} kb)")
            return res
        }
        catch (e: Exception) {
            println("Error: $e")
            throw e
        }
    }

    fun<R> ifAlive(body: () -> R): R = rwlock.read {
        if (!alive) throw IllegalStateException("Kotlin Compiler Service is not in alive state")
        else body()
    }

    fun<R> ifAliveExclusive(body: () -> R): R = rwlock.write {
        if (!alive) throw IllegalStateException("Kotlin Compiler Service is not in alive state")
        else body()
    }

    fun<R> spy(msg: String, body: () -> R): R {
        val res = body()
        println(msg + " = " + res.toString())
        return res
    }

    override fun getCompilerId(): CompilerId = ifAlive { selfCompilerId }

    override fun getUsedMemory(): Long = ifAlive { usedMemory() }

    override fun shutdown() {
        ifAliveExclusive {
            println("Shutdown started")
            alive = false
            UnicastRemoteObject.unexportObject(this, true)
            println("Shutdown complete")
        }
    }

    override fun remoteCompile(args: Array<out String>, errStream: RemoteOutputStream, outputFormat: CompileService.OutputFormat): Int =
        ifAlive {
            checkedCompile(args) {
                val strm = RemoteOutputStreamClient(errStream)
                val printStrm = PrintStream(strm)
                when (outputFormat) {
                    CompileService.OutputFormat.PLAIN -> compiler.exec(printStrm, *args)
                    CompileService.OutputFormat.XML -> compiler.execAndOutputXml(printStrm, Services.EMPTY, *args)
                }.code
            }
        }

    override fun remoteIncrementalCompile(args: Array<String>, caches: Map<String, CompileService.RemoteIncrementalCache>, errStream: RemoteOutputStream, outputFormat: CompileService.OutputFormat): Int =
        ifAlive {
            checkedCompile(args) {
                val strm = RemoteOutputStreamClient(errStream)
                val printStrm = PrintStream(strm)
                when (outputFormat) {
                    CompileService.OutputFormat.PLAIN -> throw NotImplementedError("Only XML output is supported in remote incremental compilation")
                    CompileService.OutputFormat.XML -> compiler.execAndOutputXml(printStrm, createCompileServices(caches), *args)
                }.code
            }
        }
}
