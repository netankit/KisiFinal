package de.kisi.android;

import java.util.List;

import de.kisi.android.api.KisiAPI;
import de.kisi.android.model.Place;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class PlaceFragmentPagerAdapter extends FragmentPagerAdapter {

	private int PAGE_COUNT;
	private List<Fragment> fragments;

	/** Constructor of the class */
	public PlaceFragmentPagerAdapter(FragmentManager fm, List<Fragment> fragments) {
		super(fm);
		this.fragments = fragments;
		PAGE_COUNT = fragments.size();
	}

	/** This method will be invoked when a page is requested to create */
	@Override
	public Fragment getItem(int position) {

		return this.fragments.get(position);
	}

	/** Returns the number of pages */
	@Override
	public int getCount() {
		return PAGE_COUNT;
	}
	
	@Override
	public CharSequence getPageTitle(int num) {
		Place l = KisiAPI.getInstance().getPlaceAt(num);
		return l.getName();
	}
}