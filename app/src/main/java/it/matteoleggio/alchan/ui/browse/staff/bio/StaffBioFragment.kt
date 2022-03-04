package it.matteoleggio.alchan.ui.browse.staff.bio


import android.net.Uri
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Observer

import it.matteoleggio.alchan.R
import it.matteoleggio.alchan.helper.enums.ResponseStatus
import it.matteoleggio.alchan.helper.handleSpoilerAndLink
import it.matteoleggio.alchan.helper.libs.GlideApp
import it.matteoleggio.alchan.helper.utils.AndroidUtility
import it.matteoleggio.alchan.helper.utils.DialogUtility
import it.matteoleggio.alchan.ui.base.BaseFragment
import it.matteoleggio.alchan.ui.browse.staff.StaffFragment
import kotlinx.android.synthetic.main.fragment_staff_bio.*
import kotlinx.android.synthetic.main.layout_loading.*
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * A simple [Fragment] subclass.
 */
class StaffBioFragment : BaseFragment() {

    private val viewModel by viewModel<StaffBioViewModel>()

    private var staffData: StaffBioQuery.Staff? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_staff_bio, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel.staffId = arguments?.getInt(StaffFragment.STAFF_ID)

        setupObserver()
    }

    private fun setupObserver() {
        viewModel.staffBioData.observe(viewLifecycleOwner, Observer {
            when (it.responseStatus) {
                ResponseStatus.LOADING -> loadingLayout.visibility = View.VISIBLE
                ResponseStatus.SUCCESS -> {
                    loadingLayout.visibility = View.GONE

                    if (it.data?.staff?.id != viewModel.staffId) {
                        return@Observer
                    }

                    viewModel.staffData = it.data?.staff
                    staffData = it.data?.staff
                    initLayout()
                }
                ResponseStatus.ERROR -> {
                    loadingLayout.visibility = View.GONE
                    DialogUtility.showToast(activity, it.message)
                }
            }
        })

        if (viewModel.staffData == null) {
            viewModel.getStaffBio()
        } else {
            staffData = viewModel.staffData
            initLayout()
        }
    }

    private fun initLayout() {
        if (staffData?.name?.alternative.isNullOrEmpty()) {
            var aliasesString = ""
            staffData?.name?.alternative?.forEachIndexed { index, s ->
                aliasesString += s
                if (index != staffData?.name?.alternative?.lastIndex) aliasesString += ", "
            }
            staffAliasesText.text = aliasesString
            staffAliasesText.visibility = View.VISIBLE
        } else {
            staffAliasesText.visibility = View.GONE
        }

        staffDescriptionText.text = staffData?.description?.handleSpoilerAndLink(requireActivity()) { page, id ->
            listener?.changeFragment(page, id)
        }
        staffDescriptionText.movementMethod = LinkMovementMethod.getInstance()
    }
}
