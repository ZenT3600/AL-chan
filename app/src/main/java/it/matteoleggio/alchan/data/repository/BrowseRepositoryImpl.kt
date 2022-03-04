package it.matteoleggio.alchan.data.repository

import android.annotation.SuppressLint
import androidx.lifecycle.LiveData
import com.apollographql.apollo.api.Response
import it.matteoleggio.alchan.data.datasource.BrowseDataSource
import it.matteoleggio.alchan.data.network.Resource
import it.matteoleggio.alchan.helper.libs.SingleLiveEvent
import it.matteoleggio.alchan.helper.utils.AndroidUtility
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import type.MediaSort
import type.MediaType

class BrowseRepositoryImpl(private val browseDataSource: BrowseDataSource) : BrowseRepository {

    private val _characterData = SingleLiveEvent<Resource<CharacterQuery.Data>>()
    override val characterData: LiveData<Resource<CharacterQuery.Data>>
        get() = _characterData

    private val _characterMediaData = SingleLiveEvent<Resource<CharacterMediaConnectionQuery.Data>>()
    override val characterMediaData: LiveData<Resource<CharacterMediaConnectionQuery.Data>>
        get() = _characterMediaData

    private val _characterIsFavoriteData = SingleLiveEvent<Resource<CharacterIsFavoriteQuery.Data>>()
    override val characterIsFavoriteData: LiveData<Resource<CharacterIsFavoriteQuery.Data>>
        get() = _characterIsFavoriteData

    private val _staffData = SingleLiveEvent<Resource<StaffQuery.Data>>()
    override val staffData: LiveData<Resource<StaffQuery.Data>>
        get() = _staffData

    private val _staffBioData = SingleLiveEvent<Resource<StaffBioQuery.Data>>()
    override val staffBioData: LiveData<Resource<StaffBioQuery.Data>>
        get() = _staffBioData

    private val _staffCharacterData = SingleLiveEvent<Resource<StaffCharacterConnectionQuery.Data>>()
    override val staffCharacterData: LiveData<Resource<StaffCharacterConnectionQuery.Data>>
        get() = _staffCharacterData

    private val _staffMediaCharacterData = SingleLiveEvent<Resource<StaffMediaCharacterConnectionQuery.Data>>()
    override val staffMediaCharacterData: LiveData<Resource<StaffMediaCharacterConnectionQuery.Data>>
        get() = _staffMediaCharacterData

    private val _staffAnimeData = SingleLiveEvent<Resource<StaffMediaConnectionQuery.Data>>()
    override val staffAnimeData: LiveData<Resource<StaffMediaConnectionQuery.Data>>
        get() = _staffAnimeData

    private val _staffMangaData = SingleLiveEvent<Resource<StaffMediaConnectionQuery.Data>>()
    override val staffMangaData: LiveData<Resource<StaffMediaConnectionQuery.Data>>
        get() = _staffMangaData

    private val _staffIsFavoriteData = SingleLiveEvent<Resource<StaffIsFavoriteQuery.Data>>()
    override val staffIsFavoriteData: LiveData<Resource<StaffIsFavoriteQuery.Data>>
        get() = _staffIsFavoriteData

    private val _studioData = SingleLiveEvent<Resource<StudioQuery.Data>>()
    override val studioData: LiveData<Resource<StudioQuery.Data>>
        get() = _studioData

    private val _studioMediaData = SingleLiveEvent<Resource<StudioMediaConnectionQuery.Data>>()
    override val studioMediaData: LiveData<Resource<StudioMediaConnectionQuery.Data>>
        get() = _studioMediaData

    private val _studioIsFavoriteData = SingleLiveEvent<Resource<StudioIsFavoriteQuery.Data>>()
    override val studioIsFavoriteData: LiveData<Resource<StudioIsFavoriteQuery.Data>>
        get() = _studioIsFavoriteData

    private val _idFromNameData = SingleLiveEvent<Resource<IdFromNameQuery.Data>>()
    override val idFromNameData: LiveData<Resource<IdFromNameQuery.Data>>
        get() = _idFromNameData

    @SuppressLint("CheckResult")
    override fun getCharacter(id: Int) {
        _characterData.postValue(Resource.Loading())
        browseDataSource.getCharacter(id).subscribeWith(AndroidUtility.rxApolloCallback(_characterData))
    }

    @SuppressLint("CheckResult")
    override fun getCharacterMedia(id: Int, page: Int) {
        browseDataSource.getCharacterMedia(id, page).subscribeWith(AndroidUtility.rxApolloCallback(_characterMediaData))
    }

    @SuppressLint("CheckResult")
    override fun checkCharacterIsFavorite(id: Int) {
        browseDataSource.checkCharacterIsFavorite(id).subscribeWith(AndroidUtility.rxApolloCallback(_characterIsFavoriteData))
    }

    @SuppressLint("CheckResult")
    override fun getStaff(id: Int) {
        _staffData.postValue(Resource.Loading())
        browseDataSource.getStaff(id).subscribeWith(AndroidUtility.rxApolloCallback(_staffData))
    }

    @SuppressLint("CheckResult")
    override fun getStaffBio(id: Int) {
        _staffBioData.postValue(Resource.Loading())
        browseDataSource.getStaffBio(id).subscribeWith(AndroidUtility.rxApolloCallback(_staffBioData))
    }

    @SuppressLint("CheckResult")
    override fun getStaffCharacter(id: Int, page: Int) {
        browseDataSource.getStaffCharacter(id, page).subscribeWith(AndroidUtility.rxApolloCallback(_staffCharacterData))
    }

    @SuppressLint("CheckResult")
    override fun getStaffMediaCharacter(id: Int, page: Int, sort: MediaSort, onList: Boolean?) {
        browseDataSource.getStaffMediaCharacter(id, page, listOf(sort), onList).subscribeWith(AndroidUtility.rxApolloCallback(_staffMediaCharacterData))
    }

    @SuppressLint("CheckResult")
    override fun getStaffAnime(id: Int, page: Int, sort: MediaSort, onList: Boolean?) {
        browseDataSource.getStaffMedia(id, MediaType.ANIME, page, listOf(sort), onList).subscribeWith(AndroidUtility.rxApolloCallback(_staffAnimeData))
    }

    @SuppressLint("CheckResult")
    override fun getStaffManga(id: Int, page: Int, sort: MediaSort, onList: Boolean?) {
        browseDataSource.getStaffMedia(id, MediaType.MANGA, page, listOf(sort), onList).subscribeWith(AndroidUtility.rxApolloCallback(_staffMangaData))
    }

    @SuppressLint("CheckResult")
    override fun checkStaffIsFavorite(id: Int) {
        browseDataSource.checkStaffIsFavorite(id).subscribeWith(AndroidUtility.rxApolloCallback(_staffIsFavoriteData))
    }

    @SuppressLint("CheckResult")
    override fun getStudio(id: Int) {
        _studioData.postValue(Resource.Loading())
        browseDataSource.getStudio(id).subscribeWith(AndroidUtility.rxApolloCallback(_studioData))
    }

    @SuppressLint("CheckResult")
    override fun getStudioMedia(id: Int, page: Int, sort: MediaSort) {
        browseDataSource.getStudioMedia(id, page, sort).subscribeWith(AndroidUtility.rxApolloCallback(_studioMediaData))
    }

    @SuppressLint("CheckResult")
    override fun checkStudioIsFavorite(id: Int) {
        browseDataSource.checkStudioIsFavorite(id).subscribeWith(AndroidUtility.rxApolloCallback(_studioIsFavoriteData))
    }

    @SuppressLint("CheckResult")
    override fun getIdFromName(name: String) {
        _idFromNameData.postValue(Resource.Loading())
        browseDataSource.getIdFromName(name).subscribeWith(AndroidUtility.rxApolloCallback(_idFromNameData))
    }
}