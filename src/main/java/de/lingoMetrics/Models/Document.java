package de.lingoMetrics.Models;

import de.lingoMetrics.Enums.InterpunktiosProfil; // Falls das ein Enum/eine Klasse ist

import java.util.List;

public class Document {
    // Strukturelle Beziehungen (Listen)
    private List<Absatz> absaetze;
    private List<Wort> woerter; // Flache Liste aller Wörter im Dokument
    private List<Satz> saetze;   // Flache Liste aller Sätze im Dokument

    // Berechnete Metriken
    private InterpunktiosProfil interpunktion;
    private double wortlaengenverteilung;
    private double mittlereSatzlaenge;
    private double satzlaengenunterschied;
    private double funktionswoerterAnteil;
    private double fuellwoerterAnteil;
    private double typeTokenRatio;
    private double lesbarkeitsindex;
    private double mittleresSentiment;
    private int hapaxLegomena;
    private double adjektivVerbQuotient;
    private double mittlereKonkretheit;

    // Getter und Setter
    public List<Absatz> getAbsaetze() { return absaetze; }
    public void setAbsaetze(List<Absatz> absaetze) { this.absaetze = absaetze; }
    public void addAbsatz(Absatz absatz) { this.absaetze.add(absatz); }

    public List<Wort> getWoerter() { return woerter; }
    public void setWoerter(List<Wort> woerter) { this.woerter = woerter; }
    public void addWort(Wort wort) { this.woerter.add(wort); }

    public List<Satz> getSaetze() { return saetze; }
    public void setSaetze(List<Satz> saetze) { this.saetze = saetze; }
    public void addSatz(Satz satz) { this.saetze.add(satz); }

    public InterpunktiosProfil getInterpunktion() { return interpunktion; }
    public void setInterpunktion(InterpunktiosProfil interpunktion) { this.interpunktion = interpunktion; }

    public double getWortlaengenverteilung() { return wortlaengenverteilung; }
    public void setWortlaengenverteilung(double wortlaengenverteilung) { this.wortlaengenverteilung = wortlaengenverteilung; }

    public double getMittlereSatzlaenge() { return mittlereSatzlaenge; }
    public void setMittlereSatzlaenge(double mittlereSatzlaenge) { this.mittlereSatzlaenge = mittlereSatzlaenge; }

    public double getSatzlaengenunterschied() { return satzlaengenunterschied; }
    public void setSatzlaengenunterschied(double satzlaengenunterschied) { this.satzlaengenunterschied = satzlaengenunterschied; }

    public double getFunktionswoerterAnteil() { return funktionswoerterAnteil; }
    public void setFunktionswoerterAnteil(double funktionswoerterAnteil) { this.funktionswoerterAnteil = funktionswoerterAnteil; }

    public double getFuellwoerterAnteil() { return fuellwoerterAnteil; }
    public void setFuellwoerterAnteil(double fuellwoerterAnteil) { this.fuellwoerterAnteil = fuellwoerterAnteil; }

    public double getTypeTokenRatio() { return typeTokenRatio; }
    public void setTypeTokenRatio(double typeTokenRatio) { this.typeTokenRatio = typeTokenRatio; }

    public double getLesbarkeitsindex() { return lesbarkeitsindex; }
    public void setLesbarkeitsindex(double lesbarkeitsindex) { this.lesbarkeitsindex = lesbarkeitsindex; }

    public double getMittleresSentiment() { return mittleresSentiment; }
    public void setMittleresSentiment(double mittleresSentiment) { this.mittleresSentiment = mittleresSentiment; }

    public int getHapaxLegomena() { return hapaxLegomena; }
    public void setHapaxLegomena(int hapaxLegomena) { this.hapaxLegomena = hapaxLegomena; }

    public double getAdjektivVerbQuotient() { return adjektivVerbQuotient; }
    public void setAdjektivVerbQuotient(double adjektivVerbQuotient) { this.adjektivVerbQuotient = adjektivVerbQuotient; }

    public double getMittlereKonkretheit() { return this.mittlereKonkretheit; }
    public void setMittlereKonkretheit(double mittlereKonkretheit) { this.mittlereKonkretheit = mittlereKonkretheit; }
}