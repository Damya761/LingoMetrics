package de.lingoMetrics.Service;

import de.lingoMetrics.Models.AnalysisResult;
import de.lingoMetrics.Models.Document;
import de.lingoMetrics.Repository.JsonReferenzRepository;
import de.lingoMetrics.Repository.ReferenzRepository;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AuswertungsServiceTest {

    @Test
    void calculateScore_gibtScore100UndKeineHinweiseZurueck_wennAlleMetrikenGutSind() {
        AuswertungsService service = new AuswertungsService(new FakeReferenzRepository());

        Document document = new Document();
        document.setMittlereSatzlaenge(18.0);
        document.setFuellwoerterAnteil(0.05);
        document.setTypeTokenRatio(0.60);

        List<String> hinweise = new ArrayList<>();
        int score = service.calculateScore(document, hinweise);

        assertEquals(100, score);
        assertEquals("Sehr gut", service.determineRating(score));
        assertTrue(hinweise.isEmpty());
    }

    @Test
    void calculateScore_ziehtPunkteAbUndErzeugtHinweise_wennAlleMetrikenSchlechtSind() {
        AuswertungsService service = new AuswertungsService(new FakeReferenzRepository());

        Document document = new Document();
        document.setMittlereSatzlaenge(30.0);
        document.setFuellwoerterAnteil(0.12);
        document.setTypeTokenRatio(0.30);

        List<String> hinweise = new ArrayList<>();
        int score = service.calculateScore(document, hinweise);

        assertEquals(60, score);
        assertEquals("Befriedigend", service.determineRating(score));
        assertEquals(3, hinweise.size());

        assertTrue(hinweise.stream().anyMatch(hinweis -> hinweis.contains("Referenzwert")));
        assertTrue(hinweise.stream().anyMatch(hinweis -> hinweis.contains("vergleichsweise")));
        assertTrue(hinweise.stream().anyMatch(hinweis -> hinweis.contains("Wortvielfalt")));
    }

    @Test
    void calculateScore_ziehtNurFuellwortPunkteAb_wennNurFuellwortanteilZuHochIst() {
        AuswertungsService service = new AuswertungsService(new FakeReferenzRepository());

        Document document = new Document();
        document.setMittlereSatzlaenge(18.0);
        document.setFuellwoerterAnteil(0.10);
        document.setTypeTokenRatio(0.60);

        List<String> hinweise = new ArrayList<>();
        int score = service.calculateScore(document, hinweise);

        assertEquals(85, score);
        assertEquals("Gut", service.determineRating(score));
        assertEquals(1, hinweise.size());
        assertTrue(hinweise.stream().anyMatch(hinweis -> hinweis.contains("vergleichsweise")));
    }

    @Test
    void analyse_speichertAuswertungImServiceManagerResult_wennComparisonAktivIst() {
        ServiceManager serviceManager = new ServiceManager(
                new TextStrukturService(),
                List.of(document -> {
                    document.setMittlereSatzlaenge(30.0);
                    document.setFuellwoerterAnteil(0.12);
                    document.setTypeTokenRatio(0.30);
                }),
                new AuswertungsService(new FakeReferenzRepository())
        );

        ServiceManager.AnalysisRequest request = new ServiceManager.AnalysisRequest(
                "Hallo Welt.",
                "Artikel",
                false,
                true
        );

        AnalysisResult result = serviceManager.analyse(request);

        assertTrue(result.comparison());
        assertTrue(result.hasAuswertung());
        assertEquals(60, result.score());
        assertEquals("Befriedigend", result.gesamtBewertung());
        assertEquals(3, result.hinweise().size());
    }

    @Test
    void analyse_laesstAuswertungLeer_wennComparisonInaktivIst() {
        ServiceManager serviceManager = new ServiceManager(
                new TextStrukturService(),
                List.of(document -> {
                    document.setMittlereSatzlaenge(30.0);
                    document.setFuellwoerterAnteil(0.12);
                    document.setTypeTokenRatio(0.30);
                }),
                new AuswertungsService(new FakeReferenzRepository())
        );

        ServiceManager.AnalysisRequest request = new ServiceManager.AnalysisRequest(
                "Hallo Welt.",
                null,
                false,
                false
        );

        AnalysisResult result = serviceManager.analyse(request);

        assertFalse(result.comparison());
        assertFalse(result.hasAuswertung());
        assertNull(result.score());
        assertNull(result.gesamtBewertung());
        assertTrue(result.hinweise().isEmpty());
    }

    @Test
    void calculateScore_bewertetUnterschiedlichJeNachStiltyp() {
        JsonReferenzRepository repository = new JsonReferenzRepository();
        AuswertungsService service = new AuswertungsService(repository);

        Document document = new Document();
        document.setMittlereSatzlaenge(22.0);
        document.setFuellwoerterAnteil(0.05);
        document.setTypeTokenRatio(0.60);

        List<String> hinweiseWissenschaft = new ArrayList<>();
        int scoreWissenschaft = service.calculateScore(document, "Wissenschaftliche Arbeit", hinweiseWissenschaft);

        List<String> hinweiseMail = new ArrayList<>();
        int scoreMail = service.calculateScore(document, "Mail", hinweiseMail);

        assertEquals(100, scoreWissenschaft);
        assertTrue(hinweiseWissenschaft.isEmpty());

        assertTrue(scoreMail < 100);
        assertFalse(hinweiseMail.isEmpty());
        assertTrue(hinweiseMail.stream().anyMatch(h -> h.contains("Satzlänge")));
    }

    private static class FakeReferenzRepository implements ReferenzRepository {

        @Override
        public double getIdealeSatzlaenge(String stiltyp) {
            return 18.0;
        }

        @Override
        public double getMaxFuellwortAnteil(String stiltyp) {
            return 0.08;
        }

        @Override
        public double getMinTypeTokenRatio(String stiltyp) {
            return 0.45;
        }

        @Override
        public double getTolerance(String stiltyp) {
            return 5.0;
        }
    }
}
