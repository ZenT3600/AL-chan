package it.matteoleggio.alchan.ui.main

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.get
import androidx.core.view.updatePadding
import androidx.lifecycle.Observer
import androidx.viewpager.widget.ViewPager
import it.matteoleggio.alchan.R
import it.matteoleggio.alchan.helper.*
import it.matteoleggio.alchan.helper.utils.DialogUtility
import it.matteoleggio.alchan.helper.utils.Utility
import it.matteoleggio.alchan.ui.animelist.AnimeListFragment
import it.matteoleggio.alchan.ui.auth.SplashActivity
import it.matteoleggio.alchan.ui.base.BaseActivity
import it.matteoleggio.alchan.ui.base.BaseMainFragmentListener
import it.matteoleggio.alchan.ui.home.HomeFragment
import it.matteoleggio.alchan.ui.mangalist.MangaListFragment
import it.matteoleggio.alchan.ui.notification.NotificationActivity
import it.matteoleggio.alchan.ui.profile.ProfileFragment
import it.matteoleggio.alchan.ui.social.SocialFragment
import kotlinx.android.synthetic.main.activity_main.*
import org.koin.androidx.viewmodel.ext.android.viewModel


class MainActivity : BaseActivity(), BaseMainFragmentListener {

    private val viewModel by viewModel<MainViewModel>()

    private lateinit var handler: Handler
    private lateinit var runnable: Runnable

    companion object {
        const val GO_TO_NOTIFICATION = "goToNotification"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mainLayout.doOnApplyWindowInsets { view, windowInsets, initialPadding ->
            view.updateSidePadding(windowInsets, initialPadding)
            mainBottomNavigation.updateBottomPadding(windowInsets, initialPadding)
        }

        if (intent.getBooleanExtra(GO_TO_NOTIFICATION, false)) {
            // hate it but necessary because of SingleLiveEvent
            val handler = Handler()
            val runnable = { initPage() }
            handler.postDelayed(runnable, 100)
        } else {
            initPage()
        }
    }

    private fun initPage() {
        setupObserver()
        initLayout()

        if (intent.getBooleanExtra(GO_TO_NOTIFICATION, false)) {
            intent.removeExtra(GO_TO_NOTIFICATION)
            val intent = Intent(this, NotificationActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupObserver() {
        viewModel.appColorThemeLiveData.observe(this, Observer {
            if (Utility.isLightTheme(viewModel.appColorTheme)) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
            recreate()
        })

        viewModel.listOrAniListSettingsChanged.observe(this, Observer {
            recreate()
        })

        viewModel.notificationCount.observe(this, Observer {
            if (it != 0) {
                mainBottomNavigation.getOrCreateBadge(R.id.itemProfile).number = it
            } else {
                mainBottomNavigation.removeBadge(R.id.itemProfile)
            }
        })

        viewModel.sessionResponse.observe(this, Observer {
            if (!it) {
                DialogUtility.showForceActionDialog(
                    this,
                    R.string.you_are_logged_out,
                    R.string.your_session_has_ended,
                    R.string.logout
                ) {
                    viewModel.clearStorage()
                    startActivity(Intent(this, SplashActivity::class.java))
                    finish()
                }
            }
        })

        viewModel.checkSession()
    }

    override fun onStart() {
        super.onStart()
        viewModel.getNotificationCount()
    }

    private fun initLayout() {
        val fragmentList = listOf(HomeFragment(), AnimeListFragment(), MangaListFragment(), SocialFragment(), ProfileFragment())

        mainViewPager.setPagingEnabled(true)
        mainViewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) { }
            override fun onPageScrollStateChanged(state: Int) {}
            override fun onPageSelected(position: Int) { mainBottomNavigation.menu[position].isChecked = true }
        })
        mainViewPager.offscreenPageLimit = fragmentList.size
        mainViewPager.adapter = MainViewPagerAdapter(supportFragmentManager, fragmentList)

        mainBottomNavigation.setOnNavigationItemSelectedListener {
            mainViewPager.currentItem = mainBottomNavigation.menu.findItem(it.itemId).order
            true
        }
    }

    override fun changeMenu(targetMenuId: Int) {
        mainBottomNavigation.selectedItemId = targetMenuId
    }

    override fun onDestroy() {
        if (this::handler.isInitialized && this::runnable.isInitialized) {
            handler.removeCallbacks(runnable)
        }
        super.onDestroy()
    }
}
