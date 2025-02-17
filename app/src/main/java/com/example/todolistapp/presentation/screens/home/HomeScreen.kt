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
import androidx.compose.material.DismissValue
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

    // 🔹 État pour savoir si on est sur TO DO ou DONE
    var selectedTab by remember { mutableStateOf("TO DO") }

    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            taskListViewModel.loadUserTasks(userId)
        } else {
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

                // 🔹 Boutons de sélection TO DO / DONE
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = { selectedTab = "TO DO" },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedTab == "TO DO") Color.Black else Color.Gray
                        )
                    ) {
                        Text("TO DO", color = Color.White)
                    }

                    Button(
                        onClick = { selectedTab = "DONE" },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedTab == "DONE") Color.Black else Color.Gray
                        )
                    ) {
                        Text("DONE", color = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 🔹 Sélection des tâches en fonction du bouton cliqué
                val filteredTasks = when (selectedTab) {
                    "TO DO" -> tasks.filter { !(it.taskIsFinished ?: false) }
                    "DONE" -> tasks.filter { it.taskIsFinished ?: false }
                    else -> tasks
                }

                // 🔹 Liste des tâches
                TaskColumn(filteredTasks, taskListViewModel, userId, navController)

            }
        }
    }
}


@Composable
fun TaskColumn(tasks: List<Task>, taskListViewModel: TaskListViewModel, userId: String, navController: NavController) {
    LazyColumn {
        items(tasks, key = { it.taskID ?: "" }) { task ->
            SwipeTaskItem(
                task = task,
                onDelete = { taskListViewModel.deleteTask(task.taskID ?: "", userId) },
                onEdit = { task.taskID?.let { navController.navigate("AddEditTaskScreen/$it") } },
                onTaskChecked = { isChecked ->
                    taskListViewModel.updateTaskCompletion(task.taskID ?: "", isChecked, userId)
                },
                navController = navController
            )
        }
    }
}






@Composable
fun TaskSection(
    title: String,
    tasks: List<Task>,
    onTaskChecked: (Task, Boolean) -> Unit,
    navController: NavController // 🔹 Ajout du NavController si nécessaire
) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = title,
            fontSize = 20.sp,
            color = if (title == "TO DO") Color.Black else Color.Gray,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 8.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))

        if (tasks.isEmpty()) {
            Text(text = "No tasks", color = Color.Gray, modifier = Modifier.padding(16.dp))
        } else {
            LazyColumn {
                items(tasks, key = { it.taskID ?: "" }) { task ->
                    TaskItem(
                        task = task,
                        onTaskChecked = { isChecked -> onTaskChecked(task, isChecked) },
                        navController = navController // 🔹 Ajout si requis
                    )
                }
            }
        }
    }
}


@Composable
fun TaskItem(task: Task, onTaskChecked: (Boolean) -> Unit, navController: NavController) {
    var isChecked by remember { mutableStateOf(task.taskIsFinished ?: false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isChecked,
                onCheckedChange = { newCheckedState ->
                    isChecked = newCheckedState
                    onTaskChecked(newCheckedState) // 🔥 Met à jour Firestore
                },
                modifier = Modifier.padding(end = 12.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.taskName ?: "No title",
                    fontSize = 16.sp,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
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
                isSelected = true,
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




@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun SwipeTaskItem(
    task: Task,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    onTaskChecked: (Boolean) -> Unit,
    navController: NavController
) {
    val dismissState = rememberDismissState(
        confirmStateChange = { dismissValue ->
            when (dismissValue) {
                DismissValue.DismissedToEnd -> {
                    onEdit() // Modifier sur Swipe droite
                    true
                }
                DismissValue.DismissedToStart -> {
                    onDelete() // Supprimer sur Swipe gauche
                    true
                }
                else -> false
            }
        }
    )

    SwipeToDismiss(
        state = dismissState,
        directions = setOf(DismissDirection.StartToEnd, DismissDirection.EndToStart),
        background = {
            val color = when (dismissState.targetValue) {
                DismissValue.DismissedToEnd -> Color.Blue
                DismissValue.DismissedToStart -> Color.Red
                else -> Color.Transparent
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(color),
                contentAlignment = if (dismissState.targetValue == DismissValue.DismissedToStart) Alignment.CenterEnd else Alignment.CenterStart
            ) {
                if (dismissState.targetValue == DismissValue.DismissedToStart) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = "Supprimer",
                        tint = Color.White,
                        modifier = Modifier.padding(16.dp)
                    )
                } else {
                    Icon(
                        Icons.Filled.Edit,
                        contentDescription = "Modifier",
                        tint = Color.White,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        },
        dismissContent = {
            TaskItem(
                task = task,
                onTaskChecked = { isChecked -> onTaskChecked(isChecked) },
                navController = navController
            )
        }
    )
}

