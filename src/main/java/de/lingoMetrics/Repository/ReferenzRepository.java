package de.lingoMetrics.repository;

public interface ReferenzRepository {
    double getIdealeSatzlaenge();
    double getMaxFuellwortAnteil();
    double getMinTypeTokenRatio();
    double getTolerance();
}
