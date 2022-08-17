package it.matteoleggio.alchan.ui.settings.about


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
import androidx.core.content.ContextCompat
import com.google.firebase.BuildConfig

import it.matteoleggio.alchan.R
import it.matteoleggio.alchan.helper.Constant
import it.matteoleggio.alchan.helper.doOnApplyWindowInsets
import it.matteoleggio.alchan.helper.updateBottomPadding
import kotlinx.android.synthetic.main.fragment_about.*
import kotlinx.android.synthetic.main.layout_toolbar.*

/**
 * A simple [Fragment] subclass.
 */
class AboutFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_about, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        toolbarLayout.apply {
            title = getString(R.string.about_al_chan)
            navigationIcon = ContextCompat.getDrawable(requireActivity(), R.drawable.ic_left)
            setNavigationOnClickListener { activity?.onBackPressed() }
        }

        aboutLayout.doOnApplyWindowInsets { view, windowInsets, initialPadding ->
            view.updateBottomPadding(windowInsets, initialPadding)
        }

        versionText.text = "Version X"
        linkGmailText.text = Constant.EMAIL_ADDRESS

        linkAniListLayout.setOnClickListener { openLink(Constant.ALCHAN_THREAD_URL) }
        linkGitHubLayout.setOnClickListener { openLink(Constant.GITHUB_URL) }
        linkGmailLayout.setOnClickListener {
            val intent =
                Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", Constant.EMAIL_ADDRESS, null))
            startActivity(intent)

            linkGitHubLayout.setOnClickListener { openLink(Constant.GITHUB_URL) }
        }


    }

    fun openLink(url: String) {
        CustomTabsIntent.Builder().build().launchUrl(requireActivity(), Uri.parse(url))
    }
}
