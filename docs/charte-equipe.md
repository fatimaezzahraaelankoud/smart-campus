# Charte d'équipe - Smart Campus

*Brouillon rédigé par M1 (chef de projet, Sprint 1). À valider ensemble à la planification du Sprint 1, puis à signer
dans le dépôt par une PR approuvée par les 7 membres.*

## 1. Objectif et définition du succès

Livrer en 8 semaines une plateforme où un étudiant se connecte, voit son emploi du temps, réserve une salle,
s'inscrit à un événement et reçoit une notification. Succès = parcours A, B et C fonctionnels sur le cluster,
CI/CD verte, rapport remis, soutenance maîtrisée par chaque membre.

## 2. Équipe et propriétés

| Membre | Nom | GitHub | Service possédé | Relecteur désigné (suppléant) |
| --- | --- | --- | --- | --- |
| M1 | | | api-gateway + common | M2 |
| M2 | | | auth-service | M3 |
| M3 | | | student-service | M4 |
| M4 | | | teacher-service + front | M5 |
| M5 | | | room-service + DevOps | M6 |
| M6 | | | event-service | M7 |
| M7 | | | notification-service | M1 |

Le propriétaire est responsable du code, des tests et de l'API de son service. Le suppléant sait le démarrer, le
déboguer et le présenter en soutenance.

## 3. Rythme

- Sprints de 2 semaines : planification le premier lundi (1 h), revue et démo le dernier vendredi (1 h),
  rétrospective juste après (45 min).
- Daily de 15 minutes à heure fixe : hier, aujourd'hui, blocages. Les discussions longues se règlent après.
- Canal de discussion unique et ticket GitHub pour toute décision qui touche un autre service.

## 4. Règles de travail

- Une PR par ticket, relue dans les 24 h ouvrées. Deux relecteurs pour la sécurité et les contrats d'API ou Kafka.
- Un contrat (OpenAPI ou schéma d'événement) est publié et relu avant d'être implémenté par un consommateur.
- La branche `develop` reste toujours verte. Qui la casse la répare en priorité.
- Aucun secret dans Git. Un secret commité est considéré comme compromis et renouvelé.
- Quand on est bloqué plus d'une demi-journée, on le dit au daily : demander de l'aide n'est pas un échec.

## 5. Décisions

- Décision technique : l'architecte du sprint propose, le propriétaire du service concerné décide, l'ADR trace.
- Désaccord non résolu en 24 h : vote à la majorité, M1 tranche en cas d'égalité pendant son sprint de chef de projet.
- Changement de périmètre : discuté avec l'encadrant, consigné dans le backlog.

## 6. Indisponibilités

Prévenir au plus tôt (canal d'équipe) dès qu'une absence dépasse une journée. M1 redistribue les tickets à la
planification ou en cours de sprint. Les tâches « option » sautent en premier (ordre de coupe de la section 10.6 du
cahier de développement).

## 7. Évaluation individuelle

Chacun tient son journal de bord (`docs/journal/<membre>.md`) : décisions, options écartées, bilan de sprint.
Le rapport détaille le rôle tenu par chaque membre à chaque sprint.

## 8. Signatures (PR approuvées)

M1 [ ] M2 [ ] M3 [ ] M4 [ ] M5 [ ] M6 [ ] M7 [ ]
