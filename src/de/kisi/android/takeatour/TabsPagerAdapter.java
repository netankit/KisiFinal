package de.kisi.android.takeatour;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import de.kisi.android.KisiApplication;
import de.kisi.android.R;

public class TabsPagerAdapter extends FragmentPagerAdapter {

	private Bundle bundle;
	private Fragment frag;

	public TabsPagerAdapter(FragmentManager fm) {
		super(fm);
	}

	@Override
	public Fragment getItem(int index) {

		frag = new TakeTourFragment();
		bundle = new Bundle();
		bundle.putInt(
				"IMG_ID",
				KisiApplication
						.getInstance()
						.getResources()
						.getIdentifier("taketourimage" + (index + 1), "drawable",
								KisiApplication.getInstance().getPackageName()));
		frag.setArguments(bundle);
		return frag;

	}

	@Override
	public int getCount() {
		// get item count - equal to number of tabs
		
		Field[] fields = R.drawable.class.getFields();
	    List<Integer> drawables = new ArrayList<Integer>();
	    for (Field field : fields) {
	        // Take only those with name starting with "taketourimage"
	        if (field.getName().startsWith("taketourimage")) {
	            try {
					drawables.add(field.getInt(null));
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	        }
	    }
	    int lngth = drawables.size();
	    return lngth;
	}

}
