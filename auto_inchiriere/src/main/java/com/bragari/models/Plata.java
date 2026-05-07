package com.bragari.models;

import java.time.LocalDate;

public class Plata extends BaseEntity {
    private Inchiriere inchiriere;
    private double suma;
    private String metodaPlata;
    private LocalDate dataPlata;

    public Plata() {
    }

    public Plata(int id, Inchiriere inchiriere, double suma, String metodaPlata, LocalDate dataPlata) {
        super(id);
        this.inchiriere = inchiriere;
        this.suma = suma;
        this.metodaPlata = metodaPlata;
        this.dataPlata = dataPlata;
    }

    public Inchiriere getInchiriere() {
        return inchiriere;
    }

    public void setInchiriere(Inchiriere inchiriere) {
        this.inchiriere = inchiriere;
    }

    public double getSuma() {
        return suma;
    }

    public void setSuma(double suma) {
        this.suma = suma;
    }

    public String getMetodaPlata() {
        return metodaPlata;
    }

    public void setMetodaPlata(String metodaPlata) {
        this.metodaPlata = metodaPlata;
    }

    public LocalDate getDataPlata() {
        return dataPlata;
    }

    public void setDataPlata(LocalDate dataPlata) {
        this.dataPlata = dataPlata;
    }

    @Override
    public String toString() {
        return getId() + " | Inchiriere ID: " +
                inchiriere.getId() + " | " +
                suma + " lei | " +
                metodaPlata + " | " +
                dataPlata;
    }
}