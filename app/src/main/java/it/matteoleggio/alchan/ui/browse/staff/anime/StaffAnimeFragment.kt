package it.matteoleggio.alchan.ui.browse.staff.anime


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView

import it.matteoleggio.alchan.R
import it.matteoleggio.alchan.helper.enums.BrowsePage
import it.matteoleggio.alchan.helper.enums.ResponseStatus
import it.matteoleggio.alchan.helper.pojo.StaffMedia
import it.matteoleggio.alchan.helper.utils.DialogUtility
import it.matteoleggio.alchan.ui.base.BaseFragment
import it.matteoleggio.alchan.ui.browse.media.MediaFragment
import it.matteoleggio.alchan.ui.browse.staff.StaffFragment
import kotlinx.android.synthetic.main.fragment_staff_anime.*
import kotlinx.android.synthetic.main.layout_empty.*
import kotlinx.android.synthetic.main.layout_loading.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import type.MediaType
import java.util.*

/**
 * A simple [Fragment] subclass.
 */
class StaffAnimeFragment : BaseFragment() {

    private val viewModel by viewModel<StaffAnimeViewModel>()

    private lateinit var adapter: StaffAnimeRvAdapter
    private var isLoading = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_staff_anime, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel.staffId = arguments?.getInt(StaffFragment.STAFF_ID)
        adapter = assignAdapter()
        staffAnimeRecyclerView.adapter = adapter

        initLayout()
        setupObserver()
    }

    private fun assignAdapter(): StaffAnimeRvAdapter {
        return StaffAnimeRvAdapter(requireActivity(), viewModel.staffMedia, object : StaffAnimeRvAdapter.StaffAnimeListener {
            override fun passSelectedMedia(mediaId: Int, mediaType: MediaType) {
                listener?.changeFragment(BrowsePage.valueOf(mediaType.name), mediaId)
            }
        })
    }

    private fun setupObserver() {
        viewModel.staffMediaData.observe(viewLifecycleOwner, Observer {
            loadingLayout.visibility = View.GONE
            when (it.responseStatus) {
                ResponseStatus.SUCCESS -> {
                    if (it?.data?.staff?.id != viewModel.staffId) {
                        return@Observer
                    }

                    if (isLoading) {
                        viewModel.staffMedia.removeAt(viewModel.staffMedia.lastIndex)
                        adapter.notifyItemRemoved(viewModel.staffMedia.size)
                        isLoading = false
                    }

                    if (!viewModel.hasNextPage) {
                        return@Observer
                    }

                    viewModel.hasNextPage = it.data?.staff?.staffMedia?.pageInfo?.hasNextPage ?: false
                    viewModel.page += 1
                    viewModel.isInit = true

                    it.data?.staff?.staffMedia?.edges?.forEach { edge ->
                        val staffMedia = StaffMedia(
                            edge?.node?.id,
                            edge?.node?.title?.userPreferred,
                            edge?.node?.coverImage?.large,
                            edge?.node?.type,
                            edge?.staffRole
                        )
                        viewModel.staffMedia.add(staffMedia)
                    }

                    adapter.notifyDataSetChanged()
                    emptyLayout.visibility = if (viewModel.staffMedia.isNullOrEmpty()) View.VISIBLE else View.GONE
                    animeSortLayout.visibility = if (viewModel.staffMedia.isNullOrEmpty()) View.GONE else View.VISIBLE
                }
                ResponseStatus.ERROR -> {
                    DialogUtility.showToast(activity, it.message)
                    if (isLoading) {
                        viewModel.staffMedia.removeAt(viewModel.staffMedia.lastIndex)
                        adapter.notifyItemRemoved(viewModel.staffMedia.size)
                        isLoading = false
                    }
                    emptyLayout.visibility = if (viewModel.staffMedia.isNullOrEmpty()) View.VISIBLE else View.GONE
                    if (!viewModel.isInit) {
                        retryButton.visibility = View.VISIBLE
                        retryButton.setOnClickListener { viewModel.getStaffMedia() }
                    } else {
                        retryButton.visibility = View.GONE
                    }
                }
            }
        })

        if (!viewModel.isInit) {
            viewModel.getStaffMedia()
            loadingLayout.visibility = View.VISIBLE
        }
    }

    private fun initLayout() {
        staffAnimeRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

                if (newState == RecyclerView.SCROLL_STATE_IDLE && !recyclerView.canScrollVertically(1) && viewModel.isInit && !isLoading) {
                    loadMore()
                    isLoading = true
                }
            }
        })

        animeSortLayout.visibility = if (viewModel.staffMedia.isNullOrEmpty()) View.GONE else View.VISIBLE

        animeSortText.text = getString(viewModel.mediaSortArray[viewModel.mediaSortList.indexOf(viewModel.sortBy)]).toUpperCase(Locale.US)
        animeSortText.setOnClickListener {
            val stringArray = viewModel.mediaSortArray.map { sort -> getString(sort).toUpperCase(Locale.US) }.toTypedArray()
            AlertDialog.Builder(requireContext())
                .setItems(stringArray) { _, which ->
                    viewModel.changeSortMedia(viewModel.mediaSortList[which])
                    animeSortText.text = stringArray[which]

                    loadingLayout.visibility = View.VISIBLE
                    isLoading = false
                    viewModel.getStaffMedia(true)
                }
                .show()
        }

        animeShowOnListCheckBox.setOnClickListener {
            viewModel.onlyShowOnList = animeShowOnListCheckBox.isChecked

            loadingLayout.visibility = View.VISIBLE
            isLoading = false
            viewModel.getStaffMedia(true)
        }

        animeShowOnListText.setOnClickListener {
            animeShowOnListCheckBox.performClick()
        }
    }

    private fun loadMore() {
        if (viewModel.hasNextPage) {
            viewModel.staffMedia.add(null)
            adapter.notifyItemInserted(viewModel.staffMedia.lastIndex)
            viewModel.getStaffMedia()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        staffAnimeRecyclerView.adapter = null
    }
}
