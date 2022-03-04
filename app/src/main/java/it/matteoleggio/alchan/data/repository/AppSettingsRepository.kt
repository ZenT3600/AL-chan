package it.matteoleggio.alchan.data.repository

import androidx.lifecycle.LiveData
import it.matteoleggio.alchan.helper.enums.AppColorTheme
import it.matteoleggio.alchan.helper.pojo.AppSettings
import it.matteoleggio.alchan.helper.pojo.UserPreferences
import type.StaffLanguage

interface AppSettingsRepository {
    val appColorThemeResource: Int
    val appColorThemeLiveData: LiveData<Int>

    val appSettings: AppSettings
    val userPreferences: UserPreferences

    fun setAppSettings(appSettings: AppSettings)
    fun setDefaultSetting(isLowOnMemory: Boolean)
    fun setUserPreferences(userPreferences: UserPreferences)
    fun clearStorage()
}