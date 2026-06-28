package de.lingoMetrics.Service;

import de.lingoMetrics.Models.Absatz;
import de.lingoMetrics.Models.Document;
import de.lingoMetrics.Models.Satz;
import de.lingoMetrics.Models.Wort;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

//Autor: Tarik Marton
class TextStrukturServiceTest {

    private final TextStrukturService textStrukturService = new TextStrukturService();

    @Test
    void createDocument_shouldCreateDocumentWithAbsatzSatzAndWoerter() {
        String text = "Hallo Welt. Das ist ein Test!";

        Document document = textStrukturService.createDocument(text);

        assertNotNull(document);

        assertNotNull(document.getAbsaetze());
        assertNotNull(document.getSaetze());
        assertNotNull(document.getWoerter());

        assertEquals(1, document.getAbsaetze().size());
        assertEquals(2, document.getSaetze().size());

        assertFalse(document.getWoerter().isEmpty());
    }

    @Test
    void createDocument_shouldSplitTextIntoCorrectSentences() {
        String text = "Hallo Welt. Das ist ein Test!";

        Document document = textStrukturService.createDocument(text);

        Satz ersterSatz = document.getSaetze().get(0);
        Satz zweiterSatz = document.getSaetze().get(1);

        assertEquals(2, ersterSatz.getWoerterAnzahl());
        assertEquals(4, zweiterSatz.getWoerterAnzahl());
    }

    @Test
    void createDocument_shouldRecognizePunctuationMarks() {
        String text = "Hallo Welt.";

        Document document = textStrukturService.createDocument(text);

        List<Wort> woerter = document.getWoerter();

        assertEquals("Hallo", woerter.get(0).getInhalt());
        assertFalse(woerter.get(0).isSatzzeichen());

        assertEquals("Welt", woerter.get(1).getInhalt());
        assertFalse(woerter.get(1).isSatzzeichen());

        assertEquals(".", woerter.get(2).getInhalt());
        assertTrue(woerter.get(2).isSatzzeichen());
    }

    @Test
    void createDocument_shouldSetWordLengthCorrectly() {
        String text = "Hallo Welt.";

        Document document = textStrukturService.createDocument(text);

        List<Wort> woerter = document.getWoerter();

        assertEquals(5, woerter.get(0).getLaenge());
        assertEquals(4, woerter.get(1).getLaenge());

        assertEquals(0, woerter.get(2).getLaenge());
    }

    @Test
    void createDocument_shouldSplitParagraphsCorrectly() {
        String text = "Erster Absatz.\n\nZweiter Absatz.";

        Document document = textStrukturService.createDocument(text);

        assertEquals(2, document.getAbsaetze().size());
        assertEquals(2, document.getSaetze().size());

        Absatz ersterAbsatz = document.getAbsaetze().get(0);
        Absatz zweiterAbsatz = document.getAbsaetze().get(1);

        assertEquals(1, ersterAbsatz.getSatz().size());
        assertEquals(1, zweiterAbsatz.getSatz().size());
    }

    @Test
    void createDocument_shouldThrowExceptionForNullText() {
        assertThrows(IllegalArgumentException.class, () -> {
            textStrukturService.createDocument(null);
        });
    }

    @Test
    void createDocument_shouldThrowExceptionForEmptyText() {
        assertThrows(IllegalArgumentException.class, () -> {
            textStrukturService.createDocument("   ");
        });
    }
}