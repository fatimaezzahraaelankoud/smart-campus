# Sprint 1 : kit de cérémonies (M1, chef de projet)

## Planification (lundi S1, 1 h)

1. 10 min : objectif du sprint et critères de fin (section 10.2 du cahier de développement).
2. 25 min : chaque membre présente ses tickets, vérifie l'estimation et déclare ses dépendances.
3. 15 min : dépendances croisées (voir ci-dessous) et ordre de travail de la première semaine.
4. 10 min : charge par personne. Cible : environ 18 points par personne sur 20 de capacité.

Dépendances à régler dès le lundi :

| Producteur | Livrable | Consommateur | À publier avant |
| --- | --- | --- | --- |
| M2 | Contrat OpenAPI auth-service (T1.08) | M1 (gateway), M4 (écrans login) | jour 2 |
| M2 | Schémas `campus.user.*` (T1.08) | M3, M4, M7 (consommateurs) | jour 2 |
| M1 | Bibliothèque `common` (T1.04) | tous les services | jour 3 |
| M3 | Archétype de service (T1.13) | M4, M5, M6, M7 (squelettes) | jour 3 |
| M5 | docker-compose (T1.23) | tous | jour 5 |
| M2 | JWKS opérationnel (T1.10) | M1 (gateway en conditions réelles) | semaine 2 |

## Daily (15 min)

Tour de table : fait hier, prévu aujourd'hui, blocage. M1 note les blocages et les traite juste après, pas pendant.
Trois questions de contrôle pour M1 : un ticket est-il resté « en cours » plus de 2 jours ? une PR attend-elle une
relecture depuis plus de 24 h ? une dépendance de la liste ci-dessus a-t-elle glissé ?

## Suivi (à mi-sprint, vendredi S1)

| Indicateur | Cible à mi-sprint |
| --- | --- |
| Points terminés | environ 40 % |
| Contrats publiés (T1.08, T1.04, T1.13) | 100 % |
| PR en attente de relecture depuis plus de 24 h | 0 |
| Risque rouge à escalader | décider quoi couper (ordre de coupe, section 10.6) |

## Revue et démonstration (vendredi S2, 1 h)

Script de démonstration du Sprint 1 :

1. `docker compose up` : toute la plateforme démarre.
2. Connexion de l'admin depuis l'interface.
3. Création d'un compte étudiant ; le profil `INCOMPLETE` apparaît dans student-service.
4. L'événement `campus.user.created` est visible dans Kafka UI ; l'e-mail de bienvenue est visible dans MailHog.
5. Santé et Swagger des 7 services ; pipeline CI vert sur `develop`.

Critères de fin de sprint : voir 10.2. Un critère non atteint est expliqué et replanifié, pas masqué.

## Rétrospective (45 min)

Format : ce qui a bien marché, ce qui a coincé, une action concrète pour le sprint suivant, avec un responsable.
Actions consignées dans `docs/retro/sprint-1.md`.

## Registre des risques initial

| Risque | Probabilité | Impact | Parade | Responsable |
| --- | --- | --- | --- | --- |
| Charge trop lourde pour 8 semaines | Élevée | Élevé | Priorités MoSCoW, ordre de coupe, revue de capacité à chaque planification | M1 |
| Dépendances bloquantes entre membres | Élevée | Moyen | Contrats publiés en premier, mocks MSW et WireMock | M1 |
| Machines trop justes pour Kafka et Kubernetes en local | Moyenne | Moyen | Un seul broker, limites mémoire basses, minikube 8 Go, cluster distant en secours | M5 |
| Membre indisponible | Moyenne | Élevé | Relectures croisées, suppléants désignés, journaux de bord | M1 |
