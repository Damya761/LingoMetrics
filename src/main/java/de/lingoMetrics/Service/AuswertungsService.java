package de.lingoMetrics.Service;

import de.lingoMetrics.Models.Document;
import de.lingoMetrics.repository.ReferenzRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AuswertungsService {

    private final ReferenzRepository referenzRepository;

    public AuswertungsService(ReferenzRepository referenzRepository) {
        this.referenzRepository = referenzRepository;
    }

    public AnalysisResult auswertung(Document doc) {
        Map<String, Double> metriken = aggregateMetrics(doc);
        int score = calculateScore(metriken);
        List<String> hinweise = generateHints(metriken);

        return new AnalysisResult(metriken, score, hinweise);
    }

    private Map<String, Double> aggregateMetrics(Document doc) {
        Map<String, Double> metriken = new HashMap<>();

        metriken.put("Mittlere Satzlänge", doc.getMittlereSatzlaenge());
        metriken.put("Füllwortanteil", doc.getFuellwoerterAnteil());
        metriken.put("Type-Token-Ratio", doc.getTypeTokenRatio());

        return metriken;
    }

    private int calculateScore(Map<String, Double> metriken) {
        int score = 100;

        double mittlereSatzlaenge = metriken.get("Mittlere Satzlänge");
        double idealeSatzlaenge = referenzRepository.getIdealeSatzlaenge();
        double tolerance = referenzRepository.getTolerance();

        if (Math.abs(mittlereSatzlaenge - idealeSatzlaenge) > tolerance) {
            score -= 15;
        }

        if (metriken.get("Füllwortanteil") > referenzRepository.getMaxFuellwortAnteil()) {
            score -= 15;
        }

        if (metriken.get("Type-Token-Ratio") < referenzRepository.getMinTypeTokenRatio()) {
            score -= 10;
        }

        return Math.max(score, 0);
    }

    private List<String> generateHints(Map<String, Double> metriken) {
        List<String> hinweise = new ArrayList<>();

        double mittlereSatzlaenge = metriken.get("Mittlere Satzlänge");
        double idealeSatzlaenge = referenzRepository.getIdealeSatzlaenge();
        double tolerance = referenzRepository.getTolerance();

        if (Math.abs(mittlereSatzlaenge - idealeSatzlaenge) > tolerance) {
            hinweise.add("Die durchschnittliche Satzlänge weicht deutlich vom Referenzwert ab.");
        }

        if (metriken.get("Füllwortanteil") > referenzRepository.getMaxFuellwortAnteil()) {
            hinweise.add("Der Text enthält vergleichsweise viele Füllwörter.");
        }

        if (metriken.get("Type-Token-Ratio") < referenzRepository.getMinTypeTokenRatio()) {
            hinweise.add("Die Wortvielfalt ist eher niedrig.");
        }

        return hinweise;
    }
}