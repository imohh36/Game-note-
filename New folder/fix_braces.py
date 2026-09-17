import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

# Fix the broken braces area
bad_pattern = """                                    } else {
                                        FloatingOverlayService.stop(context)
                                    }
                                }
                            )
                    // AI Settings & Overlay Behavior"""

good_pattern = """                                    } else {
                                        FloatingOverlayService.stop(context)
                                    }
                                }
                            )
                        }
                    }
                    // AI Settings & Overlay Behavior"""
content = content.replace(bad_pattern, good_pattern)

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)

print("Done")
