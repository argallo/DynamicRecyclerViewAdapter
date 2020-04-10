package com.gallo.dynamicrecyclerviewadapterlib

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import java.lang.Exception
import kotlin.reflect.KClass

typealias Holder = Factory<ViewHolder<ViewModel>>
open class AdapterRecyclerView(var viewModels: List<ViewModel>) : RecyclerView.Adapter<ViewHolder<ViewModel>>() {
    private var register = HashMap<Int, Any>() //Leaving as Any because its annoying to cast every VH to Holder
    private var delegates = HashMap<Int, Delegate>()

    fun registerViewHolders(vararg viewTypes: KClass<*>) {
        viewTypes.forEach { register[ViewType.ordinal(it)] = ViewType.holder(it) }
    }

    fun registerDelegates(vararg viewTypes: KClass<*>, delegate: Delegate) {
        viewTypes.forEach { delegates[ViewType.ordinal(it)] = delegate }
    }

    fun registerVH(type: KClass<*>) {
        register[ViewType.ordinal(type)] = ViewType.holder(type)
    }

    fun registerDelegate(type: KClass<*>, delegate: Delegate) {
        delegates[ViewType.ordinal(type)] = delegate
    }

    fun updateViewModels(viewModels: List<ViewModel>, notifyDataSetChanged: Boolean = true) {
        this.viewModels = viewModels
        if (notifyDataSetChanged) this.notifyDataSetChanged()
    }

    fun updateViewModelAtIndex(viewModel: ViewModel, index: Int) {
        val mutableViewModels = this.viewModels.toMutableList()
        mutableViewModels[index] = viewModel
        this.viewModels = mutableViewModels
        notifyItemChanged(index)
    }

    @Suppress("UNCHECKED_CAST")
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder<ViewModel> {
        val inflater = LayoutInflater.from(parent.context)
        val type = register[viewType]
        val delegate = delegates[viewType]
        val f: Factory<ViewHolder<ViewModel>> = type!! as Holder
        return f.create(FactoryCreator(inflater, parent, delegate))
    }

    override fun onBindViewHolder(holder: ViewHolder<ViewModel>, position: Int) {
        holder.apply(viewModels[position])
    }

    override fun getItemCount(): Int = viewModels.size
    override fun getItemViewType(position: Int): Int = viewModels[position].viewType()
}

abstract class ViewModel(private val identifier: KClass<*>?) {
    fun viewType(): Int = identifier?.let { ViewType.ordinal(it) } ?: throw Exception()
}

interface Delegate

data class FactoryCreator(val inflater: LayoutInflater, val parent: ViewGroup, val delegate: Delegate? = null)
interface Factory<T> {
    fun create(creator: FactoryCreator): T
}

interface VMType<in T> {
    fun apply(viewModel: T)
}

abstract class ViewHolder<T: ViewModel>(val view: View) : RecyclerView.ViewHolder(view),
    VMType<T> {
    constructor(inflater: LayoutInflater, parent: ViewGroup, layout: Int) : this(inflater.inflate(layout, parent, false))
}