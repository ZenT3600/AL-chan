package it.matteoleggio.alchan.ui.browse.staff

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import it.matteoleggio.alchan.data.repository.BrowseRepository
import it.matteoleggio.alchan.data.repository.UserRepository
import it.matteoleggio.alchan.helper.enums.StaffPage
import it.matteoleggio.alchan.helper.pojo.StaffCharacter
import it.matteoleggio.alchan.helper.pojo.StaffMedia

class StaffViewModel(private val browseRepository: BrowseRepository,
                     private val userRepository: UserRepository
) : ViewModel() {

    private val _currentSection = MutableLiveData<StaffPage>()
    val currentSection: LiveData<StaffPage>
        get() = _currentSection

    var staffId: Int? = null
    var currentStaffData: StaffQuery.Staff? = null

    val staffData by lazy {
        browseRepository.staffData
    }

    val staffIsFavoriteData by lazy {
        browseRepository.staffIsFavoriteData
    }

    val toggleFavouriteResponse by lazy {
        userRepository.toggleFavouriteResponse
    }

    fun initData() {
        getStaff()

        if (currentSection.value == null) {
            _currentSection.postValue(StaffPage.BIO)
        }
    }

    fun getStaff() {
        if (staffId != null) browseRepository.getStaff(staffId!!)
    }

    fun checkStaffIsFavorite() {
        if (staffId != null) browseRepository.checkStaffIsFavorite(staffId!!)
    }

    fun updateFavorite() {
        if (staffId != null) {
            userRepository.toggleFavourite(null, null, null, staffId, null)
        }
    }

    fun setStaffSection(section: StaffPage) {
        _currentSection.postValue(section)
    }

    fun refreshData() {
        if (staffId != null) {
            getStaff()
            checkStaffIsFavorite()
            browseRepository.getStaffBio(staffId!!)
        }
    }
}