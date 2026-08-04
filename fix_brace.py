import re

file_path = r'app\src\main\java\com\example\notecalc\MainActivity.java'
with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

search_brace = '''                        if (useScaleAnimation) {
                            view.animate().scaleX(1.0f).scaleY(1.0f).setDuration(60).start();
                        }
                    }
                }
            };'''
replace_brace = '''                        if (useScaleAnimation) {
                            view.animate().scaleX(1.0f).scaleY(1.0f).setDuration(60).start();
                        }
                    }
            };'''
content = content.replace(search_brace, replace_brace)

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)
print("Brace fixed")
