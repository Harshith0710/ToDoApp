package com.practice.todo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
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

// Main TodoApp Interface
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoAppInterface(taskViewModel: TaskViewModel = koinViewModel()) {
    val focusManager = LocalFocusManager.current
    val tasks by taskViewModel.tasks.collectAsState()
    var newTask by remember { mutableStateOf("") }

    // Update dialog state
    var showUpdateDialog by remember { mutableStateOf(false) }
    var updateDialogTaskTitle by remember { mutableStateOf("") }
    var updateDialogTaskId by remember { mutableStateOf<Int?>(null) }

    val dismissUpdateDialog = {
        showUpdateDialog = false
        updateDialogTaskTitle = ""
        updateDialogTaskId = null
        focusManager.clearFocus()
    }

    val scrollBehaviour = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())

    Scaffold(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.primaryContainer)
            .fillMaxSize()
            .statusBarsPadding()
            .nestedScroll(scrollBehaviour.nestedScrollConnection),
        topBar = {
            TodoTopAppBar(scrollBehaviour = scrollBehaviour)
        },
        bottomBar = {
            TodoBottomBar(
                newTask = newTask,
                onTaskChange = { newTask = it },
                onAddTask = {
                    if (newTask.isNotEmpty()) {
                        taskViewModel.upsertTask(Task(title = newTask))
                        newTask = ""
                        focusManager.clearFocus()
                    }
                },
                focusManager = focusManager
            )
        }
    ) { paddingValues ->
        TodoTaskList(
            paddingValues = paddingValues,
            tasks = tasks,
            onDeleteTask = { task -> taskViewModel.deleteTask(task) },
            onUpdateTask = { task ->
                showUpdateDialog = true
                updateDialogTaskTitle = task.title
                updateDialogTaskId = task.id
            },
            onToggleTask = { task ->
                taskViewModel.upsertTask(
                    Task(task.id, task.title, !task.isDone)
                )
            }
        )
    }

    if (showUpdateDialog) {
        TaskUpdateDialog(
            taskTitle = updateDialogTaskTitle,
            onTaskTitleChange = { updateDialogTaskTitle = it },
            onConfirm = {
                if (updateDialogTaskTitle.isNotEmpty()) {
                    updateDialogTaskId?.let {
                        taskViewModel.upsertTask(Task(it, updateDialogTaskTitle))
                    }
                    dismissUpdateDialog()
                }
            },
            onDismiss = dismissUpdateDialog
        )
    }
}

// Top App Bar Component
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoTopAppBar(
    scrollBehaviour: TopAppBarScrollBehavior
) {
    TopAppBar(
        title = {
            Text(
                text = "My ToDo's",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        navigationIcon = {
            IconButton(onClick = {}) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Menu"
                )
            }
        },
        scrollBehavior = scrollBehaviour,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            scrolledContainerColor = MaterialTheme.colorScheme.primaryContainer,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            actionIconContentColor = MaterialTheme.colorScheme.onPrimary
        )
    )
}

// Bottom Input Bar Component
@Composable
fun TodoBottomBar(
    newTask: String,
    onTaskChange: (String) -> Unit,
    onAddTask: () -> Unit,
    focusManager: FocusManager
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .imePadding()
            .navigationBarsPadding()
    ) {
        TaskInputField(
            value = newTask,
            onValueChange = onTaskChange,
            focusManager = focusManager,
            modifier = Modifier.weight(1f)
        )

        AddTaskButton(
            onClick = onAddTask
        )
    }
}

// Task Input Field Component
@Composable
fun TaskInputField(
    value: String,
    onValueChange: (String) -> Unit,
    focusManager: FocusManager,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(text = "Enter your task here...") },
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = colorScheme.onSurface,
            unfocusedTextColor = colorScheme.onSurface,
            disabledTextColor = colorScheme.onSurface.copy(alpha = 0.5f),
            errorTextColor = colorScheme.error,
            focusedContainerColor = colorScheme.surfaceVariant,
            unfocusedContainerColor = colorScheme.surfaceVariant,
            disabledContainerColor = colorScheme.surfaceVariant.copy(alpha = 0.8f),
            errorContainerColor = colorScheme.error.copy(alpha = 0.1f),
            cursorColor = colorScheme.primary,
            errorCursorColor = colorScheme.error,
            focusedBorderColor = colorScheme.primary,
            unfocusedBorderColor = colorScheme.onSurface.copy(alpha = 0.3f),
            disabledBorderColor = colorScheme.onSurface.copy(alpha = 0.2f),
            errorBorderColor = colorScheme.error,
            focusedPlaceholderColor = colorScheme.onSurface.copy(alpha = 0.5f),
            unfocusedPlaceholderColor = colorScheme.onSurface.copy(alpha = 0.4f),
            disabledPlaceholderColor = colorScheme.onSurface.copy(alpha = 0.3f),
            errorPlaceholderColor = colorScheme.error
        ),
        modifier = modifier,
        keyboardOptions = KeyboardOptions.Default.copy(
            capitalization = KeyboardCapitalization.Sentences,
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(
            onDone = { focusManager.clearFocus() }
        ),
        shape = RoundedCornerShape(16.dp)
    )
}

// Add Task Button Component
@Composable
fun AddTaskButton(
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
    ) {
        Text(text = "Add")
    }
}

// Task List Component
@Composable
fun TodoTaskList(
    paddingValues: PaddingValues,
    tasks: List<Task>,
    onDeleteTask: (Task) -> Unit,
    onUpdateTask: (Task) -> Unit,
    onToggleTask: (Task) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
        contentPadding = paddingValues
    ) {
        items(tasks) { task ->
            TaskItem(
                task = task,
                onDelete = { onDeleteTask(task) },
                onUpdate = { onUpdateTask(task) },
                onToggle = { onToggleTask(task) }
            )
        }
    }
}

// Individual Task Item Component
@Composable
fun TaskItem(
    task: Task,
    onDelete: () -> Unit,
    onUpdate: () -> Unit,
    onToggle: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        TaskCheckbox(
            isChecked = task.isDone,
            onCheckedChange = { onToggle() },
            modifier = Modifier.align(Alignment.Top)
        )

        TaskText(
            text = task.title,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )

        TaskActionButtons(
            onUpdate = onUpdate,
            onDelete = onDelete,
            modifier = Modifier.align(Alignment.Top)
        )
    }
}

// Task Checkbox Component
@Composable
fun TaskCheckbox(
    isChecked: Boolean,
    onCheckedChange: () -> Unit,
    modifier: Modifier = Modifier
) {
    Checkbox(
        checked = isChecked,
        onCheckedChange = { onCheckedChange() },
        colors = CheckboxDefaults.colors(
            checkedColor = MaterialTheme.colorScheme.primary,
            uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant,
            checkmarkColor = MaterialTheme.colorScheme.onPrimary,
            disabledCheckedColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
            disabledUncheckedColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
        ),
        modifier = modifier
    )
}

// Task Text Component
@Composable
fun TaskText(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyLarge.copy(
            color = MaterialTheme.colorScheme.onSurface
        ),
        modifier = modifier
    )
}

// Task Action Buttons Component
@Composable
fun TaskActionButtons(
    onUpdate: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier) {
        IconButton(
            onClick = onUpdate,
            colors = IconButtonDefaults.iconButtonColors(
                contentColor = MaterialTheme.colorScheme.onSurface
            )
        ) {
            Icon(
                imageVector = Icons.Rounded.Edit,
                contentDescription = "Update the task"
            )
        }

        IconButton(
            onClick = onDelete,
            colors = IconButtonDefaults.iconButtonColors(
                contentColor = MaterialTheme.colorScheme.onSurface
            )
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Remove the task"
            )
        }
    }
}

// Task Update Dialog Component
@Composable
fun TaskUpdateDialog(
    taskTitle: String,
    onTaskTitleChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            DialogButton(
                text = "Ok",
                onClick = onConfirm
            )
        },
        dismissButton = {
            DialogButton(
                text = "Cancel",
                onClick = onDismiss
            )
        },
        title = {
            Text(
                text = "Update Task",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            UpdateTaskTextField(
                value = taskTitle,
                onValueChange = onTaskTitleChange
            )
        },
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        iconContentColor = MaterialTheme.colorScheme.error,
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 6.dp,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    )
}

// Dialog Button Component
@Composable
fun DialogButton(
    text: String,
    onClick: () -> Unit
) {
    Text(
        text = text,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .padding(horizontal = 8.dp)
            .clickable { onClick() }
    )
}

// Update Task Text Field Component
@Composable
fun UpdateTaskTextField(
    value: String,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text("Enter updated task") },
        singleLine = true,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            cursorColor = MaterialTheme.colorScheme.primary,
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
            unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
        shape = RoundedCornerShape(12.dp),
        textStyle = MaterialTheme.typography.bodyLarge.copy(
            color = MaterialTheme.colorScheme.onSurface
        ),
        keyboardOptions = KeyboardOptions.Default.copy(
            capitalization = KeyboardCapitalization.Sentences
        )
    )
}