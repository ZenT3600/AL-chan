package it.matteoleggio.alchan.ui.profile.bio

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import it.matteoleggio.alchan.data.repository.AppSettingsRepository
import it.matteoleggio.alchan.data.repository.OtherUserRepository
import it.matteoleggio.alchan.data.repository.UserRepository
import io.noties.markwon.Markwon

class BioViewModel(private val userRepository: UserRepository,
                   private val otherUserRepository: OtherUserRepository,
                   private val appSettingsRepository: AppSettingsRepository
) : ViewModel() {

    var otherUserId: Int? = null

    val showBioAutomatically: Boolean
        get() = appSettingsRepository.appSettings.showBioAutomatically != false

    val viewerData by lazy {
        userRepository.viewerData
    }

    val userData by lazy {
        otherUserRepository.userData
    }
}