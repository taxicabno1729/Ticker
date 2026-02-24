---
name: android-app
description: Android app development using Kotlin with MVVM architecture, Navigation Component, and Material Design. Use when creating or modifying Android app components including Fragments, ViewModels, Repositories, Adapters, layouts, or navigation graphs. Supports projects using Retrofit, View Binding, StateFlow, and Repository pattern.
---

# Android App Development

## Quick Reference

- **Architecture**: MVVM with Repository pattern
- **UI Layer**: Fragments with View Binding, Navigation Component
- **State Management**: StateFlow with Resource<T> sealed class
- **Lists**: RecyclerView with ListAdapter and DiffUtil
- **Networking**: Retrofit with coroutines

## Component Templates

Templates in `assets/templates/` use `{{variable}}` placeholders:

| Variable | Description | Example |
|----------|-------------|---------|
| `{{name}}` | Component name (PascalCase) | `Feature`, `CoinDetail` |
| `{{package_name}}` | Full package name | `com.example.liveticker` |
| `{{data_type}}` | Data model class name | `List<Feature>`, `CoinDetail` |
| `{{data_name}}` | Data name for methods | `Features`, `CoinDetail` |
| `{{repository}}` | Repository class name | `Feature`, `Coin` |
| `{{repository_var}}` | Repository variable name (camelCase) | `feature`, `coin` |
| `{{state_name}}` | State flow name (camelCase) | `features`, `coinDetail` |
| `{{item_type}}` | Adapter item type | `Feature`, `Ticker` |
| `{{layout_name}}` | Layout file name (snake_case) | `feature_item` |
| `{{label}}` | Screen label | `@string/feature_title` |
| `{{arg_name}}` | Navigation argument name | `featureId` |
| `{{destination}}` | Navigation destination | `OtherFragment` |

### Available Templates

| Component | Template |
|-----------|----------|
| Fragment | `Fragment.kt.template` |
| ViewModel | `ViewModel.kt.template` |
| Repository | `Repository.kt.template` |
| RecyclerView Adapter | `Adapter.kt.template` |
| Data Class | `DataClass.kt.template` |
| Fragment Layout | `layout_fragment.xml.template` |
| Item Layout | `layout_item.xml.template` |
| Navigation Entry | `navigation.xml.template` |

## Adding a New Screen

See [references/new-screen-workflow.md](references/new-screen-workflow.md) for the complete 7-step workflow.

## Key Patterns

### Resource Sealed Class

```kotlin
sealed class Resource<T> {
    class Loading<T> : Resource<T>()
    data class Success<T>(val data: T?) : Resource<T>()
    data class Error<T>(val message: String, val data: T? = null) : Resource<T>()
}
```

### View Binding in Fragments

```kotlin
private var _binding: FragmentNameBinding? = null
private val binding get() = _binding!!

override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
}
```

### RecyclerView ListAdapter

```kotlin
class MyAdapter : ListAdapter<Item, MyAdapter.ViewHolder>(DiffCallback())

class DiffCallback : DiffUtil.ItemCallback<Item>() {
    override fun areItemsTheSame(old: Item, new: Item) = old.id == new.id
    override fun areContentsTheSame(old: Item, new: Item) = old == new
}
```

## References

- **Architecture patterns**: See [references/architecture.md](references/architecture.md)
- **Code conventions**: See [references/conventions.md](references/conventions.md)
