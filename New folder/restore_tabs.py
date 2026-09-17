import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

# Replace the Column + LazyColumn structure back to a single LazyColumn
target = """            // =============================================================
            // MAIN CONTENT: Tabs (Sticky Top) + Notes List
            // =============================================================
            
            Column(modifier = Modifier.fillMaxSize()) {
                
                // Requirement 1: Tabs Bar & Management (Sticky Top)
                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
                ) {"""
replacement = """            // =============================================================
            // MAIN CONTENT: Tabs + Notes List
            // =============================================================
            
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 12.dp, bottom = 88.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                
                // Requirement 1: Tabs Bar & Management
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {"""
content = content.replace(target, replacement)

# Now we need to remove the closing brace of the Column and the start of the LazyColumn
target2 = """                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {"""
replacement2 = """                    }
                }"""
content = content.replace(target2, replacement2)

# Also remove the extra closing brace at the very end of the Scaffold
target3 = """                    }
                }
            }
        }
    }"""
# Wait, let's just count braces carefully. 
# It's easier to use sed or just edit it manually.
