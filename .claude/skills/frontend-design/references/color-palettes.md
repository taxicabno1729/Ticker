# Color Palette Reference

## Dark Theme Palettes

### Purple (Default Material 3)
```xml
<color name="primary">#D0BCFF</color>
<color name="onPrimary">#381E72</color>
<color name="primaryContainer">#4F378B</color>
<color name="onPrimaryContainer">#EADDFF</color>

<color name="secondary">#CCC2DC</color>
<color name="onSecondary">#332D41</color>
<color name="secondaryContainer">#4A4458</color>
<color name="onSecondaryContainer">#E8DEF8</color>

<color name="tertiary">#EFB8C8</color>
<color name="onTertiary">#492532</color>
<color name="tertiaryContainer">#633B48</color>
<color name="onTertiaryContainer">#FFD8E4</color>

<color name="surface">#1C1B1F</color>
<color name="onSurface">#E6E1E5</color>
<color name="surfaceVariant">#49454F</color>
<color name="onSurfaceVariant">#CAC4D0</color>

<color name="background">#1C1B1F</color>
<color name="onBackground">#E6E1E5</color>
<color name="outline">#938F99</color>
<color name="outlineVariant">#49454F</color>
```

### Blue (Trust/Finance)
```xml
<color name="primary">#9FCAFF</color>
<color name="onPrimary">#003258</color>
<color name="primaryContainer">#00497D</color>
<color name="onPrimaryContainer">#D1E4FF</color>

<color name="secondary">#BBC7DB</color>
<color name="onSecondary">#253140</color>
<color name="secondaryContainer">#3B4858</color>
<color name="onSecondaryContainer">#D7E3F7</color>

<color name="tertiary">#D6BEE4</color>
<color name="onTertiary">#3B2948</color>
<color name="tertiaryContainer">#523F5F</color>
<color name="onTertiaryContainer">#F2DAFF</color>

<color name="surface">#1A1C1E</color>
<color name="onSurface">#E2E2E5</color>
<color name="surfaceVariant">#42474E</color>
<color name="onSurfaceVariant">#C2C7CF</color>
```

### Green (Growth/Nature)
```xml
<color name="primary">#7DDA9B</color>
<color name="onPrimary">#00391A</color>
<color name="primaryContainer">#005228</color>
<color name="onPrimaryContainer">#99F7B6</color>

<color name="secondary">#B6CCB8</color>
<color name="onSecondary">#223526</color>
<color name="secondaryContainer">#384B3C</color>
<color name="onSecondaryContainer">#D2E8D4</color>

<color name="tertiary">#A2C9DC</color>
<color name="onTertiary">#063544</color>
<color name="tertiaryContainer">#244C5B</color>
<color name="onTertiaryContainer">#BEE5F8</color>

<color name="surface">#191C1A</color>
<color name="onSurface">#E1E3DE</color>
<color name="surfaceVariant">#414941</color>
<color name="onSurfaceVariant">#C1C9BF</color>
```

### Orange (Energy/Warning)
```xml
<color name="primary">#FFB787</color>
<color name="onPrimary">#502400</color>
<color name="primaryContainer">#723600</color>
<color name="onPrimaryContainer">#FFDCC7</color>

<color name="secondary">#E6BEAC</color>
<color name="onSecondary">#432B1E</color>
<color name="secondaryContainer">#5C4133</color>
<color name="onSecondaryContainer">#FFDCC7</color>

<color name="tertiary">#D0C88E</color>
<color name="onTertiary">#363107</color>
<color name="tertiaryContainer">#4D481B</color>
<color name="onTertiaryContainer">#ECE4A8</color>

<color name="surface">#201A17</color>
<color name="onSurface">#ECE0DA</color>
<color name="surfaceVariant">#52443C</color>
<color name="onSurfaceVariant">#D7C2B6</color>
```

### Red (Error/Alert)
```xml
<color name="error">#FFB4AB</color>
<color name="onError">#690005</color>
<color name="errorContainer">#93000A</color>
<color name="onErrorContainer">#FFDAD6</color>
```

### Neutral (Gray Scale)
```xml
<!-- Surface tints for elevation -->
<color name="surfaceTint">#D0BCFF</color>

<!-- Inverse colors -->
<color name="inverseSurface">#E6E1E5</color>
<color name="inverseOnSurface">#322F33</color>
<color name="inversePrimary">#6750A4</color>

<!-- Scrim -->
<color name="scrim">#000000</color>

<!-- Shadow -->
<color name="shadow">#000000</color>
```

## Crypto App Specific

### Price Change Colors
```xml
<color name="price_up">#79DD77</color>
<color name="price_up_container">#1B3A1A</color>
<color name="price_down">#FFB4AB</color>
<color name="price_down_container">#5C1A16</color>
```

### Chain Colors
```xml
<color name="ethereum">#627EEA</color>
<color name="polygon">#8247E5</color>
<color name="arbitrum">#28A0F0</color>
<color name="optimism">#FF0420</color>
<color name="base">#0052FF</color>
```

### Elevation Overlays (Dark Theme)
```xml
<!-- Surface with elevation levels -->
<color name="surface_1">#1C1B1F</color> <!-- +0dp -->
<color name="surface_2">#232329</color> <!-- +1dp -->
<color name="surface_3">#2A2A33</color> <!-- +2dp -->
<color name="surface_4">#31313D</color> <!-- +3dp -->
<color name="surface_5">#383847</color> <!-- +4dp -->
```

## Gradient Examples

### Subtle Surface Gradient
```xml
<gradient
    android:startColor="#1C1B1F"
    android:endColor="#252430"
    android:angle="135" />
```

### Card Gradient
```xml
<gradient
    android:startColor="#4F378B"
    android:endColor="#633B48"
    android:angle="45" />
```

### Shimmer Loading
```xml
<gradient
    android:startColor="#33FFFFFF"
    android:centerColor="#1AFFFFFF"
    android:endColor="#33FFFFFF"
    android:angle="90" />
```

## Creating ColorScheme in Code

```kotlin
// Dynamic color scheme
dynamicDarkColorScheme(context)
dynamicLightColorScheme(context)

// Custom color scheme
darkColorScheme(
    primary = Color(0xFFD0BCFF),
    onPrimary = Color(0xFF381E72),
    primaryContainer = Color(0xFF4F378B),
    onPrimaryContainer = Color(0xFFEADDFF),
    // ... other colors
)
```
