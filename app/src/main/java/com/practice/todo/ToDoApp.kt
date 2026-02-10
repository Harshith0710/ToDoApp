package com.practice.todo

import android.app.Application
import com.practice.todo.di.databaseModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class ToDoApp: Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@ToDoApp)
            modules(databaseModule)
        }
    }
}