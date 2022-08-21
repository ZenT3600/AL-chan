package it.matteoleggio.alchan.ui.browse.character


import UserQuery
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.util.DisplayMetrics
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AlertDialog
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Input
import com.apollographql.apollo.rx2.Rx2Apollo
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.appbar.AppBarLayout
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.stfalcon.imageviewer.StfalconImageViewer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

import it.matteoleggio.alchan.R
import it.matteoleggio.alchan.data.localstorage.LocalStorage
import it.matteoleggio.alchan.data.localstorage.LocalStorageImpl
import it.matteoleggio.alchan.data.network.ApolloHandler
import it.matteoleggio.alchan.data.network.CountryCodeAdapter
import it.matteoleggio.alchan.data.network.JsonAdapter
import it.matteoleggio.alchan.data.network.Resource
import it.matteoleggio.alchan.data.response.Hated
import it.matteoleggio.alchan.data.response.User
import it.matteoleggio.alchan.data.response.UserResponse
import it.matteoleggio.alchan.helper.*
import it.matteoleggio.alchan.helper.enums.BrowsePage
import it.matteoleggio.alchan.helper.enums.ResponseStatus
import it.matteoleggio.alchan.helper.libs.GlideApp
import it.matteoleggio.alchan.helper.libs.SingleLiveEvent
import it.matteoleggio.alchan.helper.pojo.CharacterMedia
import it.matteoleggio.alchan.helper.pojo.CharacterVoiceActors
import it.matteoleggio.alchan.helper.utils.AndroidUtility
import it.matteoleggio.alchan.helper.utils.DialogUtility
import it.matteoleggio.alchan.ui.base.BaseFragment
import kotlinx.android.synthetic.main.fragment_character.*
import kotlinx.android.synthetic.main.layout_loading.*
import okhttp3.*
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import org.koin.androidx.viewmodel.ext.android.viewModel
import type.CustomType
import type.MediaFormat
import type.MediaSort
import type.MediaType
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread
import kotlin.math.abs

/**
 * A simple [Fragment] subclass.
 */
class CharacterFragment : BaseFragment() {

    private val viewModel by viewModel<CharacterViewModel>()
    private val _userQueryResponse = SingleLiveEvent<Resource<UserQuery.Data>>()
    private val userQueryResponse: LiveData<Resource<UserQuery.Data>>
        get() = _userQueryResponse

    private lateinit var scaleUpAnim: Animation
    private lateinit var scaleDownAnim: Animation

    private lateinit var adapter: CharacterMediaRvAdapter
    private lateinit var voiceActorAdapter: CharacterVoiceActorRvAdapter
    private var itemOpenAniList: MenuItem? = null
    private var itemCopyLink: MenuItem? = null

    companion object {
        const val CHARACTER_ID = "charactedId"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_character, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        characterToolbar.doOnApplyWindowInsets { view, windowInsets, initialPadding ->
            view.updateTopPadding(windowInsets, initialPadding)
        }

        viewModel.characterId = arguments?.getInt(CHARACTER_ID)
        scaleUpAnim = AnimationUtils.loadAnimation(activity, R.anim.scale_up)
        scaleDownAnim = AnimationUtils.loadAnimation(activity, R.anim.scale_down)

        characterToolbar.setNavigationOnClickListener { activity?.finish() }
        characterToolbar.navigationIcon = ContextCompat.getDrawable(requireActivity(), R.drawable.ic_delete)
        characterToolbar.inflateMenu(R.menu.menu_anilist_link)
        itemOpenAniList = characterToolbar.menu.findItem(R.id.itemOpenAnilist)
        itemCopyLink = characterToolbar.menu.findItem(R.id.itemCopyLink)

        adapter = assignAdapter()
        characterMediaRecyclerView.adapter = adapter

        voiceActorAdapter = assignVoiceActorAdapter()
        characterVoiceActorsRecyclerView.adapter = voiceActorAdapter

        setupObserver()
        initLayout()
    }

    override fun onStart() {
        super.onStart()
        viewModel.checkCharacterIsFavorite()
    }

    private fun setupObserver() {
        viewModel.characterData.observe(viewLifecycleOwner, Observer {
            when (it.responseStatus) {
                ResponseStatus.LOADING -> loadingLayout.visibility = View.VISIBLE
                ResponseStatus.SUCCESS -> {
                    loadingLayout.visibility = View.GONE
                    if (it.data?.character != null && it.data.character.id == viewModel.characterId) {
                        viewModel.currentCharacterData = it.data.character
                        setupHeader()
                        handleDescription()
                    }
                }
                ResponseStatus.ERROR -> {
                    loadingLayout.visibility = View.GONE
                    DialogUtility.showToast(activity, it.message)
                }
            }
        })

        viewModel.characterMediaData.observe(viewLifecycleOwner, Observer {
            if (it.responseStatus == ResponseStatus.SUCCESS) {
                if (it.data?.character?.id != viewModel.characterId) {
                    return@Observer
                }

                if (!viewModel.hasNextPage) {
                    return@Observer
                }

                viewModel.hasNextPage = it.data?.character?.media?.pageInfo?.hasNextPage ?: false
                viewModel.page += 1
                viewModel.isInit = true

                it.data?.character?.media?.edges?.forEach { edge ->
                    val characterMedia = CharacterMedia(
                        edge?.node?.id,
                        edge?.node?.title?.userPreferred,
                        edge?.node?.coverImage?.large,
                        edge?.node?.type,
                        edge?.node?.format,
                        "${edge?.node?.startDate?.year ?: 9999}${edge?.node?.startDate?.month ?: 12}${edge?.node?.startDate?.day ?: 31}".toInt(),
                        edge?.node?.averageScore,
                        edge?.node?.popularity,
                        edge?.node?.favourites,
                        edge?.node?.mediaListEntry?.status,
                        edge?.characterRole
                    )
                    viewModel.characterMedia.add(characterMedia)

                    edge?.voiceActors?.forEach { va ->
                        val findVa = viewModel.characterVoiceActors.find { cva -> cva.voiceActorId == va?.id }
                        if (findVa != null) {
                            val vaIndex = viewModel.characterVoiceActors.indexOf(findVa)
                            viewModel.characterVoiceActors[vaIndex].characterMediaList?.add(characterMedia)
                        } else {
                            val voiceActor = CharacterVoiceActors(
                                va?.id, va?.name?.full, va?.image?.large, va?.language, arrayListOf(characterMedia)
                            )
                            viewModel.characterVoiceActors.add(voiceActor)
                        }
                    }
                }

                if (viewModel.hasNextPage) {
                    viewModel.getCharacterMedia()
                } else {
                    adapter = assignAdapter()
                    characterMediaRecyclerView.adapter = adapter

                    voiceActorAdapter = assignVoiceActorAdapter()
                    characterVoiceActorsRecyclerView.adapter = voiceActorAdapter
                    characterVoiceActorsLayout.visibility = if (viewModel.characterVoiceActors.isNullOrEmpty()) View.GONE else View.VISIBLE
                }
            }
        })

        viewModel.characterIsFavoriteData.observe(viewLifecycleOwner, Observer {
            if (it.data?.character?.id != viewModel.characterId) {
                return@Observer
            }

            if (it.responseStatus == ResponseStatus.SUCCESS) {
                if (it.data?.character?.isFavourite == true) {
                    characterFavoriteButton.text = getString(R.string.favorited)
                    characterFavoriteButton.setTextColor(AndroidUtility.getResValueFromRefAttr(context, R.attr.themePrimaryColor))
                    characterFavoriteButton.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireActivity(), android.R.color.transparent))
                    characterFavoriteButton.strokeColor = ColorStateList.valueOf(AndroidUtility.getResValueFromRefAttr(context, R.attr.themePrimaryColor))
                    characterFavoriteButton.strokeWidth = 2
                } else {
                    characterFavoriteButton.text = getString(R.string.set_as_favorite)
                    characterFavoriteButton.setTextColor(AndroidUtility.getResValueFromRefAttr(context, R.attr.themeBackgroundColor))
                    characterFavoriteButton.backgroundTintList = ColorStateList.valueOf(AndroidUtility.getResValueFromRefAttr(context, R.attr.themePrimaryColor))
                    characterFavoriteButton.strokeWidth = 0
                }

                val hated = HatedHelper(Constant.user_about).getHatedCharactersSelf()
                var found = false
                for (h in hated) {
                    if (h == null) {
                        continue
                    }
                    if (it.data?.character?.id == h.id) {
                        found = true
                        characterHatedButton.text = getString(R.string.hated)
                        characterHatedButton.setTextColor(AndroidUtility.getResValueFromRefAttr(context, R.attr.themePrimaryColor))
                        characterHatedButton.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireActivity(), android.R.color.transparent))
                        characterHatedButton.strokeColor = ColorStateList.valueOf(AndroidUtility.getResValueFromRefAttr(context, R.attr.themePrimaryColor))
                        characterHatedButton.strokeWidth = 2
                        characterHatedButton.setOnClickListener {
                            val gson = GsonBuilder().serializeSpecialFloatingPointValues().create()
                            val l = LocalStorageImpl(context!!, Constant.SHARED_PREFERENCES_NAME, gson)
                            HatedHelper(Constant.user_about).removeHatedCharacter(h, l.bearerToken!!)
                            characterHatedButton.text = getString(R.string.set_as_hated)
                            characterHatedButton.setTextColor(AndroidUtility.getResValueFromRefAttr(context, R.attr.themeBackgroundColor))
                            characterHatedButton.backgroundTintList = ColorStateList.valueOf(AndroidUtility.getResValueFromRefAttr(context, R.attr.themePrimaryColor))
                            characterHatedButton.strokeWidth = 0
                        }
                        break
                    } else {
                        continue
                    }
                }
                if (!found) {
                    characterHatedButton.text = getString(R.string.set_as_hated)
                    characterHatedButton.setTextColor(AndroidUtility.getResValueFromRefAttr(context, R.attr.themeBackgroundColor))
                    characterHatedButton.backgroundTintList = ColorStateList.valueOf(AndroidUtility.getResValueFromRefAttr(context, R.attr.themePrimaryColor))
                    characterHatedButton.strokeWidth = 0
                    characterHatedButton.setOnClickListener {
                        val gson = GsonBuilder().serializeSpecialFloatingPointValues().create()
                        val l = LocalStorageImpl(context!!, Constant.SHARED_PREFERENCES_NAME, gson)
                        HatedHelper(Constant.user_about).addHatedCharacter(viewModel.characterData.value?.data?.character?.id!!, viewModel.characterData.value?.data?.character?.image?.large!!, l.bearerToken!!)
                        characterHatedButton.text = getString(R.string.hated)
                        characterHatedButton.setTextColor(AndroidUtility.getResValueFromRefAttr(context, R.attr.themePrimaryColor))
                        characterHatedButton.backgroundTintList = ColorStateList.valueOf(AndroidUtility.getResValueFromRefAttr(context, R.attr.themeBackgroundColor))
                        characterHatedButton.strokeWidth = 0
                    }
                }

                characterHatedButton.isEnabled = true
            }
        })


        viewModel.toggleFavouriteResponse.observe(this, Observer {
            when (it.responseStatus) {
                ResponseStatus.LOADING -> loadingLayout.visibility = View.VISIBLE
                ResponseStatus.SUCCESS -> {
                    viewModel.checkCharacterIsFavorite()
                    loadingLayout.visibility = View.GONE
                    DialogUtility.showToast(activity, R.string.change_saved)
                }
                ResponseStatus.ERROR -> {
                    loadingLayout.visibility = View.GONE
                    DialogUtility.showToast(activity, it.message)
                }
            }
        })

        viewModel.getCharacter()
        if (!viewModel.isInit) {
            viewModel.getCharacterMedia()
        }
    }

    private fun setupHeader() {
        GlideApp.with(this).load(viewModel.currentCharacterData?.image?.large).apply(RequestOptions.circleCropTransform()).into(characterImage)

        if (viewModel.currentCharacterData?.image?.large != null) {
            characterImage.setOnClickListener {
                StfalconImageViewer.Builder<String>(context, arrayOf(viewModel.currentCharacterData?.image?.large)) { view, image ->
                    GlideApp.with(requireContext()).load(image).into(view)
                }.withTransitionFrom(characterImage).withHiddenStatusBar(false).show(true)
            }
        }

        characterNameText.text = viewModel.currentCharacterData?.name?.full
        characterNativeNameText.text = viewModel.currentCharacterData?.name?.native_

        if (!viewModel.currentCharacterData?.name?.alternative.isNullOrEmpty()) {
            var aliasesString = ""
            viewModel.currentCharacterData?.name?.alternative?.forEachIndexed { index, s ->
                aliasesString += s
                if (index != viewModel.currentCharacterData?.name?.alternative?.lastIndex) aliasesString += ", "
            }
            characterAliasesText.text = aliasesString

            if (aliasesString.isBlank()) {
                characterAliasesText.visibility = View.GONE
            } else {
                characterAliasesText.visibility = View.VISIBLE
            }
        } else {
            characterAliasesText.visibility = View.GONE
        }

        characterFavoriteCountText.text = viewModel.currentCharacterData?.favourites?.toString()

        itemOpenAniList?.isVisible = true
        itemCopyLink?.isVisible = true

        itemOpenAniList?.setOnMenuItemClickListener {
            CustomTabsIntent.Builder()
                .build()
                .launchUrl(requireActivity(), Uri.parse(viewModel.currentCharacterData?.siteUrl))
            true
        }

        itemCopyLink?.setOnMenuItemClickListener {
            AndroidUtility.copyToClipboard(activity, viewModel.currentCharacterData?.siteUrl!!)
            DialogUtility.showToast(activity, R.string.link_copied)
            true
        }
    }

    private fun initLayout() {
        characterRefreshLayout.setOnRefreshListener {
            characterRefreshLayout.isRefreshing = false
            viewModel.getCharacter()
            viewModel.checkCharacterIsFavorite()
            if (!viewModel.isInit) {
                viewModel.getCharacterMedia()
            }
        }

        characterAppBarLayout.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
            // disable refresh when toolbar is not fully expanded
            characterRefreshLayout?.isEnabled = verticalOffset == 0

            // 50 is magic number gotten from trial and error
            if (abs(verticalOffset) - appBarLayout.totalScrollRange >= -50) {
                if (characterBannerContentLayout?.isVisible == true) {
                    characterBannerContentLayout?.startAnimation(scaleDownAnim)
                    characterBannerContentLayout?.visibility = View.INVISIBLE
                }
            } else {
                if (characterBannerContentLayout?.isInvisible == true) {
                    characterBannerContentLayout?.startAnimation(scaleUpAnim)
                    characterBannerContentLayout?.visibility = View.VISIBLE
                }
            }
        })

        characterFavoriteButton.setOnClickListener {
            viewModel.updateFavorite()
        }

        characterVoiceActorsLayout.visibility = if (viewModel.characterVoiceActors.isNullOrEmpty()) View.GONE else View.VISIBLE

        seriesFilterIcon.setOnClickListener {
            val dialog = FilterCharacterMediaBottomSheet()
            dialog.setListener(object : FilterCharacterMediaBottomSheet.FilterCharacterMediaListener {
                override fun passFilterData(
                    sortBy: MediaSort?,
                    orderByDescending: Boolean,
                    selectedFormats: ArrayList<MediaFormat>?,
                    showOnlyOnList: Boolean?
                ) {
                    viewModel.changeMediaSort(sortBy, orderByDescending)
                    viewModel.selectedFormats = selectedFormats
                    viewModel.showOnlyOnList = showOnlyOnList

                    adapter = assignAdapter()
                    characterMediaRecyclerView.adapter = adapter
                }
            })
            val bundle = Bundle()
            if (viewModel.sortBy != null) {
                bundle.putString(FilterCharacterMediaBottomSheet.SORT_BY, viewModel.sortBy?.name)
                bundle.putBoolean(FilterCharacterMediaBottomSheet.ORDER_BY_DESCENDING, viewModel.orderByDescending)

                if (!viewModel.selectedFormats.isNullOrEmpty()) {
                    bundle.putString(FilterCharacterMediaBottomSheet.SELECTED_FORMATS, viewModel.getSerializedSelectedFormats())
                }

                if (viewModel.showOnlyOnList != null) {
                    bundle.putBoolean(FilterCharacterMediaBottomSheet.SHOW_ONLY_ON_LIST, viewModel.showOnlyOnList!!)
                }
            }
            dialog.arguments = bundle
            dialog.show(childFragmentManager, null)
        }
    }

    private fun handleDescription() {
        characterDescriptionText.text = viewModel.currentCharacterData?.description?.handleSpoilerAndLink(requireActivity()) { page, id ->
            listener?.changeFragment(page, id)
        }
        characterDescriptionText.movementMethod = LinkMovementMethod.getInstance()

        characterDescriptionArrow.setOnClickListener {
            if (dummyCharacterDescriptionText.isVisible) {
                dummyCharacterDescriptionText.visibility = View.GONE
                GlideApp.with(this).load(R.drawable.ic_chevron_up).into(characterDescriptionArrow)
            } else {
                dummyCharacterDescriptionText.visibility = View.VISIBLE
                GlideApp.with(this).load(R.drawable.ic_chevron_down).into(characterDescriptionArrow)
            }
        }
    }

    private fun assignAdapter(): CharacterMediaRvAdapter {
        return CharacterMediaRvAdapter(requireActivity(), viewModel.getFilteredMedia(), object : CharacterMediaRvAdapter.CharacterMediaListener {
            override fun passSelectedMedia(mediaId: Int, mediaType: MediaType) {
                listener?.changeFragment(BrowsePage.valueOf(mediaType.name), mediaId)
            }
        })
    }

    private fun assignVoiceActorAdapter(): CharacterVoiceActorRvAdapter {
        val metrics = DisplayMetrics()
        activity?.windowManager?.defaultDisplay?.getMetrics(metrics)
        val width = metrics.widthPixels / resources.getInteger(R.integer.horizontalListCharacterDivider)
        return CharacterVoiceActorRvAdapter(requireActivity(), viewModel.characterVoiceActors, width, object : CharacterVoiceActorRvAdapter.CharacterVoiceActorListener {
            override fun passSelectedVoiceActor(voiceActorId: Int) {
                listener?.changeFragment(BrowsePage.STAFF, voiceActorId)
            }

            override fun showMediaList(list: List<CharacterMedia>) {
                val titleList = ArrayList<String>()
                list.forEach {
                    titleList.add("${it.mediaTitle} (${it.mediaFormat})")
                }
                AlertDialog.Builder(requireActivity())
                    .setItems(titleList.toTypedArray()) { _, which ->
                        listener?.changeFragment(BrowsePage.valueOf(list[which].mediaType?.name!!), list[which].mediaId!!)
                    }
                    .show()
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        characterMediaRecyclerView.adapter = null
        characterVoiceActorsRecyclerView.adapter = null
        itemOpenAniList = null
        itemCopyLink = null
    }
}
