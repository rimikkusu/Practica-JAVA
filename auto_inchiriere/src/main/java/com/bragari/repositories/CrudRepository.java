package com.bragari.repositories;

import java.util.List;

public interface CrudRepository<T> {
    void adauga(T obiect);

    List<T> obtineToate();

    T cautaDupaId(int id);

    void actualizeaza(T obiect);

    void sterge(int id);
}