package it.matteoleggio.alchan.ui.animelist.editor

import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import it.matteoleggio.alchan.data.repository.MediaListRepository
import it.matteoleggio.alchan.data.repository.UserRepository
import it.matteoleggio.alchan.data.response.FuzzyDate
import it.matteoleggio.alchan.data.response.User
import it.matteoleggio.alchan.helper.pojo.AdvancedScoresItem
import it.matteoleggio.alchan.helper.pojo.CustomListsItem
import type.MediaListStatus
import type.MediaType
import type.ScoreFormat

class AnimeListEditorViewModel(private val mediaListRepository: MediaListRepository,
                               private val userRepository: UserRepository,
                               val gson: Gson
) : ViewModel() {

    var isInit = false
    var entryId: Int? = null

    var mediaId: Int? = null
    var mediaTitle: String? = null
    var mediaEpisode: Int? = null

    var customListsList = ArrayList<CustomListsItem>()
    var advancedScoresList = ArrayList<AdvancedScoresItem>()

    var isFavourite: Boolean? = null
    var selectedStatus: MediaListStatus? = null
    var selectedScore: Double? = null
    var selectedAdvancedScores = ArrayList<Double>()
    var selectedProgress: Int? = null
    var selectedStartDate: FuzzyDate? = null
    var selectedFinishDate: FuzzyDate? = null
    var selectedRewatches: Int? = null
    var selectedNotes: String? = null
    var selectedCustomLists = ArrayList<String>()
    var selectedHidden: Boolean? = null
    var selectedPrivate: Boolean? = null
    var selectedPriority: Int? = null

    var isCustomListsModified = false

    val animeListDataDetailResponse by lazy {
        mediaListRepository.mediaListDataDetailResponse
    }

    val updateAnimeListEntryDetailResponse by lazy {
        mediaListRepository.updateMediaListEntryDetailResponse
    }

    val deleteMediaListEntryResponse by lazy {
        mediaListRepository.deleteMediaListEntryResponse
    }

    val toggleFavouriteResponse by lazy {
        userRepository.toggleFavouriteResponse
    }

    val viewerData: User?
        get() = userRepository.currentUser

    val scoreFormat: ScoreFormat
        get() = userRepository.currentUser?.mediaListOptions?.scoreFormat ?: ScoreFormat.POINT_100

    val advancedScoringList: ArrayList<String?>
        get() = if (userRepository.currentUser?.mediaListOptions?.animeList?.advancedScoringEnabled == true) {
            ArrayList(userRepository.currentUser?.mediaListOptions?.animeList?.advancedScoring!!)
        } else {
            ArrayList()
        }

    val savedCustomListsList: ArrayList<String?>
        get() = if (userRepository.currentUser?.mediaListOptions?.animeList?.customLists.isNullOrEmpty()) {
            ArrayList()
        } else {
            ArrayList(userRepository.currentUser?.mediaListOptions?.animeList?.customLists!!)
        }

    val mediaListStatusList = listOf(
        MediaListStatus.CURRENT, MediaListStatus.REPEATING, MediaListStatus.COMPLETED, MediaListStatus.PAUSED, MediaListStatus.DROPPED, MediaListStatus.PLANNING
    )

    fun retrieveAnimeListDataDetail() {
        if (!isInit && entryId != null && entryId != 0) {
            mediaListRepository.retrieveAnimeListDataDetail(entryId!!)
        }
    }

    fun updateAnimeListEntryDetail() {
        if (
            selectedStatus != null &&
            selectedScore != null &&
            selectedProgress != null &&
            selectedRewatches != null &&
            selectedPrivate != null &&
            selectedHidden != null
        ) {
            if (entryId != null && entryId != 0) {
                mediaListRepository.updateAnimeList(
                    entryId!!,
                    selectedStatus!!,
                    selectedScore!!,
                    selectedProgress!!,
                    selectedRewatches!!,
                    selectedPrivate!!,
                    selectedNotes,
                    selectedHidden!!,
                    selectedCustomLists,
                    selectedAdvancedScores,
                    selectedStartDate,
                    selectedFinishDate,
                    selectedPriority,
                    isCustomListsModified
                )
            } else if (mediaId != null && mediaId != 0) {
                mediaListRepository.addAnimeList(
                    mediaId!!,
                    selectedStatus!!,
                    selectedScore!!,
                    selectedProgress!!,
                    selectedRewatches!!,
                    selectedPrivate!!,
                    selectedNotes,
                    selectedHidden!!,
                    selectedCustomLists,
                    selectedAdvancedScores,
                    selectedStartDate,
                    selectedFinishDate,
                    selectedPriority
                )
            }
        }
    }

    fun deleteAnimeListEntry() {
        if (entryId == null) return
        mediaListRepository.deleteMediaList(entryId!!, MediaType.ANIME)
    }

    fun updateFavourite() {
        userRepository.toggleFavourite(
            animeListDataDetailResponse.value?.data?.media?.id ?: mediaId, null, null, null, null
        )
    }

    fun getPriorityLabel(): String {
        return when (selectedPriority) {
            0 -> "No Priority"
            1 -> "Very Low"
            2 -> "Low"
            3 -> "Medium"
            4 -> "High"
            5 -> "Very High"
            else -> "No Priority"
        }
    }
}