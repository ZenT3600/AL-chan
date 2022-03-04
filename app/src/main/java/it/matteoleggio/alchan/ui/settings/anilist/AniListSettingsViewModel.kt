package it.matteoleggio.alchan.ui.settings.anilist

import androidx.lifecycle.ViewModel
import it.matteoleggio.alchan.data.repository.UserRepository
import type.UserTitleLanguage

class AniListSettingsViewModel(private val userRepository: UserRepository) : ViewModel() {

    val languageList = listOf(UserTitleLanguage.ROMAJI, UserTitleLanguage.ENGLISH, UserTitleLanguage.NATIVE)
    var selectedTitleLanguage: UserTitleLanguage? = null
    var isInit = false

    val viewerData by lazy {
        userRepository.viewerData
    }

    val updateAniListSettingsResponse by lazy {
        userRepository.updateAniListSettingsResponse
    }

    fun initData() {
        userRepository.getViewerData()

        if (selectedTitleLanguage == null) {
            selectedTitleLanguage = userRepository.currentUser?.options?.titleLanguage
        }
    }

    fun updateAniListSettings(adultContent: Boolean, airingNotifications: Boolean) {
        if (selectedTitleLanguage == null) {
            return
        }
        userRepository.updateAniListSettings(selectedTitleLanguage!!, adultContent, airingNotifications)
    }
}