package it.matteoleggio.alchan.helper.pojo

import it.matteoleggio.alchan.helper.enums.ListType

class ListStyle(
    var listType: ListType? = ListType.LINEAR,
    var primaryColor: String? = null,
    var secondaryColor: String? = null,
    var textColor: String? = null,
    var cardColor: String? = null,
    var toolbarColor: String? = null,
    var backgroundColor: String? = null,
    var floatingButtonColor: String? = null,
    var floatingIconColor: String? = null,
    var backgroundImage: Boolean? = false,
    var longPressViewDetail: Boolean? = true,
    var hideMangaVolume: Boolean? = false,
    var hideMangaChapter: Boolean? = false,
    var hideNovelVolume: Boolean? = false,
    var hideNovelChapter: Boolean? = false,
    var showNotesIndicator: Boolean? = false,
    var showPriorityIndicator: Boolean? = false,
    var hideMediaFormat: Boolean? = false,
    var hideScoreWhenNotScored: Boolean? = false,
    var hideAiringIndicator: Boolean? = false
)