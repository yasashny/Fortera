package com.yasashny.fortera.core.database.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.yasashny.fortera.core.database.secure.SecureStorage
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

private val Context.appDataStore: DataStore<Preferences> by preferencesDataStore(name = "app_preferences")

val databaseModule = module {

    single<DataStore<Preferences>> { androidContext().appDataStore }

    single { SecureStorage(androidContext(), "fortera_secrets") }
}
