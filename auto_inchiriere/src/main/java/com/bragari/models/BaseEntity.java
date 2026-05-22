package com.bragari.models;

// BaseEntity este clasa de baza pentru modelele care au id.
// Am pus id-ul aici ca sa nu il repetam separat in fiecare model.

public abstract class BaseEntity {
    private int id;

    public BaseEntity() {
    }
    
    public BaseEntity(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        if(id < 0) {
            throw new IllegalArgumentException("ID-ul nu poate fi negativ.");
        }
        
        this.id = id;
    }
    
}
