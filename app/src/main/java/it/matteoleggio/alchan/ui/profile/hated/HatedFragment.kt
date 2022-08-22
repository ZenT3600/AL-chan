package it.matteoleggio.alchan.ui.profile.hated


import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.GridLayoutManager
import it.matteoleggio.alchan.R
import it.matteoleggio.alchan.data.response.HatedCharacter
import it.matteoleggio.alchan.helper.Constant
import it.matteoleggio.alchan.helper.HatedHelper
import it.matteoleggio.alchan.helper.enums.BrowsePage
import it.matteoleggio.alchan.helper.utils.DialogUtility
import it.matteoleggio.alchan.ui.base.BaseFragment
import it.matteoleggio.alchan.ui.browse.BrowseActivity
import it.matteoleggio.alchan.ui.browse.user.UserFragment
import it.matteoleggio.alchan.ui.settings.app.AppSettingsViewModel
import kotlinx.android.synthetic.main.dialog_input.*
import kotlinx.android.synthetic.main.dialog_input.view.*
import kotlinx.android.synthetic.main.fragment_hated.*
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread
import kotlin.system.exitProcess


/**
 * A simple [Fragment] subclass.
 */
class HatedFragment(val otherUserId: Int? = null) : BaseFragment() {
    private val viewModel by viewModel<HatedViewModel>()
    private val viewModelSettings by viewModel<AppSettingsViewModel>()
    private lateinit var hatedAdapter: HatedRvAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_hated, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        if (arguments != null && arguments?.getInt(UserFragment.USER_ID) != null && arguments?.getInt(UserFragment.USER_ID) != 0) {
            viewModel.otherUserId = arguments?.getInt(UserFragment.USER_ID)
        }

        viewModel.charactersList.clear()
        hatedAdapter = HatedRvAdapter(requireActivity(), viewModel.getMixedList(), handleListenerAction())
        hatedListRecyclerView.adapter = hatedAdapter

        (hatedListRecyclerView.layoutManager as GridLayoutManager).spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return if (hatedAdapter.getItemViewType(position) == HatedRvAdapter.VIEW_TYPE_TITLE) {
                    resources.getInteger(R.integer.gridSpan)
                } else {
                    1
                }
            }
        }

        initLayout()
        setupObserver()
    }

    private fun setupObserver() {
        if (viewModel.otherUserId != null && viewModel.otherUserId != viewModelSettings.appSettings.userid) {
            println("OTHER USER")

            try {
                val body = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart(
                        "query", "query {\n" +
                                "User(id: ${viewModel.otherUserId}) {\n" +
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
                val about =
                    JSONObject(responseString).getJSONObject("data").getJSONObject("User")
                        .get("about")
                        .toString()
                println(about)
                viewModel.charactersList.clear()
                lateinit var hatedCharacters: List<HatedCharacter>
                try {
                    hatedCharacters = HatedHelper(about).getHatedCharactersSelf()
                } catch (e: java.lang.NullPointerException) {
                    return
                }
                hatedListLoading.visibility = View.GONE
                hatedCharacters.forEach {
                    if (it != null) {
                        viewModel.charactersList.add(
                            HatedCharacter(it.image, it.id)
                        )
                    }
                }
                hatedAdapter = HatedRvAdapter(requireActivity(), viewModel.getMixedList(), handleListenerAction())
                hatedListRecyclerView.adapter = hatedAdapter
                return
            } catch (e: Exception) {
                return
            }
        }

        try {
            val body = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(
                    "query", "query {\n" +
                            "User(id: ${viewModelSettings.appSettings.userid}) {\n" +
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
            val about =
                JSONObject(responseString).getJSONObject("data").getJSONObject("User").get("about")
                    .toString()
            println(about)
            Constant.user_about = about
        } catch (e: Exception) {
            val inputDialogView = layoutInflater.inflate(R.layout.dialog_input, inputDialogLayout, false)
            DialogUtility.showCustomViewDialog(context!!, R.string.could_not_detect_username, inputDialogView, R.string.ok, {
                println("ok")
                val username = inputDialogView.inputField.text.toString()
                val body = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart(
                        "query", "query {\n" +
                                "User(name: \"${username}\") {\n" +
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

                Constant.user_about = user.getString("about")
                HatedHelper(Constant.user_about).getHatedCharactersSelf().forEach {
                    if (it != null) {
                        viewModel.animeList.add(
                            HatedCharacter(it.image, it.id)
                        )
                    }
                }
                hatedAdapter = HatedRvAdapter(requireActivity(), viewModel.getMixedList(), handleListenerAction())
                hatedListRecyclerView.adapter = hatedAdapter
                viewModelSettings.setAppSettings(
                    viewModelSettings.appSettings.circularAvatar!!,
                    viewModelSettings.appSettings.whiteBackgroundAvatar!!,
                    viewModelSettings.appSettings.showRecentReviews!!,
                    viewModelSettings.appSettings.showSocialTabAutomatically!!,
                    viewModelSettings.appSettings.showBioAutomatically!!,
                    viewModelSettings.appSettings.showStatsAutomatically!!,
                    viewModelSettings.appSettings.useRelativeDate!!,
                    viewModelSettings.appSettings.sendAiringPushNotification!!,
                    viewModelSettings.appSettings.sendActivityPushNotification!!,
                    viewModelSettings.appSettings.sendForumPushNotification!!,
                    viewModelSettings.appSettings.sendFollowsPushNotification!!,
                    viewModelSettings.appSettings.sendRelationsPushNotification!!,
                    viewModelSettings.appSettings.mergePushNotifications!!,
                    viewModelSettings.appSettings.pushNotificationMinimumHours!!,
                    viewModelSettings.appSettings.postsCustomClipboard,
                    viewModelSettings.appSettings.fetchFromMal,
                    user.getInt("id"),
                    viewModelSettings.appSettings.enableHatedList
                )
            }, R.string.cancel, {
                exitProcess(-1)
            })
        }
        viewModel.charactersList.clear()
        lateinit var hatedCharacters: List<HatedCharacter>
        try {
            hatedCharacters = HatedHelper(Constant.user_about).getHatedCharactersSelf()
        } catch (e: java.lang.NullPointerException) {
            return
        }
        hatedListLoading.visibility = View.GONE
        hatedCharacters.forEach {
            if (it != null) {
                viewModel.charactersList.add(
                    HatedCharacter(it.image, it.id)
                )
            }
        }
        hatedAdapter = HatedRvAdapter(requireActivity(), viewModel.getMixedList(), handleListenerAction())
        hatedListRecyclerView.adapter = hatedAdapter
    }

    private fun initLayout() {
    }

    private fun handleListenerAction() = object : HatedListener {
        override fun passSelectedItem(id: Int, browsePage: BrowsePage) {
            if (viewModel.otherUserId != null) {
                listener?.changeFragment(browsePage, id)
            } else {
                val intent = Intent(activity, BrowseActivity::class.java)
                intent.putExtra(BrowseActivity.TARGET_PAGE, BrowsePage.CHARACTER)
                intent.putExtra(BrowseActivity.LOAD_ID, id)
                startActivity(intent)
            }
        }
    }

    override fun onResume() {
        super.onResume()
//        val body = MultipartBody.Builder()
//            .setType(MultipartBody.FORM)
//            .addFormDataPart(
//                "query", "query {\n" +
//                        "User(id: ${viewModelSettings.appSettings.userid}) {\n" +
//                        "__typename\n" +
//                        "id\n" +
//                        "name\n" +
//                        "about(asHtml: false)\n" +
//                        "}\n" +
//                        "}"
//            )
//            .build()
//        var response: Response? = null
//        thread(start = true) {
//            val okHttpClient = OkHttpClient.Builder()
//                .connectTimeout(20, TimeUnit.SECONDS)
//                .readTimeout(20, TimeUnit.SECONDS)
//                .writeTimeout(20, TimeUnit.SECONDS)
//                .build()
//            val request = Request.Builder()
//                .url(Constant.ANILIST_API_URL)
//                .post(body)
//                .build()
//            response = okHttpClient.newCall(request).execute()
//        }.join()
//        println(response?.code)
//        val responseString = response?.body?.string().toString()
//        println(responseString)
//        val about =
//            JSONObject(responseString).getJSONObject("data").getJSONObject("User").get("about")
//                .toString()
//        println(about)
//        Constant.user_about = about
        setupObserver()
    }
}
