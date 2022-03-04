package it.matteoleggio.alchan.ui.auth

import androidx.lifecycle.ViewModel
import it.matteoleggio.alchan.data.repository.AppSettingsRepository
import it.matteoleggio.alchan.data.repository.AuthRepository
import it.matteoleggio.alchan.data.repository.InfoRepository
import it.matteoleggio.alchan.helper.enums.AppColorTheme
import it.matteoleggio.alchan.helper.pojo.AppSettings

class SplashViewModel(private val authRepository: AuthRepository,
                      private val infoRepository: InfoRepository,
                      private val appSettingsRepository: AppSettingsRepository) : ViewModel() {

    val isLoggedIn: Boolean
        get() = authRepository.isLoggedIn

    val lastAnnouncementId: Int?
        get() = infoRepository.lastAnnouncementId

    val appSettings: AppSettings
        get() = appSettingsRepository.appSettings

    val announcementResponse by lazy {
        infoRepository.announcementResponse
    }

    fun getAnnouncement() {
        infoRepository.getAnnouncement()
    }

    fun setNeverShowAgain(id: Int) {
        infoRepository.setLastAnnouncementId(id)
    }

    fun setDefaultAppSetting(isLowOnMemory: Boolean) {
        appSettingsRepository.setDefaultSetting(isLowOnMemory)
    }
}