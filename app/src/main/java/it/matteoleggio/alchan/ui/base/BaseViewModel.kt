package it.matteoleggio.alchan.ui.base

import androidx.lifecycle.ViewModel
import it.matteoleggio.alchan.data.repository.AppSettingsRepository
import it.matteoleggio.alchan.data.repository.AuthRepository
import it.matteoleggio.alchan.data.repository.UserRepository
import it.matteoleggio.alchan.helper.enums.AppColorTheme

// view model for BaseActivity
class BaseViewModel(private val appSettingsRepository: AppSettingsRepository) : ViewModel() {

    val appColorThemeResource: Int
        get() = appSettingsRepository.appColorThemeResource

    val appColorTheme: AppColorTheme?
        get() = appSettingsRepository.appSettings.appTheme
}