import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

# Replace the start of LazyColumn with Column
start_target = """        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 14.dp),
                contentPadding = PaddingValues(bottom = 88.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // =============================================================
                // Requirement 1: Tabs Bar & Management
                // =============================================================
                item {"""

start_replacement = """        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 14.dp)
            ) {
                // =============================================================
                // Requirement 1: Tabs Bar & Management (Sticky Top)
                // =============================================================
                Spacer(modifier = Modifier.height(8.dp))"""

content = content.replace(start_target, start_replacement)

# Remove the closing brace of the `item` block for Tabs, and start the LazyColumn
end_target = """                        }
                    }
                }

                // =============================================================
                // Requirement 1: Tab Header & Dedicated Add Note Action inside Tab
                // =============================================================
                if (selectedGameTag != "الكل") {"""

end_replacement = """                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                // =============================================================
                // Requirement 1: Tab Header & Dedicated Add Note Action inside Tab
                // =============================================================
                if (selectedGameTag != "الكل") {"""

content = content.replace(end_target, end_replacement)

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)

print("Done")
