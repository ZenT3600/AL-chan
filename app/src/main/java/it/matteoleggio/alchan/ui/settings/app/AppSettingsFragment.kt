package it.matteoleggio.alchan.ui.settings.app


import android.net.Uri
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat

import it.matteoleggio.alchan.R
import it.matteoleggio.alchan.helper.*
import it.matteoleggio.alchan.helper.enums.AppColorTheme
import it.matteoleggio.alchan.helper.utils.AndroidUtility
import it.matteoleggio.alchan.helper.utils.DialogUtility
import kotlinx.android.synthetic.main.fragment_app_settings.*
import kotlinx.android.synthetic.main.layout_toolbar.*
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * A simple [Fragment] subclass.
 */
class AppSettingsFragment : Fragment() {

    private val viewModel by viewModel<AppSettingsViewModel>()

    private lateinit var itemSave: MenuItem

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_app_settings, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        toolbarLayout.apply {
            title = getString(R.string.app_settings)
            navigationIcon = ContextCompat.getDrawable(requireActivity(), R.drawable.ic_left)
            setNavigationOnClickListener { activity?.onBackPressed() }

            inflateMenu(R.menu.menu_save)
            itemSave = menu.findItem(R.id.itemSave)
        }

        appSettingsLayout.doOnApplyWindowInsets { view, windowInsets, initialPadding ->
            view.updateBottomPadding(windowInsets, initialPadding)
        }

        initLayout()
    }

    private fun initLayout() {
        if (!viewModel.isInit) {
            viewModel.selectedAppTheme = viewModel.appSettings.appTheme
            circularAvatarCheckBox.isChecked = viewModel.appSettings.circularAvatar == true
            whiteBackgroundAvatarCheckBox.isChecked = viewModel.appSettings.whiteBackgroundAvatar == true
            showRecentReviewsCheckBox.isChecked = viewModel.appSettings.showRecentReviews == true
            enableSocialCheckBox.isChecked = viewModel.appSettings.showSocialTabAutomatically != false
            showBioCheckBox.isChecked = viewModel.appSettings.showBioAutomatically != false
            showStatsCheckBox.isChecked = viewModel.appSettings.showStatsAutomatically != false
            useRelativeDateCheckBox.isChecked = viewModel.appSettings.useRelativeDate == true
            sendAiringPushNotificationsCheckBox.isChecked = viewModel.appSettings.sendAiringPushNotification == true
            sendActivityPushNotificationsCheckBox.isChecked = viewModel.appSettings.sendActivityPushNotification == true
            sendForumPushNotificationsCheckBox.isChecked = viewModel.appSettings.sendForumPushNotification == true
            sendFollowsPushNotificationsCheckBox.isChecked = viewModel.appSettings.sendFollowsPushNotification == true
            sendRelationsPushNotificationsCheckBox.isChecked = viewModel.appSettings.sendRelationsPushNotification == true
            mergePushNotificationsCheckBox.isChecked = viewModel.appSettings.mergePushNotifications == true
            viewModel.pushNotificationsMinHours = viewModel.appSettings.pushNotificationMinimumHours
            viewModel.isInit = true
        }

        itemSave.setOnMenuItemClickListener {
            DialogUtility.showOptionDialog(
                requireActivity(),
                R.string.save_settings,
                R.string.are_you_sure_you_want_to_save_this_configuration,
                R.string.save,
                {
                    viewModel.setAppSettings(
                        circularAvatarCheckBox.isChecked,
                        whiteBackgroundAvatarCheckBox.isChecked,
                        showRecentReviewsCheckBox.isChecked,
                        enableSocialCheckBox.isChecked,
                        showBioCheckBox.isChecked,
                        showStatsCheckBox.isChecked,
                        useRelativeDateCheckBox.isChecked,
                        sendAiringPushNotificationsCheckBox.isChecked,
                        sendActivityPushNotificationsCheckBox.isChecked,
                        sendForumPushNotificationsCheckBox.isChecked,
                        sendFollowsPushNotificationsCheckBox.isChecked,
                        sendRelationsPushNotificationsCheckBox.isChecked,
                        mergePushNotificationsCheckBox.isChecked,
                        pushNotificationsMinHoursInput.text.toString().toDouble()
                    )

                    activity?.recreate()
                    DialogUtility.showToast(activity, R.string.settings_saved)
                },
                R.string.cancel,
                { }
            )
            true
        }

        selectedThemeText.text = viewModel.selectedAppTheme?.name.replaceUnderscore()
        selectedThemeText.setOnClickListener { showAppThemeDialog() }

        pushNotificationsMinHoursInput.setText(viewModel.pushNotificationsMinHours.toString())

        resetDefaultButton.setOnClickListener {
            val isLowOnMemory = AndroidUtility.isLowOnMemory(activity)
            viewModel.pushNotificationsMinHours = 0.5

            DialogUtility.showOptionDialog(
                requireActivity(),
                R.string.reset_to_default,
                R.string.this_will_reset_your_app_settings_to_default_configuration,
                R.string.reset,
                {
                    viewModel.setAppSettings(
                        showSocialTab = !isLowOnMemory,
                        showBio = !isLowOnMemory,
                        showStats = !isLowOnMemory
                    )
                    viewModel.isInit = false
                    initLayout()

                    activity?.recreate()
                    DialogUtility.showToast(activity, R.string.settings_saved)
                },
                R.string.cancel,
                { }
            )
        }

        val dontKillMyApp = "https://dontkillmyapp.com/"
        val explanationText = SpannableString(getString(R.string.important_to_know_n1_push_notification_will_show_up_periodically_not_real_time_2_depending_on_your_rom_and_phone_setting_it_might_not_show_up_at_all_reference_https_dontkillmyapp_com))
        val startIndex = explanationText.indexOf(dontKillMyApp)
        val endIndex = startIndex + dontKillMyApp.length

        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                CustomTabsIntent.Builder()
                    .build()
                    .launchUrl(requireActivity(), Uri.parse(dontKillMyApp))
            }
        }

        explanationText.setSpan(clickableSpan, startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        pushNotificationExplanation.movementMethod = LinkMovementMethod.getInstance()
        pushNotificationExplanation.text = explanationText
    }

    private fun showAppThemeDialog() {
        val dialog = AppThemeDialog()
        dialog.setListener(object : AppThemeDialogListener {
            override fun passSelectedTheme(theme: AppColorTheme) {
                viewModel.selectedAppTheme = theme
                selectedThemeText.text = theme.name.replaceUnderscore()

                val palette = viewModel.selectedAppTheme?.value ?: Constant.DEFAULT_THEME.value
                primaryColorItem.setCardBackgroundColor(ContextCompat.getColor(requireActivity(), palette.primaryColor))
                secondaryColorItem.setCardBackgroundColor(ContextCompat.getColor(requireActivity(), palette.secondaryColor))
                negativeColorItem.setCardBackgroundColor(ContextCompat.getColor(requireActivity(), palette.negativeColor))
            }
        })
        dialog.show(childFragmentManager, null)
    }
}
