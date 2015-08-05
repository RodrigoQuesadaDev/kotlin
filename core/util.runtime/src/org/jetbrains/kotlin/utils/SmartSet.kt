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

package org.jetbrains.kotlin.utils

import java.util.*

/**
 * A set which maintains the order in which the elements were added and is optimized for small sizes.
 * This set is not synchronized and it does not support removal operations such as [MutableSet.remove],
 * [MutableSet.removeAll] and [MutableSet.retainAll].
 * Also, [iterator] returns an iterator which does not support [MutableIterator.remove].
 */
@suppress("UNCHECKED_CAST")
public class SmartSet<T> private constructor() : AbstractSet<T>() {
    public companion object {
        private val ARRAY_THRESHOLD = 5

        public jvmStatic fun create<T>(): SmartSet<T> = SmartSet()

        public jvmStatic fun create<T>(set: Set<T>): SmartSet<T> = SmartSet<T>().apply { this.addAll(set) }
    }

    // null if size = 0, object if size = 1, array of objects if size < threshold, linked hash set otherwise
    private var obj: Any? = null

    private var size: Int = 0

    override fun iterator(): MutableIterator<T> = when {
        size == 0 -> emptySet<T>().iterator()
        size == 1 -> SingletonIterator(obj as T)
        size < ARRAY_THRESHOLD -> (obj as Array<T>).iterator()
        else -> (obj as MutableSet<T>).iterator()
    } as MutableIterator<T>

    override fun add(e: T): Boolean {
        when {
            size == 0 -> {
                obj = e
            }
            size == 1 -> {
                if (obj == e) return false
                obj = arrayOf(obj, e)
            }
            size < ARRAY_THRESHOLD -> {
                val arr = obj as Array<T>
                if (e in arr) return false
                obj = if (size == ARRAY_THRESHOLD - 1) linkedSetOf(*arr).apply { add(e) }
                else Arrays.copyOf(arr, size + 1).apply { set(size() - 1, e) }
            }
            else -> {
                val set = obj as MutableSet<T>
                if (!set.add(e)) return false
            }
        }

        size++
        return true
    }

    override fun clear() {
        obj = null
        size = 0
    }

    override fun size(): Int = size

    override fun contains(o: Any?): Boolean = when {
        size == 0 -> false
        size == 1 -> obj == o
        size < ARRAY_THRESHOLD -> o in obj as Array<T>
        else -> o in obj as Set<T>
    }

    private class SingletonIterator<T>(private val element: T) : Iterator<T> {
        private var hasNext = true

        override fun next(): T =
                if (hasNext) {
                    hasNext = false
                    element
                }
                else throw NoSuchElementException()

        override fun hasNext() = hasNext
    }
}
