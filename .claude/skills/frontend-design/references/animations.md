# Animation Guidelines

## Duration Guidelines

| Animation Type | Duration | Use Case |
|----------------|----------|----------|
| Instant feedback | 50-100ms | Button presses, checkbox checks |
| Micro-interactions | 100-200ms | Icon changes, small movements |
| Standard transitions | 200-300ms | Screen transitions, list items |
| Complex transitions | 300-500ms | Shared elements, page changes |
| Emphasis | 200-400ms | Attention-drawing animations |

## Interpolators

```kotlin
// Standard - natural acceleration/deceleration
FastOutSlowInInterpolator()

// Entering - decelerate
DecelerateInterpolator()

// Exiting - accelerate
AccelerateInterpolator()

// Bounce effect
BounceInterpolator()

// Spring (Material you feel)
// Use spring animations for natural feel
```

## Common Animation Patterns

### Fade In
```kotlin
fun View.fadeIn(duration: Long = 200) {
    alpha = 0f
    visibility = View.VISIBLE
    animate()
        .alpha(1f)
        .setDuration(duration)
        .setInterpolator(FastOutSlowInInterpolator())
        .start()
}
```

### Fade Out
```kotlin
fun View.fadeOut(duration: Long = 200, onEnd: (() -> Unit)? = null) {
    animate()
        .alpha(0f)
        .setDuration(duration)
        .setInterpolator(FastOutSlowInInterpolator())
        .withEndAction {
            visibility = View.GONE
            onEnd?.invoke()
        }
        .start()
}
```

### Slide Up
```kotlin
fun View.slideUp(duration: Long = 300) {
    translationY = height.toFloat()
    alpha = 0f
    visibility = View.VISIBLE
    
    animate()
        .translationY(0f)
        .alpha(1f)
        .setDuration(duration)
        .setInterpolator(FastOutSlowInInterpolator())
        .start()
}
```

### Scale In (Pop)
```kotlin
fun View.scaleIn(duration: Long = 200) {
    scaleX = 0.8f
    scaleY = 0.8f
    alpha = 0f
    visibility = View.VISIBLE
    
    animate()
        .scaleX(1f)
        .scaleY(1f)
        .alpha(1f)
        .setDuration(duration)
        .setInterpolator(DecelerateInterpolator())
        .start()
}
```

### List Item Animation
```kotlin
class SlideUpItemAnimator : DefaultItemAnimator() {
    override fun animateAdd(holder: RecyclerView.ViewHolder): Boolean {
        holder.itemView.apply {
            alpha = 0f
            translationY = 100f
            animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(300)
                .setInterpolator(FastOutSlowInInterpolator())
                .setStartDelay(holder.bindingAdapterPosition * 50L)
                .start()
        }
        return true
    }
}

// Usage
recyclerView.itemAnimator = SlideUpItemAnimator()
```

### Shared Element Transition
```kotlin
// In Fragment A
val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
    requireActivity(),
    sharedView,
    "shared_element_name"
)
findNavController().navigate(
    R.id.action_a_to_b,
    null,
    null,
    FragmentNavigatorExtras(sharedView to "shared_element_name")
)

// In Fragment B layout
<ImageView
    android:id="@+id/image"
    android:transitionName="shared_element_name" />
```

### Ripple Effect
```xml
<!-- For clickable views -->
<LinearLayout
    android:background="?attr/selectableItemBackground"
    android:clickable="true"
    android:focusable="true">
</LinearLayout>

<!-- Bounded ripple -->
<LinearLayout
    android:background="?attr/selectableItemBackgroundBorderless">
</LinearLayout>
```

### Skeleton Loading
```kotlin
fun View.startShimmer() {
    val shimmer = ShimmerDrawable().apply {
        setShimmer(
            Shimmer.AlphaHighlightBuilder()
                .setDuration(1000)
                .setBaseAlpha(0.7f)
                .setHighlightAlpha(0.9f)
                .setDirection(Shimmer.Direction.LEFT_TO_RIGHT)
                .setAutoStart(true)
                .build()
        )
    }
    background = shimmer
}
```

### Value Animator
```kotlin
// Animate a value
ValueAnimator.ofFloat(0f, 100f).apply {
    duration = 300
    interpolator = FastOutSlowInInterpolator()
    addUpdateListener { animator ->
        val value = animator.animatedValue as Float
        view.translationX = value
    }
    start()
}
```

## Spring Animations

```kotlin
// Spring animation for natural feel
SpringAnimation(view, DynamicAnimation.TRANSLATION_Y, 0f).apply {
    spring.apply {
        dampingRatio = SpringForce.DAMPING_RATIO_MEDIUM_BOUNCY
        stiffness = SpringForce.STIFFNESS_LOW
    }
    start()
}
```

## Transition Manager (Scene Changes)

```kotlin
// Animate layout changes
TransitionManager.beginDelayedTransition(container)
view.visibility = if (view.visibility == View.VISIBLE) View.GONE else View.VISIBLE
```

## Material Motion

```kotlin
// Material fade through
MaterialFadeThrough().apply {
    duration = 300
    // Apply to fragments
}

// Material shared axis
MaterialSharedAxis(MaterialSharedAxis.X, true).apply {
    duration = 300
}

// Material elevation scale
MaterialElevationScale(true).apply {
    duration = 300
}
```
