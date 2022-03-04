package it.matteoleggio.alchan.ui.settings

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import it.matteoleggio.alchan.R
import it.matteoleggio.alchan.helper.*
import it.matteoleggio.alchan.helper.enums.BrowsePage
import it.matteoleggio.alchan.helper.utils.AndroidUtility
import it.matteoleggio.alchan.ui.base.BaseActivity
import it.matteoleggio.alchan.ui.base.BaseListener
import kotlinx.android.synthetic.main.activity_settings.*


class SettingsActivity : BaseActivity(), BaseListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        changeStatusBarColor(AndroidUtility.getResValueFromRefAttr(this, R.attr.themeCardColor))

        settingsFrameLayout.doOnApplyWindowInsets { view, windowInsets, initialPadding ->
            view.updateTopPadding(windowInsets, initialPadding)
            view.updateSidePadding(windowInsets, initialPadding)
        }

        if (supportFragmentManager.backStackEntryCount == 0) {
            changeFragment(SettingsFragment(), false)
        }
    }

    override fun changeFragment(targetFragment: Fragment, addToBackStack: Boolean) {
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(settingsFrameLayout.id, targetFragment)
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
        if (addToBackStack) {
            fragmentTransaction.addToBackStack(null)
        }
        fragmentTransaction.commit()
    }
}
