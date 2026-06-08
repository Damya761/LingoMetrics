package de.lingoMetrics.Repository;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class JsonReferenzRepository implements ReferenzRepository {

    private final Map<String, ReferenceConfig> configs = new HashMap<>();
    private ReferenceConfig defaultConfig;

    public JsonReferenzRepository() {
        load();
    }

    private void load() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            InputStream is = getClass().getResourceAsStream("/data/referenzwerte.json");
            JsonNode root = mapper.readTree(is);

            root.fields().forEachRemaining(entry -> {
                String key = entry.getKey();
                JsonNode node = entry.getValue();
                ReferenceConfig config = new ReferenceConfig(
                        node.get("idealeSatzlaenge").asDouble(),
                        node.get("maxFuellwortAnteil").asDouble(),
                        node.get("minTypeTokenRatio").asDouble(),
                        node.get("tolerance").asDouble()
                );
                configs.put(key, config);
            });

            defaultConfig = configs.get("default");
            if (defaultConfig == null) {
                defaultConfig = new ReferenceConfig(18.0, 0.08, 0.45, 5.0);
            }
        } catch (Exception e) {
            throw new RuntimeException("Referenzwerte konnten nicht geladen werden.", e);
        }
    }

    private ReferenceConfig getConfig(String stiltyp) {
        if (stiltyp == null) {
            return defaultConfig;
        }
        return configs.getOrDefault(stiltyp, defaultConfig);
    }

    @Override
    public double getIdealeSatzlaenge(String stiltyp) {
        return getConfig(stiltyp).idealeSatzlaenge;
    }

    @Override
    public double getMaxFuellwortAnteil(String stiltyp) {
        return getConfig(stiltyp).maxFuellwortAnteil;
    }

    @Override
    public double getMinTypeTokenRatio(String stiltyp) {
        return getConfig(stiltyp).minTypeTokenRatio;
    }

    @Override
    public double getTolerance(String stiltyp) {
        return getConfig(stiltyp).tolerance;
    }

    private static class ReferenceConfig {
        final double idealeSatzlaenge;
        final double maxFuellwortAnteil;
        final double minTypeTokenRatio;
        final double tolerance;

        ReferenceConfig(double idealeSatzlaenge, double maxFuellwortAnteil, double minTypeTokenRatio, double tolerance) {
            this.idealeSatzlaenge = idealeSatzlaenge;
            this.maxFuellwortAnteil = maxFuellwortAnteil;
            this.minTypeTokenRatio = minTypeTokenRatio;
            this.tolerance = tolerance;
        }
    }
}
