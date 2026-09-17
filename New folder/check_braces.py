with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    lines = f.readlines()

count = 0
for i, line in enumerate(lines):
    for c in line:
        if c == '{': count += 1
        elif c == '}': count -= 1
    if "ModalNavigationDrawer(" in line:
        print(f"ModalNavigationDrawer at {i+1}, count: {count}")
    if i+1 == 718:
        print(f"Line 718, count: {count}")
    if i+1 == 1347:
        print(f"Line 1347, count: {count}")

