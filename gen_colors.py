import os

light_xml = '''<?xml version="1.0" encoding="utf-8"?>
<resources>
    <!-- Default Text Colors (Light) -->
    <color name="text_primary">#FF0F172A</color> 
    <color name="text_secondary">#FF38BDF8</color> 
    <color name="text_tertiary">#FF64748B</color> 
    <color name="text_on_accent">#FFFFFFFF</color> 
    
    <color name="error_red">#FFEF4444</color>
    <color name="black">#FF0B0F19</color>
    <color name="white">#FFFFFFFF</color>
    
    <!-- Accent Colors -->
    <color name="accent_blue_primary">#FF0284C7</color>
    <color name="accent_blue_secondary">#FF38BDF8</color>
    <color name="accent_green_primary">#FF16A34A</color>
    <color name="accent_green_secondary">#FF4ADE80</color>
    <color name="accent_purple_primary">#FF9333EA</color>
    <color name="accent_purple_secondary">#FFC084FC</color>
    <color name="accent_red_primary">#FFDC2626</color>
    <color name="accent_red_secondary">#FFF87171</color>
    <color name="accent_yellow_primary">#FFCA8A04</color>
    <color name="accent_yellow_secondary">#FFFACC15</color>
    <color name="accent_orange_primary">#FFEA580C</color>
    <color name="accent_orange_secondary">#FFFB923C</color>
    <color name="accent_pink_primary">#FFDB2777</color>
    <color name="accent_pink_secondary">#FFF472B6</color>

    <!-- Blue Theme Backgrounds -->
    <color name="bg_primary_blue">#FFF8F9FA</color>
    <color name="bg_secondary_blue">#FFFFFFFF</color>
    <color name="bg_tertiary_blue">#FFF1F5F9</color>
    <color name="border_blue">#FFE2E8F0</color>
    <color name="ripple_blue">#FFE2E8F0</color>

    <!-- Green Theme Backgrounds -->
    <color name="bg_primary_green">#FFF8FAF9</color>
    <color name="bg_secondary_green">#FFFFFFFF</color>
    <color name="bg_tertiary_green">#FFF1F9F3</color>
    <color name="border_green">#FFE2F0E5</color>
    <color name="ripple_green">#FFE2F0E5</color>

    <!-- Purple Theme Backgrounds -->
    <color name="bg_primary_purple">#FFFAF8FA</color>
    <color name="bg_secondary_purple">#FFFFFFFF</color>
    <color name="bg_tertiary_purple">#FFF5F1F9</color>
    <color name="border_purple">#FFE6E2F0</color>
    <color name="ripple_purple">#FFE6E2F0</color>

    <!-- Red Theme Backgrounds -->
    <color name="bg_primary_red">#FFFAF8F8</color>
    <color name="bg_secondary_red">#FFFFFFFF</color>
    <color name="bg_tertiary_red">#FFF9F1F1</color>
    <color name="border_red">#FFF0E2E2</color>
    <color name="ripple_red">#FFF0E2E2</color>

    <!-- Yellow Theme Backgrounds -->
    <color name="bg_primary_yellow">#FFFAFAF8</color>
    <color name="bg_secondary_yellow">#FFFFFFFF</color>
    <color name="bg_tertiary_yellow">#FFF9F7F1</color>
    <color name="border_yellow">#FFF0EBE2</color>
    <color name="ripple_yellow">#FFF0EBE2</color>

    <!-- Orange Theme Backgrounds -->
    <color name="bg_primary_orange">#FFFAFAF9</color>
    <color name="bg_secondary_orange">#FFFFFFFF</color>
    <color name="bg_tertiary_orange">#FFF9F4F1</color>
    <color name="border_orange">#FFF0E7E2</color>
    <color name="ripple_orange">#FFF0E7E2</color>

    <!-- Pink Theme Backgrounds -->
    <color name="bg_primary_pink">#FFFAF8F9</color>
    <color name="bg_secondary_pink">#FFFFFFFF</color>
    <color name="bg_tertiary_pink">#FFF9F1F5</color>
    <color name="border_pink">#FFF0E2E6</color>
    <color name="ripple_pink">#FFF0E2E6</color>
    
    <!-- Legacy compatibility mappings -->
    <color name="bg_primary">#FFF8F9FA</color>
    <color name="bg_secondary">#FFFFFFFF</color>
    <color name="accent_dark">#FF0284C7</color>
    <color name="border_color">#FFE2E8F0</color>
    <color name="bg_tertiary">#FFF1F5F9</color>
    <color name="ripple_mono">#FFE2E8F0</color>
</resources>
'''

dark_xml = '''<?xml version="1.0" encoding="utf-8"?>
<resources>
    <!-- Default Text Colors (Dark) -->
    <color name="text_primary">#FFE2E8F0</color> 
    <color name="text_secondary">#FF38BDF8</color> 
    <color name="text_tertiary">#FF94A3B8</color> 
    <color name="text_on_accent">#FFFFFFFF</color> 
    
    <color name="error_red">#FFEF4444</color>
    <color name="black">#FF0B0F19</color>
    <color name="white">#FFFFFFFF</color>
    
    <!-- Accent Colors (Dark variants usually same as Light for vibrant buttons) -->
    <color name="accent_blue_primary">#FF0284C7</color>
    <color name="accent_blue_secondary">#FF38BDF8</color>
    <color name="accent_green_primary">#FF16A34A</color>
    <color name="accent_green_secondary">#FF4ADE80</color>
    <color name="accent_purple_primary">#FF9333EA</color>
    <color name="accent_purple_secondary">#FFC084FC</color>
    <color name="accent_red_primary">#FFDC2626</color>
    <color name="accent_red_secondary">#FFF87171</color>
    <color name="accent_yellow_primary">#FFCA8A04</color>
    <color name="accent_yellow_secondary">#FFFACC15</color>
    <color name="accent_orange_primary">#FFEA580C</color>
    <color name="accent_orange_secondary">#FFFB923C</color>
    <color name="accent_pink_primary">#FFDB2777</color>
    <color name="accent_pink_secondary">#FFF472B6</color>

    <!-- Blue Theme Backgrounds -->
    <color name="bg_primary_blue">#FF0B0F19</color>
    <color name="bg_secondary_blue">#FF161E2E</color>
    <color name="bg_tertiary_blue">#FF1C2538</color>
    <color name="border_blue">#FF243046</color>
    <color name="ripple_blue">#FF1F2937</color>

    <!-- Green Theme Backgrounds -->
    <color name="bg_primary_green">#FF0B1910</color>
    <color name="bg_secondary_green">#FF162E1D</color>
    <color name="bg_tertiary_green">#FF1C3823</color>
    <color name="border_green">#FF24462B</color>
    <color name="ripple_green">#FF1F3724</color>

    <!-- Purple Theme Backgrounds -->
    <color name="bg_primary_purple">#FF130B19</color>
    <color name="bg_secondary_purple">#FF23162E</color>
    <color name="bg_tertiary_purple">#FF2A1C38</color>
    <color name="border_purple">#FF352446</color>
    <color name="ripple_purple">#FF2B1F37</color>

    <!-- Red Theme Backgrounds -->
    <color name="bg_primary_red">#FF190B0B</color>
    <color name="bg_secondary_red">#FF2E1616</color>
    <color name="bg_tertiary_red">#FF381C1C</color>
    <color name="border_red">#FF462424</color>
    <color name="ripple_red">#FF371F1F</color>

    <!-- Yellow Theme Backgrounds -->
    <color name="bg_primary_yellow">#FF19140B</color>
    <color name="bg_secondary_yellow">#FF2E2616</color>
    <color name="bg_tertiary_yellow">#FF382F1C</color>
    <color name="border_yellow">#FF463A24</color>
    <color name="ripple_yellow">#FF372D1F</color>

    <!-- Orange Theme Backgrounds -->
    <color name="bg_primary_orange">#FF19100B</color>
    <color name="bg_secondary_orange">#FF2E1D16</color>
    <color name="bg_tertiary_orange">#FF38231C</color>
    <color name="border_orange">#FF462B24</color>
    <color name="ripple_orange">#FF37241F</color>

    <!-- Pink Theme Backgrounds -->
    <color name="bg_primary_pink">#FF190B12</color>
    <color name="bg_secondary_pink">#FF2E1621</color>
    <color name="bg_tertiary_pink">#FF381C28</color>
    <color name="border_pink">#FF462432</color>
    <color name="ripple_pink">#FF371F2B</color>
    
    <!-- Legacy compatibility mappings -->
    <color name="bg_primary">#FF0B0F19</color>
    <color name="bg_secondary">#FF161E2E</color>
    <color name="accent_dark">#FF0284C7</color>
    <color name="border_color">#FF243046</color>
    <color name="bg_tertiary">#FF1C2538</color>
    <color name="ripple_mono">#FF1F2937</color>
</resources>
'''

themes_xml = '''<?xml version="1.0" encoding="utf-8"?>
<resources xmlns:tools="http://schemas.android.com/tools">
    <!-- Base application theme. -->
    <style name="Base.Theme.NoteCalc" parent="Theme.Material3.DayNight.NoActionBar">
        <item name="android:windowBackground">?attr/colorBgPrimary</item>
        <item name="android:textColorPrimary">@color/text_primary</item>
    </style>

    <!-- Theme Variants -->
    <style name="Theme.NoteCalc.Blue" parent="Base.Theme.NoteCalc">
        <item name="colorAccentPrimary">@color/accent_blue_primary</item>
        <item name="colorAccentSecondary">@color/accent_blue_secondary</item>
        <item name="colorBgPrimary">@color/bg_primary_blue</item>
        <item name="colorBgSecondary">@color/bg_secondary_blue</item>
        <item name="colorBgTertiary">@color/bg_tertiary_blue</item>
        <item name="colorBorder">@color/border_blue</item>
        <item name="colorRipple">@color/ripple_blue</item>
    </style>

    <style name="Theme.NoteCalc.Green" parent="Base.Theme.NoteCalc">
        <item name="colorAccentPrimary">@color/accent_green_primary</item>
        <item name="colorAccentSecondary">@color/accent_green_secondary</item>
        <item name="colorBgPrimary">@color/bg_primary_green</item>
        <item name="colorBgSecondary">@color/bg_secondary_green</item>
        <item name="colorBgTertiary">@color/bg_tertiary_green</item>
        <item name="colorBorder">@color/border_green</item>
        <item name="colorRipple">@color/ripple_green</item>
    </style>

    <style name="Theme.NoteCalc.Purple" parent="Base.Theme.NoteCalc">
        <item name="colorAccentPrimary">@color/accent_purple_primary</item>
        <item name="colorAccentSecondary">@color/accent_purple_secondary</item>
        <item name="colorBgPrimary">@color/bg_primary_purple</item>
        <item name="colorBgSecondary">@color/bg_secondary_purple</item>
        <item name="colorBgTertiary">@color/bg_tertiary_purple</item>
        <item name="colorBorder">@color/border_purple</item>
        <item name="colorRipple">@color/ripple_purple</item>
    </style>

    <style name="Theme.NoteCalc.Red" parent="Base.Theme.NoteCalc">
        <item name="colorAccentPrimary">@color/accent_red_primary</item>
        <item name="colorAccentSecondary">@color/accent_red_secondary</item>
        <item name="colorBgPrimary">@color/bg_primary_red</item>
        <item name="colorBgSecondary">@color/bg_secondary_red</item>
        <item name="colorBgTertiary">@color/bg_tertiary_red</item>
        <item name="colorBorder">@color/border_red</item>
        <item name="colorRipple">@color/ripple_red</item>
    </style>

    <style name="Theme.NoteCalc.Yellow" parent="Base.Theme.NoteCalc">
        <item name="colorAccentPrimary">@color/accent_yellow_primary</item>
        <item name="colorAccentSecondary">@color/accent_yellow_secondary</item>
        <item name="colorBgPrimary">@color/bg_primary_yellow</item>
        <item name="colorBgSecondary">@color/bg_secondary_yellow</item>
        <item name="colorBgTertiary">@color/bg_tertiary_yellow</item>
        <item name="colorBorder">@color/border_yellow</item>
        <item name="colorRipple">@color/ripple_yellow</item>
    </style>

    <style name="Theme.NoteCalc.Orange" parent="Base.Theme.NoteCalc">
        <item name="colorAccentPrimary">@color/accent_orange_primary</item>
        <item name="colorAccentSecondary">@color/accent_orange_secondary</item>
        <item name="colorBgPrimary">@color/bg_primary_orange</item>
        <item name="colorBgSecondary">@color/bg_secondary_orange</item>
        <item name="colorBgTertiary">@color/bg_tertiary_orange</item>
        <item name="colorBorder">@color/border_orange</item>
        <item name="colorRipple">@color/ripple_orange</item>
    </style>

    <style name="Theme.NoteCalc.Pink" parent="Base.Theme.NoteCalc">
        <item name="colorAccentPrimary">@color/accent_pink_primary</item>
        <item name="colorAccentSecondary">@color/accent_pink_secondary</item>
        <item name="colorBgPrimary">@color/bg_primary_pink</item>
        <item name="colorBgSecondary">@color/bg_secondary_pink</item>
        <item name="colorBgTertiary">@color/bg_tertiary_pink</item>
        <item name="colorBorder">@color/border_pink</item>
        <item name="colorRipple">@color/ripple_pink</item>
    </style>
    
    <style name="CustomDialogTheme" parent="Theme.AppCompat.Dialog.Alert">
        <item name="android:background">?attr/colorBgSecondary</item>
        <item name="android:textColorPrimary">@color/text_primary</item>
        <item name="android:textColor">@color/text_primary</item>
        <item name="colorAccent">?attr/colorAccentSecondary</item>
    </style>
</resources>
'''

with open(r'app\src\main\res\values\colors.xml', 'w', encoding='utf-8') as f: f.write(light_xml)
with open(r'app\src\main\res\values-night\colors.xml', 'w', encoding='utf-8') as f: f.write(dark_xml)
with open(r'app\src\main\res\values\themes.xml', 'w', encoding='utf-8') as f: f.write(themes_xml)

print("Colors updated!")
