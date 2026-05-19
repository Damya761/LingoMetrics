package de.lingoMetrics.Repository;

import de.lingoMetrics.Enums.WortTyp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class WordRepositoryTest {

    private WordRepository repository;

    @BeforeEach
    void setUp() throws IOException {
        // Bereitet vor jedem Test ein frisch geladenes Repository vor
        repository = new WordRepository();
        repository.load();
    }

    @Test
    void testIsFunktionswort() {
        String gueltigesFunktionswort = "aber";
        String ungueltigesFunktionswort = "Computer";

        assertTrue(repository.isFunktionswort(gueltigesFunktionswort),
                "Das Wort '" + gueltigesFunktionswort + "' müsste als Funktionswort erkannt werden.");
        assertFalse(repository.isFunktionswort(ungueltigesFunktionswort),
                "Das Wort '" + ungueltigesFunktionswort + "' dürfte KEIN Funktionswort sein.");
    }

    @Test
    void testIsFuellwort() {
        String gueltigesFuellwort = "daraus";
        String keinFuellwort = "C ist eine tolle Programmiersprache";

        assertTrue(repository.isFuellwort(gueltigesFuellwort));
        assertFalse(repository.isFuellwort(keinFuellwort));
    }

    @Test
    void testGetSentiment() {
        String negativesWort = "Abneigung";
        String unbekanntesWort = "Timmy der Buckelwal";

        double sentimentNegativ = repository.getSentiment(negativesWort);
        double sentimentUnbekannt = repository.getSentiment(unbekanntesWort);

        assertEquals(-0.0048, sentimentNegativ, "Sentiment für '" + negativesWort + "' wurde nicht korrekt geladen.");
        assertEquals(0.0, sentimentUnbekannt, "Ein unbekanntes Wort muss exakt 0.0 zurückgeben.");
    }

    @Test
    void testGetWortTyp() {
        String einVerb = "sagst";
        String einAdjektiv = "aalförmig";
        String unbekannterTyp = "Schlechter Witz";

        assertEquals(WortTyp.TYP_VERB, repository.getWortTyp(einVerb),
                "'" + einVerb + "' sollte als TYP_VERB erkannt werden.");
        assertEquals(WortTyp.TYP_ADJEKTIV, repository.getWortTyp(einAdjektiv),
                "'" + einAdjektiv + "' sollte als TYP_ADJEKTIV erkannt werden.");
        assertEquals(WortTyp.TYP_OTHER, repository.getWortTyp(unbekannterTyp),
                "Unbekannte Wortarten müssen TYP_OTHER sein.");
    }

    @Test
    void testMethodsWithEmptyAndNullInput() {
        assertFalse(repository.isFunktionswort(""));
        assertFalse(repository.isFuellwort(""));
        assertEquals(0.0, repository.getSentiment(""));
        assertEquals(WortTyp.TYP_OTHER, repository.getWortTyp(""));

        assertFalse(repository.isFunktionswort(null));
        assertFalse(repository.isFuellwort(null));
        assertEquals(WortTyp.TYP_OTHER, repository.getWortTyp(null));
    }
}