package it.matteoleggio.alchan.data.repository

import androidx.lifecycle.LiveData
import it.matteoleggio.alchan.data.network.Resource
import it.matteoleggio.alchan.data.response.SeasonalAnime
import it.matteoleggio.alchan.helper.enums.SeasonalCategory
import it.matteoleggio.alchan.helper.pojo.MediaFilterData
import type.*

interface SearchRepository {
    val searchAnimeResponse: LiveData<Resource<SearchAnimeQuery.Data>>
    val searchMangaResponse: LiveData<Resource<SearchMangaQuery.Data>>
    val searchCharactersResponse: LiveData<Resource<SearchCharactersQuery.Data>>
    val searchStaffsResponse: LiveData<Resource<SearchStaffsQuery.Data>>
    val searchStudiosResponse: LiveData<Resource<SearchStudiosQuery.Data>>
    val searchUsersResponse: LiveData<Resource<SearchUsersQuery.Data>>

    val seasonalAnimeTvResponse: LiveData<Resource<SeasonalAnimeQuery.Data>>
    val seasonalAnimeTvData: LiveData<List<SeasonalAnime>>
    val seasonalAnimeTvShortResponse: LiveData<Resource<SeasonalAnimeQuery.Data>>
    val seasonalAnimeTvShortData: LiveData<List<SeasonalAnime>>
    val seasonalAnimeMovieResponse: LiveData<Resource<SeasonalAnimeQuery.Data>>
    val seasonalAnimeMovieData: LiveData<List<SeasonalAnime>>
    val seasonalAnimeOthersResponse: LiveData<Resource<SeasonalAnimeQuery.Data>>
    val seasonalAnimeOthersData: LiveData<List<SeasonalAnime>>

    val airingScheduleResponse: LiveData<Resource<AiringScheduleQuery.Data>>
    val filteredAiringSchedule: LiveData<List<AiringScheduleQuery.AiringSchedule>>

    fun searchAnime(
        page: Int,
        search: String,
        filterData: MediaFilterData? = null
    )

    fun searchManga(
        page: Int,
        search: String,
        filterData: MediaFilterData? = null
    )

    fun searchCharacters(page: Int, search: String?, sort: List<CharacterSort>? = null)
    fun searchStaffs(page: Int, search: String?, sort: List<StaffSort>? = null)
    fun searchStudios(page: Int, search: String?, sort: List<StudioSort>? = null)
    fun searchUsers(page: Int, search: String?, sort: List<UserSort>? = null)

    fun getSeasonalAnime(
        page: Int,
        season: MediaSeason?,
        seasonYear: Int?,
        status: MediaStatus?,
        seasonalCategory: SeasonalCategory,
        isAdult: Boolean,
        onList: Boolean?,
        sort: List<MediaSort>
    )

    fun getAiringSchedule(page: Int, airingAtGreater: Int, airingAtLesser: Int)
    fun setFilteredAiringSchedule(list: List<AiringScheduleQuery.AiringSchedule>)
}