package it.matteoleggio.alchan.ui.base

import androidx.fragment.app.Fragment
import it.matteoleggio.alchan.helper.enums.BrowsePage

// listener for BaseFragment, to navigate between fragment
interface BaseListener {
    fun changeFragment(targetFragment: Fragment, addToBackStack: Boolean = true)
    fun changeFragment(browsePage: BrowsePage, id: Int, extraLoad: String? = null, addToBackStack: Boolean = true) { }
}