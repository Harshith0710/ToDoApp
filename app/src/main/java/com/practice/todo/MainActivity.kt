package com.practice.todo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.saveable.rememberSaveable
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoAppInterface(taskViewModel: TaskViewModel = koinViewModel()) {
    val focusManager = LocalFocusManager.current

    val tasks by taskViewModel.tasks.collectAsState()

    var newTask by rememberSaveable { mutableStateOf("") }
    var showAddTaskError by rememberSaveable { mutableStateOf(false) }

    var showUpdateDialog by rememberSaveable { mutableStateOf(false) }
    var updateDialogTaskTitle by rememberSaveable { mutableStateOf("") }
    var updateDialogTaskId by rememberSaveable { mutableStateOf<Int?>(null) }
    var originalTaskTitle by rememberSaveable { mutableStateOf("") }
    var showUpdateTaskError by rememberSaveable { mutableStateOf(false) }

    val dismissUpdateDialog = {
        showUpdateDialog = false
        updateDialogTaskTitle = ""
        updateDialogTaskId = null
        originalTaskTitle = ""
        showUpdateTaskError = false
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
                onTaskChange = {
                    newTask = it
                    showAddTaskError = false
                },
                onAddTask = {
                    if (taskViewModel.isValidTaskText(newTask)) {
                        taskViewModel.upsertTask(Task(title = newTask.trim()))
                        newTask = ""
                        showAddTaskError = false
                        focusManager.clearFocus()
                    } else {
                        showAddTaskError = true
                    }
                },
                focusManager = focusManager,
                showError = showAddTaskError,
                validateTaskText = { newTaskToAdd ->
                    taskViewModel.validateTaskText(newTaskToAdd)
                }
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
                originalTaskTitle = task.title
                updateDialogTaskId = task.id
                showUpdateTaskError = false
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
            originalTaskTitle = originalTaskTitle,
            onTaskTitleChange = {
                updateDialogTaskTitle = it
                showUpdateTaskError = false
            },
            onConfirm = {
                if (taskViewModel.isValidTaskText(updateDialogTaskTitle, originalTaskTitle)) {
                    updateDialogTaskId?.let {
                        taskViewModel.upsertTask(Task(it, updateDialogTaskTitle.trim()))
                    }
                    dismissUpdateDialog()
                } else {
                    showUpdateTaskError = true
                }
            },
            onDismiss = dismissUpdateDialog,
            focusManager = focusManager,
            showError = showUpdateTaskError,
            validateTaskText = { updatedTitle, existingTitle ->
                taskViewModel.validateTaskText(updatedTitle, existingTitle)
            }
        )
    }
}

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

@Composable
fun TodoBottomBar(
    newTask: String,
    onTaskChange: (String) -> Unit,
    onAddTask: () -> Unit,
    focusManager: FocusManager,
    showError: Boolean,
    validateTaskText: (String) -> ValidationResult
) {
    val validationResult = remember(newTask) {
        validateTaskText(newTask)
    }

    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
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
            modifier = Modifier.weight(1f),
            placeholderText = "Enter your task...",
            validationResult = validationResult,
            showError = showError
        )

        AddTaskButton(
            onClick = onAddTask,
            enabled = true
        )
    }
}

@Composable
fun TaskInputField(
    value: String,
    onValueChange: (String) -> Unit,
    focusManager: FocusManager,
    modifier: Modifier = Modifier,
    placeholderText: String,
    validationResult: ValidationResult = ValidationResult(isValid = true),
    showError: Boolean = false
) {
    val colorScheme = MaterialTheme.colorScheme
    val isError = !validationResult.isValid && showError

    Column(modifier = modifier) {
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(text = placeholderText) },
            singleLine = true,
            isError = isError,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = colorScheme.onSurface,
                unfocusedTextColor = colorScheme.onSurface,
                disabledTextColor = colorScheme.onSurface.copy(alpha = 0.5f),
                errorTextColor = colorScheme.error,
                focusedContainerColor = colorScheme.surfaceVariant,
                unfocusedContainerColor = colorScheme.surfaceVariant,
                disabledContainerColor = colorScheme.surfaceVariant.copy(alpha = 0.8f),
                errorContainerColor = colorScheme.surfaceVariant,
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
            keyboardOptions = KeyboardOptions.Default.copy(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { focusManager.clearFocus() }
            ),
            shape = RoundedCornerShape(16.dp),
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                color = MaterialTheme.colorScheme.onSurface
            )
        )

        if (isError) {
            Text(
                text = validationResult.errorMessage ?: "",
                color = colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
    }
}

@Composable
fun AddTaskButton(
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
            disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
        )
    ) {
        Text(text = "Add")
    }
}

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
            .background(MaterialTheme.colorScheme.surface)
            .padding(paddingValues)
    ) {
        items(
            items = tasks,
            key = {
                it.id
            }
        ) { task ->
            TaskItem(
                task = task,
                onDelete = { onDeleteTask(task) },
                onUpdate = { onUpdateTask(task) },
                onToggle = { onToggleTask(task) }
            )
        }
    }
}

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

@Composable
fun TaskUpdateDialog(
    taskTitle: String,
    originalTaskTitle: String,
    onTaskTitleChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    focusManager: FocusManager,
    showError: Boolean,
    validateTaskText: (String, String) -> ValidationResult
) {
    val validationResult = remember(taskTitle) {
        validateTaskText(taskTitle, originalTaskTitle)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            DialogButton(
                text = "Ok",
                onClick = onConfirm,
                enabled = true
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
            TaskInputField(
                value = taskTitle,
                onValueChange = onTaskTitleChange,
                focusManager = focusManager,
                modifier = Modifier,
                placeholderText = "Enter updated task...",
                validationResult = validationResult,
                showError = showError
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

@Composable
fun DialogButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    Text(
        text = text,
        color = if (enabled)
            MaterialTheme.colorScheme.primary
        else
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
        modifier = Modifier
            .padding(horizontal = 8.dp)
            .clickable(enabled = enabled) {
                if (enabled) onClick()
            }
    )
}