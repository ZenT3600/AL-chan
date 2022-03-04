package it.matteoleggio.alchan.ui.animelist.editor

import android.app.DatePickerDialog
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.text.InputFilter
import android.text.InputType
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.SeekBar
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.apollographql.apollo.api.CustomTypeValue
import it.matteoleggio.alchan.R
import it.matteoleggio.alchan.data.response.FuzzyDate
import it.matteoleggio.alchan.helper.*
import it.matteoleggio.alchan.helper.enums.BrowsePage
import it.matteoleggio.alchan.helper.enums.ResponseStatus
import it.matteoleggio.alchan.helper.libs.GlideApp
import it.matteoleggio.alchan.helper.pojo.AdvancedScoresItem
import it.matteoleggio.alchan.helper.pojo.CustomListsItem
import it.matteoleggio.alchan.helper.utils.AndroidUtility
import it.matteoleggio.alchan.helper.utils.DialogUtility
import it.matteoleggio.alchan.ui.base.BaseActivity
import it.matteoleggio.alchan.ui.browse.BrowseActivity
import it.matteoleggio.alchan.ui.common.CustomListsRvAdapter
import it.matteoleggio.alchan.ui.common.SetProgressDialog
import it.matteoleggio.alchan.ui.common.SetScoreDialog
import kotlinx.android.synthetic.main.activity_anime_list_editor.*
import kotlinx.android.synthetic.main.dialog_input.*
import kotlinx.android.synthetic.main.dialog_input.view.*
import kotlinx.android.synthetic.main.layout_loading.*
import kotlinx.android.synthetic.main.layout_toolbar.*
import kotlinx.android.synthetic.main.list_anime_list_grid.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import type.MediaListStatus
import type.ScoreFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.LinkedHashMap

class AnimeListEditorActivity : BaseActivity() {

    val viewModel by viewModel<AnimeListEditorViewModel>()
    private lateinit var customListsAdapter: CustomListsRvAdapter

    private lateinit var startDatePickerDialog: DatePickerDialog
    private lateinit var startDateSetListener: DatePickerDialog.OnDateSetListener

    private lateinit var finishDatePickerDialog: DatePickerDialog
    private lateinit var finishDateSetListener: DatePickerDialog.OnDateSetListener

    companion object {
        const val INTENT_ENTRY_ID = "entryId"

        const val INTENT_MEDIA_ID = "mediaId"
        const val INTENT_MEDIA_TITLE = "mediaTitle"
        const val INTENT_MEDIA_EPISODE = "mediaEpisode"
        const val INTENT_IS_FAVOURITE = "isFavourite"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_anime_list_editor)

        changeStatusBarColor(AndroidUtility.getResValueFromRefAttr(this, R.attr.themeCardColor))

        animeEditorLayout.doOnApplyWindowInsets { view, windowInsets, initialPadding ->
            view.updateTopPadding(windowInsets, initialPadding)
            view.updateSidePadding(windowInsets, initialPadding)
        }

        animeEditorFormLayout.doOnApplyWindowInsets { view, windowInsets, initialPadding ->
            view.updateBottomPadding(windowInsets, initialPadding)
        }

        viewModel.entryId = intent.getIntExtra(INTENT_ENTRY_ID, 0)

        viewModel.mediaId = intent.getIntExtra(INTENT_MEDIA_ID, 0)
        viewModel.mediaTitle = intent.getStringExtra(INTENT_MEDIA_TITLE)
        viewModel.mediaEpisode = intent.getIntExtra(INTENT_MEDIA_EPISODE, 0)
        viewModel.isFavourite = intent.getBooleanExtra(INTENT_IS_FAVOURITE, false)

        /**
         * Important Note:
         * - When editing an anime that's not on your list, entryId will be 0
         * - Other than that, mediaId will have value
         * - And vice versa
         */

        setSupportActionBar(toolbarLayout)
        supportActionBar?.apply {
            title = getString(R.string.anime_list_editor)
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_delete)
        }

        animeListEditorRefreshLayout.setOnRefreshListener {
            viewModel.retrieveAnimeListDataDetail()
        }

        setupObserver()

        if (viewModel.isInit || (viewModel.mediaId != null && viewModel.mediaId != 0)) {
            initLayout()
        }
    }

    private fun setupObserver() {
        viewModel.animeListDataDetailResponse.observe(this, Observer {
            when (it.responseStatus) {
                ResponseStatus.LOADING -> {
                    animeListEditorRefreshLayout.isRefreshing = false
                    loadingLayout.visibility = View.VISIBLE
                }
                ResponseStatus.SUCCESS -> {
                    loadingLayout.visibility = View.GONE
                    initLayout()
                }
                ResponseStatus.ERROR -> {
                    loadingLayout.visibility = View.GONE
                    DialogUtility.showToast(this, it.message)
                }
            }
        })

        viewModel.updateAnimeListEntryDetailResponse.observe(this, Observer {
            when (it.responseStatus) {
                ResponseStatus.LOADING -> {
                    loadingLayout.visibility = View.VISIBLE
                }
                ResponseStatus.SUCCESS -> {
                    loadingLayout.visibility = View.GONE
                    DialogUtility.showToast(this, R.string.change_saved)
                }
                ResponseStatus.ERROR -> {
                    loadingLayout.visibility = View.GONE
                    DialogUtility.showToast(this, it.message)
                }
            }
        })

        viewModel.deleteMediaListEntryResponse.observe(this, Observer {
            when (it.responseStatus) {
                ResponseStatus.LOADING -> {
                    loadingLayout.visibility = View.VISIBLE
                }
                ResponseStatus.SUCCESS -> {
                    loadingLayout.visibility = View.GONE
                    DialogUtility.showToast(this, R.string.entry_deleted)
                    finish()
                }
                ResponseStatus.ERROR -> {
                    loadingLayout.visibility = View.GONE
                    DialogUtility.showToast(this, it.message)
                }
            }
        })

        viewModel.toggleFavouriteResponse.observe(this, Observer {
            when (it.responseStatus) {
                ResponseStatus.LOADING -> {
                    loadingLayout.visibility = View.VISIBLE
                }
                ResponseStatus.SUCCESS -> {
                    loadingLayout.visibility = View.GONE
                    DialogUtility.showToast(this, R.string.change_saved)
                }
                ResponseStatus.ERROR -> {
                    loadingLayout.visibility = View.GONE
                    DialogUtility.showToast(this, it.message)
                }
            }
        })

        viewModel.retrieveAnimeListDataDetail()
    }

    private fun initLayout() {
        val mediaList = if (viewModel.entryId != null && viewModel.entryId != 0) {
            viewModel.animeListDataDetailResponse.value?.data
        } else {
            null
        }

        // set default value when editing anime that is on our list
        if (mediaList != null && !viewModel.isInit) {
            viewModel.isInit = true

            viewModel.isFavourite = mediaList.media?.isFavourite
            viewModel.selectedStatus = mediaList.status
            viewModel.selectedScore = mediaList.score

            if (mediaList.advancedScores != null) {
                viewModel.advancedScoresList.clear()
                viewModel.selectedAdvancedScores.clear()

                val advancedScoresMap = (mediaList.advancedScores as CustomTypeValue<*>).value as LinkedHashMap<String, Double>
                advancedScoresMap.forEach { (key, value) ->
                    viewModel.advancedScoresList.add(AdvancedScoresItem(key, value))
                    viewModel.selectedAdvancedScores.add(value)
                }
            }

            viewModel.selectedProgress = mediaList.progress
            viewModel.selectedStartDate = mediaList.startedAt
            viewModel.selectedFinishDate = mediaList.completedAt
            viewModel.selectedRewatches = mediaList.repeat
            viewModel.selectedNotes = mediaList.notes
            viewModel.selectedPriority = mediaList.priority

            if (mediaList.customLists != null) {
                viewModel.customListsList.clear()
                viewModel.selectedCustomLists.clear()

                val customListsMap = (mediaList.customLists as CustomTypeValue<*>).value as LinkedHashMap<String, Boolean>
                customListsMap.forEach { (key, value) ->
                    viewModel.customListsList.add(CustomListsItem(key, value))
                    if (value) viewModel.selectedCustomLists.add(key)
                }
            }

            viewModel.selectedHidden = mediaList.hiddenFromStatusList
            viewModel.selectedPrivate = mediaList.private
        }

        // set default value when editing anime that is NOT on our list
        if (mediaList == null && !viewModel.isInit) {
            viewModel.isInit = true

            viewModel.selectedStatus = MediaListStatus.PLANNING
            viewModel.selectedScore = 0.0
            viewModel.selectedProgress = 0
            viewModel.selectedRewatches = 0
            viewModel.selectedHidden = false
            viewModel.selectedPrivate = false
            viewModel.selectedPriority = 0

            // handle advanced score
            viewModel.advancedScoresList.clear()
            viewModel.selectedAdvancedScores.clear()

            viewModel.advancedScoringList.forEach {
                viewModel.advancedScoresList.add(AdvancedScoresItem(it!!, 0.0))
                viewModel.selectedAdvancedScores.add(0.0)
            }

            // handle custom lists
            viewModel.customListsList.clear()
            viewModel.selectedCustomLists.clear()

            viewModel.savedCustomListsList.forEach {
                viewModel.customListsList.add(CustomListsItem(it!!, false))
            }
        }

        if (viewModel.isInit || (viewModel.mediaId != null && viewModel.mediaId != 0)) {
            animeListEditorRefreshLayout.isEnabled = false
        }

        setupDateDialog()

        // handle title
        titleText.text = mediaList?.media?.title?.userPreferred ?: viewModel.mediaTitle
        if (mediaList != null) {
            titleText.setOnClickListener {
                if (mediaList.media?.isAdult == true && viewModel.viewerData?.options?.displayAdultContent != true) {
                    DialogUtility.showToast(this, R.string.you_are_not_allowed_to_view_this_content)
                    return@setOnClickListener
                }

                val intent = Intent(this, BrowseActivity::class.java)
                intent.putExtra(BrowseActivity.TARGET_PAGE, BrowsePage.ANIME.name)
                intent.putExtra(BrowseActivity.LOAD_ID, mediaList.media?.id)
                startActivity(intent)
                finish()
            }
        }


        // handle favorite
        favoriteIcon.imageTintList = if (viewModel.isFavourite == true) {
            ColorStateList.valueOf(ContextCompat.getColor(this, R.color.redHeart))
        } else {
            ColorStateList.valueOf(AndroidUtility.getResValueFromRefAttr(this, R.attr.themeContentColor))
        }
        favoriteIcon.setOnClickListener {
            if (viewModel.isFavourite == true) {
                viewModel.isFavourite = false
                favoriteIcon.imageTintList = ColorStateList.valueOf(AndroidUtility.getResValueFromRefAttr(this, R.attr.themeContentColor))
            } else {
                viewModel.isFavourite = true
                favoriteIcon.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.redHeart))
            }

            viewModel.updateFavourite()
        }

        // handle status
        val mediaListStatusIndex = viewModel.mediaListStatusList.indexOf(viewModel.selectedStatus)
        statusText.text = Constant.DEFAULT_ANIME_LIST_ORDER[if (mediaListStatusIndex < 0) 0 else mediaListStatusIndex]
        statusText.setOnClickListener {
            AlertDialog.Builder(this)
                .setItems(Constant.DEFAULT_ANIME_LIST_ORDER.toTypedArray()) { _, which ->
                    viewModel.selectedStatus = viewModel.mediaListStatusList[which]
                    statusText.text = Constant.DEFAULT_ANIME_LIST_ORDER[which]

                    if (viewModel.selectedStatus == MediaListStatus.COMPLETED) {
                        if (mediaList?.media?.episodes != 0 && mediaList?.media?.episodes != null) {
                            viewModel.selectedProgress = mediaList.media?.episodes
                            progressText.text = viewModel.selectedProgress?.toString()
                        } else if (viewModel.mediaEpisode != 0 && viewModel.mediaEpisode != null) {
                            viewModel.selectedProgress = viewModel.mediaEpisode
                            progressText.text = viewModel.selectedProgress?.toString()
                        }
                    }
                }
                .show()
        }

        // handle score
        if (viewModel.viewerData?.mediaListOptions?.scoreFormat == ScoreFormat.POINT_3) {
            scoreText.visibility = View.GONE
            scoreSmileyIcon.visibility = View.VISIBLE
            GlideApp.with(this).load(AndroidUtility.getSmileyFromScore(viewModel.selectedScore)).into(scoreSmileyIcon)
        } else {
            scoreText.visibility = View.VISIBLE
            scoreSmileyIcon.visibility = View.GONE
            scoreText.text = viewModel.selectedScore?.roundToOneDecimal() ?: "0"
        }

        scoreSmileyIcon.setOnClickListener {
            showScoreDialog()
        }

        scoreText.setOnClickListener {
            showScoreDialog()
        }

        // handle progress
        progressText.text = viewModel.selectedProgress?.toString() ?: "0"
        progressText.setOnClickListener {
            val setProgressDialog = SetProgressDialog()
            val bundle = Bundle()
            bundle.putInt(SetProgressDialog.BUNDLE_CURRENT_PROGRESS, viewModel.selectedProgress ?: 0)
            if (mediaList?.media?.episodes != null) {
                bundle.putInt(SetProgressDialog.BUNDLE_TOTAL_EPISODES, mediaList.media?.episodes!!)
            } else if (viewModel.mediaEpisode != null || viewModel.mediaEpisode != 0) {
                bundle.putInt(SetProgressDialog.BUNDLE_TOTAL_EPISODES, viewModel.mediaEpisode!!)
            }
            setProgressDialog.arguments = bundle
            setProgressDialog.setListener(object : SetProgressDialog.SetProgressListener {
                override fun passProgress(newProgress: Int) {
                    viewModel.selectedProgress = newProgress
                    progressText.text = newProgress.toString()
                }
            })
            setProgressDialog.show(supportFragmentManager, null)
        }

        // handle start date
        startDateText.text = viewModel.selectedStartDate.toStringDateFormat()
        startDateText.setOnClickListener {
            startDatePickerDialog.show()
        }

        if (viewModel.selectedStartDate?.year == null) {
            startDateRemoveIcon.visibility = View.GONE
        } else {
            startDateRemoveIcon.visibility = View.VISIBLE
        }

        startDateRemoveIcon.setOnClickListener {
            viewModel.selectedStartDate = null
            startDateText.text = viewModel.selectedStartDate.toStringDateFormat()
            startDateRemoveIcon.visibility = View.GONE
        }

        // handle finish date
        finishDateText.text = viewModel.selectedFinishDate.toStringDateFormat()
        finishDateText.setOnClickListener {
            finishDatePickerDialog.show()
        }

        if (viewModel.selectedFinishDate?.year == null) {
            finishDateRemoveIcon.visibility = View.GONE
        } else {
            finishDateRemoveIcon.visibility = View.VISIBLE
        }

        finishDateRemoveIcon.setOnClickListener {
            viewModel.selectedFinishDate = null
            finishDateText.text = viewModel.selectedFinishDate.toStringDateFormat()
            finishDateRemoveIcon.visibility = View.GONE
        }

        // handle total rewatches
        totalRewatchesText.text = viewModel.selectedRewatches?.toString() ?: "0"
        totalRewatchesText.setOnClickListener {
            val inputDialogView = layoutInflater.inflate(R.layout.dialog_input, inputDialogLayout, false)
            inputDialogView.inputField.inputType = InputType.TYPE_CLASS_NUMBER
            inputDialogView.inputField.filters = arrayOf(InputFilter.LengthFilter(5))
            inputDialogView.inputField.setText(viewModel.selectedRewatches?.toString())
            inputDialogView.inputField.setSelection(viewModel.selectedRewatches?.toString()?.length ?: 0)

            DialogUtility.showCustomViewDialog(
                this,
                R.string.total_rewatches,
                inputDialogView,
                R.string.set,
                {
                    val newNumber = inputDialogView.inputField.text.toString().trim()
                    if (newNumber.isNotBlank()) {
                        try {
                            val newNumberInt = newNumber.toInt()
                            viewModel.selectedRewatches = newNumberInt
                            totalRewatchesText.text = newNumber
                        } catch (e: Exception) {
                            DialogUtility.showToast(this, "")
                        }
                    }
                },
                R.string.cancel,
                { }
            )
        }

        // handle notes
        notesText.text = viewModel.selectedNotes ?: "-"
        notesText.setOnClickListener {
            val inputDialogView = layoutInflater.inflate(R.layout.dialog_input, inputDialogLayout, false)
            inputDialogView.inputField.inputType = InputType.TYPE_TEXT_FLAG_CAP_SENTENCES or InputType.TYPE_TEXT_FLAG_MULTI_LINE
            inputDialogView.inputField.setSingleLine(false)
            inputDialogView.inputField.imeOptions = EditorInfo.IME_FLAG_NO_ENTER_ACTION
            inputDialogView.inputField.filters = arrayOf(InputFilter.LengthFilter(6000))
            inputDialogView.inputField.setText(viewModel.selectedNotes)
            inputDialogView.inputField.setSelection(viewModel.selectedNotes?.length ?: 0)

            DialogUtility.showCustomViewDialog(
                this,
                R.string.notes,
                inputDialogView,
                R.string.set,
                {
                    val newNotes = inputDialogView.inputField.text.toString().trim()
                    viewModel.selectedNotes = newNotes
                    notesText.text = newNotes
                },
                R.string.cancel,
                { }
            )
        }

        // handle priority
        prioritySeekBar.progress = viewModel.selectedPriority ?: 0
        priorityText.text = viewModel.getPriorityLabel()
        prioritySeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                viewModel.selectedPriority = progress
                priorityText.text = viewModel.getPriorityLabel()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // handle custom lists
        customListsAdapter = assignAdapter(viewModel.customListsList)
        customListsRecyclerView.adapter = customListsAdapter

        if (viewModel.customListsList.isNullOrEmpty()) {
            customListsTitleText.visibility = View.GONE
            customListsRecyclerView.visibility = View.GONE
        } else {
            customListsTitleText.visibility = View.VISIBLE
            customListsRecyclerView.visibility = View.VISIBLE
        }

        // handle hidden
        hideFromStatusListsCheckBox.isChecked = viewModel.selectedHidden == true
        hideFromStatusListsCheckBox.setOnClickListener {
            viewModel.selectedHidden = viewModel.selectedHidden != true
            viewModel.isCustomListsModified = true
        }
        hideFromStatusListsText.setOnClickListener {
            hideFromStatusListsCheckBox.performClick()
        }

        // handle private
        privateCheckBox.isChecked = viewModel.selectedPrivate == true
        privateCheckBox.setOnClickListener {
            viewModel.selectedPrivate = viewModel.selectedPrivate != true
        }
        privateText.setOnClickListener {
            privateCheckBox.performClick()
        }

        if (mediaList == null) {
            removeFromListButton.visibility = View.GONE
        } else {
            removeFromListButton.setOnClickListener {
                DialogUtility.showOptionDialog(
                    this,
                    R.string.remove_from_list,
                    R.string.are_you_sure_you_want_to_remove_this_entry_from_your_list,
                    R.string.delete,
                    {
                        viewModel.deleteAnimeListEntry()
                    },
                    R.string.cancel,
                    { }
                )
            }
        }
    }

    private fun setupDateDialog() {
        startDateSetListener = DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
            if (viewModel.selectedStartDate == null) {
                viewModel.selectedStartDate = FuzzyDate(year, month + 1, dayOfMonth)
            } else {
                viewModel.selectedStartDate?.year = year
                viewModel.selectedStartDate?.month = month + 1
                viewModel.selectedStartDate?.day = dayOfMonth
            }
            startDateText.text = viewModel.selectedStartDate.toStringDateFormat()
            startDateRemoveIcon.visibility = View.VISIBLE
        }

        finishDateSetListener = DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
            if (viewModel.selectedFinishDate == null) {
                viewModel.selectedFinishDate = FuzzyDate(year, month + 1, dayOfMonth)
            } else {
                viewModel.selectedFinishDate?.year = year
                viewModel.selectedFinishDate?.month = month + 1
                viewModel.selectedFinishDate?.day = dayOfMonth
            }
            finishDateText.text = viewModel.selectedFinishDate.toStringDateFormat()
            finishDateRemoveIcon.visibility = View.VISIBLE
        }

        val today = Calendar.getInstance()

        startDatePickerDialog = DatePickerDialog(
            this,
            startDateSetListener,
            viewModel.selectedStartDate?.year ?: today.get(Calendar.YEAR),
            viewModel.selectedStartDate?.month?.minus(1) ?: today.get(Calendar.MONTH),
            viewModel.selectedStartDate?.day ?: today.get(Calendar.DATE)
        )

        finishDatePickerDialog = DatePickerDialog(
            this,
            finishDateSetListener,
            viewModel.selectedFinishDate?.year ?: today.get(Calendar.YEAR),
            viewModel.selectedFinishDate?.month?.minus(1) ?: today.get(Calendar.MONTH),
            viewModel.selectedFinishDate?.day ?: today.get(Calendar.DATE)
        )
    }

    private fun showScoreDialog() {
        val setScoreDialog = SetScoreDialog()
        val bundle = Bundle()
        bundle.putString(SetScoreDialog.BUNDLE_SCORE_FORMAT, viewModel.scoreFormat.name)
        bundle.putDouble(SetScoreDialog.BUNDLE_CURRENT_SCORE, viewModel.selectedScore ?: 0.0)
        bundle.putStringArrayList(SetScoreDialog.BUNDLE_ADVANCED_SCORING, viewModel.advancedScoringList)

        if (viewModel.scoreFormat == ScoreFormat.POINT_10_DECIMAL || viewModel.scoreFormat == ScoreFormat.POINT_100) {
            val advancedScoresList = ArrayList<AdvancedScoresItem>()
            viewModel.advancedScoresList.forEach {
                advancedScoresList.add(AdvancedScoresItem(it.criteria, it.score))
            }
            bundle.putString(SetScoreDialog.BUNDLE_ADVANCED_SCORES_LIST, viewModel.gson.toJson(advancedScoresList))
        }

        setScoreDialog.arguments = bundle
        setScoreDialog.setListener(object : SetScoreDialog.SetScoreListener {
            override fun passScore(newScore: Double, newAdvancedScores: List<Double>?) {
                viewModel.selectedScore = newScore
                
                newAdvancedScores?.forEachIndexed { index: Int, score: Double ->
                    viewModel.advancedScoresList[index].score = score
                    viewModel.selectedAdvancedScores[index] = score
                }
                if (viewModel.scoreFormat == ScoreFormat.POINT_3) {
                    GlideApp.with(this@AnimeListEditorActivity).load(AndroidUtility.getSmileyFromScore(newScore)).into(scoreSmileyIcon)
                } else {
                    scoreText.text = newScore.roundToOneDecimal()
                }
            }
        })
        setScoreDialog.show(supportFragmentManager, null)
    }

    private fun assignAdapter(list: List<CustomListsItem>): CustomListsRvAdapter {
        return CustomListsRvAdapter(list, object : CustomListsRvAdapter.CustomListsListener {
            override fun passSelected(index: Int, isChecked: Boolean) {
                viewModel.customListsList[index].isChecked = isChecked
                if (isChecked) {
                    viewModel.selectedCustomLists.add(viewModel.customListsList[index].customList)
                } else {
                    viewModel.selectedCustomLists.remove(viewModel.customListsList[index].customList)
                }
                customListsAdapter.notifyDataSetChanged()
                viewModel.isCustomListsModified = true
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_save, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.itemSave) {
            viewModel.updateAnimeListEntryDetail()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return super.onSupportNavigateUp()
    }
}
