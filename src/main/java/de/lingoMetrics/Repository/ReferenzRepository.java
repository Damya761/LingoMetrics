package de.lingoMetrics.Repository;

public interface ReferenzRepository {
    double getIdealeSatzlaenge(String stiltyp);
    double getMaxFuellwortAnteil(String stiltyp);
    double getMinTypeTokenRatio(String stiltyp);
    double getTolerance(String stiltyp);
}
