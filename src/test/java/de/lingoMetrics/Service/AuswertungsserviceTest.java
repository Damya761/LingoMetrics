package de.lingoMetrics.Service;

import de.lingoMetrics.Service.AnalysisResult;
import de.lingoMetrics.Models.Document;
import de.lingoMetrics.repository.ReferenzRepository;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AuswertungsServiceTest {

    @Test
    void auswertung_gibtScore100UndKeineHinweiseZurueck_wennAlleMetrikenGutSind() {
        AuswertungsService service = new AuswertungsService(new FakeReferenzRepository());

        Document document = new Document();
        document.setMittlereSatzlaenge(18.0);
        document.setFuellwoerterAnteil(0.05);
        document.setTypeTokenRatio(0.60);

        AnalysisResult result = service.auswertung(document);

        assertEquals(100, result.getScore());
        assertTrue(result.getHinweise().isEmpty());

        assertEquals(18.0, result.getMetriken().get("Mittlere Satzlänge"));
        assertEquals(0.05, result.getMetriken().get("Füllwortanteil"));
        assertEquals(0.60, result.getMetriken().get("Type-Token-Ratio"));
    }

    @Test
    void auswertung_ziehtPunkteAbUndErzeugtHinweise_wennAlleMetrikenSchlechtSind() {
        AuswertungsService service = new AuswertungsService(new FakeReferenzRepository());

        Document document = new Document();
        document.setMittlereSatzlaenge(30.0);
        document.setFuellwoerterAnteil(0.12);
        document.setTypeTokenRatio(0.30);

        AnalysisResult result = service.auswertung(document);

        assertEquals(60, result.getScore());
        assertEquals(3, result.getHinweise().size());

        assertTrue(result.getHinweise().contains(
                "Die durchschnittliche Satzlänge weicht deutlich vom Referenzwert ab."
        ));
        assertTrue(result.getHinweise().contains(
                "Der Text enthält vergleichsweise viele Füllwörter."
        ));
        assertTrue(result.getHinweise().contains(
                "Die Wortvielfalt ist eher niedrig."
        ));
    }

    @Test
    void auswertung_ziehtNurFuellwortPunkteAb_wennNurFuellwortanteilZuHochIst() {
        AuswertungsService service = new AuswertungsService(new FakeReferenzRepository());

        Document document = new Document();
        document.setMittlereSatzlaenge(18.0);
        document.setFuellwoerterAnteil(0.10);
        document.setTypeTokenRatio(0.60);

        AnalysisResult result = service.auswertung(document);

        assertEquals(85, result.getScore());
        assertEquals(1, result.getHinweise().size());
        assertTrue(result.getHinweise().contains(
                "Der Text enthält vergleichsweise viele Füllwörter."
        ));
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