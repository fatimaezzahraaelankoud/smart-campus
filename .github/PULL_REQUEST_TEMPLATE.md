## Ticket
Closes #<numéro>  ·  ID du backlog : T<sprint>.<nn>  ·  Service : <nom>

## Ce qui change
<!-- 2 à 4 lignes : quoi et pourquoi -->

## Comment le tester
<!-- commandes ou étapes, jeu de données utilisé -->

## Definition of Done
- [ ] Code relu, CI verte
- [ ] Tests unitaires écrits, couverture des nouvelles lignes ≥ 80 %
- [ ] Au moins un test d'intégration par endpoint ajouté
- [ ] OpenAPI à jour (vérifiée dans Swagger UI)
- [ ] Pas de secret dans le code, entrées validées
- [ ] Logs et métriques ajoutés pour les nouveaux flux
- [ ] Journal de bord mis à jour (décision technique prise, alternatives écartées)

## Sécurité et contrats (2 relecteurs si coché)
- [ ] Touche l'authentification, les droits ou des données personnelles
- [ ] Modifie un contrat d'API ou un schéma d'événement Kafka (rétrocompatible : champs optionnels uniquement)
