package it.matteoleggio.alchan.ui.browse.media.characters

import androidx.lifecycle.ViewModel
import it.matteoleggio.alchan.data.repository.AppSettingsRepository
import it.matteoleggio.alchan.data.repository.MediaRepository
import it.matteoleggio.alchan.helper.pojo.MediaCharacters
import type.MediaType
import type.StaffLanguage

class MediaCharactersViewModel(private val mediaRepository: MediaRepository,
                               private val appSettingsRepository: AppSettingsRepository
) : ViewModel() {

    var mediaId: Int? = null
    var mediaType: MediaType? = null
    var page = 1
    var hasNextPage = true
    var staffLanguage = appSettingsRepository.userPreferences.voiceActorLanguage

    var isInit = false
    var mediaCharacters = ArrayList<MediaCharacters?>()

    val staffLanguageArray = arrayOf(
        StaffLanguage.JAPANESE.name,
        StaffLanguage.ENGLISH.name,
        StaffLanguage.KOREAN.name,
        StaffLanguage.ITALIAN.name,
        StaffLanguage.SPANISH.name,
        StaffLanguage.PORTUGUESE.name,
        StaffLanguage.FRENCH.name,
        StaffLanguage.GERMAN.name,
        StaffLanguage.HEBREW.name,
        StaffLanguage.HUNGARIAN.name
    )

    val mediaCharactersData by lazy {
        mediaRepository.mediaCharactersData
    }

    val triggerMediaCharacter by lazy {
        mediaRepository.triggerMediaCharacter
    }

    fun getMediaCharacters() {
        if (hasNextPage && mediaId != null) mediaRepository.getMediaCharacters(mediaId!!, page)
    }

    fun changeVoiceActorLanguage(index: Int) {
        staffLanguage = StaffLanguage.valueOf(staffLanguageArray[index])

        val savedUserPreference = appSettingsRepository.userPreferences
        savedUserPreference.voiceActorLanguage = staffLanguage
        appSettingsRepository.setUserPreferences(savedUserPreference)
    }

    fun refresh() {
        mediaCharacters.clear()
        page = 1
        hasNextPage = true
        getMediaCharacters()
    }
}