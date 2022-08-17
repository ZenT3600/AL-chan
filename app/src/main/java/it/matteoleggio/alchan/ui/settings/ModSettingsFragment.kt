package it.matteoleggio.alchan.ui.settings


import android.app.ActionBar
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Switch
import androidx.annotation.Dimension
import androidx.core.content.ContextCompat
import androidx.core.view.marginBottom
import androidx.core.view.marginLeft
import androidx.core.view.marginRight
import androidx.core.view.marginTop
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

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
import it.matteoleggio.alchan.ui.settings.app.AppSettingsViewModel
import it.matteoleggio.alchan.ui.settings.list.ListSettingsFragment
import it.matteoleggio.alchan.ui.settings.notifications.NotificationsSettingsFragment
import kotlinx.android.synthetic.main.fragment_app_settings.*
import kotlinx.android.synthetic.main.fragment_mod_settings.*
import kotlinx.android.synthetic.main.fragment_settings.*
import kotlinx.android.synthetic.main.fragment_settings.settingsMenuLayout
import kotlinx.android.synthetic.main.layout_toolbar.*
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * A simple [Fragment] subclass.
 */
class ModSettingsFragment : BaseFragment() {

    private val viewModel by viewModel<AppSettingsViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_mod_settings, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        settingsMenuLayout.doOnApplyWindowInsets { view, windowInsets, initialPadding ->
            view.updateBottomPadding(windowInsets, initialPadding)
        }

        toolbarLayout.title = getString(R.string.mod_settings)
        toolbarLayout.setNavigationOnClickListener { activity?.onBackPressed() }
        toolbarLayout.navigationIcon = ContextCompat.getDrawable(requireActivity(), R.drawable.ic_delete)

        for (clip in viewModel.appSettings.postsCustomClipboard) {
            val newClipboardText = TextInputEditText(context!!)
            newClipboardText.setHorizontallyScrolling(false)
            newClipboardText.maxLines = Integer.MAX_VALUE
            newClipboardText.width = ViewGroup.LayoutParams.MATCH_PARENT
            newClipboardText.hint = "Custom Clipboard"
            newClipboardText.setText(clip)

            addClipboard.addView(newClipboardText, 0)
        }

        addButton.setOnClickListener {
            val newClipboardText = TextInputEditText(context!!)
            newClipboardText.setHorizontallyScrolling(false)
            newClipboardText.maxLines = Integer.MAX_VALUE
            newClipboardText.width = ViewGroup.LayoutParams.MATCH_PARENT
            newClipboardText.hint = "Custom Clipboard"

            addClipboard.addView(newClipboardText, 0)
        }

        saveButton.setOnClickListener {
            var clip = arrayListOf<String>()
            for (i in 0 until (addClipboard.childCount - 1)) {
                val editText = addClipboard.getChildAt(i) as TextInputEditText
                if (editText.text.toString().isEmpty()) continue
                clip.add(editText.text.toString())
            }

            viewModel.setAppSettings(
                viewModel.appSettings.circularAvatar!!,
                viewModel.appSettings.whiteBackgroundAvatar!!,
                viewModel.appSettings.showRecentReviews!!,
                viewModel.appSettings.showSocialTabAutomatically!!,
                viewModel.appSettings.showBioAutomatically!!,
                viewModel.appSettings.showStatsAutomatically!!,
                viewModel.appSettings.useRelativeDate!!,
                viewModel.appSettings.sendAiringPushNotification!!,
                viewModel.appSettings.sendActivityPushNotification!!,
                viewModel.appSettings.sendForumPushNotification!!,
                viewModel.appSettings.sendFollowsPushNotification!!,
                viewModel.appSettings.sendRelationsPushNotification!!,
                viewModel.appSettings.mergePushNotifications!!,
                viewModel.appSettings.pushNotificationMinimumHours!!,
                clip
            )
        }
    }
}
