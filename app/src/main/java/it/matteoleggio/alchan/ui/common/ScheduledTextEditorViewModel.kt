package it.matteoleggio.alchan.ui.common

import androidx.lifecycle.ViewModel
import it.matteoleggio.alchan.data.repository.SocialRepository
import it.matteoleggio.alchan.helper.enums.EditorType

class ScheduledTextEditorViewModel(private val socialRepository: SocialRepository) : ViewModel() {

    val originalDate: String = ""
    var scheduleDate: String = ""
    var editorType: EditorType? = null

    var activityId: Int? = null
    var originalText: String? = null
    var recipientId: Int? = null
    var recipientName: String? = null
    var replyId: Int? = null
    var isInit = false

    val postTextActivityResponse by lazy {
        socialRepository.postTextActivityResponse
    }

    fun post(text: String) {
        socialRepository.postTextActivity(activityId, text)
    }
}