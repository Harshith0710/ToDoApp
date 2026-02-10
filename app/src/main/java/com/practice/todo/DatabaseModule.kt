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

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS focus_sessions (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                startTime TEXT NOT NULL,
                endTime TEXT NOT NULL,
                durationSeconds INTEGER NOT NULL,
                mode TEXT NOT NULL
            )
        """)
    }
}

val databaseModule = module{
    single {
        Room.databaseBuilder(
            androidContext(),
            TaskDatabase::class.java,
            "task_database"
        )
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
            .build()
    }
    single<TaskDao> {
        get<TaskDatabase>().taskDao()
    }
    single<FocusSessionDao> {
        get<TaskDatabase>().focusSessionDao()
    }
    viewModel {
        TaskViewModel(get())
    }
    viewModel {
        FocusTimerViewModel(get())
    }
    viewModel {
        StatsViewModel(get())
    }
}