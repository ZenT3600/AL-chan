package it.matteoleggio.alchan.ui.auth

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.browser.customtabs.CustomTabsIntent
import androidx.lifecycle.Observer

import it.matteoleggio.alchan.R
import it.matteoleggio.alchan.helper.Constant
import it.matteoleggio.alchan.helper.doOnApplyWindowInsets
import it.matteoleggio.alchan.helper.utils.DialogUtility
import it.matteoleggio.alchan.helper.enums.ResponseStatus
import it.matteoleggio.alchan.helper.libs.GlideApp
import it.matteoleggio.alchan.helper.updateAllPadding
import it.matteoleggio.alchan.ui.main.MainActivity
import kotlinx.android.synthetic.main.fragment_login.*
import kotlinx.android.synthetic.main.fragment_login.contentLayout
import kotlinx.android.synthetic.main.fragment_welcome.*
import kotlinx.android.synthetic.main.layout_loading.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class LoginFragment : Fragment() {

    private val viewModel by viewModel<LoginViewModel>()

    companion object {
        const val BUNDLE_ACCESS_TOKEN = "accessToken"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        contentLayout.doOnApplyWindowInsets { view, windowInsets, initialPadding ->
            view.updateAllPadding(windowInsets, initialPadding)
        }

        initLayout()
        setupObserver()

        if (arguments?.getString(BUNDLE_ACCESS_TOKEN) != null) {
            viewModel.doLogin(arguments?.getString(BUNDLE_ACCESS_TOKEN)!!)
        }
    }

    private fun setupObserver() {
        viewModel.viewerDataResponse.observe(viewLifecycleOwner, Observer {
            when (it.responseStatus) {
                ResponseStatus.LOADING -> loadingLayout.visibility = View.VISIBLE
                ResponseStatus.SUCCESS -> {
                    loadingLayout.visibility = View.GONE
                    startActivity(Intent(activity, MainActivity::class.java))
                    activity?.finish()
                }
                ResponseStatus.ERROR -> {
                    loadingLayout.visibility = View.GONE
                    DialogUtility.showToast(activity, it.message)
                }
            }
        })
    }

    private fun initLayout() {
        GlideApp.with(this).load(R.drawable.welcome_background).into(loginBackgroundImage)

        val anilistText = "anilist.co"
        val explanationText = SpannableString(getString(R.string.you_ll_be_redirected_to_anilist_co_to_login_register_make_sure_the_url_is_anilist_co_before_entering_your_email_and_password))
        val startIndex = explanationText.indexOf(anilistText)
        val endIndex = startIndex + anilistText.length

        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                launchBrowser(Constant.ANILIST_URL)
            }
        }

        explanationText.setSpan(clickableSpan, startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        loginExplanationText.movementMethod = LinkMovementMethod.getInstance()
        loginExplanationText.text = explanationText

        loginBackLayout.setOnClickListener {
            activity?.onBackPressed()
        }

        registerButton.setOnClickListener {
            launchBrowser(Constant.ANILIST_REGISTER_URL)
        }

        loginButton.setOnClickListener {
            launchBrowser(Constant.ANILIST_LOGIN_URL)
        }
    }

    private fun launchBrowser(url: String) {
        CustomTabsIntent.Builder()
            .build()
            .launchUrl(activity!!, Uri.parse(url))
    }
}
