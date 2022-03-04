package it.matteoleggio.alchan.ui.browse.staff.voice

import type.MediaType

interface StaffVoiceListener {
    fun passSelectedCharacter(characterId: Int)
    fun passSelectedMedia(mediaId: Int, mediaType: MediaType)
}