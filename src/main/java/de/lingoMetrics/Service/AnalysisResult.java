package de.lingoMetrics.Service;

import java.util.List;
import java.util.Map;

public class AnalysisResult {

    private final Map<String, Double> metriken;
    private final int score;
    private final String gesamtBewertung;
    private final List<String> hinweise;

    public AnalysisResult(Map<String, Double> metriken, int score, String gesamtBewertung, List<String> hinweise) {
        this.metriken = metriken;
        this.score = score;
        this.gesamtBewertung = gesamtBewertung;
        this.hinweise = hinweise;
    }

    public Map<String, Double> getMetriken() {
        return metriken;
    }

    public int getScore() {
        return score;
    }

    public String getGesamtBewertung() {
        return gesamtBewertung;
    }

    public List<String> getHinweise() {
        return hinweise;
    }
}