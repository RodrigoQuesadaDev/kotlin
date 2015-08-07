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
 * A map which maintains the order in which the elements were added and is optimized for small sizes.
 * This map is not synchronized and it does not support [remove]. Its [entrySet] is a set
 * which is also not synchronized and does not support any modification operations.
 */
@suppress("UNCHECKED_CAST", "BASE_WITH_NULLABLE_UPPER_BOUND", "IMPLICIT_CAST_TO_UNIT_OR_ANY", "CAST_NEVER_SUCCEEDS")
public class SmartMap<K, V> private constructor() : MutableMap<K, V> {
    public companion object {
        private val ARRAY_THRESHOLD = 5
        private val EMPTY_ARRAY = arrayOf<Any?>()

        public jvmStatic fun create<K, V>(): SmartMap<K, V> = SmartMap()

        public jvmStatic fun create<K, V>(map: Map<K, V>): SmartMap<K, V> = SmartMap<K, V>().apply { this.putAll(map) }
    }

    // array of alternating keys and values if size < threshold, linked hash map otherwise
    private var data: Any? = EMPTY_ARRAY

    private var size: Int = 0

    override fun put(key: K, value: V): V? {
        when {
            size < ARRAY_THRESHOLD -> {
                val arr = data as Array<Any?>
                val keysIndices = arr.indices step 2
                for (i in keysIndices) {
                    if (arr[i] == key) {
                        return (arr[i + 1] as V).apply { arr[i + 1] = value }
                    }
                }
                data = if (size == ARRAY_THRESHOLD - 1)
                    keysIndices.map { i -> arr[i] to arr[i + 1] }.toMap().apply {
                        (this as MutableMap).put(key, value)
                    }
                else Arrays.copyOf(arr, arr.size() + 2).apply {
                    set(size() - 2, key)
                    set(size() - 1, value)
                }
            }
            else -> {
                val map = data as MutableMap<K, V>
                return map.put(key, value).apply {
                    size = map.size()
                }
            }
        }

        size++
        return null
    }

    override fun get(key: Any?): V? = when {
        size < ARRAY_THRESHOLD -> {
            val arr = data as Array<Any?>
            val keysIndices = arr.indices step 2
            keysIndices.firstOrNull { i -> arr[i] == key }?.let { i -> arr[i + 1] }
        }
        else -> {
            val map = data as MutableMap<K, V>
            map[key]
        }
    } as V?

    override fun remove(key: Any?): V? =
            throw UnsupportedOperationException("This map does not support 'remove'.")

    override fun putAll(m: Map<out K, V>) = m.entrySet().forEach { put(it.key, it.value) }

    override fun clear() {
        data = EMPTY_ARRAY
        size = 0
    }

    override fun size(): Int = size

    override fun isEmpty(): Boolean = size == 0

    override fun containsKey(key: Any?): Boolean = key in keySet()

    override fun containsValue(value: Any?): Boolean = value in values()

    override fun entrySet(): MutableSet<MutableMap.MutableEntry<K, V>> =
            if (size < ARRAY_THRESHOLD) ArrayEntrySet() else (data as MutableMap<K, V>).entrySet()

    override fun keySet(): MutableSet<K> =
            if (size < ARRAY_THRESHOLD) ArrayKeySet() else (data as MutableMap<K, V>).keySet()

    override fun values(): MutableCollection<V> =
            if (size < ARRAY_THRESHOLD) ArrayValues() else (data as MutableMap<K, V>).values()

    override fun hashCode(): Int = entrySet().sumBy(Any::hashCode)

    override fun equals(other: Any?) = other is Map<*, *> && entrySet() == other.entrySet()

    private inner class ArrayEntrySet : AbstractSet<MutableMap.MutableEntry<K, V>>() {
        override fun size(): Int = this@SmartMap.size()

        override fun iterator() = when {
            isEmpty() -> {
                EmptyIterator as MutableIterator<MutableMap.MutableEntry<K, V>>
            }
            size() >= ARRAY_THRESHOLD -> {
                (data as MutableMap<K, V>).entrySet().iterator()
            }
            else -> object : MutableIterator<MutableMap.MutableEntry<K, V>> {
                private var i = 0

                override fun next(): MutableMap.MutableEntry<K, V> {
                    val arr = data as Array<Any?>
                    val i = i.apply { i += 2 }
                    return try {
                        AbstractMap.SimpleEntry(arr[i] as K, arr[i + 1] as V)
                    }
                    catch (e: ArrayIndexOutOfBoundsException) {
                        throw NoSuchElementException()
                    }
                }

                override fun hasNext(): Boolean = i < (data as Array<Any?>).size()

                override fun remove(): Unit =
                        throw UnsupportedOperationException("This iterator does not support 'remove'.")
            }
        }
    }

    private inner class ArrayKeySet : AbstractSet<K>() {
        override fun size(): Int = this@SmartMap.size()

        override fun iterator(): MutableIterator<K> = object : MutableIterator<K> {
            private var it = entrySet().iterator()

            override fun next(): K = it.next().key

            override fun hasNext(): Boolean = it.hasNext()

            override fun remove() = it.remove()
        }
    }

    private inner class ArrayValues : AbstractCollection<V>() {
        override fun size(): Int = this@SmartMap.size()

        override fun iterator(): MutableIterator<V> = object : MutableIterator<V> {
            private var it = entrySet().iterator()

            override fun next(): V = it.next().value

            override fun hasNext(): Boolean = it.hasNext()

            override fun remove() = it.remove()
        }
    }
}
