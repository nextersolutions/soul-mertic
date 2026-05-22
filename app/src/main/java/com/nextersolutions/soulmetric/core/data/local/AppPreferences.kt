package com.nextersolutions.soulmetric.core.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "soulmetric_prefs")

@Singleton
class AppPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val KEY_LAST_REFRESH = stringPreferencesKey("last_network_refresh")
    }

    val lastNetworkRefresh: Flow<String?> = context.dataStore.data
        .map { prefs -> prefs[KEY_LAST_REFRESH] }

    suspend fun setLastNetworkRefresh(isoTimestamp: String) {
        context.dataStore.edit { prefs -> prefs[KEY_LAST_REFRESH] = isoTimestamp }
    }
}
