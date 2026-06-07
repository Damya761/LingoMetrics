package de.lingoMetrics.Service;

import de.lingoMetrics.Models.Document;
import de.lingoMetrics.Repository.ReferenzRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class AuswertungsService {

    private final ReferenzRepository referenzRepository;

    public AuswertungsService(ReferenzRepository referenzRepository) {
        this.referenzRepository = referenzRepository;
    }

    public int calculateScore(Document doc, List<String> hinweise) {
        Objects.requireNonNull(doc, "Document must not be null.");
        Objects.requireNonNull(hinweise, "Hinweise list must not be null.");

        Map<String, Double> metriken = aggregateMetrics(doc);

        return calculateScore(metriken, hinweise);
    }

    private Map<String, Double> aggregateMetrics(Document doc) {
        Map<String, Double> metriken = new HashMap<>();

        metriken.put("Wortlängenverteilung", doc.getWortlaengenverteilung());
        metriken.put("Mittlere Satzlänge", doc.getMittlereSatzlaenge());
        metriken.put("Satzlängenunterschied", doc.getSatzlaengenunterschied());
        metriken.put("Funktionswörteranteil", doc.getFunktionswoerterAnteil());
        metriken.put("Füllwortanteil", doc.getFuellwoerterAnteil());
        metriken.put("Type-Token-Ratio", doc.getTypeTokenRatio());
        metriken.put("Lesbarkeitsindex", doc.getLesbarkeitsindex());
        metriken.put("Mittleres Sentiment", doc.getMittleresSentiment());
        metriken.put("Hapax Legomena", (double) doc.getHapaxLegomena());
        metriken.put("Adjektiv-Verb-Quotient", doc.getAdjektivVerbQuotient());
        metriken.put("Mittlere Konkretheit", doc.getMittlereKonkretheit());

        return metriken;
    }

    private int calculateScore(Map<String, Double> metriken, List<String> hinweise) {
        int score = 100;

        score -= evaluateSentenceLength(metriken, hinweise);
        score -= evaluateFillerWords(metriken, hinweise);
        score -= evaluateVocabulary(metriken, hinweise);
        score -= evaluateReadability(metriken, hinweise);
        score -= evaluateSentenceVariation(metriken, hinweise);
        score -= evaluateFunctionWords(metriken, hinweise);
        score -= evaluateSentiment(metriken, hinweise);
        score -= evaluateConcreteness(metriken, hinweise);
        score -= evaluateAdjectiveVerbRatio(metriken, hinweise);
        score -= evaluateHapaxLegomena(metriken, hinweise);

        return Math.max(score, 0);
    }

    private int evaluateSentenceLength(Map<String, Double> metriken, List<String> hinweise) {
        double mittlereSatzlaenge = metriken.get("Mittlere Satzlänge");
        double idealeSatzlaenge = referenzRepository.getIdealeSatzlaenge();
        double tolerance = referenzRepository.getTolerance();

        if (Math.abs(mittlereSatzlaenge - idealeSatzlaenge) > tolerance) {
            hinweise.add("Die durchschnittliche Satzlänge weicht deutlich vom Referenzwert ab.");
            return 15;
        }

        return 0;
    }

    private int evaluateFillerWords(Map<String, Double> metriken, List<String> hinweise) {
        if (metriken.get("Füllwortanteil") > referenzRepository.getMaxFuellwortAnteil()) {
            hinweise.add("Der Text enthält vergleichsweise viele Füllwörter.");
            return 15;
        }

        return 0;
    }

    private int evaluateVocabulary(Map<String, Double> metriken, List<String> hinweise) {
        if (metriken.get("Type-Token-Ratio") < referenzRepository.getMinTypeTokenRatio()) {
            hinweise.add("Die Wortvielfalt ist eher niedrig.");
            return 10;
        }

        return 0;
    }

    private int evaluateReadability(Map<String, Double> metriken, List<String> hinweise) {
        if (metriken.get("Lesbarkeitsindex") > 0 && metriken.get("Lesbarkeitsindex") < 40) {
            hinweise.add("Der Lesbarkeitsindex ist niedrig. Der Text könnte schwer verständlich sein.");
            return 10;
        }

        return 0;
    }

    private int evaluateSentenceVariation(Map<String, Double> metriken, List<String> hinweise) {
        if (metriken.get("Satzlängenunterschied") > 0 && metriken.get("Satzlängenunterschied") < 3) {
            hinweise.add("Die Satzlängen variieren nur wenig.");
            return 5;
        }

        return 0;
    }

    private int evaluateFunctionWords(Map<String, Double> metriken, List<String> hinweise) {
        if (metriken.get("Funktionswörteranteil") > 0.60) {
            hinweise.add("Der Anteil an Funktionswörtern ist relativ hoch.");
            return 5;
        }

        return 0;
    }

    private int evaluateSentiment(Map<String, Double> metriken, List<String> hinweise) {
        if (Math.abs(metriken.get("Mittleres Sentiment")) > 0.5) {
            hinweise.add("Der Text wirkt sprachlich stark wertend.");
            return 5;
        }

        return 0;
    }

    private int evaluateConcreteness(Map<String, Double> metriken, List<String> hinweise) {
        if (metriken.get("Mittlere Konkretheit") > 0 && metriken.get("Mittlere Konkretheit") < 0.4) {
            hinweise.add("Der Text verwendet viele abstrakte Begriffe.");
            return 5;
        }

        return 0;
    }

    private int evaluateAdjectiveVerbRatio(Map<String, Double> metriken, List<String> hinweise) {
        if (metriken.get("Adjektiv-Verb-Quotient") > 1.5) {
            hinweise.add("Der Text enthält verhältnismäßig viele beschreibende Formulierungen.");
            return 5;
        }

        return 0;
    }

    private int evaluateHapaxLegomena(Map<String, Double> metriken, List<String> hinweise) {
        if (metriken.get("Hapax Legomena") > 0 && metriken.get("Hapax Legomena") < 5) {
            hinweise.add("Die Wortvielfalt könnte erhöht werden.");
            return 3;
        }

        return 0;
    }

    public String determineRating(int score) {
        if (score >= 90) return "Sehr gut";
        if (score >= 75) return "Gut";
        if (score >= 60) return "Befriedigend";
        if (score >= 40) return "Verbesserungswürdig";
        return "Kritisch";
    }
}
