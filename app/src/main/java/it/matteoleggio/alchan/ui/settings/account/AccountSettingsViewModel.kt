package it.matteoleggio.alchan.ui.settings.account

import androidx.lifecycle.ViewModel
import it.matteoleggio.alchan.data.repository.AppSettingsRepository

class AccountSettingsViewModel(private val appSettingsRepository: AppSettingsRepository) : ViewModel() {

    fun clearStorage() {
        appSettingsRepository.clearStorage()
    }
}