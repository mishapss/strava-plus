import sys

has_error = False

for file_path in sys.argv[1:]: #schleife über alle dateien die an commit gesendet wurden
    try:
        with open(file_path, encoding='utf-8') as file: #öffnet die datei im lesemodus und versucht mit urf8 die datei kodieren
            lines = file.readlines() #speichert die liste von texten(eingelesene dateien)
    except UnicodeDecodeError as e:
        print(f"Datei {file_path} ist nicht in UTF-8 kodiert")
        has_error = True

if has_error:
    sys.exit(1)


