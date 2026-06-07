package de.lingoMetrics.repository;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;

public class JsonReferenzRepository implements ReferenzRepository {

    private double idealeSatzlaenge;
    private double maxFuellwortAnteil;
    private double minTypeTokenRatio;
    private double tolerance;

    public JsonReferenzRepository() {
        load();
    }

    private void load() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            InputStream is = getClass().getResourceAsStream("/data/referenzwerte.json");
            JsonNode root = mapper.readTree(is);

            idealeSatzlaenge   = root.get("idealeSatzlaenge").asDouble();
            maxFuellwortAnteil = root.get("maxFuellwortAnteil").asDouble();
            minTypeTokenRatio  = root.get("minTypeTokenRatio").asDouble();
            tolerance          = root.get("tolerance").asDouble();
        } catch (Exception e) {
            throw new RuntimeException("Referenzwerte konnten nicht geladen werden.", e);
        }
    }

    @Override public double getIdealeSatzlaenge()   { return idealeSatzlaenge; }
    @Override public double getMaxFuellwortAnteil() { return maxFuellwortAnteil; }
    @Override public double getMinTypeTokenRatio()  { return minTypeTokenRatio; }
    @Override public double getTolerance()          { return tolerance; }
}
