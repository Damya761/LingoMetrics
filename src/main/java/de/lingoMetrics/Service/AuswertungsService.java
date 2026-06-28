package de.lingoMetrics.Service;

import de.lingoMetrics.Models.Document;
import de.lingoMetrics.Repository.ReferenzRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

//Autor: Simon Ortlieb, Tarik Marton, Damya Hennige
public class AuswertungsService {

    private final ReferenzRepository referenzRepository;

    public AuswertungsService(ReferenzRepository referenzRepository) {
        this.referenzRepository = referenzRepository;
    }

    public int calculateScore(Document doc, List<String> hinweise) {
        return calculateScore(doc, null, hinweise);
    }

    public int calculateScore(Document doc, String stiltyp, List<String> hinweise) {
        Objects.requireNonNull(doc, "Document must not be null.");
        Objects.requireNonNull(hinweise, "Hinweise list must not be null.");

        Map<String, Double> metriken = aggregateMetrics(doc);
        int score = 100;

        // Satzlänge (Gewichtung: 15 Punkte)
        score -= checkMetric(metriken, "mittlereSatzlaenge", stiltyp, hinweise, 15,
                "Die Sätze sind für diesen Texttyp ungewöhnlich lang.",
                "Die Sätze sind für diesen Texttyp ungewöhnlich kurz.",
                val -> val > 20.0 ? "Die durchschnittliche Satzlänge ist sehr hoch. Kürzere Sätze verbessern die Lesbarkeit." : null);

        // Füllwörter (Gewichtung: 15 Punkte)
        score -= checkMetric(metriken, "fuellwoerterAnteil", stiltyp, hinweise, 15,
                "Der Anteil an Füllwörtern ist für diesen Stil untypisch hoch.",
                null, // Zu wenig Füllwörter bestrafen wir in der Regel nicht
                val -> val > 0.40 ? "Der Text enthält relativ viele Füllwörter. Ein direkterer Schreibstil wirkt professioneller." : null);

        // Wortvielfalt / TTR (Gewichtung: 10 Punkte)
        score -= checkMetric(metriken, "typeTokenRatio", stiltyp, hinweise, 10,
                null,
                "Die Wortvielfalt (Type-Token-Ratio) ist untypisch niedrig für diesen Stil.",
                val -> val < 0.25 ? "Die Wortvielfalt ist gering. Versuchen Sie, Wortwiederholungen zu reduzieren." : null);

        // Lesbarkeitsindex (Gewichtung: 5 Punkte)
        score -= checkMetric(metriken, "lesbarkeitsindex", stiltyp, hinweise, 5,
                "Der Text ist deutlich schwerer lesbar (hoher LIX) als für diesen Typ üblich.",
                "Der Text ist auffällig simpler geschrieben als für diesen Typ üblich.",
                val -> val > 60.0 ? "Der Text ist extrem schwer verständlich (hoher LIX). Bandwurmsätze aufbrechen!" : null);

        // Satzlängenunterschied (Gewichtung: 5 Punkte)
        score -= checkMetric(metriken, "satzlaengenunterschied", stiltyp, hinweise, 5,
                null,
                "Die Satzlängen variieren untypisch wenig, der Text wirkt für diesen Stil zu monoton.",
                val -> val < 3.0 ? "Die Satzlängen variieren kaum. Mischen Sie kurze und lange Sätze für mehr Dynamik." : null);

        // Funktionswörter (Gewichtung: 5 Punkte)
        score -= checkMetric(metriken, "funktionswoerterAnteil", stiltyp, hinweise, 5,
                "Der Text enthält ungewöhnlich viele grammatikalische Funktionswörter.",
                null,
                val -> val > 0.55 ? "Der Anteil an Funktionswörtern ist sehr hoch. Nutzen Sie stärkere Nomen und Verben." : null);

        // Sentiment (Gewichtung: 10 Punkte)
        score -= checkMetric(metriken, "mittleresSentiment", stiltyp, hinweise, 10,
                "Der Text ist deutlich positiver/wertender als für diesen Stil üblich.",
                "Der Text ist deutlich negativer/kritischer als für diesen Stil üblich.",
                val -> Math.abs(val) > 0.5 ? "Der Text wirkt sprachlich sehr stark emotional/wertend." : null);

        // Konkretheit (Gewichtung: 10 Punkte)
        score -= checkMetric(metriken, "mittlereKonkretheit", stiltyp, hinweise, 10,
                null,
                "Der Text verwendet deutlich mehr abstrakte Begriffe als für diesen Stil üblich.",
                val -> val < 0.35 ? "Der Text ist sehr abstrakt geschrieben. Greifbare Beispiele erhöhen die Verständlichkeit." : null);

        // Adjektiv-Verb-Quotient (Gewichtung: 5 Punkte)
        score -= checkMetric(metriken, "adjektivVerbQuotient", stiltyp, hinweise, 5,
                "Der Text ist extrem stark beschreibend (viele Adjektive) für diesen Stil.",
                null,
                val -> val > 1.2 ? "Es werden verhältnismäßig viele Adjektive genutzt. Verben erzeugen mehr Handlung." : null);

        return Math.max(score, 0);
    }

    private Map<String, Double> aggregateMetrics(Document doc) {
        Map<String, Double> metriken = new HashMap<>();
        // Die Keys matchen ab sofort EXAKT mit der JSON-Struktur der Referenzprofile
        metriken.put("mittlereSatzlaenge", doc.getMittlereSatzlaenge());
        metriken.put("fuellwoerterAnteil", doc.getFuellwoerterAnteil());
        metriken.put("typeTokenRatio", doc.getTypeTokenRatio());
        metriken.put("lesbarkeitsindex", doc.getLesbarkeitsindex());
        metriken.put("satzlaengenunterschied", doc.getSatzlaengenunterschied());
        metriken.put("funktionswoerterAnteil", doc.getFunktionswoerterAnteil());
        metriken.put("mittleresSentiment", doc.getMittleresSentiment());
        metriken.put("mittlereKonkretheit", doc.getMittlereKonkretheit());
        metriken.put("adjektivVerbQuotient", doc.getAdjektivVerbQuotient());
        metriken.put("wortlaengenverteilung", doc.getWortlaengenverteilung());
        return metriken;
    }

    private int checkMetric(Map<String, Double> metriken, String metricKey, String stiltyp,
                            List<String> hinweise, int penalty,
                            String tooHighMsg, String tooLowMsg,
                            FallbackCheck fallbackCheck) {

        double actual = metriken.getOrDefault(metricKey, 0.0);

        if (stiltyp != null && !stiltyp.isEmpty() && referenzRepository != null) {
            Double mean = referenzRepository.getMittelwert(stiltyp, metricKey);
            Double std = referenzRepository.getStandardabweichung(stiltyp, metricKey);

            if (mean != null && std != null) {
                double tolerance = Math.max(std * 1.5, Math.abs(mean * 0.05));

                if (actual > mean + tolerance) {
                    if (tooHighMsg != null) hinweise.add(tooHighMsg);
                    return penalty;
                } else if (actual < mean - tolerance) {
                    if (tooLowMsg != null) hinweise.add(tooLowMsg);
                    return penalty;
                }
                return 0;
            }
        }

        String fallbackMsg = fallbackCheck.evaluate(actual);
        if (fallbackMsg != null) {
            hinweise.add(fallbackMsg);
            return penalty;
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

    @FunctionalInterface
    private interface FallbackCheck {
        String evaluate(double actualValue);
    }
}