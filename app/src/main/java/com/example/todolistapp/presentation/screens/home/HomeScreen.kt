package com.example.todolistapp.presentation.screens.home

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.todolistapp.domain.model.Task
import com.example.todolistapp.presentation.screens.auth.AuthViewModel
import com.example.todolistapp.presentation.screens.task.TaskListViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.rememberDismissState
import androidx.compose.material.DismissDirection
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ScaffoldState
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Home
import androidx.compose.ui.draw.clip

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun HomeScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel(),
    taskListViewModel: TaskListViewModel = viewModel()
) {
    val username by authViewModel.username.collectAsState()
    val userId by authViewModel.userId.collectAsState()
    val tasks by taskListViewModel.tasks.collectAsState()

    // 🔹 Charger les tâches dès qu'on a l'ID utilisateur
    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            Log.d("HomeScreen", "🔄 Chargement des tâches pour l'utilisateur: $userId")
            taskListViewModel.loadUserTasks(userId)
        } else {
            Log.e("HomeScreen", "⚠ Aucun utilisateur connecté ! Redirection vers connexion.")
            navController.navigate("SignInScreen")
        }
    }

    Scaffold(
        topBar = { HomeTopBar() },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (userId.isNotEmpty()) {
                        navController.navigate("AddEditTaskScreen")
                    } else {
                        Log.e("HomeScreen", "⚠ Aucun utilisateur connecté, impossible d'ajouter une tâche.")
                    }
                },
                containerColor = Color.Black
            ) {
                Icon(imageVector = Icons.Filled.Add, contentDescription = "Ajouter une tâche", tint = Color.White)
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text(
                text = "Hello, $username!",
                fontSize = 26.sp,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(10.dp))

            if (tasks.isEmpty()) {
                Text(
                    text = "No tasks available",
                    color = Color.Gray,
                    modifier = Modifier.padding(16.dp)
                )
            } else {
                LazyColumn {
                    items(tasks, key = { it.taskID ?: "" }) { task ->
                        SwipeTaskItem(
                            task = task,
                            onDelete = { taskListViewModel.deleteTask(task.taskID ?: "", userId) },
                            onEdit = { task.taskID?.let { navController.navigate("AddEditTaskScreen/$it") } },
                            navController = navController
                        )
                    }
                }
            }
        }
    }
}

// 🔹 Menu Latéral (Drawer)
@Composable
fun DrawerMenu(
    authViewModel: AuthViewModel,
    navController: NavController,
    scaffoldState: ScaffoldState
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(text = "Menu", fontSize = 22.sp, color = Color.Black)

            Spacer(modifier = Modifier.height(20.dp))

            // 🔹 Bouton Home
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        navController.navigate("HomeScreen")
                    }
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.Home, contentDescription = "Home")
                Spacer(modifier = Modifier.width(10.dp))
                Text(text = "Home", fontSize = 18.sp)
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 🔹 Bouton Déconnexion
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        authViewModel.signOut()
                        navController.navigate("SignInScreen") {
                            popUpTo("SignInScreen") { inclusive = true }
                        }
                    }
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.ExitToApp, contentDescription = "Logout", tint = Color.Red)
                Spacer(modifier = Modifier.width(10.dp))
                Text(text = "Logout", fontSize = 18.sp, color = Color.Red)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun SwipeTaskItem(
    task: Task,
    onDelete: (Task) -> Unit,
    onEdit: (Task) -> Unit,
    navController: NavController
) {
    val dismissState = rememberDismissState()

    if (dismissState.isDismissed(DismissDirection.StartToEnd)) {
        Log.d("Navigation", "Task ID envoyé: ${task.taskID}")
        navController.navigate("AddEditTaskScreen/${task.taskID}")
    }
    if (dismissState.isDismissed(DismissDirection.EndToStart)) {
        onDelete(task)
    }

    SwipeToDismiss(
        state = dismissState,
        directions = setOf(DismissDirection.StartToEnd, DismissDirection.EndToStart),
        background = {
            val color = when (dismissState.dismissDirection) {
                DismissDirection.StartToEnd -> Color.Blue
                DismissDirection.EndToStart -> Color.Red
                else -> Color.Transparent
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(color),
                contentAlignment = if (dismissState.dismissDirection == DismissDirection.EndToStart) Alignment.CenterEnd else Alignment.CenterStart
            ) {
                if (dismissState.dismissDirection == DismissDirection.EndToStart) {
                    Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = Color.White, modifier = Modifier.padding(16.dp))
                } else {
                    Icon(Icons.Filled.Edit, contentDescription = "Edit", tint = Color.White, modifier = Modifier.padding(16.dp))
                }
            }
        },
        dismissContent = {
            TaskItem(task, onTaskChecked = {}, onClick = {})
        }
    )
}

@Composable
fun TaskItem(task: Task, onTaskChecked: (Boolean) -> Unit, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = task.taskIsFinished ?: false,
                onCheckedChange = { isChecked -> onTaskChecked(isChecked) },
                modifier = Modifier.padding(end = 12.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.taskName ?: "No title",
                    fontSize = 18.sp,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Due on ${task.taskDueDate ?: "No due date"}",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopBar() {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = "HOME",
                color = Color.Black,
                fontSize = 20.sp
            )
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = Color.White,
            titleContentColor = Color.Black
        )
    )
}
