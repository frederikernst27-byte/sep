# Kalender App — Projektstruktur

## Architektur
Die App folgt einer klassischen 3-Schichten-Architektur:

- **Frontend** — Kalender-View, Formulare, API-Kommunikation
- **Backend** — REST API mit Controller → Service → Repository
- **Datenbank** — PostgreSQL/MySQL mit den Tabellen users, trips, calendar_entries

## Ordnerstruktur

backend/src/
  model/       → Datenklassen (z.B. CalendarEntry)
  repository/  → Datenbankzugriffe
  service/     → Geschäftslogik
  controller/  → REST-Endpunkte

frontend/src/
  views/       → Kalender-Ansicht und andere Seiten
  services/    → API-Kommunikation (api.js)
  models/      → Datenmodelle frontend-seitig
  router/      → Navigation zwischen den Ansichten

## Endpunkte (geplant)

| Methode | Endpunkt              | Beschreibung                  |
|---------|-----------------------|-------------------------------|
| GET     | /entries              | Alle Einträge des Nutzers     |
| POST    | /entries              | Neuen Eintrag erstellen       |
| PUT     | /entries/{id}         | Eintrag bearbeiten            |
| DELETE  | /entries/{id}         | Eintrag löschen               |
| GET     | /trips                | Alle Reisen des Nutzers       |
| POST    | /trips                | Neue Reise anlegen            |
| GET     | /trips/{id}/entries   | Einträge einer Reise          |
| POST    | /register             | Nutzer registrieren           |
| POST    | /login                | Nutzer einloggen (→ JWT)      |

## Aufgabenverteilung

| Person        | Zuständigkeit                          |
|---------------|----------------------------------------|
| Lennart       | Grundgerüst, Kalender-View             |
| Frederik      | US1: Kalendereinträge erstellen        |
| Alan          | US2: Einträge bearbeiten & löschen     |
| Melike        | US3: Reise anlegen                     |
| Caren & Lina  | US4: Multi-User Support & Auth         |