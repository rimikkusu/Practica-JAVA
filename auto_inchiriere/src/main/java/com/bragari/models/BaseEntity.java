package com.bragari.models;

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
