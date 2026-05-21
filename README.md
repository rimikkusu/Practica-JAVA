# AutoFleet — Sistem de Management Flota Auto

AutoFleet este o aplicație desktop dezvoltată în JavaFX pentru gestionarea unei flote auto și a procesului de închiriere automobile. Aplicația permite administrarea clienților, automobilelor, închirierilor, plăților, rapoartelor și utilizatorilor.

Proiectul folosește Java 17+, JavaFX pentru interfața grafică și PostgreSQL Neon Cloud pentru stocarea datelor.

## Funcționalități principale

- Autentificare cu username și parolă
- Gestionare utilizatori cu roluri ADMIN și USER
- Administrare clienți
- Administrare automobile
- Administrare categorii auto
- Administrare închirieri
- Calcul automat al totalului pentru o închiriere
- Administrare plăți
- Dashboard cu statistici și grafice
- Rapoarte și export în format TXT sau CSV
- Căutare și filtrare date
- Setări pentru temă, dimensiune text și densitate tabele
- Skeleton loading pentru încărcarea datelor
- Interfață modernă cu teme Light, Dark și Amber Noir

## Tehnologii utilizate

- Java 17+
- JavaFX
- Maven
- PostgreSQL Neon Cloud
- JDBC
- HikariCP
- CSS pentru stilizarea interfeței
- PBKDF2 pentru securizarea parolelor

## Structura proiectului

```text
com.bragari
├── database
│   └── DatabaseManager.java
├── models
│   ├── BaseEntity.java
│   ├── Client.java
│   ├── Automobil.java
│   ├── CategorieAuto.java
│   ├── Inchiriere.java
│   ├── Plata.java
│   └── Utilizator.java
├── repositories
│   ├── CrudRepository.java
│   ├── ClientRepository.java
│   ├── AutomobilRepository.java
│   ├── CategorieAutoRepository.java
│   ├── InchiriereRepository.java
│   ├── PlataRepository.java
│   └── UtilizatorRepository.java
├── services
│   ├── AutoInchiriereService.java
│   ├── AuthService.java
│   └── SettingsService.java
├── views
│   ├── DashboardView.java
│   ├── ClientiView.java
│   ├── AutomobileView.java
│   ├── InchirieriView.java
│   ├── PlatiView.java
│   ├── RapoarteView.java
│   ├── UtilizatoriView.java
│   └── SetariView.java
├── util
│   ├── DialogHelper.java
│   ├── FormValidator.java
│   ├── PasswordUtil.java
│   ├── CsvExporter.java
│   └── SkeletonFactory.java
└── MainApp.java
