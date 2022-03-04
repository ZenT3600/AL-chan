package it.matteoleggio.alchan.ui.browse

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Observer
import it.matteoleggio.alchan.R
import it.matteoleggio.alchan.helper.doOnApplyWindowInsets
import it.matteoleggio.alchan.helper.enums.BrowsePage
import it.matteoleggio.alchan.helper.enums.FollowPage
import it.matteoleggio.alchan.helper.enums.ResponseStatus
import it.matteoleggio.alchan.helper.updateSidePadding
import it.matteoleggio.alchan.helper.utils.DialogUtility
import it.matteoleggio.alchan.ui.base.BaseActivity
import it.matteoleggio.alchan.ui.base.BaseListener
import it.matteoleggio.alchan.ui.browse.activity.ActivityDetailFragment
import it.matteoleggio.alchan.ui.browse.activity.ActivityListFragment
import it.matteoleggio.alchan.ui.browse.character.CharacterFragment
import it.matteoleggio.alchan.ui.browse.media.MediaFragment
import it.matteoleggio.alchan.ui.browse.reviews.ReviewsReaderFragment
import it.matteoleggio.alchan.ui.browse.staff.StaffFragment
import it.matteoleggio.alchan.ui.browse.studio.StudioFragment
import it.matteoleggio.alchan.ui.browse.user.UserFragment
import it.matteoleggio.alchan.ui.browse.user.follows.UserFollowsFragment
import it.matteoleggio.alchan.ui.browse.user.list.UserMediaListFragment
import it.matteoleggio.alchan.ui.browse.user.stats.UserStatsDetailFragment
import kotlinx.android.synthetic.main.activity_browse.*
import kotlinx.android.synthetic.main.layout_loading.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import type.MediaType

class BrowseActivity : BaseActivity(), BaseListener {

    private val viewModel by viewModel<BrowseViewModel>()

    companion object {
        const val TARGET_PAGE = "targetPage"
        const val LOAD_ID = "loadId"
        const val EXTRA_LOAD = "extraLoad"
    }

    private fun String.isInt() = toIntOrNull() != null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_browse)

        browseFrameLayout.doOnApplyWindowInsets { view, windowInsets, initialPadding ->
            view.updateSidePadding(windowInsets, initialPadding)
        }

        setupObserver()

        if (intent?.data != null) {
            try {
                val appLinkData = intent?.data.toString()
                val splitLink = appLinkData.substring(appLinkData.indexOf("anilist.co")).split("/")
                val target = splitLink[1]
                val load = splitLink[2]
                if (!load.isInt() && BrowsePage.valueOf(target.toUpperCase()) == BrowsePage.USER) {
                    viewModel.getIdFromName(load)
                } else {
                    changeFragment(BrowsePage.valueOf(target.toUpperCase()), load.toInt(), null, supportFragmentManager.backStackEntryCount != 0)
                }
            } catch (e: Exception) {
                DialogUtility.showToast(this, R.string.invalid_link)
                finish()
            } finally {
                return
            }
        }

        try {
            if (supportFragmentManager.backStackEntryCount == 0) {
                changeFragment(BrowsePage.valueOf(intent.getStringExtra(TARGET_PAGE) ?: ""), intent.getIntExtra(LOAD_ID, 0), intent.getStringExtra(EXTRA_LOAD) ,false)
            }
        } catch (e: Exception) {
            DialogUtility.showToast(this, R.string.invalid_link)
            finish()
        }
    }

    override fun changeFragment(targetFragment: Fragment, addToBackStack: Boolean) {
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(browseFrameLayout.id, targetFragment)
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
        if (addToBackStack) {
            fragmentTransaction.addToBackStack(null)
        }
        fragmentTransaction.commit()
    }

    override fun changeFragment(browsePage: BrowsePage, id: Int, extraLoad: String?, addToBackStack: Boolean) {
        lateinit var targetFragment: Fragment
        val bundle = Bundle()

        when (browsePage) {
            BrowsePage.ANIME -> {
                targetFragment = MediaFragment()
                bundle.putInt(MediaFragment.MEDIA_ID, id)
                bundle.putString(MediaFragment.MEDIA_TYPE, MediaType.ANIME.name)
            }
            BrowsePage.MANGA -> {
                targetFragment = MediaFragment()
                bundle.putInt(MediaFragment.MEDIA_ID, id)
                bundle.putString(MediaFragment.MEDIA_TYPE, MediaType.MANGA.name)
            }
            BrowsePage.CHARACTER -> {
                targetFragment = CharacterFragment()
                bundle.putInt(CharacterFragment.CHARACTER_ID, id)
            }
            BrowsePage.STAFF -> {
                targetFragment = StaffFragment()
                bundle.putInt(StaffFragment.STAFF_ID, id)
            }
            BrowsePage.STUDIO -> {
                targetFragment = StudioFragment()
                bundle.putInt(StudioFragment.STUDIO_ID, id)
            }
            BrowsePage.USER -> {
                targetFragment = UserFragment()
                bundle.putInt(UserFragment.USER_ID, id)
            }
            BrowsePage.USER_STATS_DETAIL -> {
                targetFragment = UserStatsDetailFragment()
                bundle.putInt(UserStatsDetailFragment.USER_ID, id)
            }
            BrowsePage.USER_ANIME_LIST -> {
                targetFragment = UserMediaListFragment()
                bundle.putInt(UserMediaListFragment.USER_ID, id)
                bundle.putString(UserMediaListFragment.MEDIA_TYPE, MediaType.ANIME.name)
            }
            BrowsePage.USER_MANGA_LIST -> {
                targetFragment = UserMediaListFragment()
                bundle.putInt(UserMediaListFragment.USER_ID, id)
                bundle.putString(UserMediaListFragment.MEDIA_TYPE, MediaType.MANGA.name)
            }
            BrowsePage.USER_FOLLOWING_LIST -> {
                targetFragment = UserFollowsFragment()
                bundle.putInt(UserFollowsFragment.USER_ID, id)
                bundle.putInt(UserFollowsFragment.START_POSITION, FollowPage.FOLLOWING.ordinal)
            }
            BrowsePage.USER_FOLLOWERS_LIST -> {
                targetFragment = UserFollowsFragment()
                bundle.putInt(UserFollowsFragment.USER_ID, id)
                bundle.putInt(UserFollowsFragment.START_POSITION, FollowPage.FOLLOWERS.ordinal)
            }
            BrowsePage.ACTIVITY_LIST -> {
                targetFragment = ActivityListFragment()
                bundle.putInt(ActivityListFragment.USER_ID, id)
                bundle.putString(ActivityListFragment.USER_NAME, extraLoad)
            }
            BrowsePage.ACTIVITY_DETAIL -> {
                targetFragment = ActivityDetailFragment()
                bundle.putInt(ActivityDetailFragment.ACTIVITY_ID, id)
            }
            BrowsePage.REVIEW -> {
                targetFragment = ReviewsReaderFragment()
                bundle.putInt(ReviewsReaderFragment.REVIEW_ID, id)
            }
        }

        targetFragment.arguments = bundle
        changeFragment(targetFragment, addToBackStack)
    }

    private fun setupObserver() {
        viewModel.idFromNameData.observe(this, Observer {
            when (it.responseStatus) {
                ResponseStatus.LOADING -> loadingLayout.visibility = View.VISIBLE
                ResponseStatus.SUCCESS -> {
                    loadingLayout.visibility = View.GONE
                    val userId = it.data?.user?.id
                    if (userId != null) {
                        changeFragment(BrowsePage.USER, userId, null, supportFragmentManager.backStackEntryCount != 0)
                    } else {
                        DialogUtility.showToast(this, R.string.invalid_link)
                        finish()
                    }
                }
                ResponseStatus.ERROR -> {
                    loadingLayout.visibility = View.GONE
                    DialogUtility.showToast(this, R.string.invalid_link)
                    finish()
                }
            }
        })
    }
}
