package it.matteoleggio.alchan.ui.settings


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat

import it.matteoleggio.alchan.R
import it.matteoleggio.alchan.helper.doOnApplyWindowInsets
import it.matteoleggio.alchan.helper.updateBottomPadding
import it.matteoleggio.alchan.helper.updateSidePadding
import it.matteoleggio.alchan.helper.updateTopPadding
import it.matteoleggio.alchan.ui.base.BaseFragment
import it.matteoleggio.alchan.ui.settings.about.AboutFragment
import it.matteoleggio.alchan.ui.settings.account.AccountSettingsFragment
import it.matteoleggio.alchan.ui.settings.anilist.AniListSettingsFragment
import it.matteoleggio.alchan.ui.settings.app.AppSettingsFragment
import it.matteoleggio.alchan.ui.settings.list.ListSettingsFragment
import it.matteoleggio.alchan.ui.settings.notifications.NotificationsSettingsFragment
import kotlinx.android.synthetic.main.fragment_settings.*
import kotlinx.android.synthetic.main.layout_toolbar.*

/**
 * A simple [Fragment] subclass.
 */
class SettingsFragment : BaseFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        settingsMenuLayout.doOnApplyWindowInsets { view, windowInsets, initialPadding ->
            view.updateBottomPadding(windowInsets, initialPadding)
        }

        toolbarLayout.title = getString(R.string.settings)
        toolbarLayout.setNavigationOnClickListener { activity?.onBackPressed() }
        toolbarLayout.navigationIcon = ContextCompat.getDrawable(requireActivity(), R.drawable.ic_delete)

        appSettingsMenu.setOnClickListener { listener?.changeFragment(AppSettingsFragment()) }
        anilistSettingsMenu.setOnClickListener { listener?.changeFragment(AniListSettingsFragment()) }
        listSettingsMenu.setOnClickListener { listener?.changeFragment(ListSettingsFragment()) }
        notificationsSettingsMenu.setOnClickListener { listener?.changeFragment(NotificationsSettingsFragment()) }
        accountSettingsMenu.setOnClickListener { listener?.changeFragment(AccountSettingsFragment()) }
        aboutMenu.setOnClickListener { listener?.changeFragment(AboutFragment()) }
    }
}
