package com.practice.todo

import androidx.room.Room
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val databaseModule = module{
    single {
        Room.databaseBuilder(
            androidContext(),
            TaskDatabase::class.java,
            "task_database"
        ).build()
    }
    single<TaskDao> {
        get<TaskDatabase>().taskDao()
    }
    viewModel {
        TaskViewModel(get())
    }
}