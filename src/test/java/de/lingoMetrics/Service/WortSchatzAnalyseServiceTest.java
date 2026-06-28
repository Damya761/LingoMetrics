package de.lingoMetrics.Service;

import de.lingoMetrics.Enums.WortTyp;
import de.lingoMetrics.Models.Document;
import de.lingoMetrics.Models.Wort;
import de.lingoMetrics.Repository.WordRepository;
import de.lingoMetrics.Service.analysis.WortSchatzAnalyseService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

//Autor: Simon Ortlieb, Tarik Marton
class WortSchatzAnalyseServiceTest {

    @Test
    void analyze_aggregiertFunktionswoerterFuellwoerterUndSentimentOhneSatzzeichen() {
        Document document = new Document();
        document.setWoerter(List.of(
                createWord("Aber", false),
                createWord("halt", false),
                createWord("Abbau", false),
                createWord("neutral", false),
                createWord(".", true)
        ));

        WortSchatzAnalyseService service = new WortSchatzAnalyseService(new FakeWordRepository());

        service.analyze(document);

        assertEquals(0.25, document.getFunktionswoerterAnteil(), 0.0001);
        assertEquals(0.25, document.getFuellwoerterAnteil(), 0.0001);
        assertEquals(-0.0145, document.getMittleresSentiment(), 0.0001);
        assertEquals(-0.0145, document.getMittlereKonkretheit(), 0.0001);
    }

    private Wort createWord(String inhalt, boolean satzzeichen) {
        Wort wort = new Wort();
        wort.setInhalt(inhalt);
        wort.setSatzzeichen(satzzeichen);
        wort.setLaenge(satzzeichen ? 0 : inhalt.length());
        return wort;
    }

    private static class FakeWordRepository extends WordRepository {
        @Override
        public boolean isFunktionswort(String wort) {
            return "aber".equalsIgnoreCase(wort);
        }

        @Override
        public boolean isFuellwort(String wort) {
            return "halt".equalsIgnoreCase(wort);
        }

        @Override
        public double getSentiment(String wort) {
            return "abbau".equalsIgnoreCase(wort) ? -0.058 : 0.0;
        }

        @Override
        public double getKonkretheit(String wort) {
            return "abbau".equalsIgnoreCase(wort) ? -0.058 : 0.0;
        }

        @Override
        public WortTyp getWortTyp(String wort) {
            return WortTyp.TYP_OTHER;
        }
    }
}
