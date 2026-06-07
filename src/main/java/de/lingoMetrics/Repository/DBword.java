package de.lingoMetrics.repository;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class DBword {
    final private String wort;
    final private Double value;

    @JsonCreator
    public DBword(@JsonProperty("wort") String wort,
                  @JsonProperty("value") Double value) {
        this.wort = wort;
        this.value = (value != null) ? value : 0.0;
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public DBword(String wort) {
        this.wort = wort;
        this.value = 0.0;
    }

    public String getWort() {
        return wort;
    }

    public Double getValue() {
        return value;
    }
}
