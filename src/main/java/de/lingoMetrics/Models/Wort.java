package de.lingoMetrics.Models;

import de.lingoMetrics.Enums.WortTyp;

//Autor: Simon Ortlieb, Tarik Marton
public class Wort {
    private String inhalt;
    private int laenge;
    private boolean isSatzzeichen;
    private WortTyp wortart;
    private double sentiment;
    private double konkretheit;
    private boolean nominalStil;
    private boolean verbalStil;
    private boolean isFunktionsWort; // Im Diagramm als "isFunktionsWort: true" (Typ-Fehler im UML, hier boolean)
    private boolean isFuellWort;       // Im Diagramm als "isFuellWort: true" (Typ-Fehler im UML, hier boolean)
    private int vorkommenInText;

    public String getInhalt() { return inhalt; }
    public void setInhalt(String inhalt) { this.inhalt = inhalt; }

    public int getLaenge() { return laenge; }
    public void setLaenge(int laenge) { this.laenge = laenge; }

    public boolean isSatzzeichen() { return isSatzzeichen; }
    public void setSatzzeichen(boolean satzzeichen) { isSatzzeichen = satzzeichen; }

    public WortTyp getWortart() { return wortart; }
    public void setWortart(WortTyp wortart) { this.wortart = wortart; }

    public double getSentiment() { return sentiment; }
    public void setSentiment(double sentiment) { this.sentiment = sentiment; }

    public double getKonkretheit() { return konkretheit; }
    public void setKonkretheit(double konkretheit) { this.konkretheit = konkretheit; }

    public boolean isNominalStil() { return nominalStil; }
    public void setNominalStil(boolean nominalStil) { this.nominalStil = nominalStil; }

    public boolean isVerbalStil() { return verbalStil; }
    public void setVerbalStil(boolean verbalStil) { this.verbalStil = verbalStil; }

    public boolean isFunktionsWort() { return isFunktionsWort; }
    public void setFunktionsWort(boolean funktionsWort) { isFunktionsWort = funktionsWort; }

    public boolean isFuellWort() { return isFuellWort; }
    public void setFuellWort(boolean fuellWort) { isFuellWort = fuellWort; }

    public int getVorkommenInText() { return vorkommenInText; }
    public void setVorkommenInText(int vorkommenInText) { this.vorkommenInText = vorkommenInText; }
}