---
name: frontend-design
description: Android UI/UX design with Material 3 (Material You) guidelines. Use when creating or improving Android app interfaces, layouts, themes, colors, typography, or visual design. Covers Material Design components, dark/light themes, responsive layouts, animations, and modern Android visual patterns.
---

# Android Frontend Design

## Quick Reference

### Material 3 Key Principles
- Dynamic Color: Use ColorScheme with dynamic theming
- Elevation: Surface elevation with tonal color shifts
- Shape: Rounded corners (12dp cards, 28dp FAB, full rounded buttons)
- Motion: Smooth 200-300ms transitions, shared element transitions

### Common Component Specs

| Component | Background | Elevation | Corner Radius |
|-----------|------------|-----------|---------------|
| Cards | surface | 1dp | 12dp |
| Dialogs | surface | 3dp | 28dp |
| FAB | primaryContainer | 3dp | 16dp (small), 28dp (large) |
| Buttons | primaryContainer | 0dp | 20dp (full rounded) |
| Input Fields | surfaceVariant | 0dp | 4dp |
| Bottom Sheets | surface | 1dp | 28dp top |

## Color System

### Material 3 Color Roles

Primary - main brand color:
- primary: main color
- onPrimary: text/icon on primary
- primaryContainer: container background
- onPrimaryContainer: text on container

Secondary - accent color:
- secondary: accent
- onSecondary: text on accent
- secondaryContainer: accent container
- onSecondaryContainer: text on container

Tertiary - complementary accent:
- tertiary: complement to primary
- onTertiary: text on tertiary

Surface - backgrounds:
- surface: main background
- onSurface: main text
- surfaceVariant: secondary background
- onSurfaceVariant: secondary text

Semantic:
- error: error states
- success: success states
- warning: warning states

### Elevation Tonal Shifts (Dark Theme)
- Surface at +1dp: Surface +5% lighter
- Surface at +2dp: Surface +8% lighter
- Surface at +3dp: Surface +11% lighter

## Typography

### Type Scale (Material 3)

| Style | Size | Weight | Line Height | Usage |
|-------|------|--------|-------------|-------|
| Display Large | 57sp | 400 | 64sp | Hero text |
| Display Medium | 45sp | 400 | 52sp | Large headers |
| Display Small | 36sp | 400 | 44sp | Medium headers |
| Headline Large | 32sp | 400 | 40sp | Screen titles |
| Headline Medium | 28sp | 400 | 36sp | Section headers |
| Headline Small | 24sp | 400 | 32sp | Card titles |
| Title Large | 22sp | 500 | 28sp | App bar titles |
| Title Medium | 16sp | 500 | 24sp | List titles |
| Title Small | 14sp | 500 | 20sp | Subtitles |
| Body Large | 16sp | 400 | 24sp | Primary body |
| Body Medium | 14sp | 400 | 20sp | Secondary body |
| Label Large | 14sp | 500 | 20sp | Buttons, chips |
| Label Medium | 12sp | 500 | 16sp | Small buttons |
| Label Small | 11sp | 500 | 16sp | Captions, badges |

## Spacing System

Use 8dp grid system:
- space_0: 0dp
- space_1: 4dp
- space_2: 8dp
- space_3: 12dp
- space_4: 16dp
- space_5: 24dp
- space_6: 32dp
- space_7: 48dp
- space_8: 64dp

## Layout Patterns

### Card Layout
- MaterialCardView with 12dp corner radius
- 1dp elevation for subtle depth
- 16dp padding inside
- surface background color

### List Item Layout
- 72dp height standard
- 16dp horizontal padding
- 40dp icon/avatar
- Title (TitleMedium) + Subtitle (BodyMedium)

## Animations

### Standard Durations
- Micro interactions: 100ms
- Standard transitions: 200-300ms
- Complex transitions: 400-500ms

### Common Animation Patterns
- Fade in: alpha 0 to 1, 200ms
- Slide up: translationY 50dp to 0, alpha 0 to 1, 300ms
- Staggered list: 50ms delay between items
- Use FastOutSlowInInterpolator for natural motion

## Shape System

### Corner Families
- Small components (buttons, chips): 8dp
- Medium components (cards, sheets): 12dp
- Large components (dialogs): 28dp
- Full rounded (FAB): 28dp or 50%

## Dark Theme Best Practices

1. Surface colors: Use surface/surfaceVariant, not pure black
2. Elevation: Express elevation through tonal color lighter
3. Accents: Vibrant accents pop against dark surfaces
4. Contrast: Maintain 4.5:1 minimum contrast ratio
5. Images: Add subtle scrim behind text on images
