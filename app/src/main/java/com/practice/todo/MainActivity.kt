package com.practice.todo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.practice.todo.ui.screens.FocusTimerScreen
import com.practice.todo.ui.screens.StatsScreen
import com.practice.todo.ui.screens.TasksScreen
import com.practice.todo.ui.theme.ToDoAppTheme
import com.practice.todo.viewmodel.FocusTimerViewModel
import com.practice.todo.viewmodel.StatsViewModel
import com.practice.todo.viewmodel.TaskViewModel
import kotlinx.coroutines.launch
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
fun TodoAppInterface(
    taskViewModel: TaskViewModel = koinViewModel(),
    focusTimerViewModel: FocusTimerViewModel = koinViewModel(),
    statsViewModel: StatsViewModel = koinViewModel()
) {
    var selectedScreen by rememberSaveable { mutableIntStateOf(0) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Menu",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(16.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = null) },
                    label = { Text("Home") },
                    selected = selectedScreen == 0,
                    onClick = {
                        selectedScreen = 0
                        scope.launch { drawerState.close() }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.CheckCircle, contentDescription = null) },
                    label = { Text("Tasks") },
                    selected = selectedScreen == 1,
                    onClick = {
                        selectedScreen = 1
                        scope.launch { drawerState.close() }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Timer, contentDescription = null) },
                    label = { Text("Focus Timer") },
                    selected = selectedScreen == 2,
                    onClick = {
                        selectedScreen = 3
                        scope.launch { drawerState.close() }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
            }
        }
    ) {
        when (selectedScreen) {
            0 -> StatsScreen(statsViewModel, onMenuClick = { scope.launch { drawerState.open() } })
            1 -> TasksScreen(taskViewModel, onMenuClick = { scope.launch { drawerState.open() } })
            3 -> FocusTimerScreen(focusTimerViewModel, onMenuClick = { scope.launch { drawerState.open() } })
        }
    }
}

