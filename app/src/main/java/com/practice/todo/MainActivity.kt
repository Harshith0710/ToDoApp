package com.practice.todo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.core.view.WindowCompat
import com.practice.todo.ui.theme.ToDoAppTheme
import org.koin.compose.viewmodel.koinViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        enableEdgeToEdge()
        setContent {
            ToDoAppTheme {
                TodoAppInterface()
            }
        }
    }
}

@Composable
fun TodoAppInterface(taskViewModel: TaskViewModel = koinViewModel()){
    val tasks by taskViewModel.tasks.collectAsState()
    var newTask by remember { mutableStateOf("") }
    var showUpdateDialog by remember {mutableStateOf(false)}
    var updateDialogTaskTitle by remember { mutableStateOf("") }
    var updateDialogTaskId by remember { mutableStateOf<Int?>(null) }
    val dismissUpdateDialog = {
        showUpdateDialog = false
        updateDialogTaskTitle = ""
        updateDialogTaskId = null
    }
    Scaffold(
        modifier = Modifier
            .background(Color.LightGray)
            .windowInsetsPadding(WindowInsets.safeDrawing),
        bottomBar = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                OutlinedTextField(
                    value = newTask,
                    onValueChange = {
                        newTask = it
                    },
                    label = {Text(text = "New Task")},
                    placeholder = {Text(text = "Enter your task here...")},
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        disabledTextColor = Color.Gray,
                        errorTextColor = Color.Red,

                        focusedContainerColor = Color(0xFFF5F5F5),
                        unfocusedContainerColor = Color(0xFFF5F5F5),
                        disabledContainerColor = Color(0xFFE0E0E0),
                        errorContainerColor = Color(0xFFFFEBEE),

                        cursorColor = Color.Black,
                        errorCursorColor = Color.Red,

                        focusedBorderColor = Color(0xFF3F51B5),
                        unfocusedBorderColor = Color(0xFFBDBDBD),
                        disabledBorderColor = Color(0xFFDDDDDD),
                        errorBorderColor = Color.Red,

                        focusedLabelColor = Color(0xFF3F51B5),
                        unfocusedLabelColor = Color.DarkGray,
                        disabledLabelColor = Color.Gray,
                        errorLabelColor = Color.Red,

                        focusedPlaceholderColor = Color.Gray,
                        unfocusedPlaceholderColor = Color.LightGray,
                        disabledPlaceholderColor = Color.Gray,
                        errorPlaceholderColor = Color.Red
                    ),
                    modifier = Modifier
                        .weight(1f),
                    keyboardOptions = KeyboardOptions.Default.copy(
                        capitalization = KeyboardCapitalization.Sentences
                    )
                )
                Button(
                    onClick = {
                        if (newTask.isNotEmpty()) {
                            taskViewModel.upsertTask(Task(title = newTask))
                            newTask = ""
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    elevation = ButtonDefaults.buttonElevation(6.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF3F51B5),
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = "Add"
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.LightGray)
                .padding(paddingValues)
        ){
            ListOfTasks(tasks, { task ->
                taskViewModel.deleteTask(task)
            }, { task ->
                showUpdateDialog = true
                updateDialogTaskTitle = task.title
                updateDialogTaskId = task.id
            })
        }
    }
    if(showUpdateDialog){
        AlertDialog(
            onDismissRequest = {
                dismissUpdateDialog()
            },
            confirmButton = {
                Text(
                    text = "Ok",
                    color = Color(0xFF3F51B5),
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .clickable(
                            onClick = {
                                updateDialogTaskId?.let { taskViewModel.upsertTask(Task(it, updateDialogTaskTitle)) }
                                dismissUpdateDialog()
                            }
                        )
                )
            },
            dismissButton = {
                Text(
                    text = "Cancel",
                    color = Color(0xFF3F51B5),
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .clickable(
                            onClick = {
                                dismissUpdateDialog()
                            }
                        )
                )
            },
            title = {
                Text(
                    text = "Update Task",
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                OutlinedTextField(
                    value = updateDialogTaskTitle,
                    onValueChange = { updateDialogTaskTitle = it },
                    placeholder = { Text("Enter updated task") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF3F51B5),
                        unfocusedBorderColor = Color(0xFFBDBDBD),
                        focusedLabelColor = Color(0xFF3F51B5),
                        unfocusedLabelColor = Color.DarkGray,
                        cursorColor = Color.Black,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        focusedContainerColor = Color(0xFFF0F0F0),
                        unfocusedContainerColor = Color(0xFFF0F0F0)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.Black),
                    keyboardOptions = KeyboardOptions.Default.copy(
                        capitalization = KeyboardCapitalization.Sentences
                    )
                )
            },
            containerColor = Color(0xFFF5F5F5),
            titleContentColor = Color.Black,
            textContentColor = Color.DarkGray,
            iconContentColor = Color(0xFFEF5350),
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 6.dp,
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true
            )
        )
    }
}

@Composable
fun ListOfTasks(tasks: List<Task>, removeTask: (Task) -> Unit, updateTask: (Task) -> Unit){
    LazyColumn {
        items(tasks){ task ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
            ){
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Black,
                    modifier = Modifier
                        .weight(1f)
                        .padding(8.dp)
                )
                IconButton(
                    onClick = {
                        updateTask(task)
                    },
                    modifier = Modifier
                        .align(Alignment.Top)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Edit,
                        contentDescription = "Update the task",
                        tint = Color.Black
                    )
                }
                IconButton(
                    onClick = {
                        removeTask(task)
                    },
                    modifier = Modifier
                        .align(Alignment.Top)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Remove the task",
                        tint = Color.Black
                    )
                }
            }
        }
    }
}