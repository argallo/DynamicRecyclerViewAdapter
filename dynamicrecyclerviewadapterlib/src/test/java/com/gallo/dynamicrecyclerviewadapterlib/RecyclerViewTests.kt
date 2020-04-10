package com.gallo.dynamicrecyclerviewadapterlib

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import io.mockk.every
import io.mockk.mockkClass
import io.mockk.mockkStatic
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class RecyclerViewTests {

    data class ExampleViewModel(
        val text: String
    ): ViewModel(ExampleViewHolder::class)

    interface ExampleDelegate: Delegate {
        fun didSelectExample(index: Int)
    }

    class ExampleViewHolder(creator: FactoryCreator, val delegate: ExampleDelegate? = creator.delegate as? ExampleDelegate):
        ViewHolder<ExampleViewModel>(creator.inflater, creator.parent, R.layout.example_layout) {
        companion object: Factory<ExampleViewHolder> {
            override fun create(creator: FactoryCreator): ExampleViewHolder = ExampleViewHolder(creator)
        }
        override fun apply(viewModel: ExampleViewModel) {
            val title = view.findViewById<TextView>(R.id.textView)
            title.text = viewModel.text
        }
    }


    data class ExampleViewModel2(
        val text: String
    ): ViewModel(ExampleViewHolder2::class)

    interface ExampleDelegate2: Delegate {
        fun didSelectExample2(index: Int)
    }

    class ExampleViewHolder2(creator: FactoryCreator, val delegate: ExampleDelegate2? = creator.delegate as? ExampleDelegate2):
        ViewHolder<ExampleViewModel2>(creator.inflater, creator.parent, R.layout.example_layout) {
        companion object: Factory<ExampleViewHolder2> {
            override fun create(creator: FactoryCreator): ExampleViewHolder2 = ExampleViewHolder2(creator)
        }
        override fun apply(viewModel: ExampleViewModel2) {
            val title = view.findViewById<TextView>(R.id.textView)
            title.text = viewModel.text
        }
    }

    class MockFragment: Fragment(), ExampleDelegate, ExampleDelegate2 {
        var viewModels: MutableList<ViewModel> = mutableListOf()
        val recyclerViewAdapter = AdapterRecyclerView(viewModels)
        var example1Pressed = -1
        var example2Pressed = -1

        override fun didSelectExample(index: Int) {
            example1Pressed = index
        }

        override fun didSelectExample2(index: Int) {
            example2Pressed = index
        }
    }

    lateinit var fragment: MockFragment

    @Before
    fun setup() {
        fragment = MockFragment()
    }

    @Test
    fun empty_view_models_passed_to_recycler() {
        Assert.assertEquals(0, fragment.recyclerViewAdapter.viewModels.size)
        Assert.assertEquals(0, fragment.recyclerViewAdapter.itemCount)
    }

    @Test
    fun view_models_create_holders() {
        // Registers both view holders to be used in the recyclerview
        fragment.recyclerViewAdapter.registerVH(ExampleViewHolder::class)
        fragment.recyclerViewAdapter.registerVH(ExampleViewHolder2::class)
        // Create list of viewModels associated with the registered viewHolder types
        val viewModels: MutableList<ViewModel> = mutableListOf(ExampleViewModel("test1"), ExampleViewModel2("test2"))
        fragment.recyclerViewAdapter.viewModels = viewModels
        // Test that the item count for our adapter is equal to the 2 viewmodels that were added
        Assert.assertEquals(2, fragment.recyclerViewAdapter.itemCount)
        // Test that the ordinal associated with a view type is correctly returned from the interface method getItemViewType
        Assert.assertEquals(ViewType.ordinal(ExampleViewHolder::class), fragment.recyclerViewAdapter.getItemViewType(0))
        Assert.assertEquals(ViewType.ordinal(ExampleViewHolder2::class), fragment.recyclerViewAdapter.getItemViewType(1))
    }

    @Test
    fun view_models_create_holders_together() {
        // Registers both view holders to be used in the recyclerview
        fragment.recyclerViewAdapter.registerViewHolders(ExampleViewHolder::class, ExampleViewHolder2::class)
        // Create list of viewModels associated with the registered viewHolder types
        val viewModels: MutableList<ViewModel> = mutableListOf(ExampleViewModel("test1"), ExampleViewModel2("test2"))
        fragment.recyclerViewAdapter.viewModels = viewModels
        // Test that the item count for our adapter is equal to the 2 viewmodels that were added
        Assert.assertEquals(2, fragment.recyclerViewAdapter.itemCount)
        // Test that the ordinal associated with a view type is correctly returned from the interface method getItemViewType
        Assert.assertEquals(ViewType.ordinal(ExampleViewHolder::class), fragment.recyclerViewAdapter.getItemViewType(0))
        Assert.assertEquals(ViewType.ordinal(ExampleViewHolder2::class), fragment.recyclerViewAdapter.getItemViewType(1))
    }

    @Test
    fun view_models_delegate_register() {
        // Registers view Holder
        fragment.recyclerViewAdapter.registerViewHolders(ExampleViewHolder::class)
        // Registers delegate callback
        fragment.recyclerViewAdapter.registerDelegate(ExampleViewHolder::class, fragment)

        //Generate view models list
        val viewModels: MutableList<ViewModel> = mutableListOf(ExampleViewModel("test1"))
        fragment.recyclerViewAdapter.viewModels = viewModels

        // Assert that cartLinePressed is -1
        Assert.assertEquals(-1, fragment.example1Pressed)

        mockkStatic("android.view.LayoutInflater")
        val layoutInfalter = mockkClass(LayoutInflater::class)
        every { LayoutInflater.from(any()) } returns layoutInfalter
        val layout = mockkClass(LinearLayout::class)
        every { layout.context } returns mockkClass(Context::class)
        every { layoutInfalter.inflate(any<Int>(), any(), any()) } returns mockkClass(View::class)
        val viewHolder = fragment.recyclerViewAdapter.onCreateViewHolder(layout, ViewType.ordinal(ExampleViewHolder::class))
        // Delegate should be set to the mock fragment class that was registered
        Assert.assertEquals((viewHolder as ExampleViewHolder).delegate!!::class, MockFragment::class)
        // Delegate is called with index 10
        (viewHolder as ExampleViewHolder).delegate?.didSelectExample(10)
        // Assert that cartLinePressed was set from the callback with index 10
        Assert.assertEquals(10, fragment.example1Pressed)
    }

    @Test
    fun view_models_delegate_register_multiple() {
        // Registers view Holder
        fragment.recyclerViewAdapter.registerViewHolders(ExampleViewHolder::class, ExampleViewHolder2::class)
        // Registers delegates callbacks
        fragment.recyclerViewAdapter.registerDelegates(ExampleViewHolder::class, ExampleViewHolder2::class, delegate = fragment)

        //Generate view models list
        val viewModels: MutableList<ViewModel> = mutableListOf(ExampleViewModel("test1"), ExampleViewModel2("test2"))
        fragment.recyclerViewAdapter.viewModels = viewModels

        // Assert that cartLinePressed is -1
        Assert.assertEquals(-1, fragment.example1Pressed)

        mockkStatic("android.view.LayoutInflater")
        val layoutInfalter = mockkClass(LayoutInflater::class)
        every { LayoutInflater.from(any()) } returns layoutInfalter
        val layout = mockkClass(LinearLayout::class)
        every { layout.context } returns mockkClass(Context::class)
        every { layoutInfalter.inflate(any<Int>(), any(), any()) } returns mockkClass(View::class)
        val viewHolder = fragment.recyclerViewAdapter.onCreateViewHolder(layout, ViewType.ordinal(ExampleViewHolder::class))
        // Delegate should be set to the mock fragment class that was registered
        Assert.assertEquals((viewHolder as ExampleViewHolder).delegate!!::class, MockFragment::class)
        // Delegate is called with index 10
        (viewHolder as ExampleViewHolder).delegate?.didSelectExample(10)
        // Assert that cartLinePressed was set from the callback with index 10
        Assert.assertEquals(10, fragment.example1Pressed)

        val viewHolder2 = fragment.recyclerViewAdapter.onCreateViewHolder(layout, ViewType.ordinal(ExampleViewHolder2::class))
        // Delegate should be set to the mock fragment class that was registered
        Assert.assertEquals((viewHolder2 as ExampleViewHolder2).delegate!!::class, MockFragment::class)

        (viewHolder2 as ExampleViewHolder2).delegate?.didSelectExample2(4)
        // Assert that Add to cart was pressed
        Assert.assertEquals(4, fragment.example2Pressed)

    }

    // Test that not registering for a viewtype will throw a null pointer exception
    @Test(expected = KotlinNullPointerException::class)
    fun view_holder_not_registered() {
        fragment.recyclerViewAdapter.registerVH(ExampleViewHolder2::class)

        val viewModels: MutableList<ViewModel> = mutableListOf(ExampleViewModel("test1"), ExampleViewModel2("test2"))
        fragment.recyclerViewAdapter.viewModels = viewModels

        mockkStatic("android.view.LayoutInflater")
        every { LayoutInflater.from(any()) } returns mockkClass(LayoutInflater::class)
        val layout = mockkClass(LinearLayout::class)
        every { layout.context } returns mockkClass(Context::class)
        val exception = fragment.recyclerViewAdapter.onCreateViewHolder(layout, ViewType.ordinal(ExampleViewHolder::class))
        // Exception terminated before reaching fail. Expecting KotlinNullPointerException
        Assert.fail()
    }

    @Test
    fun view_holder_registered() {
        fragment.recyclerViewAdapter.registerVH(ExampleViewHolder::class)
        fragment.recyclerViewAdapter.registerVH(ExampleViewHolder2::class)

        val viewModels: MutableList<ViewModel> = mutableListOf(ExampleViewModel("test1"), ExampleViewModel2("test2"))
        fragment.recyclerViewAdapter.viewModels = viewModels

        mockkStatic("android.view.LayoutInflater")
        val layoutInfalter = mockkClass(LayoutInflater::class)
        every { LayoutInflater.from(any()) } returns layoutInfalter
        val layout = mockkClass(LinearLayout::class)
        every { layout.context } returns mockkClass(Context::class)
        every { layoutInfalter.inflate(any<Int>(), any(), any()) } returns mockkClass(View::class)
        val viewHolder = fragment.recyclerViewAdapter.onCreateViewHolder(layout, ViewType.ordinal(ExampleViewHolder2::class))
        Assert.assertNotNull(viewHolder)
        Assert.assertEquals(viewHolder::class, ExampleViewHolder2::class)
    }

    @Test
    fun view_binder_succeeds() {
        fragment.recyclerViewAdapter.registerVH(ExampleViewHolder::class)
        fragment.recyclerViewAdapter.registerVH(ExampleViewHolder2::class)
        val example2ViewModel = ExampleViewModel2("test2")
        val viewModels: MutableList<ViewModel> = mutableListOf(ExampleViewModel("test1"), example2ViewModel)
        fragment.recyclerViewAdapter.viewModels = viewModels

        mockkStatic("android.view.LayoutInflater")
        val layoutInfalter = mockkClass(LayoutInflater::class)
        every { LayoutInflater.from(any()) } returns layoutInfalter
        val layout = mockkClass(LinearLayout::class)
        every { layout.context } returns mockkClass(Context::class)
        every { layoutInfalter.inflate(any<Int>(), any(), any()) } returns mockkClass(View::class)
        val viewHolder = fragment.recyclerViewAdapter.onCreateViewHolder(layout, ViewType.ordinal(ExampleViewHolder2::class))
        val itemViewHolder = (viewHolder as ExampleViewHolder2)

        val textView = mockkClass(TextView::class)
        every { itemViewHolder.apply(example2ViewModel) } returns Unit
        every { itemViewHolder.view.findViewById<TextView>(any()) } returns textView
        every { textView.setText(any<String>()) } returns Unit
        every { textView.setVisibility(any()) } returns Unit

        fragment.recyclerViewAdapter.onBindViewHolder(viewHolder, 1)

    }

    @Test
    fun holder_factory() {
        val holder = ViewType.holder(ExampleViewHolder::class)
        Assert.assertNotNull(holder)
    }

    @Test
    fun factory_create() {
        mockkStatic("android.view.LayoutInflater")
        val layoutInfalter = mockkClass(LayoutInflater::class)
        val layout = mockkClass(LinearLayout::class)
        every { layout.context } returns mockkClass(Context::class)
        every { layoutInfalter.inflate(any<Int>(), any(), any()) } returns mockkClass(View::class)
        val browseHeaderViewHolder = ExampleViewHolder.create(FactoryCreator(layoutInfalter, layout))
        Assert.assertNotNull(browseHeaderViewHolder)
    }
}