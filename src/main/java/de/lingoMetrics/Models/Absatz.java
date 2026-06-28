package de.lingoMetrics.Models;

import java.util.ArrayList;
import java.util.List;

//Autor: Simon Ortlieb, Tarik Marton
public class Absatz {
    private List<Satz> saetze = new ArrayList<>();

    public List<Satz> getSatz() {
        return saetze;
    }

    public void setSaetze(List<Satz> saetze) {
        this.saetze = saetze;
    }

    public void addSatz(Satz satz) {
        this.saetze.add(satz);
    }
}