package de.lingoMetrics.services;

import java.util.List;
import java.util.Map;

public class AnalysisResult {

    private final Map<String, Double> metriken;
    private final int score;
    private final List<String> hinweise;

    public AnalysisResult(Map<String, Double> metriken, int score, List<String> hinweise) {
        this.metriken = metriken;
        this.score = score;
        this.hinweise = hinweise;
    }

    public Map<String, Double> getMetriken() {
        return metriken;
    }

    public int getScore() {
        return score;
    }

    public List<String> getHinweise() {
        return hinweise;
    }
}