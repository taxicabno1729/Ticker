# Material 3 Components Reference

## Buttons

### Filled Button (Primary)
```xml
<com.google.android.material.button.MaterialButton
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:text="Button"
    app:cornerRadius="20dp" />
```

### Outlined Button
```xml
<com.google.android.material.button.MaterialButton
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:text="Button"
    style="@style/Widget.Material3.Button.OutlinedButton"
    app:cornerRadius="20dp" />
```

### Text Button
```xml
<com.google.android.material.button.MaterialButton
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:text="Button"
    style="@style/Widget.Material3.Button.TextButton" />
```

## Cards

### Elevated Card
```xml
<com.google.android.material.card.MaterialCardView
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_margin="16dp"
    app:cardCornerRadius="12dp"
    app:cardElevation="1dp"
    style="@style/Widget.Material3.CardView.Elevated">
    
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:padding="16dp">
        <!-- Content -->
    </LinearLayout>
</com.google.android.material.card.MaterialCardView>
```

### Filled Card (No elevation)
```xml
<com.google.android.material.card.MaterialCardView
    style="@style/Widget.Material3.CardView.Filled"
    app:cardCornerRadius="12dp">
    <!-- Content -->
</com.google.android.material.card.MaterialCardView>
```

### Outlined Card
```xml
<com.google.android.material.card.MaterialCardView
    style="@style/Widget.Material3.CardView.Outlined"
    app:cardCornerRadius="12dp"
    app:strokeWidth="1dp"
    app:strokeColor="@color/outline">
    <!-- Content -->
</com.google.android.material.card.MaterialCardView>
```

## Chips

### Input Chip
```xml
<com.google.android.material.chip.Chip
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:text="Chip"
    app:chipIcon="@drawable/ic_icon"
    style="@style/Widget.Material3.Chip.Input" />
```

### Filter Chip
```xml
<com.google.android.material.chip.Chip
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:text="Filter"
    style="@style/Widget.Material3.Chip.Filter" />
```

### Choice Chip
```xml
<com.google.android.material.chip.Chip
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:text="Choice"
    style="@style/Widget.Material3.Chip.Choice" />
```

## Text Fields

### Outlined Text Field
```xml
<com.google.android.material.textfield.TextInputLayout
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:hint="Label"
    style="@style/Widget.Material3.TextInputLayout.OutlinedBox">
    
    <com.google.android.material.textfield.TextInputEditText
        android:layout_width="match_parent"
        android:layout_height="wrap_content" />
</com.google.android.material.textfield.TextInputLayout>
```

### Filled Text Field
```xml
<com.google.android.material.textfield.TextInputLayout
    style="@style/Widget.Material3.TextInputLayout.FilledBox"
    android:hint="Label">
    
    <com.google.android.material.textfield.TextInputEditText
        android:layout_width="match_parent"
        android:layout_height="wrap_content" />
</com.google.android.material.textfield.TextInputLayout>
```

## Bottom Navigation

```xml
<com.google.android.material.bottomnavigation.BottomNavigationView
    android:layout_width="match_parent"
    android:layout_height="80dp"
    android:layout_gravity="bottom"
    app:menu="@menu/bottom_nav_menu"
    style="@style/Widget.Material3.BottomNavigationView" />
```

## Navigation Rail (Tablet)

```xml
<com.google.android.material.navigationrail.NavigationRailView
    android:layout_width="80dp"
    android:layout_height="match_parent"
    app:menu="@menu/navigation_rail_menu"
    style="@style/Widget.Material3.NavigationRailView" />
```

## Floating Action Button (FAB)

### Regular FAB
```xml
<com.google.android.material.floatingactionbutton.FloatingActionButton
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:src="@drawable/ic_add"
    app:fabSize="normal"
    style="@style/Widget.Material3.FloatingActionButton.Primary" />
```

### Small FAB
```xml
<com.google.android.material.floatingactionbutton.FloatingActionButton
    android:src="@drawable/ic_add"
    app:fabSize="mini"
    style="@style/Widget.Material3.FloatingActionButton.Secondary" />
```

### Extended FAB
```xml
<com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:text="Create"
    app:icon="@drawable/ic_add"
    style="@style/Widget.Material3.ExtendedFloatingActionButton.Primary" />
```

## Dialogs

```xml
<com.google.android.material.dialog.MaterialAlertDialogBuilder
    android:context="@style/ThemeOverlay.Material3.MaterialAlertDialog">
    <!-- Content -->
</com.google.android.material.dialog.MaterialAlertDialogBuilder>
```

## Bottom Sheets

### Modal Bottom Sheet
```xml
<com.google.android.material.bottomsheet.BottomSheetDialogFragment>
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:background="@drawable/bottom_sheet_background">
        <!-- Handle indicator -->
        <View
            android:layout_width="32dp"
            android:layout_height="4dp"
            android:layout_gravity="center_horizontal"
            android:layout_marginTop="16dp"
            android:background="@drawable/bottom_sheet_handle" />
        <!-- Content -->
    </LinearLayout>
</com.google.android.material.bottomsheet.BottomSheetDialogFragment>
```

## Switches, Checkboxes, Radio Buttons

### Switch
```xml
<com.google.android.material.materialswitch.MaterialSwitch
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:checked="true" />
```

### Checkbox
```xml
<com.google.android.material.checkbox.MaterialCheckBox
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:text="Label" />
```

### Radio Button
```xml
<com.google.android.material.radiobutton.MaterialRadioButton
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:text="Option" />
```

## Progress Indicators

### Circular Progress
```xml
<com.google.android.material.progressindicator.CircularProgressIndicator
    android:layout_width="48dp"
    android:layout_height="48dp"
    android:indeterminate="true"
    app:indicatorSize="48dp"
    app:indicatorColor="@color/primary" />
```

### Linear Progress
```xml
<com.google.android.material.progressindicator.LinearProgressIndicator
    android:layout_width="match_parent"
    android:layout_height="4dp"
    android:indeterminate="true"
    app:trackColor="@color/surfaceVariant"
    app:indicatorColor="@color/primary" />
```

## Sliders

```xml
<com.google.android.material.slider.Slider
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:valueFrom="0"
    android:valueTo="100"
    android:stepSize="1" />
```
