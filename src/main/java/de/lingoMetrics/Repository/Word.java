package de.lingoMetrics.Repository;

public class Word {
    final private String wort;
    final private Double value;

    public Word(String wort, Double value) {
        this.wort = wort;
        this.value = value;
    }

    public String getWort() {
        return wort;
    }

    public Double getValue() {
        return value;
    }
}
