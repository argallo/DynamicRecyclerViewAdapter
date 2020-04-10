package com.gallo.dynamicrecyclerviewadapter
import android.widget.TextView
import com.gallo.dynamicrecyclerviewadapterlib.*

data class ExampleViewModel(
    val text: String
): ViewModel(ExampleViewHolder::class)

interface ExampleDelegate: Delegate {
    fun didSelectExample(index: Int)
}

class ExampleViewHolder(creator: FactoryCreator, private val delegate: ExampleDelegate? = creator.delegate as? ExampleDelegate):
    ViewHolder<ExampleViewModel>(creator.inflater, creator.parent, R.layout.example_layout) {
    companion object: Factory<ExampleViewHolder> {
        override fun create(creator: FactoryCreator): ExampleViewHolder = ExampleViewHolder(creator)
    }

    init {
        //delegate for recyclerview
        view.setOnClickListener {
           delegate?.didSelectExample(adapterPosition)
        }
    }

    override fun apply(viewModel: ExampleViewModel) {
        val textView: TextView = view.findViewById(R.id.textView)
        textView.text = viewModel.text
    }
}