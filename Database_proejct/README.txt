# Database Project - Course and Resource Management System

Detta projekt är en Java-applikation för hantering av kurser, undervisningsallokering och tillhörande kostnader. Systemet använder en SQL-databas för datalagring.

## Verktyg
* **Programmeringsspråk:** Java
* **Databas:** SQL
* **Byggverktyg:** Maven

## Projektstruktur och organisation
Projektet är strikt organiserat enligt MVC-mönstret (Model-View-Controller) samt ett integrationslager för att separera affärslogik, databashantering och användargränssnitt:

```text
├── CONTROLLER/          # Innehåller klasser för applikationslogik (t.ex. TeachingManager)
├── INTEGRATION/         # Hanterar databaskopplingar och datalagring (Repository/DAO)
├── model/               # Datamodeller och entiteter (t.ex. CostData)
├── view/                # Användargränssnitt (t.ex. TeachingAllocationCLI)
├── create_database.sql  # SQL-skript för att generera databasstrukturen
├── insert_data.sql      # SQL-skript för testdata och initialisering
└── pom.xml              # Konfigurationsfil för Maven-beroenden och byggsteg
