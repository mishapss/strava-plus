import sys

has_error = False

for file_path in sys.argv[1:]:
    try:
        with open(file_path, encoding='utf-8') as f:
            lines = f.readlines()
    except UnicodeDecodeError as e:
        print(f"Datei {file_path} ist nicht in UTF-8 kodiert")
        has_error = True

if has_error:
    sys.exit(1)