package de.lingoMetrics.Service;

import de.lingoMetrics.Models.Document;
import de.lingoMetrics.repository.ReferenzRepository;
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

        ServiceManager.AnalysisResult result = serviceManager.analyse(request);

        assertTrue(result.isComparison());
        assertTrue(result.hasAuswertung());
        assertEquals(60, result.getScore());
        assertEquals("Befriedigend", result.getGesamtBewertung());
        assertEquals(3, result.getHinweise().size());
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

        ServiceManager.AnalysisResult result = serviceManager.analyse(request);

        assertFalse(result.isComparison());
        assertFalse(result.hasAuswertung());
        assertNull(result.getScore());
        assertNull(result.getGesamtBewertung());
        assertTrue(result.getHinweise().isEmpty());
    }

    private static class FakeReferenzRepository implements ReferenzRepository {

        @Override
        public double getIdealeSatzlaenge() {
            return 18.0;
        }

        @Override
        public double getMaxFuellwortAnteil() {
            return 0.08;
        }

        @Override
        public double getMinTypeTokenRatio() {
            return 0.45;
        }

        @Override
        public double getTolerance() {
            return 5.0;
        }
    }
}
