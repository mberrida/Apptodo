# ToDoListApp – Gestionnaire de Tâches avec Jetpack Compose

ToDoListApp est une application mobile Android développée avec **Jetpack Compose** et **Firebase Firestore**.  
Elle permet une gestion efficace des tâches : **ajout, modification, suppression et classification en "To Do" et "Done"**,  
le tout synchronisé en temps réel avec **Firebase**.

---

## 📌 1. Fonctionnalités
- 🔑 **Authentification sécurisée avec Firebase** (Email & Mot de passe)
- ✍️ **Ajout, modification et suppression de tâches**
- ✅ **Marquer une tâche comme terminée ou en attente**
- 🎯 **Filtrer les tâches par état ("To Do" / "Done")**
- ↔️ **Swipe pour modifier ou supprimer une tâche**
- 🛠 **Gestion de l’état avec StateFlow & MutableStateFlow**
- 🔄 **Synchronisation en temps réel avec Firebase Firestore**
- 📱 **Interface moderne et responsive avec Jetpack Compose**

---

## 🚀 2. Installation & Configuration

### Cloner le projet
Commencez par récupérer le projet en local :
```sh
git clone https://github.com/mberrida/Apptodo.git
cd Apptodo
```

### Configurer Firebase
1. **Créer un projet Firebase** sur [Firebase Console](https://console.firebase.google.com/).
2. **Activer Firebase Authentication** (Email/Mot de passe).
3. **Créer une base de données Firestore** en mode test.
4. **Télécharger le fichier** `google-services.json` et le placer dans `app/`.

---

## 🛠 3. Technologies utilisées
- **Langage** : Kotlin
- **UI** : Jetpack Compose
- **Base de données** : Firebase Firestore
- **Authentification** : Firebase Authentication
- **Gestion d'état** : StateFlow / MutableStateFlow
- **Navigation** : Jetpack Navigation
- **Architecture** : MVVM (Model-View-ViewModel)

---