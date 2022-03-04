package it.matteoleggio.alchan.ui.filter

import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import it.matteoleggio.alchan.R
import it.matteoleggio.alchan.data.repository.MediaRepository
import it.matteoleggio.alchan.data.repository.UserRepository
import it.matteoleggio.alchan.data.response.MediaTagCollection
import it.matteoleggio.alchan.helper.Constant
import it.matteoleggio.alchan.helper.enums.CountryCode
import it.matteoleggio.alchan.helper.enums.MediaListSort
import it.matteoleggio.alchan.helper.pojo.FilterRange
import it.matteoleggio.alchan.helper.pojo.MediaFilterData
import it.matteoleggio.alchan.helper.replaceUnderscore
import it.matteoleggio.alchan.helper.utils.Utility
import type.*

class MediaFilterViewModel(private val userRepository: UserRepository,
                           private val mediaRepository: MediaRepository,
                           val gson: Gson
) : ViewModel() {

    lateinit var mediaType: MediaType
    var isExplore = false
    var filterData: MediaFilterData? = null
    lateinit var scoreFormat: ScoreFormat
    var currentData = MediaFilterData()

    val defaultSort: MediaListSort
        get() = when (userRepository.currentUser?.mediaListOptions?.rowOrder) {
            "title" -> MediaListSort.TITLE
            "score" -> MediaListSort.SCORE
            "updatedAt" -> MediaListSort.LAST_UPDATED
            "id" -> MediaListSort.LAST_ADDED
            else -> MediaListSort.TITLE
        }

    val defaultOrderByDescending: Boolean
        get() = when (userRepository.currentUser?.mediaListOptions?.rowOrder) {
            "title" -> false
            "score" -> true
            "updatedAt" -> true
            "id" -> true
            else -> false
        }

    val mediaListSortList = MediaListSort.values().toList()

    val mediaSortArray = arrayOf(
        R.string.newest,
        R.string.oldest,
        R.string.title_romaji,
        R.string.title_english,
        R.string.title_native,
        R.string.first_added,
        R.string.last_added,
        R.string.highest_score,
        R.string.lowest_score,
        R.string.most_popular,
        R.string.least_popular,
        R.string.most_favorite,
        R.string.least_favorite,
        R.string.trending
    )

    val mediaSortList = arrayListOf(
        MediaSort.START_DATE_DESC,
        MediaSort.START_DATE,
        MediaSort.TITLE_ROMAJI,
        MediaSort.TITLE_ENGLISH,
        MediaSort.TITLE_NATIVE,
        MediaSort.ID,
        MediaSort.ID_DESC,
        MediaSort.SCORE_DESC,
        MediaSort.SCORE,
        MediaSort.POPULARITY_DESC,
        MediaSort.POPULARITY,
        MediaSort.FAVOURITES_DESC,
        MediaSort.FAVOURITES,
        MediaSort.TRENDING_DESC
    )

    val orderByList = listOf(R.string.ascending, R.string.descending)

    private val mediaFormatList: List<MediaFormat>
        get() {
            return when (mediaType) {
                MediaType.ANIME -> ArrayList(Constant.ANIME_FORMAT_LIST)
                MediaType.MANGA -> ArrayList(Constant.MANGA_FORMAT_LIST)
                else -> ArrayList()
            }
        }

    private val mediaStatusList: List<MediaStatus>
        get() = Constant.MEDIA_STATUS_LIST

    private val mediaSourceList: List<MediaSource>
        get() {
            return when (mediaType) {
                MediaType.ANIME -> ArrayList(Constant.ANIME_SOURCE_LIST)
                MediaType.MANGA -> ArrayList(Constant.MANGA_SOURCE_LIST)
                else -> ArrayList()
            }
        }

    val mediaCountryList: List<CountryCode?>
        get() {
            val countryList = ArrayList<CountryCode?>()
            countryList.add(null)
            countryList.addAll(CountryCode.values())
            return countryList
        }

    val mediaSeasonList: List<MediaSeason?>
        get() {
            val seasonList = ArrayList<MediaSeason?>(Constant.SEASON_LIST)
            seasonList.add(0, null)
            return seasonList
        }

    val yearSeekBarMaxValue = (Utility.getCurrentYear() + 1 - Constant.FILTER_EARLIEST_YEAR).toFloat()

    val episodeSeekBarMaxValue: Float
        get() = if (mediaType == MediaType.ANIME) 150F else 500F

    val durationSeekBarMaxValue: Float
        get() = if (mediaType == MediaType.ANIME) 170F else 50F

    val mediaLicensedList: ArrayList<Pair<String, String>>
        get() {
            return when (mediaType) {
                MediaType.ANIME -> Constant.ANIME_STREAMING_SITE
                MediaType.MANGA -> Constant.MANGA_READING_SITE
                else -> Constant.ANIME_STREAMING_SITE
            }
        }

    private val genreList: List<String>
        get() = mediaRepository.genreList.filterNotNull()

    fun getMediaFormatArrayPair(): Pair<Array<String>, BooleanArray> {
        val stringArray = mediaFormatList.map { it.name.replaceUnderscore() }.toTypedArray()
        val booleanArray = BooleanArray(stringArray.size)
        currentData.selectedFormats?.forEach {
            val selectedIndex = mediaFormatList.indexOf(it)
            if (selectedIndex != -1) {
                booleanArray[selectedIndex] = true
            }
        }
        return Pair(stringArray, booleanArray)
    }

    fun passMediaFormatFilterValue(index: Int, isChecked: Boolean) {
        if (isChecked) {
            if (currentData.selectedFormats == null) {
                currentData.selectedFormats = ArrayList()
            }
            currentData.selectedFormats?.add(mediaFormatList[index])
        } else {
            currentData.selectedFormats?.remove(mediaFormatList[index])
        }
    }

    fun getMediaStatusArrayPair(): Pair<Array<String>, BooleanArray> {
        val stringArray = mediaStatusList.map { it.name.replaceUnderscore() }.toTypedArray()
        val booleanArray = BooleanArray(stringArray.size)
        currentData.selectedStatuses?.forEach {
            val selectedIndex = mediaStatusList.indexOf(it)
            if (selectedIndex != -1) {
                booleanArray[selectedIndex] = true
            }
        }
        return Pair(stringArray, booleanArray)
    }

    fun passMediaStatusFilterValue(index: Int, isChecked: Boolean) {
        if (isChecked) {
            if (currentData.selectedStatuses == null) {
                currentData.selectedStatuses = ArrayList()
            }
            currentData.selectedStatuses?.add(mediaStatusList[index])
        } else {
            currentData.selectedStatuses?.remove(mediaStatusList[index])
        }
    }

    fun getMediaSourceArrayPair(): Pair<Array<String>, BooleanArray> {
        val stringArray = mediaSourceList.map { it.name.replaceUnderscore() }.toTypedArray()
        val booleanArray = BooleanArray(stringArray.size)
        currentData.selectedSources?.forEach {
            val selectedIndex = mediaSourceList.indexOf(it)
            if (selectedIndex != -1) {
                booleanArray[selectedIndex] = true
            }
        }
        return Pair(stringArray, booleanArray)
    }

    fun passMediaSourceFilterValue(index: Int, isChecked: Boolean) {
        if (isChecked) {
            if (currentData.selectedSources == null) {
                currentData.selectedSources = ArrayList()
            }
            currentData.selectedSources?.add(mediaSourceList[index])
        } else {
            currentData.selectedSources?.remove(mediaSourceList[index])
        }
    }

    fun getMediaCountryStringArray(): Array<String> {
        return mediaCountryList.map { it?.value ?: "-" }.toTypedArray()
    }

    fun getMediaSeasonStringArray(): Array<String> {
        return mediaSeasonList.map { it?.rawValue ?: "-" }.toTypedArray()
    }

    fun getFilterRangeForFuzzyDateInt(minValue: Int, maxValue: Int): FilterRange {
        // 0101 is to set min value to 1st January
        // 1231 is to set max value to 31st December
        return FilterRange("${minValue}0101".toInt(), "${maxValue}1231".toInt())
    }

    fun getMediaLicensedArrayPair(): Pair<Array<String>, BooleanArray> {
        val stringArray = mediaLicensedList.map { it.second }.toTypedArray()
        val booleanArray = BooleanArray(stringArray.size)
        currentData.selectedLicensed?.forEach {
            val selectedIndex = mediaLicensedList.indexOf(mediaLicensedList.find { licensed -> licensed.first == it })
            if (selectedIndex != -1) {
                booleanArray[selectedIndex] = true
            }
        }
        return Pair(stringArray, booleanArray)
    }

    fun passMediaFormatLicensedValue(index: Int, isChecked: Boolean) {
        if (isChecked) {
            if (currentData.selectedLicensed == null) {
                currentData.selectedLicensed = ArrayList()
            }
            currentData.selectedLicensed?.add(mediaLicensedList[index].first)
        } else {
            currentData.selectedLicensed?.remove(mediaLicensedList[index].first)
        }
    }

    fun getMediaGenreArrayPair(): Pair<Array<String>, BooleanArray> {
        val stringArray = genreList.toTypedArray()
        val booleanArray = BooleanArray(stringArray.size)
        currentData.selectedGenres?.forEach {
            val selectedIndex = genreList.indexOf(it)
            if (selectedIndex != -1) {
                booleanArray[selectedIndex] = true
            }
        }
        return Pair(stringArray, booleanArray)
    }

    fun passMediaGenreFilterValue(index: Int, isChecked: Boolean) {
        if (isChecked) {
            if (currentData.selectedGenres == null) {
                currentData.selectedGenres = ArrayList()
            }
            currentData.selectedGenres?.add(genreList[index])
            currentData.selectedExcludedGenres?.remove(genreList[index])
        } else {
            currentData.selectedGenres?.remove(genreList[index])
        }
    }

    fun getMediaExcludedGenreArrayPair(): Pair<Array<String>, BooleanArray> {
        val stringArray = genreList.toTypedArray()
        val booleanArray = BooleanArray(stringArray.size)
        currentData.selectedExcludedGenres?.forEach {
            val selectedIndex = genreList.indexOf(it)
            if (selectedIndex != -1) {
                booleanArray[selectedIndex] = true
            }
        }
        return Pair(stringArray, booleanArray)
    }

    fun passMediaExcludedGenreFilterValue(index: Int, isChecked: Boolean) {
        if (isChecked) {
            if (currentData.selectedExcludedGenres == null) {
                currentData.selectedExcludedGenres = ArrayList()
            }
            currentData.selectedExcludedGenres?.add(genreList[index])
            currentData.selectedGenres?.remove(genreList[index])
        } else {
            currentData.selectedExcludedGenres?.remove(genreList[index])
        }
    }

    fun passMediaTagFilterValue(name: String) {
        if (currentData.selectedTagNames == null) {
            currentData.selectedTagNames = ArrayList()
        }

        if (currentData.selectedTagNames?.contains(name) == true) {
            currentData.selectedTagNames?.remove(name)
        } else {
            currentData.selectedTagNames?.add(name)
            currentData.selectedExcludedTagNames?.remove(name)
        }
    }

    fun passMediaExcludedTagFilterValue(name: String) {
        if (currentData.selectedExcludedTagNames == null) {
            currentData.selectedExcludedTagNames = ArrayList()
        }

        if (currentData.selectedExcludedTagNames?.contains(name) == true) {
            currentData.selectedExcludedTagNames?.remove(name)
        } else {
            currentData.selectedExcludedTagNames?.add(name)
            currentData.selectedTagNames?.remove(name)
        }
    }

    fun getUserScoreMaxValue(): Float {
        if (!this::scoreFormat.isInitialized) {
            return 100F
        }

        return when (scoreFormat) {
            ScoreFormat.POINT_100 -> 100F
            ScoreFormat.POINT_10_DECIMAL -> 10F
            ScoreFormat.POINT_10 -> 10F
            ScoreFormat.POINT_3 -> 3F
            ScoreFormat.POINT_5 -> 5F
            else -> 100F
        }
    }
}