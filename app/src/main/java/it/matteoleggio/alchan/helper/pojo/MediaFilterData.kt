package it.matteoleggio.alchan.helper.pojo

import it.matteoleggio.alchan.helper.enums.CountryCode
import it.matteoleggio.alchan.helper.enums.MediaListSort
import type.*

class MediaFilterData(
    var selectedMediaListSort: MediaListSort? = null,
    var selectedMediaListOrderByDescending: Boolean? = null,
    var selectedMediaSort: MediaSort? = null,
    var selectedFormats: ArrayList<MediaFormat>? = null,
    var selectedStatuses: ArrayList<MediaStatus>? = null,
    var selectedSources: ArrayList<MediaSource>? = null,
    var selectedCountry: CountryCode? = null,
    var selectedSeason: MediaSeason? = null,
    var selectedYear: FilterRange? = null,
    var selectedGenres: ArrayList<String>? = null,
    var selectedExcludedGenres: ArrayList<String>? = null,
    var selectedTagNames: ArrayList<String>? = null,
    var selectedExcludedTagNames: ArrayList<String>? = null,
    var selectedMinimumTagRank: Int? = null,
    var selectedLicensed: ArrayList<String>? = null,
    var selectedEpisodes: FilterRange? = null,
    var selectedDuration: FilterRange? = null,
    var selectedChapters: FilterRange? = null,
    var selectedVolumes: FilterRange? = null,
    var selectedAverageScore: FilterRange? = null,
    var selectedPopularity: FilterRange? = null,
    var selectedIsAdult: Boolean? = null,
    var selectedOnList: Boolean? = null,
    var selectedUserScore: FilterRange? = null,
    var selectedUserStartYear: FilterRange? = null,
    var selectedUserFinishYear: FilterRange? = null,
    var selectedUserPriority: FilterRange? = null
)