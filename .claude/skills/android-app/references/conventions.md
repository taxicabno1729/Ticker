# Code Conventions

## Naming Conventions

| Type | Convention | Example |
|------|------------|---------|
| Packages | lowercase, no underscores | `com.example.liveticker` |
| Classes | PascalCase | `CoinRepository`, `FirstFragment` |
| Functions/Variables | camelCase | `fetchTickers`, `connectedAddress` |
| Constants | UPPER_SNAKE_CASE | `REFRESH_INTERVAL` |
| XML resources | snake_case | `fragment_first.xml`, `ticker_item.xml` |

## Kotlin Conventions

- Use `val` over `var`, immutable collections where possible
- Use data classes for models
- Use Kotlin Coroutines for asynchronous operations
- Prefer sealed classes for state representation

## Android Conventions

### View Binding Pattern

```kotlin
private var _binding: FragmentNameBinding? = null
private val binding get() = _binding!!

override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
}
```

### ViewModel Factory Pattern

```kotlin
class MyViewModelFactory(
    private val repository: MyRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MyViewModel::class.java)) {
            return MyViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
```

### RecyclerView Adapter Pattern

Use `ListAdapter` with `DiffUtil`:

```kotlin
class MyAdapter : ListAdapter<Item, MyAdapter.ViewHolder>(DiffCallback()) {
    class DiffCallback : DiffUtil.ItemCallback<Item>() {
        override fun areItemsTheSame(old: Item, new: Item) = old.id == new.id
        override fun areContentsTheSame(old: Item, new: Item) = old == new
    }
}
```

## File Organization

```
com.example.app/
├── MainActivity.kt              # Single Activity entry point
├── App.kt                       # Application class
├── data/                        # Data layer
│   ├── models.kt               # Data classes
│   ├── Resource.kt             # Sealed class for states
│   └── *Repository.kt          # Repository classes
├── network/                     # API layer
│   ├── *ApiService.kt          # Retrofit interfaces
│   └── RetrofitClient.kt       # Network client
└── ui/                          # UI layer
    ├── *ViewModel.kt           # ViewModels
    └── *Adapter.kt             # RecyclerView adapters
```

## Dependency Injection (Manual)

Repositories are instantiated in Fragments and passed to ViewModels via Factories:

```kotlin
val repository = CoinRepository()
val factory = MyViewModelFactory(repository)
val viewModel = ViewModelProvider(this, factory)[MyViewModel::class.java]
```
