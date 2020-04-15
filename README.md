# DynamicRecyclerViewAdapter
 
DynamicRecyclerViewAdapter is a library that abstracts out the need for creating a new recyclerViewAdapter for each of your recyclerViews. 
The main goal for DynamicRecyclerViewAdapter is to allow a developer to register the view holders that will be present in a recyclerView and have them be automatically created under the hood by passing in the viewHolder's associated ViewModel.

# How to use

First add the library to your gradle.build file using the following line

```
```

Next let's create a ViewModel, DelegateCallback, and a ViewHolder:

The ViewModel should extend `ViewModel` and pass any properties that will need to be set on the viewHolder. Make sure to pass the associated `ViewHolder::class` in the parent constructor. 
```
data class ExampleViewModel(
    val text: String
): ViewModel(ExampleViewHolder::class)
```

The Delegate will allow our viewHolder to make callbacks to our Activity or Fragment where we implement our delegate.
```
interface ExampleDelegate: Delegate {
    fun didSelectExample(index: Int)
}
```

Finally we create a ViewHolder class which extends `ViewHolder<T>`. Our generic type in this case is the ViewModel we want to associate with our viewHolder. The ViewHolder must also create a companion object that implements `Factory<T>`. This will allow our AdapterRecyclerView to create viewHolder instances under the hood when given a specific associated ViewModel.
```
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
```

# Setting up our recyclerView

In this example we will create a simple recyclerView in our mainActivity.

First we want to have an objecct which consists of our Identifiers. Identifiers are just ViewHolder class types we plan to register with our AdapterRecyclerView.
```
class MainActivity : AppCompatActivity(), ExampleDelegate {
    object Identifiers {
        val ExampleViewHolder = ExampleViewHolder::class
    }
```

Next we setup our recyclerView and our dynamic adapter.
*note the `AdapterRecyclerView` takes in a default list but can also be left empty and updated later.
```
    private var adapterRecyclerView = AdapterRecyclerView(mutableListOf(ExampleViewModel("Hello there!"), ExampleViewModel("Goodbye!!")))
    private lateinit var recyclerView: RecyclerView
```

Our on create will setup our recyclerView. We need to give it a layout and set the adapter. 
Here we also register our viewHolder classes and any callbacks associated with viewHolders.
```
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(applicationContext)
        recyclerView.adapter = adapterRecyclerView

        adapterRecyclerView.registerVH(Identifiers.ExampleViewHolder)
        adapterRecyclerView.registerDelegate(Identifiers.ExampleViewHolder, this)
    }
```

There's a few update call's that will updated the list of ViewModels in the adapterRecyclerView and notify the recyclerview of the proper changes. This works for a specific index as well.
```
    fun updateViewModels(viewModels: List<ViewModel>) {
        adapterRecyclerView.updateViewModels(viewModels)
    }

    fun updateViewModelAtIndex(viewModel: ViewModel, index: Int) {
        adapterRecyclerView.updateViewModelAtIndex(viewModel, index)
    }
```
Finally, our Activity implements the ExampleDelegate interface so we need to provide the implementation for its functions.
```
    override fun didSelectExample(index: Int) {
        Log.d("MainActivity", "$index")
    }
}
```
