package com.zen.alchan

import android.app.Application
import com.google.gson.GsonBuilder
import com.zen.alchan.data.datasource.*
import com.zen.alchan.data.localstorage.DefaultJsonStorageHandler
import com.zen.alchan.data.localstorage.DefaultSharedPreferencesHandler
import com.zen.alchan.data.localstorage.JsonStorageHandler
import com.zen.alchan.data.localstorage.SharedPreferencesHandler
import com.zen.alchan.data.manager.ContentManager
import com.zen.alchan.data.manager.DefaultContentManager
import com.zen.alchan.data.manager.UserManager
import com.zen.alchan.data.manager.DefaultUserManager
import com.zen.alchan.data.network.apollo.AniListApolloHandler
import com.zen.alchan.data.network.apollo.ApolloHandler
import com.zen.alchan.data.network.header.AniListHeaderInterceptorImpl
import com.zen.alchan.data.network.header.HeaderInterceptor
import com.zen.alchan.data.repository.*
import com.zen.alchan.helper.Constant
import com.zen.alchan.ui.base.BaseActivityViewModel
import com.zen.alchan.ui.bio.BioViewModel
import com.zen.alchan.ui.home.HomeViewModel
import com.zen.alchan.ui.landing.LandingViewModel
import com.zen.alchan.ui.login.LoginViewModel
import com.zen.alchan.ui.main.MainViewModel
import com.zen.alchan.ui.main.SharedMainViewModel
import com.zen.alchan.ui.medialist.MediaListViewModel
import com.zen.alchan.ui.profile.ProfileViewModel
import com.zen.alchan.ui.profile.SharedProfileViewModel
import com.zen.alchan.ui.reorder.ReorderViewModel
import com.zen.alchan.ui.reorder.SharedReorderViewModel
import com.zen.alchan.ui.review.ReviewViewModel
import com.zen.alchan.ui.settings.SettingsViewModel
import com.zen.alchan.ui.settings.account.AccountSettingsViewModel
import com.zen.alchan.ui.settings.anilist.AniListSettingsViewModel
import com.zen.alchan.ui.settings.app.AppSettingsViewModel
import com.zen.alchan.ui.settings.list.ListSettingsViewModel
import com.zen.alchan.ui.settings.notifications.NotificationsSettingsViewModel
import com.zen.alchan.ui.social.SocialViewModel
import com.zen.alchan.ui.splash.SplashViewModel

import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.dsl.module

class ALchanApplication : Application() {

    private val appModules = module {
        val gson = GsonBuilder()
            .setLenient()
            .serializeSpecialFloatingPointValues()
            .create()

        // local storage
        single<SharedPreferencesHandler> {
            DefaultSharedPreferencesHandler(
                this@ALchanApplication.applicationContext,
                Constant.SHARED_PREFERENCES_NAME,
                gson
            )
        }

        single<JsonStorageHandler> {
            DefaultJsonStorageHandler(
                this@ALchanApplication,
                gson
            )
        }

        // local storage manager
        single<UserManager> { DefaultUserManager(get(), get()) }
        single<ContentManager> { DefaultContentManager(get()) }

        // network
        single<HeaderInterceptor> { AniListHeaderInterceptorImpl(get()) }
        single<ApolloHandler> { AniListApolloHandler(get(), Constant.ANILIST_API_BASE_URL, Constant.ANILIST_API_VERSION) }

        // data source
        single<ContentDataSource> { DefaultContentDataSource(get()) }
        single<UserDataSource> { DefaultUserDataSource(get()) }
        single<MediaListDataSource> { DefaultMediaListDataSource(get()) }

        // repository
        single<ContentRepository> { DefaultContentRepository(get(), get()) }
        single<UserRepository> { DefaultUserRepository(get(), get()) }
        single<MediaListRepository> { DefaultMediaListRepository(get()) }

        // view model
        viewModel { BaseActivityViewModel(get()) }

        viewModel { SplashViewModel(get()) }
        viewModel { LandingViewModel() }
        viewModel { LoginViewModel(get()) }

        viewModel { SharedMainViewModel() }
        viewModel { MainViewModel(get()) }

        viewModel { HomeViewModel(get(), get()) }

        viewModel { MediaListViewModel(get(), get()) }

        viewModel { SocialViewModel() }

        viewModel { ProfileViewModel(get()) }
        viewModel { SharedProfileViewModel(get()) }
        viewModel { BioViewModel() }
        viewModel { ReviewViewModel() }

        viewModel { SettingsViewModel() }
        viewModel { AppSettingsViewModel(get()) }
        viewModel { AniListSettingsViewModel(get()) }
        viewModel { ListSettingsViewModel(get()) }
        viewModel { NotificationsSettingsViewModel(get()) }
        viewModel { AccountSettingsViewModel(get()) }

        viewModel { ReorderViewModel() }
        viewModel { SharedReorderViewModel() }
    }

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@ALchanApplication)
            modules(appModules)
        }
    }
}