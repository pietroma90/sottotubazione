# Sottotubazione Automatica — GSFNS-1845

Procedura Java/Spring Boot per l'assegnazione automatica delle sotto-tubazioni
alle tratte interrate, come da requisito Daphne.

## Architettura

```
┌─────────────────────────────────────────────────┐
│           SottotubazioneProcedureController      │
│           POST /api/v1/sottotubazione/assegna    │
└──────────────────────┬──────────────────────────┘
                       │
┌──────────────────────▼──────────────────────────┐
│         SottotubazioneProcedureService           │
│         @Transactional — orchestratore           │
└──────────────────────┬──────────────────────────┘
                       │ per ogni TrattaInterrata
┌──────────────────────▼──────────────────────────┐
│       SottotubazioneProcedureFactory             │
│       seleziona la Strategy corretta             │
└──────────┬──────────────────────┬───────────────┘
           │                      │
┌──────────▼──────────┐  ┌────────▼───────────────┐
│  NuovoScavoStrategy │  │ EsistenteInterratoStrat.│
│  (tratte di scavo)  │  │ (tratte esistenti)      │
└──────────┬──────────┘  └────────┬───────────────┘
           └──────────┬───────────┘
                      │ Template Method comune
┌─────────────────────▼──────────────────────────┐
│     AbstractSottotubazioneProcedure             │
│     Chain of Responsibility (ConfigRule)        │
└────────────────────────────────────────────────┘
```

## Pattern utilizzati

| Pattern              | Dove                                          |
|----------------------|-----------------------------------------------|
| Strategy             | NuovoScavoStrategy / EsistenteInterratoStrategy |
| Template Method      | AbstractSottotubazioneProcedure               |
| Chain of Resp.       | RuleChainBuilder + ConfigRuleHandler          |
| Factory              | SottotubazioneProcedureFactory                |
| Builder              | ProcedureOutput, AssignmentResult, DTO        |
| Context Object       | AssignmentContext                             |

## Avvio rapido

```bash
# 1. configura application.properties con i dati del tuo DB
# 2. build
mvn clean package -DskipTests
# 3. run
java -jar target/sottotubazione-1.0.0-SNAPSHOT.jar
```

## Endpoint

| Metodo | Path                                        | Descrizione                        |
|--------|---------------------------------------------|------------------------------------|
| POST   | /api/v1/sottotubazione/assegna/{projectId}  | Lancia la procedura                |
| GET    | /api/v1/sottotubazione/log/{projectId}      | Legge i log prodotti               |

## Struttura package

```
com.daphne.sottotubazione
├── domain/           # entità JPA + oggetti risultato
│   └── enums/
├── repository/       # Spring Data JPA repositories
├── procedure/        # Strategy + Template Method
│   └── chain/        # Chain of Responsibility
├── service/          # orchestratore + log service
├── dto/              # oggetti REST request/response
└── controller/       # endpoint REST
```
