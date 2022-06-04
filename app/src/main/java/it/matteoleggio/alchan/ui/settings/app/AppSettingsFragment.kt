package it.matteoleggio.alchan.ui.settings.app


import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import androidx.fragment.app.Fragment
import android.widget.EditText
import android.widget.LinearLayout
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
import android.content.Context.WINDOW_SERVICE
import android.util.DisplayMetrics
import android.view.*
import android.widget.Switch
import it.matteoleggio.alchan.helper.pojo.AppSettings
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


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
        val displayMetrics = DisplayMetrics()
        val windowsManager = context!!.getSystemService(WINDOW_SERVICE) as WindowManager
        windowsManager.defaultDisplay.getMetrics(displayMetrics)
        val deviceWidth = displayMetrics.widthPixels
        for (clip in viewModel.appSettings.postsCustomClipboard) {
            val newClipboardBoundary = LinearLayout(context)
            newClipboardBoundary.gravity = Gravity.CENTER_VERTICAL
            newClipboardBoundary.orientation = LinearLayout.HORIZONTAL
            val newClipboardRemove = Switch(context)
            newClipboardRemove.isChecked = clip[1] == "true"
            val newClipboardText = EditText(context)
            newClipboardText.setHorizontallyScrolling(false)
            newClipboardText.maxLines = Integer.MAX_VALUE
            newClipboardText.typeface = Typeface.MONOSPACE
            newClipboardText.textSize = 12F
            newClipboardText.maxWidth = deviceWidth - 260
            newClipboardText.width = deviceWidth - 260
            newClipboardText.setText(clip[0])

            newClipboardBoundary.addView(newClipboardText)
            newClipboardBoundary.addView(newClipboardRemove)

            postsClipboardLayout.addView(newClipboardBoundary, 0)
        }

        addClipboardButton.setOnClickListener {
            val newClipboardBoundary = LinearLayout(context)
            newClipboardBoundary.gravity = Gravity.CENTER_VERTICAL
            newClipboardBoundary.orientation = LinearLayout.HORIZONTAL
            val newClipboardRemove = Switch(context)
            newClipboardRemove.isChecked = true
            val newClipboardText = EditText(context)
            newClipboardText.setHorizontallyScrolling(false)
            newClipboardText.maxLines = Integer.MAX_VALUE
            newClipboardText.typeface = Typeface.MONOSPACE
            newClipboardText.textSize = 12F
            newClipboardText.maxWidth = deviceWidth - 260
            newClipboardText.width = deviceWidth - 260

            newClipboardBoundary.addView(newClipboardText)
            newClipboardBoundary.addView(newClipboardRemove)

            postsClipboardLayout.addView(newClipboardBoundary, 0)
        }

        itemSave.setOnMenuItemClickListener {
            DialogUtility.showOptionDialog(
                requireActivity(),
                R.string.save_settings,
                R.string.are_you_sure_you_want_to_save_this_configuration,
                R.string.save,
                {
                    var clip = arrayListOf<ArrayList<String>>()
                    for (i in 0 until (postsClipboardLayout.childCount)) {
                        lateinit var boundary: LinearLayout
                        try { boundary = postsClipboardLayout.getChildAt(i) as LinearLayout } catch(e: ClassCastException) { continue }
                        val editText = boundary.getChildAt(0) as EditText
                        val switch = boundary.getChildAt(1) as Switch
                        if (editText.text.toString().isEmpty()) continue
                        clip.add(arrayListOf(editText.text.toString(), switch.isChecked.toString()))
                    }
                    if (hoursCalendarSelection.text.toString().toDouble() < 0.1) { hoursCalendarSelection.setText("0.1") }
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
                        hoursCalendarSelection.text.toString().toDouble(),
                        clip
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

        hoursCalendarSelection.setText(viewModel.pushNotificationsMinHours.toString())

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
