package de.lingoMetrics.Service;

import de.lingoMetrics.Models.Document;
import de.lingoMetrics.Repository.ReferenzRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AuswertungsServiceTest {

    private AuswertungsService service;

    @BeforeEach
    void setUp() {
        // Wir injizieren unser Fake-Repository für kontrollierte Testbedingungen
        service = new AuswertungsService(new FakeReferenzRepository());
    }

    @Test
    void calculateScore_gibtScore100_wennMetrikenPerfektImProfilLiegen() {
        Document document = new Document();
        // Wir setzen exakt die Mittelwerte aus unserem Fake-Repository
        document.setMittlereSatzlaenge(12.0);
        document.setFuellwoerterAnteil(0.05);
        document.setTypeTokenRatio(0.40);
        document.setLesbarkeitsindex(45.0);
        document.setSatzlaengenunterschied(5.0);

        List<String> hinweise = new ArrayList<>();
        int score = service.calculateScore(document, "Mails", hinweise);

        assertEquals(100, score, "Der Score sollte 100 sein, da alle Werte im Toleranzbereich liegen.");
        assertEquals("Sehr gut", service.determineRating(score));
        assertTrue(hinweise.isEmpty(), "Es sollten keine Hinweise generiert werden.");
    }

    @Test
    void calculateScore_ziehtPunkteAb_wennMetrikenStarkVomProfilAbweichen() {
        Document document = new Document();
        // Wir setzen extrem schlechte Werte, die weit über der Toleranzgrenze (Mittelwert + 1.5 * Std) liegen
        document.setMittlereSatzlaenge(25.0); // Zu hoch (Erlaubt ca. 12 + (2*1.5) = 15)
        document.setFuellwoerterAnteil(0.30); // Viel zu hoch
        document.setTypeTokenRatio(0.10);     // Zu niedrig

        List<String> hinweise = new ArrayList<>();
        int score = service.calculateScore(document, "Mails", hinweise);

        assertTrue(score < 100, "Der Score sollte unter 100 fallen, da Metriken stark abweichen.");
        assertFalse(hinweise.isEmpty(), "Es sollten Hinweise generiert worden sein.");

        // Prüfen ob die spezifischen Fehlermeldungen (aus checkMetric) getriggert wurden
        assertTrue(hinweise.stream().anyMatch(h -> h.contains("ungewöhnlich lang")), "Hinweis für zu lange Sätze fehlt.");
        assertTrue(hinweise.stream().anyMatch(h -> h.contains("Füllwörtern")), "Hinweis für Füllwörter fehlt.");
    }

    @Test
    void calculateScore_greiftAufAllgemeineHeuristikZurueck_wennKeinStiltypGesetztIst() {
        Document document = new Document();
        // Werte, die die allgemeinen Fallback-Checks triggern
        document.setMittlereSatzlaenge(25.0); // Trigger: > 20.0
        document.setFuellwoerterAnteil(0.50); // Trigger: > 0.40

        List<String> hinweise = new ArrayList<>();
        // Aufruf OHNE Stiltyp (null)
        int score = service.calculateScore(document, null, hinweise);

        assertTrue(score < 100, "Auch ohne Profil sollten extreme Werte durch Heuristiken bestraft werden.");
        assertFalse(hinweise.isEmpty());
        // Die Meldung muss nun die aus dem Fallback-Check sein
        assertTrue(hinweise.stream().anyMatch(h -> h.contains("durchschnittliche Satzlänge ist sehr hoch")));
    }


    // --- HILFSKLASSE FÜR DEN TEST --- //

    /**
     * Ein Fake-Repository, das konstante Werte zurückliefert, damit unsere Tests
     * unabhängig von einer echten JSON-Datei auf der Festplatte funktionieren.
     */
    private static class FakeReferenzRepository extends ReferenzRepository {

        // Verhindert, dass das Fake-Repo beim Testen versucht, die echte JSON zu laden
        public FakeReferenzRepository() {
            super();
        }

        @Override
        public Double getMittelwert(String stiltyp, String metrikKey) {
            if ("Mails".equals(stiltyp)) {
                return switch (metrikKey) {
                    case "mittlereSatzlaenge" -> 12.0;
                    case "fuellwoerterAnteil" -> 0.05;
                    case "typeTokenRatio" -> 0.40;
                    case "lesbarkeitsindex" -> 45.0;
                    case "satzlaengenunterschied" -> 5.0;
                    default -> 0.0;
                };
            }
            return null; // Wenn der Stiltyp nicht gefunden wird
        }

        @Override
        public Double getStandardabweichung(String stiltyp, String metrikKey) {
            if ("Mails".equals(stiltyp)) {
                return switch (metrikKey) {
                    case "mittlereSatzlaenge" -> 2.0;
                    case "fuellwoerterAnteil" -> 0.02;
                    case "typeTokenRatio" -> 0.05;
                    case "lesbarkeitsindex" -> 5.0;
                    case "satzlaengenunterschied" -> 1.0;
                    default -> 0.0;
                };
            }
            return null;
        }
    }
}