package com.example.todolistapp.presentation.screens.task

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todolistapp.domain.model.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TaskListViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()

    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks

    /**
     * 🔹 Charger toutes les tâches de l'utilisateur connecté depuis Firestore.
     */
    fun loadUserTasks(userId: String) {
        if (userId.isEmpty()) return

        Log.d("TaskListViewModel", "🔍 Écoute en temps réel des tâches de l'utilisateur: $userId")

        firestore.collection("tasks")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("TaskListViewModel", "❌ Erreur Firestore", e)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val taskList = snapshot.documents.map { doc ->
                        Task(
                            taskID = doc.id,
                            taskName = doc.getString("taskName") ?: "No Name",
                            taskDescription = doc.getString("taskDescription") ?: "",
                            taskDueDate = doc.getString("taskDueDate") ?: "",
                            taskIsFinished = doc.getBoolean("taskIsFinished") ?: false,
                            userId = doc.getString("userId") ?: ""
                        )
                    }
                    _tasks.value = taskList
                    Log.d("TaskListViewModel", "✅ Tâches mises à jour en direct : ${taskList.size}")
                }
            }
    }



    /**
     * 🔹 Ajouter une nouvelle tâche dans Firestore.
     */
    fun addTask(task: Task) {
        viewModelScope.launch {
            val currentUser = FirebaseAuth.getInstance().currentUser
            val userId = currentUser?.uid ?: ""

            if (userId.isEmpty()) {
                Log.e("TaskListViewModel", "❌ Impossible d'ajouter la tâche : UserID manquant")
                return@launch
            }

            val taskData = hashMapOf(
                "userId" to userId,  // 🔹 Met le vrai userId ici
                "taskName" to task.taskName,
                "taskDescription" to task.taskDescription,
                "taskDueDate" to task.taskDueDate,
                "taskIsFinished" to task.taskIsFinished
            )

            firestore.collection("tasks").add(taskData)
                .addOnSuccessListener { documentReference ->
                    Log.d("TaskListViewModel", "✅ Tâche ajoutée avec ID: ${documentReference.id}")
                    loadUserTasks(userId) // 🔄 Recharger après ajout
                }
                .addOnFailureListener { e ->
                    Log.e("TaskListViewModel", "❌ Erreur lors de l'ajout de la tâche", e)
                }
        }
    }






    /**
     * 🔹 Mettre à jour une tâche existante dans Firestore.
     */
    fun updateTask(task: Task) {
        viewModelScope.launch {
            if (task.taskID.isNullOrEmpty()) {
                Log.e("TaskListViewModel", "❌ Impossible de mettre à jour la tâche : TaskID manquant")
                return@launch
            }

            val taskData = hashMapOf(
                "taskName" to task.taskName,
                "taskDescription" to task.taskDescription,
                "taskDueDate" to task.taskDueDate,
                "taskIsFinished" to task.taskIsFinished
            )

            firestore.collection("tasks").document(task.taskID).update(taskData as Map<String, Any>)
                .addOnSuccessListener {
                    Log.d("TaskListViewModel", "✅ Tâche mise à jour : ${task.taskID}")
                    loadUserTasks(task.userId ?: "") // 🔄 Recharger les tâches après mise à jour
                }
                .addOnFailureListener { e ->
                    Log.e("TaskListViewModel", "❌ Erreur lors de la mise à jour de la tâche", e)
                }
        }
    }

    /**
     * 🔹 Supprimer une tâche et recharger les tâches après suppression.
     */
    fun deleteTask(taskId: String, userId: String) {
        if (taskId.isEmpty()) {
            Log.e("TaskListViewModel", "❌ Impossible de supprimer la tâche : TaskID manquant")
            return
        }

        firestore.collection("tasks").document(taskId).delete()
            .addOnSuccessListener {
                Log.d("TaskListViewModel", "✅ Tâche supprimée : $taskId")
                loadUserTasks(userId)
            }
            .addOnFailureListener { e ->
                Log.e("TaskListViewModel", "❌ Erreur lors de la suppression de la tâche", e)
            }
    }

    /**
     * 🔹 Mettre à jour l'état d'une tâche (terminée ou non).
     */
    fun updateTaskCompletion(taskId: String, isFinished: Boolean, userId: String) {
        viewModelScope.launch {
            if (taskId.isEmpty()) {
                Log.e("TaskListViewModel", "❌ Impossible de mettre à jour la tâche : TaskID manquant")
                return@launch
            }

            firestore.collection("tasks").document(taskId)
                .update("taskIsFinished", isFinished)
                .addOnSuccessListener {
                    Log.d("TaskListViewModel", "✅ Tâche mise à jour : $taskId")
                    loadUserTasks(userId) // 🔄 Recharge la liste immédiatement après mise à jour
                }
                .addOnFailureListener { e ->
                    Log.e("TaskListViewModel", "❌ Erreur lors de la mise à jour de la tâche", e)
                }
        }
    }





    /*  fun updateTaskStatus(taskId: String, isFinished: Boolean) {
        viewModelScope.launch {
            try {
                FirebaseFirestore.getInstance().collection("tasks").document(taskId)
                    .update("taskIsFinished", isFinished)
            } catch (e: Exception) {
                Log.e("TaskListViewModel", "Erreur lors de la mise à jour de la tâche", e)
            }
        }
    }*/


}
