package it.matteoleggio.alchan

import android.app.Application
import com.google.gson.GsonBuilder
import it.matteoleggio.alchan.data.datasource.*
import it.matteoleggio.alchan.data.localstorage.*
import it.matteoleggio.alchan.data.network.*
import it.matteoleggio.alchan.data.network.header.*
import it.matteoleggio.alchan.data.network.service.*
import it.matteoleggio.alchan.data.repository.*
import it.matteoleggio.alchan.helper.Constant
import it.matteoleggio.alchan.ui.main.MainViewModel
import it.matteoleggio.alchan.ui.animelist.AnimeListViewModel
import it.matteoleggio.alchan.ui.animelist.editor.AnimeListEditorViewModel
import it.matteoleggio.alchan.ui.auth.LoginViewModel
import it.matteoleggio.alchan.ui.base.BaseViewModel
import it.matteoleggio.alchan.ui.auth.SplashViewModel
import it.matteoleggio.alchan.ui.browse.activity.ActivityDetailViewModel
import it.matteoleggio.alchan.ui.browse.activity.ActivityListViewModel
import it.matteoleggio.alchan.ui.browse.character.CharacterViewModel
import it.matteoleggio.alchan.ui.common.customise.CustomiseListViewModel
import it.matteoleggio.alchan.ui.filter.MediaFilterViewModel
import it.matteoleggio.alchan.ui.home.HomeViewModel
import it.matteoleggio.alchan.ui.mangalist.MangaListViewModel
import it.matteoleggio.alchan.ui.mangalist.editor.MangaListEditorViewModel
import it.matteoleggio.alchan.ui.browse.media.MediaViewModel
import it.matteoleggio.alchan.ui.browse.media.characters.MediaCharactersViewModel
import it.matteoleggio.alchan.ui.browse.media.overview.MediaOverviewViewModel
import it.matteoleggio.alchan.ui.browse.media.reviews.MediaReviewsViewModel
import it.matteoleggio.alchan.ui.browse.media.social.MediaSocialViewModel
import it.matteoleggio.alchan.ui.browse.media.staffs.MediaStaffsViewModel
import it.matteoleggio.alchan.ui.browse.media.stats.MediaStatsViewModel
import it.matteoleggio.alchan.ui.browse.reviews.ReviewsReaderViewModel
import it.matteoleggio.alchan.ui.browse.staff.StaffViewModel
import it.matteoleggio.alchan.ui.browse.staff.anime.StaffAnimeViewModel
import it.matteoleggio.alchan.ui.browse.staff.bio.StaffBioViewModel
import it.matteoleggio.alchan.ui.browse.staff.manga.StaffMangaViewModel
import it.matteoleggio.alchan.ui.browse.staff.voice.StaffVoiceViewModel
import it.matteoleggio.alchan.ui.browse.studio.StudioViewModel
import it.matteoleggio.alchan.ui.browse.BrowseViewModel
import it.matteoleggio.alchan.ui.browse.character.FilterCharacterMediaViewModel
import it.matteoleggio.alchan.ui.browse.media.overview.ThemesPlayerViewModel
import it.matteoleggio.alchan.ui.browse.reviews.editor.ReviewEditorViewModel
import it.matteoleggio.alchan.ui.browse.user.stats.UserStatsDetailViewModel
import it.matteoleggio.alchan.ui.browse.user.UserViewModel
import it.matteoleggio.alchan.ui.browse.user.list.UserMediaListViewModel
import it.matteoleggio.alchan.ui.calendar.CalendarScheduleViewModel
import it.matteoleggio.alchan.ui.calendar.CalendarViewModel
import it.matteoleggio.alchan.ui.common.ChartViewModel
import it.matteoleggio.alchan.ui.common.LikesViewModel
import it.matteoleggio.alchan.ui.common.MediaListDetailDialogViewModel
import it.matteoleggio.alchan.ui.common.TextEditorViewModel
import it.matteoleggio.alchan.ui.explore.ExploreViewModel
import it.matteoleggio.alchan.ui.filter.MediaFilterTagViewModel
import it.matteoleggio.alchan.ui.notification.NotificationViewModel
import it.matteoleggio.alchan.ui.profile.ProfileViewModel
import it.matteoleggio.alchan.ui.profile.bio.BioViewModel
import it.matteoleggio.alchan.ui.profile.favorites.FavoritesViewModel
import it.matteoleggio.alchan.ui.profile.favorites.reorder.ReorderFavoritesViewModel
import it.matteoleggio.alchan.ui.profile.follows.FollowsViewModel
import it.matteoleggio.alchan.ui.profile.hated.HatedViewModel
import it.matteoleggio.alchan.ui.profile.reviews.UserReviewsViewModel
import it.matteoleggio.alchan.ui.profile.stats.StatsViewModel
import it.matteoleggio.alchan.ui.profile.stats.details.StatsDetailViewModel
import it.matteoleggio.alchan.ui.reviews.ReviewsViewModel
import it.matteoleggio.alchan.ui.search.SearchListViewModel
import it.matteoleggio.alchan.ui.search.SearchViewModel
import it.matteoleggio.alchan.ui.seasonal.SeasonalDialogViewModel
import it.matteoleggio.alchan.ui.seasonal.SeasonalViewModel
import it.matteoleggio.alchan.ui.settings.account.AccountSettingsViewModel
import it.matteoleggio.alchan.ui.settings.anilist.AniListSettingsViewModel
import it.matteoleggio.alchan.ui.settings.app.AppSettingsViewModel
import it.matteoleggio.alchan.ui.settings.list.ListSettingsViewModel
import it.matteoleggio.alchan.ui.settings.notifications.NotificationsSettingsViewModel
import it.matteoleggio.alchan.ui.social.SocialViewModel
import it.matteoleggio.alchan.ui.social.global.GlobalFeedFilterViewModel
import it.matteoleggio.alchan.ui.social.global.GlobalFeedViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.dsl.module

class ALchanApplication : Application() {

    private val appModules = module {
        val gson = GsonBuilder().serializeSpecialFloatingPointValues().create()

        single<LocalStorage> { LocalStorageImpl(this@ALchanApplication.applicationContext, Constant.SHARED_PREFERENCES_NAME, gson) }
        single<AppSettingsManager> { AppSettingsManagerImpl(get()) }
        single<UserManager> { UserManagerImpl(get()) }
        single<MediaManager> { MediaManagerImpl(get()) }
        single<ListStyleManager> { ListStyleManagerImpl(get()) }
        single<InfoManager> { InfoManagerImpl(get()) }
        single<TempStorageManager> { TempStorageManagerImpl() }

        single<HeaderInterceptor> { HeaderInterceptorImpl(get()) }
        single<SpotifyAuthHeaderInterceptor> { SpotifyAuthHeaderInterceptorImpl(get()) }
        single<SpotifyHeaderInterceptor> { SpotifyHeaderInterceptorImpl(get()) }

        single { ApolloHandler(get()) }
        single { GithubRestService() }
        single { JikanRestService() }
        single { YouTubeRestService() }
        single { SpotifyAuthRestService(get()) }
        single { SpotifyRestService(get()) }

        // AniList GraphQL data source
        single<UserDataSource> { UserDataSourceImpl(get()) }
        single<MediaListDataSource> { MediaListDataSourceImpl(get()) }
        single<MediaDataSource> { MediaDataSourceImpl(get(), get()) }
        single<BrowseDataSource> { BrowseDataSourceImpl(get()) }
        single<SearchDataSource> { SearchDataSourceImpl(get()) }
        single<UserStatisticDataSource> { UserStatisticDataSourceImpl(get()) }
        single<SocialDataSource> { SocialDataSourceImpl(get()) }

        // REST API data source
        single<InfoDataSource> { InfoDataSourceImpl(get(), get(), get(), get()) }

        // AniList GraphQL repository
        single<AuthRepository> { AuthRepositoryImpl(get(), get()) }
        single<UserRepository> { UserRepositoryImpl(get(), get()) }
        single<AppSettingsRepository> { AppSettingsRepositoryImpl(get())}
        single<MediaListRepository> { MediaListRepositoryImpl(get(), get(), gson) }
        single<MediaRepository> { MediaRepositoryImpl(get(), get(), get()) }
        single<ListStyleRepository> { ListStyleRepositoryImpl(get()) }
        single<BrowseRepository> { BrowseRepositoryImpl(get()) }
        single<SearchRepository> { SearchRepositoryImpl(get()) }
        single<UserStatisticRepository> { UserStatisticRepositoryImpl(get(), get(), get()) }
        single<OtherUserRepository> { OtherUserRepositoryImpl(get()) }
        single<OtherUserStatisticRepository> { OtherUserStatisticRepositoryImpl(get(), get()) }
        single<SocialRepository> { SocialRepositoryImpl(get(), get()) }

        // REST API repository
        single<InfoRepository> { InfoRepositoryImpl(get(), get(), get()) }

        // common
        viewModel { BaseViewModel(get()) }
        viewModel { MediaFilterViewModel(get(), get(), gson) }
        viewModel { MediaFilterTagViewModel(get()) }
        viewModel { CustomiseListViewModel(get()) }
        viewModel { MediaListDetailDialogViewModel(gson) }
        viewModel { TextEditorViewModel(get()) }
        viewModel { LikesViewModel(gson) }
        viewModel { ChartViewModel(gson) }

        // auth
        viewModel { SplashViewModel(get(), get(), get()) }
        viewModel { LoginViewModel(get()) }

        // main
        viewModel { MainViewModel(get(), get()) }

        // home, search, explore, seasonal, reviews
        viewModel { HomeViewModel(get(), get(), get(), get()) }
        viewModel { SearchViewModel() }
        viewModel { SearchListViewModel(get()) }
        viewModel { ExploreViewModel(get(), gson) }
        viewModel { SeasonalViewModel(get(), get(), get(), get(), gson) }
        viewModel { SeasonalDialogViewModel(gson) }
        viewModel { ReviewsViewModel(get()) }
        viewModel { CalendarViewModel(get()) }
        viewModel { CalendarScheduleViewModel(get()) }

        // anime list
        viewModel { AnimeListViewModel(get(), get(), get(), get(), gson) }
        viewModel { AnimeListEditorViewModel(get(), get(), gson) }

        // manga list
        viewModel { MangaListViewModel(get(), get(), get(), gson) }
        viewModel { MangaListEditorViewModel(get(), get(), gson) }

        // browse
        viewModel { BrowseViewModel(get()) }

        // browse media
        viewModel { MediaViewModel(get(), get()) }
        viewModel { MediaOverviewViewModel(get()) }
        viewModel { ThemesPlayerViewModel(get())}
        viewModel { MediaCharactersViewModel(get(), get()) }
        viewModel { MediaStaffsViewModel(get()) }
        viewModel { MediaStatsViewModel(get(), get(), gson) }
        viewModel { MediaReviewsViewModel(get()) }
        viewModel { ReviewEditorViewModel(get()) }
        viewModel { MediaSocialViewModel(get()) }

        // browse character, staff, studio
        viewModel { CharacterViewModel(get(), get(), get(), gson) }
        viewModel { FilterCharacterMediaViewModel(gson) }
        viewModel { StaffViewModel(get(), get()) }
        viewModel { StaffBioViewModel(get()) }
        viewModel { StaffVoiceViewModel(get(), get()) }
        viewModel { StaffAnimeViewModel(get(), get()) }
        viewModel { StaffMangaViewModel(get(), get()) }
        viewModel { StudioViewModel(get(), get()) }

        // browse user
        viewModel { UserViewModel(get(), get(), get()) }
        viewModel { UserStatsDetailViewModel(get(), get(), gson) }
        viewModel { UserMediaListViewModel(get(), get(), gson) }

        // browse activity
        viewModel { ActivityDetailViewModel(get(), get(), gson) }
        viewModel { ActivityListViewModel(get(), get()) }

        // browse review
        viewModel { ReviewsReaderViewModel(get(), get()) }

        // profile and settings
        viewModel { ProfileViewModel(get(), get()) }
        viewModel { BioViewModel(get(), get(), get()) }
        viewModel { FavoritesViewModel(get(), get(), gson) }
        viewModel { ReorderFavoritesViewModel(get(), gson) }
        viewModel { StatsViewModel(get(), get(), get(), gson) }
        viewModel { StatsDetailViewModel(get(), get(), gson) }
        viewModel { UserReviewsViewModel(get(), get()) }
        viewModel { FollowsViewModel(get(), get()) }
        viewModel { AppSettingsViewModel(get()) }
        viewModel { AniListSettingsViewModel(get()) }
        viewModel { ListSettingsViewModel(get()) }
        viewModel { NotificationsSettingsViewModel(get()) }
        viewModel { AccountSettingsViewModel(get()) }
        viewModel { NotificationViewModel(get()) }

        // social
        viewModel { SocialViewModel(get(), get(), get(), get()) }
        viewModel { GlobalFeedViewModel(get(), get(), get()) }
        viewModel { GlobalFeedFilterViewModel(get()) }

        viewModel { HatedViewModel(get(), get(), gson) }

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