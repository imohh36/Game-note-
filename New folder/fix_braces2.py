import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

bad = """                    }
                }
                }
                }
            }
        }
    ) {"""

good = """                    }
                }
            }
        }
    ) {"""
content = content.replace(bad, good)

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)
print("Done")
