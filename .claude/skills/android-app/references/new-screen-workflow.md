# Adding a New Screen Workflow

Complete guide for adding a new screen to the Android app.

## 1. Create Data Model

Create a data class in `data/` package:

```kotlin
data class Feature(
    val id: String,
    val name: String,
    val description: String?
)
```

## 2. Create Repository Method

Add to existing repository or create new one in `data/`:

```kotlin
class FeatureRepository {
    suspend fun getFeatures(): Resource<List<Feature>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getFeatures()
                Resource.Success(response)
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Unknown error")
            }
        }
    }
}
```

## 3. Create ViewModel

Create in `ui/` package:

```kotlin
class FeatureViewModel(
    private val repository: FeatureRepository
) : ViewModel() {

    private val _features = MutableStateFlow<Resource<List<Feature>>>(Resource.Loading())
    val features: StateFlow<Resource<List<Feature>>> = _features

    fun fetchFeatures() {
        viewModelScope.launch {
            _features.value = Resource.Loading()
            _features.value = repository.getFeatures()
        }
    }
}

class FeatureViewModelFactory(
    private val repository: FeatureRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FeatureViewModel::class.java)) {
            return FeatureViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
```

## 4. Create Layout XML

Create `res/layout/fragment_feature.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout 
    xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical">

    <androidx.recyclerview.widget.RecyclerView
        android:id="@+id/recyclerView"
        android:layout_width="match_parent"
        android:layout_height="match_parent" />

    <ProgressBar
        android:id="@+id/loadingIndicator"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_gravity="center"
        android:visibility="gone" />
</LinearLayout>
```

## 5. Create Fragment

```kotlin
class FeatureFragment : Fragment() {
    private var _binding: FragmentFeatureBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: FeatureViewModel
    private lateinit var adapter: FeatureAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentFeatureBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val factory = FeatureViewModelFactory(FeatureRepository())
        viewModel = ViewModelProvider(this, factory)[FeatureViewModel::class.java]
        
        setupRecyclerView()
        observeViewModel()
        viewModel.fetchFeatures()
    }

    private fun setupRecyclerView() {
        adapter = FeatureAdapter { feature ->
            // Handle item click
        }
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.recyclerView.adapter = adapter
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.features.collectLatest { resource ->
                when (resource) {
                    is Resource.Loading -> binding.loadingIndicator.visibility = View.VISIBLE
                    is Resource.Success -> {
                        binding.loadingIndicator.visibility = View.GONE
                        adapter.submitList(resource.data)
                    }
                    is Resource.Error -> {
                        binding.loadingIndicator.visibility = View.GONE
                        Toast.makeText(context, resource.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
```

## 6. Add to Navigation Graph

Add to `res/navigation/nav_graph.xml`:

```xml
<fragment
    android:id="@+id/FeatureFragment"
    android:name="com.example.app.FeatureFragment"
    android:label="Feature"
    tools:layout="@layout/fragment_feature">
    
    <action
        android:id="@+id/action_FeatureFragment_to_OtherFragment"
        app:destination="@id/OtherFragment" />
</fragment>
```

## 7. Add Navigation Action

From another fragment:

```kotlin
findNavController().navigate(R.id.action_CurrentFragment_to_FeatureFragment)
```
