package com.practice.todo.ui.screens

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.practice.todo.data.entity.Task
import com.practice.todo.model.ValidationResult
import com.practice.todo.viewmodel.TaskViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(
    taskViewModel: TaskViewModel = koinViewModel(),
    onMenuClick: () -> Unit = {}
) {
    val focusManager = LocalFocusManager.current
    val density = LocalDensity.current

    val tasks by taskViewModel.tasks.collectAsStateWithLifecycle()

    var newTaskTitle by rememberSaveable { mutableStateOf("") }
    var newTaskDescription by rememberSaveable { mutableStateOf("") }

    var showAddTaskError by rememberSaveable { mutableStateOf(false) }
    var showAddButton by rememberSaveable { mutableStateOf(true) }

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

    val coroutineScope = rememberCoroutineScope()

    val bottomSheetState = rememberStandardBottomSheetState(
        initialValue = SheetValue.Hidden,
        skipHiddenState = false
    )
    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = bottomSheetState
    )

    var bottomSheetHeight by remember { mutableIntStateOf(0) }
    var targetPadding by remember { mutableStateOf(0.dp) }
    val bottomContentPadding by animateDpAsState(
        targetValue = targetPadding,
        animationSpec = tween(
            durationMillis = 300
        )
    )

    LaunchedEffect(bottomSheetState) {
        snapshotFlow { bottomSheetState.currentValue }
            .collectLatest {
                targetPadding = when(it){
                    SheetValue.Hidden, SheetValue.PartiallyExpanded -> 0.dp
                    SheetValue.Expanded -> with(density){
                        bottomSheetHeight.toDp()
                    }
                }
                showAddButton = when(it){
                    SheetValue.Hidden, SheetValue.PartiallyExpanded -> true
                    SheetValue.Expanded -> false
                }
                if(it != SheetValue.Expanded) focusManager.clearFocus(true)
            }
    }

    val scrollBehaviour = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surface)
            .windowInsetsPadding(
                WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)
            )
            .fillMaxSize()
            .nestedScroll(scrollBehaviour.nestedScrollConnection),
        sheetContent = {
            Column(
                modifier = Modifier
                    .onGloballyPositioned{
                        bottomSheetHeight = it.size.height
                    }
            ) {
                DragHandleIndicator(modifier = Modifier)
                BottomSheetContent(
                    modifier = Modifier,
                    newTaskTitle = newTaskTitle,
                    newTaskDescription = newTaskDescription,
                    onAddTask = {
                        if (taskViewModel.isValidTaskText(newTaskTitle)) {
                            focusManager.clearFocus()
                            coroutineScope.launch {
                                bottomSheetState.hide()
                            }
                            taskViewModel.upsertTask(
                                Task(
                                    title = newTaskTitle.trim(),
                                    description = newTaskDescription
                                )
                            )
                            newTaskTitle = ""
                            newTaskDescription = ""
                            showAddTaskError = false
                        }
                        else {
                            showAddTaskError = true
                        }
                    },
                    onCancel = {
                        showAddTaskError = false
                        focusManager.clearFocus()
                        coroutineScope.launch {
                            bottomSheetState.hide()
                        }
                        newTaskTitle = ""
                        newTaskDescription = ""
                    },
                    validateTaskTitle = { newTaskTitle ->
                        taskViewModel.validateTaskText(newTaskTitle)
                    },
                    onTextTitleChange = {
                        newTaskTitle = it
                        showAddTaskError = false
                    },
                    onTextDescriptionChange = {
                        newTaskDescription = it
                    },
                    focusManager = focusManager,
                    showError = showAddTaskError
                )
            }
        },
        sheetDragHandle = {},
        sheetContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        contentColor = MaterialTheme.colorScheme.onSurface,
        sheetPeekHeight = 0.dp,
        topBar = {
            TodoTopAppBar(
                scrollBehaviour = scrollBehaviour,
                onMenuClick = onMenuClick
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .padding(bottom = bottomContentPadding)
                .fillMaxSize()
        ){
            TodoTaskList(
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
                        Task(task.id, task.title, !task.isDone, task.description)
                    )
                }
            )
            if(showAddButton){
                FloatingAddButton(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp),
                    onClick = {
                        coroutineScope.launch {
                            bottomSheetState.expand()
                        }
                    }
                )
            }
        }
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
    scrollBehaviour: TopAppBarScrollBehavior,
    onMenuClick: () -> Unit = {}
) {
    TopAppBar(
        title = {
            Text(
                text = "My ToDo's",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleLarge
            )
        },
        navigationIcon = {
            IconButton(onClick = onMenuClick) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Menu"
                )
            }
        },
        scrollBehavior = scrollBehaviour,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
            navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
            actionIconContentColor = MaterialTheme.colorScheme.onSurface
        )
    )
}

@Composable
fun BottomSheetContent(
    modifier: Modifier = Modifier,
    newTaskTitle: String,
    newTaskDescription: String,
    onAddTask: () -> Unit,
    onCancel: () -> Unit,
    validateTaskTitle: (String) -> ValidationResult,
    onTextTitleChange: (String) -> Unit,
    onTextDescriptionChange: (String) -> Unit,
    focusManager: FocusManager,
    showError: Boolean
) {
    val validationResult = remember(newTaskTitle){
        validateTaskTitle(newTaskTitle)
    }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp)
            .imePadding()
    ) {
        Text(
            text = "Your Task",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        TaskInputField(
            value = newTaskTitle,
            onValueChange = onTextTitleChange,
            focusManager = focusManager,
            modifier = Modifier,
            placeholderText = "Enter your task title...",
            validationResult = validationResult,
            showError = showError,
            keyboardImeAction = ImeAction.Next,
            maxLines = 3
        )

        Spacer(Modifier.height(16.dp)) // Material 3 form field spacing

        TaskInputField(
            value = newTaskDescription,
            onValueChange = onTextDescriptionChange,
            focusManager = focusManager,
            modifier = Modifier,
            placeholderText = "Enter your task description...",
            validationResult = validationResult,
            showError = false,
            keyboardImeAction = ImeAction.Default,
            maxLines = 8
        )

        Spacer(Modifier.height(24.dp)) // Material 3 form to button spacing

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp) // Material 3 button spacing
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Text(
                    text = "Cancel",
                    style = MaterialTheme.typography.labelLarge // Material 3 button text style
                )
            }

            Button(
                onClick = onAddTask,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(
                    text = "Save",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
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
    showError: Boolean = false,
    keyboardImeAction: ImeAction,
    maxLines: Int
) {
    val colorScheme = MaterialTheme.colorScheme
    val isError = !validationResult.isValid && showError

    Column(modifier = modifier) {
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(
                text = placeholderText,
                style = MaterialTheme.typography.bodyLarge
            ) },
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
                imeAction = keyboardImeAction
            ),
            keyboardActions = KeyboardActions(
                onDone = { focusManager.clearFocus() },
                onNext = { focusManager.moveFocus(FocusDirection.Next) }
            ),
            shape = RoundedCornerShape(12.dp),
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                color = MaterialTheme.colorScheme.onSurface
            ),
            maxLines = maxLines
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
fun TodoTaskList(
    tasks: List<Task>,
    onDeleteTask: (Task) -> Unit,
    onUpdateTask: (Task) -> Unit,
    onToggleTask: (Task) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .fillMaxSize(),
        contentPadding = PaddingValues(
            start = WindowInsets.safeContent.asPaddingValues().calculateStartPadding(
                layoutDirection = LayoutDirection.Ltr
            ),
            end = WindowInsets.safeContent.asPaddingValues().calculateEndPadding(
                layoutDirection = LayoutDirection.Rtl
            ),
            bottom = WindowInsets.safeContent.asPaddingValues().calculateBottomPadding()
        )
    ) {
        items(
            items = tasks,
            key = { it.id }
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
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        TaskCheckbox(
            isChecked = task.isDone,
            onCheckedChange = onToggle,
            modifier = Modifier.align(Alignment.Top)
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = task.title,
                style = if (task.isDone) {
                    MaterialTheme.typography.bodyLarge.copy(
                        textDecoration = TextDecoration.LineThrough
                    )
                } else {
                    MaterialTheme.typography.bodyLarge
                },
                color = if (task.isDone) {
                    MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                } else {
                    MaterialTheme.colorScheme.onBackground
                }
            )

            if (task.description.isNotBlank() && !task.isDone) {
                Text(
                    text = task.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.75f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

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
fun TaskActionButtons(
    onUpdate: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp) // Material 3 icon button spacing
    ) {
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
                style = MaterialTheme.typography.headlineSmall, // Material 3 dialog title style
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Column {
                Spacer(modifier = Modifier.height(8.dp)) // Material 3 dialog content spacing
                TaskInputField(
                    value = taskTitle,
                    onValueChange = onTaskTitleChange,
                    focusManager = focusManager,
                    modifier = Modifier,
                    placeholderText = "Enter updated task...",
                    validationResult = validationResult,
                    showError = showError,
                    keyboardImeAction = ImeAction.Done,
                    maxLines = 3
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        iconContentColor = MaterialTheme.colorScheme.error,
        shape = RoundedCornerShape(28.dp), // Material 3 dialog rounded corners
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
        style = MaterialTheme.typography.labelLarge, // Material 3 dialog button text style
        modifier = Modifier
            .padding(horizontal = 12.dp, vertical = 8.dp) // Material 3 dialog button padding
            .clickable(enabled = enabled) {
                if (enabled) onClick()
            }
    )
}

@Composable
fun FloatingAddButton(modifier: Modifier = Modifier, onClick: () -> Unit) {
    FloatingActionButton(
        modifier = modifier
            .windowInsetsPadding(WindowInsets.navigationBars),
        onClick = {
            onClick()
        }
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Add Task"
        )
    }
}

@Composable
fun DragHandleIndicator(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp), // Material 3 drag handle padding
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .width(32.dp)
                .height(4.dp)
                .background(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    shape = RoundedCornerShape(2.dp)
                )
        )
    }
}