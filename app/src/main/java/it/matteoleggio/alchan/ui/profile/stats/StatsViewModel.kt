package it.matteoleggio.alchan.ui.profile.stats

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import it.matteoleggio.alchan.data.network.Resource
import it.matteoleggio.alchan.data.repository.AppSettingsRepository
import it.matteoleggio.alchan.data.repository.OtherUserRepository
import it.matteoleggio.alchan.data.repository.UserRepository
import type.ScoreFormat

class StatsViewModel(private val userRepository: UserRepository,
                     private val otherUserRepository: OtherUserRepository,
                     private val appSettingsRepository: AppSettingsRepository,
                     val gson: Gson
) : ViewModel() {

    var otherUserId: Int? = null

    var userStats: UserStatisticsQuery.Statistics? = null
    var scoreFormat: ScoreFormat? = null

    val userStatisticsResponse by lazy {
        userRepository.userStatisticsResponse
    }

    val otherUserStatisticsResponse by lazy {
        otherUserRepository.userStatisticsResponse
    }

    val showStatsAutomatically: Boolean
        get() = appSettingsRepository.appSettings.showStatsAutomatically != false

    fun getStatistics() {
        if (otherUserId != null) {
            otherUserRepository.getStatistics(otherUserId!!)
        } else {
            userRepository.getStatistics()
        }
    }

    fun getStatisticObserver(): LiveData<Resource<UserStatisticsQuery.Data>> {
        return if (otherUserId != null) {
            otherUserStatisticsResponse
        } else {
            userStatisticsResponse
        }
    }
}