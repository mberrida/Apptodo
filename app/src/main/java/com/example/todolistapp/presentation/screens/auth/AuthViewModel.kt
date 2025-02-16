package com.example.todolistapp.presentation.screens.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    private val _userId = MutableStateFlow(auth.currentUser?.uid ?: "")
    val userId: StateFlow<String> = _userId

    private val _username = MutableStateFlow(auth.currentUser?.displayName ?: "User")
    val username: StateFlow<String> = _username

    init {
        loadUserData() // Charger les données utilisateur au démarrage
    }

    fun loadUserData() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            _userId.value = currentUser.uid
            db.collection("users").document(currentUser.uid).get()
                .addOnSuccessListener { document ->
                    _username.value = document.getString("username") ?: "User"
                }
        }
    }

    fun signUp(email: String, password: String, username: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val user = task.result?.user
                        if (user != null) {
                            _userId.value = user.uid
                            _username.value = username
                            db.collection("users").document(user.uid).set(mapOf("username" to username))
                        }
                        _authState.value = AuthState.Success
                    } else {
                        _authState.value = AuthState.Error(task.exception?.message ?: "Sign-up failed")
                    }
                }
        }
    }

    fun signIn(email: String, password: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        loadUserData()
                        _authState.value = AuthState.Success
                    } else {
                        _authState.value = AuthState.Error(task.exception?.message ?: "Login failed")
                    }
                }
        }
    }

    fun signOut() {
        auth.signOut() // Déconnecter l'utilisateur de Firebase
        _authState.value = AuthState.Idle // Réinitialiser l'état d'authentification
        Log.d("AuthViewModel", "✅ Utilisateur déconnecté avec succès")
    }

}
