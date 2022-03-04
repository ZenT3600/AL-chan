package it.matteoleggio.alchan.ui.base

import androidx.fragment.app.Fragment

// listener for BaseMainFragment, to toggle moving between bottom navigation menu
interface BaseMainFragmentListener {
    fun changeMenu(targetMenuId: Int)
}