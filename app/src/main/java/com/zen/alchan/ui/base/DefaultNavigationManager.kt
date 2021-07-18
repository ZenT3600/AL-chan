package com.zen.alchan.ui.base

import android.content.Context
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.zen.alchan.helper.Constant
import com.zen.alchan.ui.activity.ActivityFragment
import com.zen.alchan.ui.browse.BrowseFragment
import com.zen.alchan.ui.landing.LandingFragment
import com.zen.alchan.ui.login.LoginFragment
import com.zen.alchan.ui.main.MainFragment
import com.zen.alchan.ui.reorder.ReorderFragment
import com.zen.alchan.ui.settings.SettingsFragment
import com.zen.alchan.ui.settings.anilist.AniListSettingsFragment
import com.zen.alchan.ui.settings.app.AppSettingsFragment
import com.zen.alchan.ui.settings.list.ListSettingsFragment
import com.zen.alchan.ui.settings.notifications.NotificationsSettingsFragment
import com.zen.alchan.ui.splash.SplashFragment

class DefaultNavigationManager(
    private val context: Context,
    private val fragmentManager: FragmentManager,
    private val layout: FragmentContainerView
) : NavigationManager {

    override fun navigateToSplash() {
        swapPage(SplashFragment.newInstance(), true)
    }

    override fun navigateToLanding() {
        swapPage(LandingFragment.newInstance(), true)
    }

    override fun navigateToLogin(bearerToken: String?) {
        swapPage(LoginFragment.newInstance(bearerToken), true)
    }

    override fun navigateToMain() {
        swapPage(MainFragment.newInstance(), true)
    }

    override fun navigateToBrowse() {
        swapPage(BrowseFragment.newInstance())
    }

    override fun navigateToActivities() {
        swapPage(ActivityFragment.newInstance())
    }

    override fun navigateToNotifications() {

    }

    override fun navigateToSettings() {
        swapPage(SettingsFragment.newInstance())
    }

    override fun navigateToAppSettings() {
        swapPage(AppSettingsFragment.newInstance())
    }

    override fun navigateToAniListSettings() {
        swapPage(AniListSettingsFragment.newInstance())
    }

    override fun navigateToListSettings() {
        swapPage(ListSettingsFragment.newInstance())
    }

    override fun navigateToNotificationsSettings() {
        swapPage(NotificationsSettingsFragment.newInstance())
    }

    override fun navigateToAccountSettings() {

    }

    override fun navigateToAbout() {

    }

    override fun navigateToReorder() {
        stackPage(ReorderFragment.newInstance())
    }

    override fun openWebView(url: String) {
        launchWebView(Uri.parse(url))
    }

    override fun openWebView(url: NavigationManager.Url) {
        launchWebView(
            Uri.parse(
                when (url) {
                    NavigationManager.Url.ANILIST_WEBSITE -> Constant.ANILIST_WEBSITE_URL
                    NavigationManager.Url.ANILIST_LOGIN -> Constant.ANILIST_LOGIN_URL
                    NavigationManager.Url.ANILIST_REGISTER -> Constant.ANILIST_REGISTER_URL
                }
            )
        )
    }

    private fun swapPage(fragment: Fragment, skipBackStack: Boolean = false) {
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(layout.id, fragment)
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
        if (!skipBackStack)
            fragmentTransaction.addToBackStack(fragment.toString())
        fragmentTransaction.commit()
    }

    private fun stackPage(fragment: Fragment) {
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.add(layout.id, fragment)
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
        fragmentTransaction.addToBackStack(fragment.toString())
        fragmentTransaction.commit()
    }

    private fun launchWebView(uri: Uri) {
        // TODO: fix crash issue
        CustomTabsIntent.Builder()
            .build()
            .launchUrl(context, uri)
    }
}