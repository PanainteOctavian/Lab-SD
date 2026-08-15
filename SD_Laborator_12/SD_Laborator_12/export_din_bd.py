#!/usr/bin/env python3
import sqlite3
import shutil
import os

# Calea către profilul Firefox (atenție la numele folderului .default)
# Înlocuiește 'abcd12ef.default' cu numele real al folderului tău
db_path = os.path.expanduser('~/.mozilla/firefox/abcd12ef.default/places.sqlite')
temp_db = 'places_copy.sqlite'
output_file = 'urls.txt'

try:
    # 1. Copiem baza de date pentru a evita blocarea
    shutil.copy2(db_path, temp_db)

    # 2. Conectare la baza de date copiată
    conn = sqlite3.connect(temp_db)
    cursor = conn.cursor()

    # 3. Interogare pentru a extrage URL-ul și vizitele (frecvența)
    # Tabelul moz_places conține coloanele 'url' și 'visit_count'
    cursor.execute("SELECT url, visit_count FROM moz_places WHERE url LIKE 'http%'")
    
    with open(output_file, 'w') as f:
        for row in cursor.fetchall():
            url, count = row
            # Format: URL <tab> frecvență
            f.write(f"{url}\t{count}\n")
    
    print(f"Fișierul {output_file} a fost generat cu succes.")

    conn.close()
    os.remove(temp_db) # Ștergem copia temporară

except Exception as e:
    print(f"Eroare: {e}")
