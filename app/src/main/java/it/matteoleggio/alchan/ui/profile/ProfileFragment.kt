package it.matteoleggio.alchan.ui.profile


import android.content.Intent
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import androidx.core.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.textview.MaterialTextView
import com.stfalcon.imageviewer.StfalconImageViewer

import it.matteoleggio.alchan.R
import it.matteoleggio.alchan.helper.Constant
import it.matteoleggio.alchan.helper.doOnApplyWindowInsets
import it.matteoleggio.alchan.helper.enums.BrowsePage
import it.matteoleggio.alchan.helper.enums.FollowPage
import it.matteoleggio.alchan.helper.enums.ProfileSection
import it.matteoleggio.alchan.helper.enums.ResponseStatus
import it.matteoleggio.alchan.helper.libs.GlideApp
import it.matteoleggio.alchan.helper.updateTopPadding
import it.matteoleggio.alchan.helper.utils.AndroidUtility
import it.matteoleggio.alchan.helper.utils.DialogUtility
import it.matteoleggio.alchan.ui.base.BaseMainFragment
import it.matteoleggio.alchan.ui.browse.BrowseActivity
import it.matteoleggio.alchan.ui.notification.NotificationActivity
import it.matteoleggio.alchan.ui.profile.bio.BioFragment
import it.matteoleggio.alchan.ui.profile.favorites.FavoritesFragment
import it.matteoleggio.alchan.ui.profile.hated.HatedFragment
import it.matteoleggio.alchan.ui.profile.follows.FollowsActivity
import it.matteoleggio.alchan.ui.profile.reviews.UserReviewsFragment
import it.matteoleggio.alchan.ui.settings.SettingsActivity
import it.matteoleggio.alchan.ui.profile.stats.StatsFragment
import it.matteoleggio.alchan.ui.settings.ModSettingsActivity
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.android.synthetic.main.layout_loading.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlin.math.abs

class ProfileFragment : BaseMainFragment() {

    private val viewModel by viewModel<ProfileViewModel>()

    private var userId: Int? = null

    private lateinit var profileSectionMap: HashMap<ProfileSection, Pair<ImageView, TextView>>
    private lateinit var profileFragmentList: List<Fragment>

    private lateinit var scaleUpAnim: Animation
    private lateinit var scaleDownAnim: Animation

    private lateinit var itemActivity: MenuItem
    private lateinit var itemNotifications: MenuItem
    private lateinit var itemSettings: MenuItem
    private lateinit var itemSettingsMod: MenuItem
    private lateinit var itemViewInAniList: MenuItem
    private lateinit var itemShareProfile: MenuItem
    private lateinit var itemCopyLink: MenuItem

    private lateinit var notificationActionView: View
    private lateinit var badgeCount: MaterialTextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        profileToolbar.doOnApplyWindowInsets { view, windowInsets, initialPadding ->
            view.updateTopPadding(windowInsets, initialPadding)
        }

        profileSectionMap = hashMapOf(
            Pair(ProfileSection.BIO, Pair(profileBioIcon, profileBioText)),
            Pair(ProfileSection.FAVORITES, Pair(profileFavoritesIcon, profileFavoritesText)),
            Pair(ProfileSection.HATED, Pair(profileHatedIcon, profileHatedText)),
            Pair(ProfileSection.STATS, Pair(profileStatsIcon, profileStatsText)),
            Pair(ProfileSection.REVIEWS, Pair(profileReviewsIcon, profileReviewsText))
        )

        profileFragmentList = arrayListOf(BioFragment(), FavoritesFragment(), HatedFragment(), StatsFragment(), UserReviewsFragment())

        scaleUpAnim = AnimationUtils.loadAnimation(activity, R.anim.scale_up)
        scaleDownAnim = AnimationUtils.loadAnimation(activity, R.anim.scale_down)

        profileToolbar.menu.apply {
            itemActivity = findItem(R.id.itemActivity)
            itemNotifications = findItem(R.id.itemNotifications)
            itemSettings = findItem(R.id.itemSettings)
            itemSettingsMod = findItem(R.id.itemSettingsMod)
            itemViewInAniList = findItem(R.id.itemViewOnAniList)
            itemShareProfile = findItem(R.id.itemShareProfile)
            itemCopyLink = findItem(R.id.itemCopyLink)
        }

        profileToolbar.overflowIcon = ContextCompat.getDrawable(requireContext(), R.drawable.custom_more_icon)

        notificationActionView = itemNotifications.actionView
        badgeCount = notificationActionView.findViewById(R.id.notification_badge)

        setupObserver()
        initLayout()
    }

    private fun setupObserver() {
        viewModel.currentSection.observe(viewLifecycleOwner, Observer {
            setupSection()
        })

        viewModel.viewerData.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                this.userId = it.id
                initLayout()
            }
        })

        viewModel.followersCount.observe(viewLifecycleOwner, Observer {
            profileFollowersCountText.text = it.toString()
        })

        viewModel.followingsCount.observe(viewLifecycleOwner, Observer {
            profileFollowingCountText.text = it.toString()
        })

        viewModel.viewerDataResponse.observe(viewLifecycleOwner, Observer {
            when (it.responseStatus) {
                ResponseStatus.LOADING -> {
                    profileRefreshLayout.isRefreshing = false
                    loadingLayout.visibility = View.VISIBLE
                }
                ResponseStatus.SUCCESS -> loadingLayout.visibility = View.GONE
                ResponseStatus.ERROR -> {
                    loadingLayout.visibility = View.GONE
                    DialogUtility.showToast(activity, it.message)
                }
            }
        })

        viewModel.notificationCount.observe(viewLifecycleOwner, Observer {
            if (it != 0) {
                if (it > 99) {
                    badgeCount.text = "99+"
                } else {
                    badgeCount.text = it.toString()
                }
                badgeCount.visibility = View.VISIBLE
            } else {
                badgeCount.visibility = View.GONE
            }
        })

        viewModel.initData()
    }

    private fun initLayout() {
        val user = viewModel.viewerData.value

        profileRefreshLayout.setOnRefreshListener {
            profileRefreshLayout.isRefreshing = false
            viewModel.retrieveViewerData()
            viewModel.triggerRefreshChildFragments()
        }

        GlideApp.with(this).load(user?.bannerImage).into(profileBannerImage)
        if (user?.bannerImage != null) {
            profileBannerImage.setOnClickListener {
                StfalconImageViewer.Builder<String>(context, arrayOf(user.bannerImage)) { view, image ->
                    GlideApp.with(context!!).load(image).into(view)
                }.withTransitionFrom(profileBannerImage).withHiddenStatusBar(false).show(true)
            }
        }

        if (viewModel.circularAvatar) {
            profileAvatarImage.background = ContextCompat.getDrawable(activity!!, R.drawable.shape_oval_transparent)
            if (viewModel.whiteBackgroundAvatar) {
                profileAvatarImage.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(activity!!, R.color.white))
            } else {
                profileAvatarImage.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(activity!!, android.R.color.transparent))
            }
            GlideApp.with(this).load(user?.avatar?.large).apply(RequestOptions.circleCropTransform()).into(profileAvatarImage)
        } else {
            profileAvatarImage.background = ContextCompat.getDrawable(activity!!, R.drawable.shape_rectangle_transparent)
            profileAvatarImage.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(activity!!, android.R.color.transparent))
            GlideApp.with(this).load(user?.avatar?.large).into(profileAvatarImage)
        }

        if (user?.avatar?.large != null) {
            profileAvatarImage.setOnClickListener {
                StfalconImageViewer.Builder<String>(context, arrayOf(user.avatar.large)) { view, image ->
                    GlideApp.with(context!!).load(image).into(view)
                }.withTransitionFrom(profileAvatarImage).withHiddenStatusBar(false).show(true)
            }
        }

        profileUsernameText.text = user?.name ?: ""

        if (!user?.moderatorStatus.isNullOrBlank()) {
            modCard.visibility = View.VISIBLE
            modText.text = user?.moderatorStatus?.split(" ")?.map { it.toLowerCase().capitalize() }?.joinToString(" ")
        } else {
            modCard.visibility = View.GONE
        }

        if (user?.donatorTier != null && user.donatorTier != 0) {
            donatorCard.visibility = View.VISIBLE
            donatorText.text = user.donatorBadge
        } else {
            donatorCard.visibility = View.GONE
        }

        if (!user?.moderatorStatus.isNullOrBlank() && user?.donatorTier != null && user.donatorTier != 0) {
            badgeSpace.visibility = View.VISIBLE
        } else {
            badgeSpace.visibility = View.GONE
        }

        profileAnimeCountText.text = user?.statistics?.anime?.count?.toString() ?: "0"
        profileMangaCountText.text = user?.statistics?.manga?.count?.toString() ?: "0"
        profileFollowersCountText.text = viewModel.followersCount.value?.toString() ?: "0"
        profileFollowingCountText.text = viewModel.followingsCount.value?.toString() ?: "0"

        profileAnimeCountLayout.setOnClickListener {
            listener?.changeMenu(R.id.itemAnime)
        }
        profileMangaCountLayout.setOnClickListener {
            listener?.changeMenu(R.id.itemManga)
        }
        profileFollowingCountLayout.setOnClickListener {
            val intent = Intent(activity, FollowsActivity::class.java)
            intent.putExtra(FollowsActivity.START_POSITION, FollowPage.FOLLOWING.ordinal)
            startActivity(intent)
        }
        profileFollowersCountLayout.setOnClickListener {
            val intent = Intent(activity, FollowsActivity::class.java)
            intent.putExtra(FollowsActivity.START_POSITION, FollowPage.FOLLOWERS.ordinal)
            startActivity(intent)
        }

        profileBioLayout.setOnClickListener { viewModel.setProfileSection(ProfileSection.BIO) }
        profileFavoritesLayout.setOnClickListener { viewModel.setProfileSection(ProfileSection.FAVORITES) }
        profileHatedLayout.setOnClickListener { viewModel.setProfileSection(ProfileSection.HATED) }
        profileStatsLayout.setOnClickListener { viewModel.setProfileSection(ProfileSection.STATS) }
        profileReviewsLayout.setOnClickListener { viewModel.setProfileSection(ProfileSection.REVIEWS) }

        if (profileViewPager.adapter == null) {
            profileViewPager.setPagingEnabled(false)
            profileViewPager.offscreenPageLimit = profileSectionMap.size
            profileViewPager.adapter = ProfileViewPagerAdapter(childFragmentManager, profileFragmentList)
        }

        viewModel.setProfileSection(viewModel.currentSection.value ?: ProfileSection.BIO)

        profileAppBarLayout.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
            // disable refresh when toolbar is not fully expanded
            profileRefreshLayout?.isEnabled = verticalOffset == 0

            // 50 is magic number gotten from trial and error
            if (abs(verticalOffset) - appBarLayout.totalScrollRange >= -50) {
                if (profileNumberLayout?.isVisible == true) {
                    profileNumberLayout?.startAnimation(scaleDownAnim)
                    profileNumberLayout?.visibility = View.INVISIBLE
                }
            } else {
                if (profileNumberLayout?.isInvisible == true) {
                    profileNumberLayout?.startAnimation(scaleUpAnim)
                    profileNumberLayout?.visibility = View.VISIBLE
                }
            }
        })

        itemActivity.isVisible = viewModel.enableSocial

        itemActivity.setOnMenuItemClickListener {
            val intent = Intent(activity, BrowseActivity::class.java)
            intent.putExtra(BrowseActivity.TARGET_PAGE, BrowsePage.ACTIVITY_LIST.name)
            intent.putExtra(BrowseActivity.LOAD_ID, user?.id)
            startActivity(intent)
            true
        }

        if (user?.unreadNotificationCount != null && user.unreadNotificationCount != 0) {
            if (user.unreadNotificationCount!! > 99) {
                badgeCount.text = "99+"
            } else {
                badgeCount.text = user.unreadNotificationCount?.toString()
            }
            badgeCount.visibility = View.VISIBLE
        } else {
            badgeCount.visibility = View.GONE
        }

        notificationActionView.setOnClickListener {
            startActivity(Intent(activity, NotificationActivity::class.java))
        }

        itemNotifications.setOnMenuItemClickListener {
            startActivity(Intent(activity, NotificationActivity::class.java))
            true
        }

        itemSettings.setOnMenuItemClickListener {
            startActivity(Intent(activity, SettingsActivity::class.java))
            true
        }

        itemSettingsMod.setOnMenuItemClickListener {
            startActivity(Intent(activity, ModSettingsActivity::class.java))
            true
        }

        itemViewInAniList.setOnMenuItemClickListener {
            if (user?.siteUrl == null) {
                DialogUtility.showToast(activity, R.string.some_data_has_not_been_retrieved)
            } else {
                CustomTabsIntent.Builder().build().launchUrl(activity!!, Uri.parse(user.siteUrl))
            }
            true
        }

        itemShareProfile.setOnMenuItemClickListener {
            if (user?.siteUrl == null) {
                DialogUtility.showToast(activity, R.string.some_data_has_not_been_retrieved)
            } else {
                val sendIntent = Intent()
                sendIntent.action = Intent.ACTION_SEND
                sendIntent.putExtra(Intent.EXTRA_TEXT, user.siteUrl)
                sendIntent.type = "text/plain"

                val shareIntent = Intent.createChooser(sendIntent, null)
                startActivity(shareIntent)
            }
            true
        }

        itemCopyLink.setOnMenuItemClickListener {
            if (user?.siteUrl == null) {
                DialogUtility.showToast(activity, R.string.some_data_has_not_been_retrieved)
            } else {
                AndroidUtility.copyToClipboard(activity, user.siteUrl)
                DialogUtility.showToast(activity, R.string.link_copied)
            }
            true
        }
    }

    private fun setupSection() {
        profileSectionMap.forEach {
            if (it.key == viewModel.currentSection.value) {
                it.value.first.imageTintList = ColorStateList.valueOf(AndroidUtility.getResValueFromRefAttr(activity, R.attr.themeSecondaryColor))
                it.value.second.setTextColor(AndroidUtility.getResValueFromRefAttr(activity, R.attr.themeSecondaryColor))
            } else {
                it.value.first.imageTintList = ColorStateList.valueOf(AndroidUtility.getResValueFromRefAttr(activity, R.attr.themeContentColor))
                it.value.second.setTextColor(AndroidUtility.getResValueFromRefAttr(activity, R.attr.themeContentColor))
            }
        }

        profileViewPager.currentItem = when (viewModel.currentSection.value) {
            ProfileSection.BIO -> 0
            ProfileSection.FAVORITES -> 1
            ProfileSection.HATED -> 2
            ProfileSection.STATS -> 3
            ProfileSection.REVIEWS -> 4
            else -> 0
        }
    }
}
