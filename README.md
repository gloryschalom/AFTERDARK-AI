# AFTERDARK AI — BY SCHALOM KINNINNON

Projet Android natif Kotlin + Jetpack Compose. Version initiale : personnage conversationnel fictif adulte nommé Ava, interface française, validation d'âge déclarative, paramètres API configurables et stockage local chiffré de la clé API.

## Prérequis
- Android Studio récent ou environnement Gradle Android
- JDK 17
- Android SDK Platform 35
- Gradle 8.9+ (le workflow GitHub Actions utilise Gradle installé par setup-gradle)

## Compilation locale
Depuis la racine :
```bash
gradle assembleDebug
```
APK attendu :
`app/build/outputs/apk/debug/app-debug.apk`

## Compilation GitHub
1. Créer un dépôt GitHub et pousser le contenu de ce dossier sur `main`.
2. Ouvrir l'onglet Actions.
3. Lancer **Build AFTERDARK AI APK**.
4. Télécharger l'artifact `AFTERDARK-AI-APK`.

## Configuration de l'IA
Dans l'application : Paramètres → URL API, modèle et clé API.
L'application attend une API compatible avec le format OpenAI Chat Completions (`POST /v1/chat/completions`).
URL par défaut : `https://api.openai.com/v1/chat/completions`.
La clé API n'est pas fournie. Il faut utiliser une clé obtenue auprès du fournisseur choisi. Des frais et limites peuvent s'appliquer.

## Sécurité et limites de cette première version
- L'écran 18+ est une déclaration d'âge, pas une vérification d'identité fiable.
- Le stockage chiffré protège la clé au repos sur l'appareil, mais un client mobile ne peut pas garantir le secret d'une clé face à un appareil compromis. Pour une diffusion publique, utiliser un backend proxy avec authentification et quotas, plutôt que distribuer une clé fournisseur dans l'APK.
- Pas de nudité explicite, génération d'images, avatar animé, compte utilisateur, paiement ou vérification d'âge tierce dans cette version.
- Ne pas saisir de données sensibles dans les conversations. Le fournisseur API peut traiter les messages selon ses propres conditions.
- Utiliser uniquement des personnages fictifs adultes et respecter les lois locales et conditions du fournisseur IA.

## Marque
AFTERDARK AI  
BY SCHALOM KINNINNON
