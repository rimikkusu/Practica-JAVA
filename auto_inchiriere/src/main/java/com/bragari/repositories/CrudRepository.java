package com.bragari.repositories;

// Interfata asta descrie operatiile de baza pentru un repository.
// Daca o clasa o implementeaza, stim ca poate salva, sterge si cauta date.

import java.util.List;

public interface CrudRepository<T> {
    void adauga(T obiect);

    List<T> obtineToate();

    T cautaDupaId(int id);

    void actualizeaza(T obiect);

    void sterge(int id);
}
