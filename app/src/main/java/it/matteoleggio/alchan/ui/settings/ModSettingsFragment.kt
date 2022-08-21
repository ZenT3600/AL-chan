package it.matteoleggio.alchan.ui.settings


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.google.android.material.textfield.TextInputEditText

import it.matteoleggio.alchan.R
import it.matteoleggio.alchan.helper.Constant
import it.matteoleggio.alchan.helper.doOnApplyWindowInsets
import it.matteoleggio.alchan.helper.updateBottomPadding
import it.matteoleggio.alchan.ui.base.BaseFragment
import it.matteoleggio.alchan.ui.settings.app.AppSettingsViewModel
import kotlinx.android.synthetic.main.fragment_mod_settings.*
import kotlinx.android.synthetic.main.fragment_settings.settingsMenuLayout
import kotlinx.android.synthetic.main.layout_toolbar.*
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

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

            internalLayout.addView(newClipboardText, 0)
        }

        fetchFromMalCheckBox.isChecked = viewModel.appSettings.fetchFromMal

        val userId = viewModel.appSettings.userid
        val body = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(
                "query", "query {\n" +
                        "User(id: ${userId}) {\n" +
                        "__typename\n" +
                        "id\n" +
                        "name\n" +
                        "about(asHtml: false)\n" +
                        "}\n" +
                        "}"
            )
            .build()
        var response: Response? = null
        thread(start = true) {
            val okHttpClient = OkHttpClient.Builder()
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS)
                .build()
            val request = Request.Builder()
                .url(Constant.ANILIST_API_URL)
                .post(body)
                .build()
            response = okHttpClient.newCall(request).execute()
        }.join()
        println(response?.code)
        val responseString = response?.body?.string().toString()
        println(responseString)
        val user =
            JSONObject(responseString).getJSONObject("data").getJSONObject("User")
        internalUsername.setText(user.getString("name").toString())

        addButton.setOnClickListener {
            val newClipboardText = TextInputEditText(context!!)
            newClipboardText.setHorizontallyScrolling(false)
            newClipboardText.maxLines = Integer.MAX_VALUE
            newClipboardText.width = ViewGroup.LayoutParams.MATCH_PARENT
            newClipboardText.hint = "Custom Clipboard"

            internalLayout.addView(newClipboardText, 0)
        }

        saveButton.setOnClickListener {
            var clip = arrayListOf<String>()
            for (i in 0 until (internalLayout.childCount - 1)) {
                val editText = internalLayout.getChildAt(i) as TextInputEditText
                if (editText.text.toString().isEmpty()) continue
                clip.add(editText.text.toString())
            }

            val body = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(
                    "query", "query {\n" +
                            "User(name: ${internalUsername.text.toString()}) {\n" +
                            "__typename\n" +
                            "id\n" +
                            "name\n" +
                            "about(asHtml: false)\n" +
                            "}\n" +
                            "}"
                )
                .build()
            var response: Response? = null
            thread(start = true) {
                val okHttpClient = OkHttpClient.Builder()
                    .connectTimeout(20, TimeUnit.SECONDS)
                    .readTimeout(20, TimeUnit.SECONDS)
                    .writeTimeout(20, TimeUnit.SECONDS)
                    .build()
                val request = Request.Builder()
                    .url(Constant.ANILIST_API_URL)
                    .post(body)
                    .build()
                response = okHttpClient.newCall(request).execute()
            }.join()
            println(response?.code)
            val responseString = response?.body?.string().toString()
            println(responseString)
            val user =
                JSONObject(responseString).getJSONObject("data").getJSONObject("User")

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
                clip,
                fetchFromMalCheckBox.isChecked,
                user.getInt("id")
            )
        }
    }
}
