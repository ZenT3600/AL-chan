package it.matteoleggio.alchan.data.localstorage

import it.matteoleggio.alchan.helper.enums.AppColorTheme
import it.matteoleggio.alchan.helper.pojo.AppSettings
import it.matteoleggio.alchan.helper.pojo.UserPreferences
import type.StaffLanguage

interface AppSettingsManager {
    val appSettings: AppSettings
    val userPreferences: UserPreferences

    fun setAppSettings(value: AppSettings)
    fun setUserPreferences(value: UserPreferences)

    fun clearStorage()
}