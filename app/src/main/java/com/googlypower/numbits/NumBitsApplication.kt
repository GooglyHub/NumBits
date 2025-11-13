package com.googlypower.numbits

import android.app.Application
import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.googlypower.numbits.data.SavedProgressRepository

private const val SAVED_PROGRESS_NAME = "saved_progress"

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = SAVED_PROGRESS_NAME)

class NumBitsApplication: Application() {
    lateinit var savedProgressRepository: SavedProgressRepository

    override fun onCreate() {
        super.onCreate()
        savedProgressRepository = SavedProgressRepository(dataStore)
    }
}