import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

target = """                // Empty State or Notes List
                if (filteredNotes.isEmpty()) {
                    item {
                        Card("""

replacement = """                // Empty State or Notes List
                if (filteredNotes.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillParentMaxHeight(0.75f),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            Card("""

content = content.replace(target, replacement)

# Now we need to add the closing brace for the Box after the Card
target2 = """                                        "ملاحظة جديدة"
                                    )
                                }
                            }
                        }
                    }
                } else {"""
replacement2 = """                                        "ملاحظة جديدة"
                                    )
                                }
                            }
                        }
                        } // End of Box
                    }
                } else {"""

content = content.replace(target2, replacement2)

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)

print("Done")
