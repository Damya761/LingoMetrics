import os
import json
import pandas as pd

#KI Generierter code:
def calculate_reference_profiles(input_folder, output_file):
    # Liste, die die Endergebnisse für jede Datei speichert
    combined_results = []

    # Überprüfen, ob der Ordner existiert
    if not os.path.exists(input_folder):
        print(f"Fehler: Der Ordner '{input_folder}' existiert nicht.")
        return

    # Alle Dateien im Ordner durchgehen
    for filename in os.listdir(input_folder):
        if filename.endswith(".json"):
            file_path = os.path.join(input_folder, filename)
            
            # Name ohne ".json" extrahieren
            profile_name = os.path.splitext(filename)[0]
            print(f"Verarbeite: {filename} -> Profilname: {profile_name}")

            try:
                # JSON-Datei in ein Pandas DataFrame laden
                df = pd.read_json(file_path)
                
                # Nicht-numerische Spalten (wie 'text_preview') ausschließen
                numeric_df = df.select_dtypes(include=['number'])

                if numeric_df.empty:
                    print(f"Warnung: Keine numerischen Daten in {filename} gefunden.")
                    continue

                # Mittelwert (mean) und Standardabweichung (std) berechnen
                means = numeric_df.mean()
                stds = numeric_df.std().fillna(0.0) # Falls nur 1 Text drin ist, wäre std NaN -> zu 0.0 machen

                # Struktur für dieses spezifische Profil aufbauen
                profile_data = {
                    "stiltyp": profile_name,
                    "metriken": {}
                }

                # Alle berechneten Parameter in das Profil eintragen
                for column in numeric_df.columns:
                    profile_data["metriken"][column] = {
                        "mittelwert": round(float(means[column]), 4),
                        "standardabweichung": round(float(stds[column]), 4)
                    }

                combined_results.append(profile_data)

            except Exception as e:
                print(f"Fehler beim Verarbeiten von {filename}: {e}")

    # Das gesammelte Ergebnis in eine große JSON-Datei schreiben
    with open(output_file, "w", encoding="utf-8") as f:
        json.dump(combined_results, f, indent=4, ensure_ascii=False)
        
    print(f"\nErfolgreich! Das kombinierte Referenzprofil wurde unter '{output_file}' gespeichert.")

if __name__ == "__main__":
    # Pfade definieren (Pfade bei Bedarf anpassen)
    INPUT_DIR = "Output"
    OUTPUT_JSON = "referenz_profile.json"
    
    calculate_reference_profiles(INPUT_DIR, OUTPUT_JSON)