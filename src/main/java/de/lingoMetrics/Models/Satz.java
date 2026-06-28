package de.lingoMetrics.Models;

import java.util.List;

//Autor: Simon Ortlieb, Tarik Marton
public class Satz {
    private List<Wort> woerter; // Im Diagramm als "+ Wörter: Wort[]" deklariert
    private int laenge;
    private int woerterAnzahl;

    public List<Wort> getWoerter() { return woerter; }
    public void setWoerter(List<Wort> woerter) { this.woerter = woerter; }
    public void addWort(Wort wort){this.woerter.add(wort); }

    public int getLaenge() { return laenge; }
    public void setLaenge(int laenge) { this.laenge = laenge; }

    public int getWoerterAnzahl() { return woerterAnzahl; }
    public void setWoerterAnzahl(int woerterAnzahl) { this.woerterAnzahl = woerterAnzahl; }
}