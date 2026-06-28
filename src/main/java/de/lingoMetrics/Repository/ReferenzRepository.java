package de.lingoMetrics.Repository;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

//Autor: Simon Ortlieb
public class ReferenzRepository {

    // Äußere Map: Stiltyp (z.B. "Mails") -> Innere Map: MetrikKey -> Stat-Objekt
    private final Map<String, Map<String, MetricStats>> profiles = new HashMap<>();

    public ReferenzRepository() {
        load();
    }

    protected ReferenzRepository(boolean skipLoad) {
        // leer — für Testdoubles
    }

    private void load() {
        try {
            ObjectMapper mapper = new ObjectMapper();

            // Lade die JSON-Datei (Stelle sicher, dass sie in src/main/resources/data/ liegt)
            InputStream is = getClass().getResourceAsStream("/data/referenz_profile.json");
            if (is == null) {
                System.err.println("Warnung: referenz_profile.json wurde im resources-Ordner nicht gefunden!");
                return;
            }

            // Die neue JSON-Struktur ist ein Array
            JsonNode rootArray = mapper.readTree(is);

            for (JsonNode profileNode : rootArray) {
                String stiltyp = profileNode.get("stiltyp").asText();
                JsonNode metrikenNode = profileNode.get("metriken");

                Map<String, MetricStats> metrikMap = new HashMap<>();

                metrikenNode.fields().forEachRemaining(entry -> {
                    String metrikKey = entry.getKey();

                    JsonNode statsNode = entry.getValue();
                    double mittelwert = statsNode.get("mittelwert").asDouble();
                    double stdAbw = statsNode.get("standardabweichung").asDouble();

                    metrikMap.put(metrikKey, new MetricStats(mittelwert, stdAbw));
                });

                profiles.put(stiltyp, metrikMap);
            }
        } catch (Exception e) {
            throw new RuntimeException("Referenzprofile konnten nicht geladen werden.", e);
        }
    }

    /**
     * Gibt den Mittelwert für eine spezifische Metrik in einem bestimmten Stiltyp zurück.
     */
    public Double getMittelwert(String stiltyp, String metrikKey) {
        MetricStats stats = getStats(stiltyp, metrikKey);
        return (stats != null) ? stats.getMittelwert() : null;
    }

    /**
     * Gibt die Standardabweichung für eine spezifische Metrik in einem bestimmten Stiltyp zurück.
     */
    public Double getStandardabweichung(String stiltyp, String metrikKey) {
        MetricStats stats = getStats(stiltyp, metrikKey);
        return (stats != null) ? stats.getStandardabweichung() : null;
    }

    /**
     * Hilfsmethode, um das Statistik-Objekt sicher aus der Map zu holen.
     */
    private MetricStats getStats(String stiltyp, String metrikKey) {
        Map<String, MetricStats> metrikMap = profiles.get(stiltyp);
        if (metrikMap != null) {
            return metrikMap.get(metrikKey);
        }
        return null;
    }

    /**
     * Interne Hilfsklasse (Data Transfer Object), um beide Werte kompakt zusammenzuhalten.
     */
    public static class MetricStats {
        private final double mittelwert;
        private final double standardabweichung;

        public MetricStats(double mittelwert, double standardabweichung) {
            this.mittelwert = mittelwert;
            this.standardabweichung = standardabweichung;
        }

        public double getMittelwert() {
            return mittelwert;
        }

        public double getStandardabweichung() {
            return standardabweichung;
        }
    }
}