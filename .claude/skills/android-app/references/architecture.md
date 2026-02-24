# Android Architecture Patterns

## MVVM with Repository Pattern

This project follows MVVM (Model-View-ViewModel) with Repository pattern:

```
UI Layer (Fragment/Activity) ←→ ViewModel ←→ Repository ←→ Data Source (API/Database)
```

### Resource Sealed Class

All repository methods return `Resource<T>` for consistent error handling:

```kotlin
sealed class Resource<T> {
    class Loading<T> : Resource<T>()
    data class Success<T>(val data: T?) : Resource<T>()
    data class Error<T>(val message: String, val data: T? = null) : Resource<T>()
}
```

### Repository Pattern

Repositories handle data operations and expose clean APIs to ViewModels:

```kotlin
class ExampleRepository {
    suspend fun getData(): Resource<Data> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getData()
                Resource.Success(response)
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Unknown error")
            }
        }
    }
}
```

### ViewModel Pattern

ViewModels expose UI state as `StateFlow`:

```kotlin
class ExampleViewModel(private val repository: ExampleRepository) : ViewModel() {
    private val _data = MutableStateFlow<Resource<Data>>(Resource.Loading())
    val data: StateFlow<Resource<Data>> = _data

    fun fetchData() {
        viewModelScope.launch {
            _data.value = Resource.Loading()
            _data.value = repository.getData()
        }
    }
}
```

### Fragment Pattern

Fragments use View Binding with nullable backing property:

```kotlin
class ExampleFragment : Fragment() {
    private var _binding: FragmentExampleBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentExampleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
```

## State Management

### Collecting StateFlow in Fragments

```kotlin
lifecycleScope.launch {
    viewModel.data.collectLatest { resource ->
        when (resource) {
            is Resource.Loading -> showLoading()
            is Resource.Success -> showData(resource.data)
            is Resource.Error -> showError(resource.message)
        }
    }
}
```

## Navigation

Uses Android Navigation Component with Safe Args:

```kotlin
// Navigate with arguments
val bundle = bundleOf("key" to value)
findNavController().navigate(R.id.action_source_to_destination, bundle)

// Retrieve arguments
val arg = arguments?.getString("key") ?: ""
```
