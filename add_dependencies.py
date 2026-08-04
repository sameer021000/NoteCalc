import re

# settings.gradle.kts
file_path = 'settings.gradle.kts'
with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

content = content.replace('mavenCentral()', 'mavenCentral()\n        maven { url = uri("https://jitpack.io") }', 1)

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)

# app/build.gradle.kts
file_path = r'app\build.gradle.kts'
with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

content = content.replace('implementation(libs.constraintlayout)', 'implementation(libs.constraintlayout)\n    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")')

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)

print("Dependencies added!")
