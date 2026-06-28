package de.lingoMetrics.Service;

import de.lingoMetrics.Models.Document;
import de.lingoMetrics.Models.Satz;
import de.lingoMetrics.Models.Wort;
import de.lingoMetrics.Service.analysis.TextStatisticsService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TextStatisticsServiceTest {

    @Test
    void analyzeCalculatesBasicTextStatisticsCorrectly() {
        Document document = new Document();

        Wort hallo = createWord("Hallo", false);
        Wort welt = createWord("Welt", false);
        Wort punkt1 = createWord(".", true);

        Wort das = createWord("Das", false);
        Wort ist = createWord("ist", false);
        Wort ein = createWord("ein", false);
        Wort einfacher = createWord("einfacher", false);
        Wort test = createWord("Test", false);
        Wort punkt2 = createWord(".", true);

        Satz satz1 = new Satz();
        satz1.setWoerter(List.of(hallo, welt));
        satz1.setWoerterAnzahl(2);

        Satz satz2 = new Satz();
        satz2.setWoerter(List.of(das, ist, ein, einfacher, test));
        satz2.setWoerterAnzahl(5);

        document.setSaetze(List.of(satz1, satz2));
        document.setWoerter(List.of(
                hallo, welt, punkt1,
                das, ist, ein, einfacher, test, punkt2
        ));

        TextStatisticsService service = new TextStatisticsService();
        service.analyze(document);

        assertEquals(3.5, document.getMittlereSatzlaenge());
        assertEquals(3.0, document.getSatzlaengenunterschied());
        assertEquals(4.428571428571429, document.getWortlaengenverteilung(), 0.0001);
        assertEquals(2L, document.getInterpunktion().get("."));
        assertEquals(17.785714285714285, document.getLesbarkeitsindex(), 0.0001);
    }

    private Wort createWord(String inhalt, boolean isSatzzeichen) {
        Wort wort = new Wort();
        wort.setInhalt(inhalt);
        wort.setSatzzeichen(isSatzzeichen);
        wort.setLaenge(inhalt.length());
        return wort;
    }
}
