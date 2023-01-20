package com.secuyou.android_v22_pin_app;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;
/* loaded from: classes.dex */
public class ViewPagerAdapter extends FragmentPagerAdapter {
    private final List<Fragment> firstFragment;
    private final List<String> firstTitles;

    public ViewPagerAdapter(FragmentManager fragmentManager) {
        super(fragmentManager);
        this.firstFragment = new ArrayList();
        this.firstTitles = new ArrayList();
    }

    @Override // androidx.fragment.app.FragmentPagerAdapter
    public Fragment getItem(int i) {
        return this.firstFragment.get(i);
    }

    @Override // androidx.viewpager.widget.PagerAdapter
    public int getCount() {
        return this.firstTitles.size();
    }

    @Override // androidx.viewpager.widget.PagerAdapter
    public CharSequence getPageTitle(int i) {
        return this.firstTitles.get(i);
    }

    public void AddFragment(Fragment fragment, String str) {
        this.firstFragment.add(fragment);
        this.firstTitles.add(str);
    }

    public void removeFragments(int i) {
        this.firstFragment.remove(i);
    }
}
