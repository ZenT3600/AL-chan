package com.zen.alchan.ui.profile

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class ProfileViewPagerAdapter(
    rootFragment: Fragment,
    private val fragments: List<Fragment>
) : FragmentStateAdapter(rootFragment) {

    override fun createFragment(position: Int): Fragment {
        return fragments[position]
    }

    override fun getItemCount(): Int {
        return fragments.size
    }
}