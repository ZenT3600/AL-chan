package it.matteoleggio.alchan.ui.profile.hated

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import it.matteoleggio.alchan.data.network.Resource
import it.matteoleggio.alchan.data.repository.OtherUserRepository
import it.matteoleggio.alchan.data.repository.UserRepository
import it.matteoleggio.alchan.data.response.HatedCharacter
import it.matteoleggio.alchan.helper.enums.BrowsePage
import it.matteoleggio.alchan.helper.pojo.FavoriteItem

class HatedViewModel(private val userRepository: UserRepository,
                     private val otherUserRepository: OtherUserRepository,
                     val gson: Gson) : ViewModel() {

    var otherUserId: Int? = null

    var animePage = 1
    var animeHasNextPage = true
    var animeIsInit = false
    var animeList = ArrayList<HatedCharacter>()

    var mangaPage = 1
    var mangaHasNextPage = true
    var mangaIsInit = false
    var mangaList = ArrayList<HatedCharacter>()

    var charactersPage = 1
    var charactersHasNextPage = true
    var charactersIsInit = false
    var charactersList = ArrayList<HatedCharacter>()

    var staffsPage = 1
    var staffsHasNextPage = true
    var staffsIsInit = false
    var staffsList = ArrayList<HatedCharacter>()

    var studiosPage = 1
    var studiosHasNextPage = true
    var studiosIsInit = false
    var studiosList = ArrayList<HatedCharacter>()

    var hatedPageArray = arrayOf(
        BrowsePage.ANIME.name, BrowsePage.MANGA.name, BrowsePage.CHARACTER.name, BrowsePage.STAFF.name, BrowsePage.STUDIO.name
    )

    fun getFavoriteData(hatedPage: BrowsePage): String? {
        return when (hatedPage) {
            BrowsePage.ANIME -> gson.toJson(animeList)
            BrowsePage.MANGA -> gson.toJson(mangaList)
            BrowsePage.CHARACTER -> gson.toJson(charactersList)
            BrowsePage.STAFF -> gson.toJson(staffsList)
            BrowsePage.STUDIO -> gson.toJson(studiosList)
            else -> null
        }
    }

    fun getMixedList(): ArrayList<HatedCharacter> {
        val mixedList = ArrayList<HatedCharacter>()
        if (!animeList.isNullOrEmpty()) {
            mixedList.addAll(animeList)
        }
        if (!mangaList.isNullOrEmpty()) {
            mixedList.addAll(mangaList)
        }
        if (!charactersList.isNullOrEmpty()) {
            mixedList.addAll(charactersList)
        }
        if (!staffsList.isNullOrEmpty()) {
            mixedList.addAll(staffsList)
        }
        return mixedList
    }
}