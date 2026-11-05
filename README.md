# Smart Campus

Plateforme de gestion intelligente d'un campus universitaire : profils et résultats des étudiants, emplois du temps,
réservation de salles, événements et notifications. Architecture microservices Spring Boot, déployée sur Kubernetes.

Le cahier de développement complet (spécifications, API, sprints, tests) est la référence de l'équipe.

## Structure

```
smart-campus/
├── common/                 # bibliothèque partagée : ErrorResponse, JwtService, EventEnvelope, Topics
├── services/
│   ├── api-gateway/        # port 8080  (Sprint 1 : routage auth, JWT, CORS, santé)
│   ├── auth-service/       # port 8081  (à créer, M2)
│   ├── student-service/    # port 8082  (à créer, M3)
│   ├── teacher-service/    # port 8083  (à créer, M4)
│   ├── room-service/       # port 8084  (à créer, M5)
│   ├── event-service/      # port 8085  (à créer, M6)
│   └── notification-service/ # port 8086 (à créer, M7)
├── frontend/               # React + TypeScript (M4)
├── deploy/                 # docker-compose, k8s, helm (M5)
├── monitoring/             # Prometheus, Grafana
├── tests/                  # e2e, performance, contrats
└── docs/                   # ADR, journaux de bord, charte, backlog
```

## Prérequis

- Java 21, Maven 3.9+
- Docker (pour les tests d'intégration Testcontainers et `docker compose`)

## Démarrer

```bash
# compiler et tester tout le monorepo
mvn -B verify

# compiler seulement un service et ses dépendances
mvn -B -pl services/api-gateway -am verify

# lancer la gateway en local (auth-service attendu sur le port 8081)
mvn -pl services/api-gateway spring-boot:run
```

Variables d'environnement de la gateway (voir `.env.example`) :

| Variable | Défaut | Rôle |
| --- | --- | --- |
| `AUTH_SERVICE_URL` | `http://localhost:8081` | adresse d'auth-service |
| `JWK_SET_URI` | `http://localhost:8081/api/v1/auth/.well-known/jwks.json` | clés publiques pour valider les JWT |
| `JWT_ISSUER` | `smart-campus` | émetteur attendu (identique dans auth-service) |
| `FRONTEND_ORIGIN` | `http://localhost:3000` | seule origine autorisée par CORS |

## Utiliser `common` dans un nouveau service

1. Ajouter la dépendance `ma.smartcampus:common` dans le `pom.xml` du service.
2. Dans un service servlet, le filtre `X-Correlation-Id` et le gestionnaire d'erreurs sont activés
   automatiquement (auto-configuration). Lever `ApiException.conflict("CODE", "message")` pour produire une
   réponse au format commun.
3. Publier un événement Kafka avec `EventEnvelope.of(type, "mon-service", correlationId, payload)` et un nom de
   topic de `Topics`.
4. auth-service signe avec `JwtService.issue(...)` et publie `JwtService.publicJwks()` ; les autres services
   vérifient avec `JwtService.verify(...)` ou la configuration `resource-server` de Spring Security.

## Règles d'équipe

- Branches : `main` (protégée), `develop`, `feature/<service>-<ticket>`, `fix/...`.
- Commits conventionnels : `feat(room): add reservation conflict check`.
- Une PR par ticket, 1 relecture minimum (2 pour la sécurité et les contrats), CI verte, squash merge.
- Definition of Done : voir `.github/PULL_REQUEST_TEMPLATE.md`.
