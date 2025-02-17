package com.example.todolistapp.presentation.screens.home

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.rememberScaffoldState
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import com.example.todolistapp.presentation.screens.task.TaskItem
import kotlinx.coroutines.launch

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

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    // Chargement des tâches au lancement
    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            Log.d("HomeScreen", "🔄 Chargement des tâches pour l'utilisateur: $userId")
            taskListViewModel.loadUserTasks(userId)
        } else {
            Log.e("HomeScreen", "⚠ Aucun utilisateur connecté ! Redirection vers connexion.")
            navController.navigate("SignInScreen")
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerMenu(authViewModel, navController, drawerState)
        }
    ) {
        Scaffold(
            topBar = {
                HomeTopBar {
                    coroutineScope.launch { drawerState.open() }
                }
            },
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
}

// 🔹 Top Bar avec bouton pour ouvrir le menu latéral
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopBar(onMenuClick: () -> Unit) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = "HOME",
                color = Color.Black,
                fontSize = 20.sp
            )
        },
        navigationIcon = {
            IconButton(onClick = onMenuClick) {
                Icon(Icons.Filled.Menu, contentDescription = "Menu")
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = Color.White,
            titleContentColor = Color.Black
        )
    )
}

// 🔹 Menu latéral (Drawer)
@Composable
fun DrawerMenu(
    authViewModel: AuthViewModel,
    navController: NavController,
    drawerState: DrawerState
) {
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212)) // Fond sombre pour une meilleure intégration
    ) {
        // 🔹 En-tête du menu avec Avatar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF6200EA)) // Violet Material
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(Color.White, shape = CircleShape)
                        .border(2.dp, Color.White, CircleShape)
                        .shadow(4.dp, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = "User",
                        tint = Color.Gray,
                        modifier = Modifier.size(50.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                val username by authViewModel.username.collectAsState()

                Text(
                    text = "Hello, $username!",
                    fontSize = 20.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )

            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 🔹 Liste des options avec highlight
        Column(
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            DrawerItem(
                icon = Icons.Filled.Home,
                text = "Home",
                isSelected = true, // Met en surbrillance l'option active
                onClick = {
                    navController.navigate("HomeScreen")
                    coroutineScope.launch { drawerState.close() }
                }
            )

            Divider(modifier = Modifier.padding(vertical = 8.dp), color = Color.Gray)

            DrawerItem(
                icon = Icons.Filled.ExitToApp,
                text = "Logout",
                color = Color.Red,
                isSelected = false,
                onClick = {
                    authViewModel.signOut()
                    navController.navigate("SignInScreen") {
                        popUpTo("SignInScreen") { inclusive = true }
                    }
                    coroutineScope.launch { drawerState.close() }
                }
            )
        }
    }
}

// 🔹 Élément du menu avec effet de sélection
@Composable
fun DrawerItem(
    icon: ImageVector,
    text: String,
    color: Color = Color.White,
    isSelected: Boolean = false,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) Color(0xFF6200EA) else Color.Transparent
    val textColor = if (isSelected) Color.White else color

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(12.dp)
            .background(backgroundColor, shape = RoundedCornerShape(8.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            tint = textColor,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            fontSize = 18.sp,
            color = textColor,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    }
}



// 🔹 Swipeable Task Item (corrigé)
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