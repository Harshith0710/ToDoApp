package com.practice.todo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.practice.todo.ui.theme.ToDoAppTheme

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
fun TodoAppInterface(){
    val tasks = remember { mutableStateListOf<String>() }
    var newTask by remember { mutableStateOf("") }
    Scaffold(
        modifier = Modifier
            .background(Color.LightGray)
            .windowInsetsPadding(WindowInsets.safeDrawing),
        topBar = {
            
        },
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
                            tasks.add(newTask)
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
            ListOfTasks(tasks)
        }
    }
}

@Composable
fun ListOfTasks(tasks: List<String>){
    LazyColumn {
        items(tasks){
            Text(
                text = it,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Black,
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth()
            )
        }
    }
}