package de.kisi.android;

import java.util.List;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import de.kisi.android.api.KisiAPI;
import de.kisi.android.model.Place;


public class PlaceFragmentPagerAdapter extends FragmentPagerAdapter {

	private int PAGE_COUNT;
	private List<PlaceFragment> fragments;
	
	/** Constructor of the class */
	public PlaceFragmentPagerAdapter(FragmentManager fm) {
		super(fm);
	}
	
	
	public void setFragementList(List<PlaceFragment> fragments) {
		this.fragments = fragments;
		PAGE_COUNT = fragments.size();
		this.notifyDataSetChanged();
	}

	/** This method will be invoked when a page is requested to create */
	@Override
	public Fragment getItem(int position) {

		return this.fragments.get(position);
	}
	
	@Override
	public int getItemPosition(Object object) {
	   	return POSITION_NONE;
	    
	}

	/** Returns the number of pages */
	@Override
	public int getCount() {
		return PAGE_COUNT;
	}
	
	@Override
	public CharSequence getPageTitle(int num) {
		Place l = KisiAPI.getInstance().getPlaceAt(num);
		if(l!=null)
			return l.getName();
		else
			return "";
	}
	
}