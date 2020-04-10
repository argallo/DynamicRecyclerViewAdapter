package com.gallo.dynamicrecyclerviewadapter

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gallo.dynamicrecyclerviewadapterlib.AdapterRecyclerView
import com.gallo.dynamicrecyclerviewadapterlib.ViewModel

class MainActivity : AppCompatActivity(), ExampleDelegate {
    object Identifiers {
        val ExampleViewHolder = ExampleViewHolder::class
    }

    private var adapterRecyclerView = AdapterRecyclerView(mutableListOf(ExampleViewModel("Hello there!"), ExampleViewModel("Goodbye!!")))
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(applicationContext)
        recyclerView.adapter = adapterRecyclerView

        adapterRecyclerView.registerVH(Identifiers.ExampleViewHolder)
        adapterRecyclerView.registerDelegate(Identifiers.ExampleViewHolder, this)
    }

    fun updateViewModels(viewModels: List<ViewModel>) {
        adapterRecyclerView.updateViewModels(viewModels)
    }

    fun updateViewModelAtIndex(viewModel: ViewModel, index: Int) {
        adapterRecyclerView.updateViewModelAtIndex(viewModel, index)
    }

    override fun didSelectExample(index: Int) {
        Log.d("MainActivity", "$index")
    }
}
