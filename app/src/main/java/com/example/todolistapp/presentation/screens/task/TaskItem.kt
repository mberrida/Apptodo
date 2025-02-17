package com.example.todolistapp.presentation.screens.task


import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.todolistapp.domain.model.Task

@Composable
fun TaskItem(
    task: Task,
    onTaskChecked: (Boolean) -> Unit,
    navController: NavController
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable {
                navController.navigate("AddEditTaskScreen/${task.taskID}")
            },
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
                onCheckedChange = { isChecked ->
                    onTaskChecked(isChecked)
                },
                modifier = Modifier.padding(end = 12.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.taskName ?: "No title",
                    fontSize = 18.sp,
                    color = if (task.taskIsFinished == true) Color.Gray else Color.Black
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
