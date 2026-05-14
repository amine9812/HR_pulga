# RH Plateforme / HR Platform

Application web de gestion RH et administrative pour un projet scolaire. Elle couvre les employes, departements, services, postes, demandes de conge, demandes administratives, documents, notifications et journal d'audit.

HR and administrative management web application for a school project. It manages employees, departments, services, positions, leave requests, administrative requests, documents, notifications, and audit logs.

## Tech Stack

| Couche | Technologie |
| --- | --- |
| Backend | Java 17, Spring Boot 3.2.5 |
| Web MVC | Spring MVC, Thymeleaf |
| UI | Bootstrap 5.3 CDN, Bootstrap Icons, CSS custom |
| Securite | Spring Security, BCrypt |
| Persistence | Spring Data JPA, Hibernate |
| Base de donnees | PostgreSQL |
| Build | Maven |
| Tests | Spring Boot Starter Test |

## Prerequis

- Java 17 ou plus recent avec compilation cible Java 17.
- Maven disponible via la commande `mvn`.
- PostgreSQL accessible en local.
- Une base de donnees nommee `hr_platform`.

## Quick Start

1. Creer la base PostgreSQL:

```sql
CREATE DATABASE hr_platform;
```

2. Verifier la configuration dans `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/hr_platform
spring.datasource.username=postgres
spring.datasource.password=postgres
```

3. Lancer l'application:

```bash
mvn spring-boot:run
```

4. Ouvrir:

```text
http://localhost:8080
```

## Comptes Par Defaut

| Login | Mot de passe | Role |
| --- | --- | --- |
| `admin` | `admin123` | `ADMIN` |
| `rh` | `rh123` | `RESPONSABLE_RH` |
| `manager` | `manager123` | `RESPONSABLE_HIERARCHIQUE` |
| `employe` | `employe123` | `EMPLOYE` |

Les comptes sont crees au premier demarrage uniquement si la table `utilisateurs` est vide.

## Structure Du Projet

```text
hr-platform/
├── pom.xml                                          🟡 [CONFIG] Maven, dependances Spring Boot et Java 17
├── README.md                                        🟢 [EDIT] Documentation du projet
├── src/
│   ├── main/
│   │   ├── java/com/hrplatform/
│   │   │   ├── HrPlatformApplication.java           🔵 [DEFAULT] Point d'entree Spring Boot
│   │   │   ├── config/
│   │   │   │   ├── SecurityConfig.java              🟢 [EDIT] Authentification, roles, login, logout
│   │   │   │   └── DataInitializer.java             🟢 [EDIT] Donnees initiales et comptes par defaut
│   │   │   ├── controller/
│   │   │   │   ├── AuthController.java              🟢 [EDIT] Page de connexion
│   │   │   │   ├── DashboardController.java         🟢 [EDIT] Tableau de bord et indicateurs
│   │   │   │   ├── EmployeController.java           🟢 [EDIT] CRUD employes et photos
│   │   │   │   ├── DepartementController.java       🟢 [EDIT] Departements, services et postes
│   │   │   │   ├── CongeController.java             🟢 [EDIT] Demandes de conge
│   │   │   │   ├── DemandeAdminController.java      🟢 [EDIT] Demandes administratives
│   │   │   │   ├── DocumentController.java          🟢 [EDIT] Upload, liste, download, suppression
│   │   │   │   └── NotificationController.java      🟢 [EDIT] Notifications utilisateur
│   │   │   ├── model/
│   │   │   │   ├── Utilisateur.java                 🟢 [EDIT] Compte applicatif et role
│   │   │   │   ├── Employe.java                     🟢 [EDIT] Dossier employe
│   │   │   │   ├── Departement.java                 🟢 [EDIT] Departement organisationnel
│   │   │   │   ├── Service.java                     🟢 [EDIT] Service rattache a un departement
│   │   │   │   ├── Poste.java                       🟢 [EDIT] Poste occupe par un employe
│   │   │   │   ├── DemandeCongé.java                🟢 [EDIT] Demande de conge
│   │   │   │   ├── DemandeAdministrative.java       🟢 [EDIT] Demande administrative RH
│   │   │   │   ├── Document.java                    🟢 [EDIT] Metadonnees fichier
│   │   │   │   ├── Notification.java                🟢 [EDIT] Notification applicative
│   │   │   │   ├── HistoriqueAction.java            🟢 [EDIT] Journal d'audit
│   │   │   │   └── enums/
│   │   │   │       ├── Role.java                    🟢 [EDIT] Roles de securite
│   │   │   │       ├── StatutDemande.java           🟢 [EDIT] Etats des demandes
│   │   │   │       └── TypeConge.java               🟢 [EDIT] Types de conge
│   │   │   ├── repository/
│   │   │   │   ├── UtilisateurRepository.java       🟢 [EDIT] Acces donnees utilisateurs
│   │   │   │   ├── EmployeRepository.java           🟢 [EDIT] Acces donnees employes
│   │   │   │   ├── DepartementRepository.java       🟢 [EDIT] Acces donnees departements
│   │   │   │   ├── ServiceRepository.java           🟢 [EDIT] Acces donnees services
│   │   │   │   ├── PosteRepository.java             🟢 [EDIT] Acces donnees postes
│   │   │   │   ├── DemandeCongéRepository.java      🟢 [EDIT] Acces donnees conges
│   │   │   │   ├── DemandeAdministrativeRepository.java 🟢 [EDIT] Acces donnees demandes admin
│   │   │   │   ├── DocumentRepository.java          🟢 [EDIT] Acces donnees documents
│   │   │   │   ├── NotificationRepository.java      🟢 [EDIT] Acces donnees notifications
│   │   │   │   └── HistoriqueActionRepository.java  🟢 [EDIT] Acces donnees audit
│   │   │   └── service/
│   │   │       ├── UtilisateurService.java          🟢 [EDIT] Utilisateurs et UserDetailsService
│   │   │       ├── EmployeService.java              🟢 [EDIT] Employes, recherche, photos
│   │   │       ├── DepartementService.java          🟢 [EDIT] Organisation interne
│   │   │       ├── CongeService.java                🟢 [EDIT] Workflow conges
│   │   │       ├── DemandeAdminService.java         🟢 [EDIT] Workflow demandes admin
│   │   │       ├── DocumentService.java             🟢 [EDIT] Stockage fichiers
│   │   │       └── NotificationService.java         🟢 [EDIT] Notifications
│   │   └── resources/
│   │       ├── application.properties               🟡 [CONFIG] Port, PostgreSQL, JPA, upload
│   │       ├── static/
│   │       │   ├── css/
│   │       │   │   └── main.css                     🟢 [EDIT] Styles Bootstrap custom
│   │       │   └── js/
│   │       │       └── main.js                      🟢 [EDIT] Interactions UI
│   │       └── templates/
│   │           ├── layout/base.html                 🟢 [EDIT] Layout principal
│   │           ├── auth/login.html                  🟢 [EDIT] Page login
│   │           ├── dashboard/index.html             🟢 [EDIT] Dashboard
│   │           ├── employes/list.html               🟢 [EDIT] Liste employes
│   │           ├── employes/form.html               🟢 [EDIT] Formulaire employe
│   │           ├── employes/detail.html             🟢 [EDIT] Detail employe
│   │           ├── departements/list.html           🟢 [EDIT] Organisation
│   │           ├── departements/form.html           🟢 [EDIT] Formulaire departement
│   │           ├── conges/list.html                 🟢 [EDIT] Liste conges
│   │           ├── conges/form.html                 🟢 [EDIT] Formulaire conge
│   │           ├── demandes/list.html               🟢 [EDIT] Liste demandes admin
│   │           ├── demandes/form.html               🟢 [EDIT] Formulaire demande admin
│   │           ├── documents/list.html              🟢 [EDIT] Documents
│   │           └── notifications/list.html          🟢 [EDIT] Notifications
│   └── test/
│       └── java/com/hrplatform/
│           └── HrPlatformApplicationTests.java      🔵 [DEFAULT] Test de chargement du contexte
```

## Modules

- **Securite**: login personnalise, BCrypt, roles Spring Security, session unique par utilisateur.
- **Employes**: creation, modification, detail, recherche, archivage logique, photo.
- **Organisation**: gestion des departements, services et postes.
- **Conges**: soumission par employe, validation/refus par responsable ou RH/admin, notifications.
- **Demandes administratives**: soumission, traitement RH/admin, reponse et notification.
- **Documents**: upload dans `./uploads/documents/`, telechargement securise, suppression.
- **Notifications**: liste par utilisateur, compteur non lu, lecture unitaire ou globale.
- **Audit**: chaque action de service qui modifie les donnees ajoute une ligne dans `historique_actions`.

## Notes UML

Diagrammes conseilles:

- Diagramme de classes: commencer par `Utilisateur`, `Employe`, `Departement`, `Service`, `Poste`, `DemandeCongé`, `DemandeAdministrative`, `Document`, `Notification`, `HistoriqueAction`.
- Diagramme de cas d'utilisation: acteurs `ADMIN`, `RESPONSABLE_RH`, `RESPONSABLE_HIERARCHIQUE`, `EMPLOYE`.
- Diagramme de sequence conge: `Employe -> CongeController -> CongeService -> NotificationService -> Responsable`.
- Diagramme de sequence demande administrative: `Employe -> DemandeAdminController -> DemandeAdminService -> NotificationService -> RH`.
- Diagramme de deploiement: navigateur, application Spring Boot, PostgreSQL, dossier `uploads`.

## Ajouter Une Nouvelle Fonctionnalite

1. Ajouter ou modifier l'entite dans `model`.
2. Creer le repository dans `repository` avec les requetes necessaires.
3. Ajouter la logique metier dans `service`, avec transaction et audit via `HistoriqueActionRepository`.
4. Exposer les pages dans un controller MVC avec validation, flash messages et controle de role.
5. Creer les templates Thymeleaf et reutiliser `layout/base.html`.
6. Ajouter les liens de navigation si la fonctionnalite doit etre visible dans le menu.
7. Tester le workflow avec un compte autorise et un compte non autorise.

## Auteurs

- Projet scolaire RH Platform.
- Auteurs: equipe etudiants.
