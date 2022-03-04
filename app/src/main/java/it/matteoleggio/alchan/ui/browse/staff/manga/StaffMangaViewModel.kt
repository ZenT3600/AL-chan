package it.matteoleggio.alchan.ui.browse.staff.manga

import androidx.lifecycle.ViewModel
import it.matteoleggio.alchan.R
import it.matteoleggio.alchan.data.repository.AppSettingsRepository
import it.matteoleggio.alchan.data.repository.BrowseRepository
import it.matteoleggio.alchan.helper.pojo.StaffMedia
import type.MediaSort

class StaffMangaViewModel(private val browseRepository: BrowseRepository,
                          private val appSettingsRepository: AppSettingsRepository
) : ViewModel() {

    var staffId: Int? = null
    var page = 1
    var hasNextPage = true

    var isInit = false
    var staffMedia = ArrayList<StaffMedia?>()

    var sortBy: MediaSort = appSettingsRepository.userPreferences.sortStaffManga ?: MediaSort.POPULARITY_DESC
    var onlyShowOnList: Boolean = false

    val mediaSortArray = arrayOf(
        R.string.newest,
        R.string.oldest,
        R.string.title_romaji,
        R.string.title_english,
        R.string.title_native,
        R.string.highest_score,
        R.string.lowest_score,
        R.string.most_popular,
        R.string.least_popular,
        R.string.most_favorite,
        R.string.least_favorite
    )

    var mediaSortList = arrayListOf(
        MediaSort.START_DATE_DESC,
        MediaSort.START_DATE,
        MediaSort.TITLE_ROMAJI,
        MediaSort.TITLE_ENGLISH,
        MediaSort.TITLE_NATIVE,
        MediaSort.SCORE_DESC,
        MediaSort.SCORE,
        MediaSort.POPULARITY_DESC,
        MediaSort.POPULARITY,
        MediaSort.FAVOURITES_DESC,
        MediaSort.FAVOURITES
    )

    val staffMediaData by lazy {
        browseRepository.staffMangaData
    }

    fun getStaffMedia(getFromBeginning: Boolean = false) {
        if (getFromBeginning) {
            page = 1
            hasNextPage = true
            staffMedia.clear()
        }

        if (hasNextPage && staffId != null) browseRepository.getStaffManga(staffId!!, page, sortBy, if (onlyShowOnList) true else null)
    }

    fun changeSortMedia(selectedSort: MediaSort) {
        sortBy = selectedSort

        val savedUserPreferences = appSettingsRepository.userPreferences
        savedUserPreferences.sortStaffManga = sortBy
        appSettingsRepository.setUserPreferences(savedUserPreferences)
    }
}