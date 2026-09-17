import re

with open('app/src/main/java/com/example/ui/components/NoteCard.kt', 'r') as f:
    content = f.read()

# I want to delete the whole "Unified Blocks Preview" if block and unnest the fallback

start_idx = content.find("// Unified Blocks Preview")
fallback_idx = content.find("// Fallback for legacy notes")
end_else_idx = content.find("Spacer(modifier = Modifier.height(10.dp))", fallback_idx)

if start_idx != -1 and fallback_idx != -1:
    # Get the inner code of fallback
    fallback_code = content[fallback_idx:end_else_idx]
    # Unindent it by removing 12 spaces if it was indented (it was inside `else {`)
    lines = fallback_code.split('\n')
    unindented_lines = []
    for line in lines:
        if line.startswith("            "):
            unindented_lines.append(line[12:])
        else:
            unindented_lines.append(line)
    
    new_fallback = '\n'.join(unindented_lines)
    
    # Replace everything from start_idx to end_else_idx with new_fallback
    # But wait, there's a closing brace `}` before `Spacer` that belongs to `else {`
    # The actual structure is:
    # } else {
    #     // Fallback for legacy notes
    #     ...
    # }
    # Spacer(...)
    
    content = content[:start_idx] + new_fallback + "\n            " + content[end_else_idx:]

with open('app/src/main/java/com/example/ui/components/NoteCard.kt', 'w') as f:
    f.write(content)
print("Done")
