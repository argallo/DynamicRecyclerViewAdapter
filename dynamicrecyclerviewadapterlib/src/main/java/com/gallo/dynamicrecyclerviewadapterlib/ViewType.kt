package com.gallo.dynamicrecyclerviewadapterlib

import java.util.concurrent.atomic.AtomicInteger
import kotlin.reflect.KClass
import kotlin.reflect.full.companionObjectInstance

/**
 * Helper object to associate ViewHolder which conforms to Holder (Factor<ViewHolder<ViewModel>>)
 */
@Suppress("UNCHECKED_CAST")
object ViewType {
    private var classMap = HashMap<KClass<*>, Int>()
    private val atomicInteger = AtomicInteger()

    fun ordinal(kclass: KClass<*>): Int {
        val identifier = classMap[kclass]
        return identifier ?: kotlin.run {
            val newKey = atomicInteger.getAndIncrement()
            classMap[kclass] = newKey
            newKey
        }
    }

    fun holder(kclass: KClass<*>): Holder {
        val viewHolder = kclass as KClass<ViewHolder<ViewModel>>
        return viewHolder.companionObjectInstance as Holder
    }
}

