package it.matteoleggio.alchan.ui.settings.account


import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import it.matteoleggio.alchan.BuildConfig

import it.matteoleggio.alchan.R
import it.matteoleggio.alchan.helper.Constant
import it.matteoleggio.alchan.helper.doOnApplyWindowInsets
import it.matteoleggio.alchan.helper.updateBottomPadding
import it.matteoleggio.alchan.helper.utils.DialogUtility
import it.matteoleggio.alchan.ui.auth.SplashActivity
import kotlinx.android.synthetic.main.fragment_account_settings.*
import kotlinx.android.synthetic.main.layout_toolbar.*
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * A simple [Fragment] subclass.
 */
class AccountSettingsFragment : Fragment() {

    private val viewModel by viewModel<AccountSettingsViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_account_settings, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        toolbarLayout.apply {
            title = getString(R.string.account_settings)
            navigationIcon = ContextCompat.getDrawable(requireActivity(), R.drawable.ic_left)
            setNavigationOnClickListener { activity?.onBackPressed() }
        }

        accountSettingsLayout.doOnApplyWindowInsets { view, windowInsets, initialPadding ->
            view.updateBottomPadding(windowInsets, initialPadding)
        }

        initLayout()
    }

    private fun initLayout() {
        updateProfileCard.setOnClickListener {
            CustomTabsIntent.Builder().build().launchUrl(requireActivity(), Uri.parse(Constant.ANILIST_SETTINGS_URL))
        }

        updateAccountCard.setOnClickListener {
            CustomTabsIntent.Builder().build().launchUrl(requireActivity(), Uri.parse(Constant.ANILIST_ACCOUNT_URL))
        }

        importListsCard.setOnClickListener {
            CustomTabsIntent.Builder().build().launchUrl(requireActivity(), Uri.parse(Constant.ANILIST_IMPORT_LISTS_URL))
        }

        logoutButton.setOnClickListener {
            DialogUtility.showOptionDialog(
                requireActivity(),
                R.string.logout_from_al_chan,
                R.string.logging_out_from_al_chan,
                R.string.logout,
                {
                    viewModel.clearStorage()
                    startActivity(Intent(activity, SplashActivity::class.java))
                    activity?.finish()
                },
                R.string.cancel,
                { }
            )
        }
    }
}
