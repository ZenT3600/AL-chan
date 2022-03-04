package it.matteoleggio.alchan.ui.browse.media.social


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer

import it.matteoleggio.alchan.R
import it.matteoleggio.alchan.helper.enums.BrowsePage
import it.matteoleggio.alchan.helper.enums.ResponseStatus
import it.matteoleggio.alchan.helper.utils.DialogUtility
import it.matteoleggio.alchan.ui.base.BaseFragment
import it.matteoleggio.alchan.ui.browse.media.MediaFragment
import kotlinx.android.synthetic.main.fragment_media_social.*
import kotlinx.android.synthetic.main.layout_loading.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import type.MediaType

/**
 * A simple [Fragment] subclass.
 */
class MediaSocialFragment : BaseFragment() {

    private val viewModel by viewModel<MediaSocialViewModel>()

    private lateinit var friendsAdapter: FriendsMediaListRvAdapter
    private lateinit var activityAdapter: MediaActivityRvAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_media_social, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel.mediaId = arguments?.getInt(MediaFragment.MEDIA_ID)
        viewModel.mediaType = MediaType.valueOf(arguments?.getString(MediaFragment.MEDIA_TYPE)!!)

        friendsAdapter = assignFriendsAdapter()
        friendsListRecyclerView.adapter = friendsAdapter

        activityAdapter = assignMediaActivity()
        recentActivityRecyclerView.adapter = activityAdapter

        setupObserver()
        initLayout()
    }

    private fun setupObserver() {
        viewModel.mediaFriendsMediaListData.observe(viewLifecycleOwner, Observer {
            when (it.responseStatus) {
                ResponseStatus.SUCCESS -> {
                    if (it.data?.page?.mediaList?.isNullOrEmpty() == false && it.data.page.mediaList[0]?.media?.id != viewModel.mediaId) {
                        return@Observer
                    }

                    viewModel.friendsHasNextPage = it.data?.page?.pageInfo?.hasNextPage ?: false
                    viewModel.friendsIsInit = true
                    viewModel.friendsPage += 1

                    it.data?.page?.mediaList?.forEach { mediaList ->
                        viewModel.friendsMediaList.add(mediaList)
                    }

                    if (viewModel.friendsHasNextPage) {
                        viewModel.getMediaFriendsMediaList()
                    } else {
                        friendsAdapter.notifyDataSetChanged()
                        if (viewModel.friendsMediaList.isNullOrEmpty()) {
                            friendsListLayout.visibility = View.GONE
                        } else {
                            friendsListLayout.visibility = View.VISIBLE
                        }
                    }
                }
                ResponseStatus.ERROR -> {
                    DialogUtility.showToast(activity, it.message)
                    if (viewModel.friendsMediaList.isNullOrEmpty()) {
                        friendsListLayout.visibility = View.GONE
                    } else {
                        friendsListLayout.visibility = View.VISIBLE
                    }
                }
            }
        })

        viewModel.mediaActivityData.observe(viewLifecycleOwner, Observer {
            when (it.responseStatus) {
                ResponseStatus.LOADING -> loadingLayout.visibility = View.VISIBLE
                ResponseStatus.SUCCESS -> {
                    loadingLayout.visibility = View.GONE

                    if (it.data?.page?.activities?.isNullOrEmpty() == false && it.data.page.activities[0]?.fragments?.onListActivity?.media?.id != viewModel.mediaId) {
                        return@Observer
                    }

                    viewModel.activityHasNextPage = it.data?.page?.pageInfo?.hasNextPage ?: false
                    viewModel.activityIsInit = true
                    viewModel.activityPage += 1

                    it.data?.page?.activities?.forEach { activity ->
                        viewModel.activityList.add(activity)
                    }

                    activityAdapter.notifyDataSetChanged()
                }
                ResponseStatus.ERROR -> {
                    loadingLayout.visibility = View.GONE
                    DialogUtility.showToast(activity, it.message)
                }
            }
        })

        viewModel.triggerMediaSocial.observe(viewLifecycleOwner, Observer {
            viewModel.refresh()
        })

        if (!viewModel.friendsIsInit) {
            viewModel.getMediaFriendsMediaList()
        } else {
            if (viewModel.friendsMediaList.isNullOrEmpty()) {
                friendsListLayout.visibility = View.GONE
            } else {
                friendsListLayout.visibility = View.VISIBLE
            }
        }

        if (!viewModel.activityIsInit) {
            viewModel.getMediaActivity()
        }
    }

    private fun initLayout() {
        recentActivityViewMore.setOnClickListener {
            viewModel.getMediaActivity()
        }
    }

    private fun assignFriendsAdapter(): FriendsMediaListRvAdapter {
        return FriendsMediaListRvAdapter(requireActivity(), viewModel.friendsMediaList, object : FriendsMediaListRvAdapter.FriendsMediaListListener {
            override fun passSelectedUser(userId: Int) {
                listener?.changeFragment(BrowsePage.USER, userId)
            }
        })
    }

    private fun assignMediaActivity(): MediaActivityRvAdapter {
        return MediaActivityRvAdapter(requireActivity(), viewModel.activityList, object : MediaActivityRvAdapter.MediaActivityListener {
            override fun passSelectedActivity(id: Int) {
                listener?.changeFragment(BrowsePage.ACTIVITY_DETAIL, id)
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        friendsListRecyclerView.adapter = null
        recentActivityRecyclerView.adapter = null
    }
}
