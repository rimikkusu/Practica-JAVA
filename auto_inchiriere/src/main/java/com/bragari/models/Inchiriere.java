package com.bragari.models;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class Inchiriere extends BaseEntity {
    private Client client;
    private Automobil automobil;
    private LocalDate dataInceput;
    private LocalDate dataSfarsit;
    private StatusInchiriere status;

    public Inchiriere() {
    }

    public Inchiriere(int id, Client client, Automobil automobil,
                      LocalDate dataInceput, LocalDate dataSfarsit,
                      StatusInchiriere status) {
        super(id);
        this.client = client;
        this.automobil = automobil;
        this.dataInceput = dataInceput;
        this.dataSfarsit = dataSfarsit;
        this.status = status;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public Automobil getAutomobil() {
        return automobil;
    }

    public void setAutomobil(Automobil automobil) {
        this.automobil = automobil;
    }

    public LocalDate getDataInceput() {
        return dataInceput;
    }

    public void setDataInceput(LocalDate dataInceput) {
        this.dataInceput = dataInceput;
    }

    public LocalDate getDataSfarsit() {
        return dataSfarsit;
    }

    public void setDataSfarsit(LocalDate dataSfarsit) {
        this.dataSfarsit = dataSfarsit;
    }

    public StatusInchiriere getStatus() {
        return status;
    }

    public void setStatus(StatusInchiriere status) {
        this.status = status;
    }

    public long calculeazaNumarZile() {
        return ChronoUnit.DAYS.between(dataInceput, dataSfarsit);
    }

    public double calculeazaTotal() {
        return calculeazaNumarZile() * automobil.getPretPeZi();
    }

    @Override
    public String toString() {
        return getId() + " | " +
                client.getNume() + " | " +
                automobil.getMarca() + " " + automobil.getModel() + " | " +
                dataInceput + " - " + dataSfarsit + " | " +
                calculeazaTotal() + " lei | " +
                status;
    }
}