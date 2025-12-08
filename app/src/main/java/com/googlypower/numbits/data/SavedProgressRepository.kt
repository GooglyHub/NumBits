package com.googlypower.numbits.data

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

class SavedProgressRepository(
    private val dataStore: DataStore<Preferences>
) {
    private companion object {
        const val TAG = "SavedProgressRepo"

        val EASY_ID = intPreferencesKey("easy_id")
        val EASY_SOLVED = booleanPreferencesKey("easy_solved")
        val EASY_TIME_TO_SOLVE = stringPreferencesKey("easy_time_to_solve")
        val EASY_START_TIME = longPreferencesKey("easy_start_time")
        val EASY_LHS = stringPreferencesKey("easy_lhs")
        val EASY_RHS = stringPreferencesKey("easy_rhs")
        val EASY_RESERVES = stringPreferencesKey("easy_reserves")

        val MEDIUM_ID = intPreferencesKey("medium_id")
        val MEDIUM_SOLVED = booleanPreferencesKey("medium_solved")
        val MEDIUM_TIME_TO_SOLVE = stringPreferencesKey("medium_time_to_solve")
        val MEDIUM_START_TIME = longPreferencesKey("medium_start_time")
        val MEDIUM_LHS = stringPreferencesKey("medium_lhs")
        val MEDIUM_RHS = stringPreferencesKey("medium_rhs")
        val MEDIUM_RESERVES = stringPreferencesKey("medium_reserves")

        val HARD_ID = intPreferencesKey("hard_id")
        val HARD_SOLVED = booleanPreferencesKey("hard_solved")
        val HARD_TIME_TO_SOLVE = stringPreferencesKey("hard_time_to_solve")
        val HARD_START_TIME = longPreferencesKey("hard_start_time")
        val HARD_LHS = stringPreferencesKey("hard_lhs")
        val HARD_RHS = stringPreferencesKey("hard_rhs")
        val HARD_RESERVES = stringPreferencesKey("hard_reserves")
    }

    val easyProgress: Flow<SavedProgress> = dataStore.data
        .catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading preferences for easyProgress", it)
                emit(emptyPreferences())
            } else {
                throw it
            }
        }
        .map {
            preferences ->
            SavedProgress(
                id = preferences[EASY_ID] ?: 0,
                solved = preferences[EASY_SOLVED] ?: false,
                timeToSolve = preferences[EASY_TIME_TO_SOLVE] ?: "",
                startTime = preferences[EASY_START_TIME] ?: 0,
                lhs = preferences[EASY_LHS] ?: "",
                rhs = preferences[EASY_RHS] ?: "",
                reserves = preferences[EASY_RESERVES] ?: ""
            )
        }

    val mediumProgress: Flow<SavedProgress> = dataStore.data
        .catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading preferences for mediumProgress", it)
                emit(emptyPreferences())
            } else {
                throw it
            }
        }
        .map {
                preferences ->
            SavedProgress(
                id = preferences[MEDIUM_ID] ?: 0,
                solved = preferences[MEDIUM_SOLVED] ?: false,
                timeToSolve = preferences[MEDIUM_TIME_TO_SOLVE] ?: "",
                startTime = preferences[MEDIUM_START_TIME] ?: 0,
                lhs = preferences[MEDIUM_LHS] ?: "",
                rhs = preferences[MEDIUM_RHS] ?: "",
                reserves = preferences[MEDIUM_RESERVES] ?: ""
            )
        }

    val hardProgress: Flow<SavedProgress> = dataStore.data
        .catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading preferences for hardProgress", it)
                emit(emptyPreferences())
            } else {
                throw it
            }
        }
        .map {
                preferences ->
            SavedProgress(
                id = preferences[HARD_ID] ?: 0,
                solved = preferences[HARD_SOLVED] ?: false,
                timeToSolve = preferences[HARD_TIME_TO_SOLVE] ?: "",
                startTime = preferences[HARD_START_TIME] ?: 0,
                lhs = preferences[HARD_LHS] ?: "",
                rhs = preferences[HARD_RHS] ?: "",
                reserves = preferences[HARD_RESERVES] ?: ""
            )
        }

    suspend fun saveEasyProgress(
        progress: SavedProgress
    ) {
        dataStore.edit { preferences ->
            preferences[EASY_ID] = progress.id
            preferences[EASY_SOLVED] = progress.solved
            preferences[EASY_TIME_TO_SOLVE] = progress.timeToSolve
            preferences[EASY_START_TIME] = progress.startTime
            preferences[EASY_LHS] = progress.lhs
            preferences[EASY_RHS] = progress.rhs
            preferences[EASY_RESERVES] = progress.reserves
        }
    }

    suspend fun saveMediumProgress(
        progress: SavedProgress
    ) {
        dataStore.edit { preferences ->
            preferences[MEDIUM_ID] = progress.id
            preferences[MEDIUM_SOLVED] = progress.solved
            preferences[MEDIUM_TIME_TO_SOLVE] = progress.timeToSolve
            preferences[MEDIUM_START_TIME] = progress.startTime
            preferences[MEDIUM_LHS] = progress.lhs
            preferences[MEDIUM_RHS] = progress.rhs
            preferences[MEDIUM_RESERVES] = progress.reserves
        }
    }

    suspend fun saveHardProgress(
        progress: SavedProgress
    ) {
        dataStore.edit { preferences ->
            preferences[HARD_ID] = progress.id
            preferences[HARD_SOLVED] = progress.solved
            preferences[HARD_TIME_TO_SOLVE] = progress.timeToSolve
            preferences[HARD_START_TIME] = progress.startTime
            preferences[HARD_LHS] = progress.lhs
            preferences[HARD_RHS] = progress.rhs
            preferences[HARD_RESERVES] = progress.reserves
        }
    }
}