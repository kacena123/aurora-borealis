package com.example.aurora.Adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.example.aurora.PredpovedWidgets.AktualneFragment
import com.example.aurora.PredpovedWidgets.DlhodobaFragment
import com.example.aurora.PredpovedWidgets.HodinuPoHodineFragment

class SkodceTabLayoutAdapter (fm: FragmentManager): FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT){

    var fragmentList : ArrayList<Fragment> = ArrayList()
    var fragmentTitle : ArrayList<String> = ArrayList()

    override fun getCount(): Int {
        return fragmentList.size
    }

    override fun getItem(position: Int): Fragment {
        return fragmentList[position]
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return fragmentTitle[position]
    }

    fun addFragment(fragment: Fragment, title: String){
        fragmentList.add(fragment)
        fragmentTitle.add(title)

    }

    fun getFragment(position: Int): Fragment {
        return fragmentList[position]
    }
}