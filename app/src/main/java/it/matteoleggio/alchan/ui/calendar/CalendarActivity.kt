package it.matteoleggio.alchan.ui.calendar

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.viewpager.widget.ViewPager
import it.matteoleggio.alchan.R
import it.matteoleggio.alchan.helper.changeStatusBarColor
import it.matteoleggio.alchan.helper.doOnApplyWindowInsets
import it.matteoleggio.alchan.helper.enums.BrowsePage
import it.matteoleggio.alchan.helper.enums.ResponseStatus
import it.matteoleggio.alchan.helper.updateSidePadding
import it.matteoleggio.alchan.helper.updateTopPadding
import it.matteoleggio.alchan.helper.utils.AndroidUtility
import it.matteoleggio.alchan.helper.utils.DialogUtility
import it.matteoleggio.alchan.ui.base.BaseActivity
import it.matteoleggio.alchan.ui.browse.BrowseActivity
import kotlinx.android.synthetic.main.activity_calendar.*
import kotlinx.android.synthetic.main.layout_empty.*
import kotlinx.android.synthetic.main.layout_loading.*
import kotlinx.android.synthetic.main.layout_toolbar.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class CalendarActivity : BaseActivity() {

    private val viewModel by viewModel<CalendarViewModel>()

    private lateinit var dateAdapter: CalendarDateRvAdapter
    private lateinit var calendarViewPagerAdapter: CalendarViewPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar)

        changeStatusBarColor(AndroidUtility.getResValueFromRefAttr(this, R.attr.themeCardColor))
        setSupportActionBar(toolbarLayout)
        supportActionBar?.apply {
            title = getString(R.string.calendar)
            setDisplayHomeAsUpEnabled(true)
        }

        calendarLayout.doOnApplyWindowInsets { view, windowInsets, initialPadding ->
            view.updateSidePadding(windowInsets, initialPadding)
            view.updateTopPadding(windowInsets, initialPadding)
        }

        dateAdapter = assignDateAdapter()
        dateRecyclerView.adapter = dateAdapter

        setupObserver()
        initLayout()
    }

    private fun setupObserver() {
        viewModel.airingScheduleResponse.observe(this, androidx.lifecycle.Observer {
            when (it.responseStatus) {
                ResponseStatus.LOADING -> loadingLayout.visibility = View.VISIBLE
                ResponseStatus.SUCCESS -> {
                    if (!viewModel.hasNextPage) {
                        loadingLayout.visibility = View.GONE
                        return@Observer
                    }

                    viewModel.hasNextPage = it.data?.page?.pageInfo?.hasNextPage ?: false
                    viewModel.page += 1
                    viewModel.isInit = true
                    viewModel.scheduleList.addAll(it.data?.page?.airingSchedules?.filterNotNull() ?: listOf())

                    if (viewModel.hasNextPage) {
                        viewModel.getAiringSchedule()
                    } else {
                        loadingLayout.visibility = View.GONE
                        viewModel.filterList()
                    }
                }
                ResponseStatus.ERROR -> {
                    loadingLayout.visibility = View.GONE
                    DialogUtility.showToast(this, it.message)
                }
            }
        })

        if (!viewModel.isInit) {
            viewModel.getAiringSchedule()
        }
    }

    private fun initLayout() {
        calendarRefreshLayout.setOnRefreshListener {
            calendarRefreshLayout.isRefreshing = false

            dateAdapter = assignDateAdapter()
            dateRecyclerView.adapter = dateAdapter

            viewModel.currentPosition = 0
            handleDate(viewModel.currentPosition)

            viewModel.scheduleList.clear()
            viewModel.hasNextPage = true
            viewModel.page = 1
            viewModel.getAiringSchedule()
        }

        calendarViewPagerAdapter = CalendarViewPagerAdapter(supportFragmentManager, viewModel.dateList)
        calendarViewPager.adapter = calendarViewPagerAdapter
        calendarViewPager.offscreenPageLimit = calendarViewPagerAdapter.count

        calendarViewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageSelected(position: Int) {
                handleDate(position)
            }

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) { }

            override fun onPageScrollStateChanged(state: Int) { }
        })

        handleDate(viewModel.currentPosition)
    }

    private fun assignDateAdapter(): CalendarDateRvAdapter {
        viewModel.setDateList()

        return CalendarDateRvAdapter(this, viewModel.dateList, object : CalendarDateRvAdapter.CalendarDateListener {
            override fun passSelectedDate(position: Int) {
                handleDate(position)
            }
        })
    }

    private fun handleDate(position: Int) {
        viewModel.dateList.forEach { it.isSelected = false }
        viewModel.dateList[position].isSelected = true
        dateAdapter.notifyDataSetChanged()

        calendarViewPager.currentItem = position
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_filter, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.itemFilter) {
            val dialog = CalendarFilterBottomSheetDialog()
            dialog.setListener(object : CalendarFilterBottomSheetDialog.CalendarFilterListener {
                override fun passFilterData(
                    showOnlyInList: Boolean,
                    showOnlyCurrentSeason: Boolean,
                    showAdultContent: Boolean
                ) {
                    viewModel.showOnlyOnWatchingAndPlanning = showOnlyInList
                    viewModel.showOnlyCurrentSeason = showOnlyCurrentSeason
                    viewModel.showAdult = showAdultContent

                    viewModel.filterList()
                }
            })

            val bundle = Bundle()
            bundle.putBoolean(CalendarFilterBottomSheetDialog.SHOW_ONLY_IN_LIST, viewModel.showOnlyOnWatchingAndPlanning)
            bundle.putBoolean(CalendarFilterBottomSheetDialog.SHOW_ONLY_CURRENT_SEASON, viewModel.showOnlyCurrentSeason)
            bundle.putBoolean(CalendarFilterBottomSheetDialog.SHOW_ADULT_CONTENT, viewModel.showAdult)

            dialog.arguments = bundle
            dialog.show(supportFragmentManager, null)
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return super.onSupportNavigateUp()
    }
}