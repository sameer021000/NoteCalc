import re

file_path = 'settings.gradle.kts'
with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

content = content.replace(
'''dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}''',
'''dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}'''
)

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)
print("Settings updated!")
