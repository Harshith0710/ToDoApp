package com.practice.todo

import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE tasks ADD COLUMN description TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE tasks ADD COLUMN endTime TEXT")
        db.execSQL("ALTER TABLE tasks ADD COLUMN importance TEXT NOT NULL DEFAULT normal")
    }
}

val databaseModule = module{
    single {
        Room.databaseBuilder(
            androidContext(),
            TaskDatabase::class.java,
            "task_database"
        ).addMigrations(MIGRATION_1_2).build()
    }
    single<TaskDao> {
        get<TaskDatabase>().taskDao()
    }
    viewModel {
        TaskViewModel(get())
    }
    viewModel {
        FocusTimerViewModel()
    }
}