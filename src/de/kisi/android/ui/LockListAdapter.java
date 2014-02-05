package de.kisi.android.ui;

import java.util.List;

import de.kisi.android.R;
import de.kisi.android.api.KisiAPI;
import de.kisi.android.model.Lock;
import de.kisi.android.model.Place;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;


public class LockListAdapter extends BaseAdapter {

	private Context mContext;
	private int placeId;

	public LockListAdapter(Context context, int  placeId) {
		this.mContext = context;
		this.placeId = placeId;
	}
	
	@Override
	public int getCount() {
		Place place = KisiAPI.getInstance().getPlaceById(placeId);
		if(place != null) {
			List<Lock> locks = place.getLocks();	
			if(locks != null) {
				return locks.size();
			}
		}
		return 0;
	}

	@Override
	public Object getItem(int position) {
		Place place = KisiAPI.getInstance().getPlaceById(placeId);
		if(place != null) {
			List<Lock> locks = place.getLocks();	
			if(locks != null) {
				return locks.get(position);
			}
		}
		return null;
	}

	@Override
	public long getItemId(int position) {;
		Place place = KisiAPI.getInstance().getPlaceById(placeId);
		if(place != null) {
			List<Lock> locks = place.getLocks();	
			if(locks != null) {
				return locks.get(position).getId();
			}
		}
		return 0;
	}
	
	
	public int getItemPosition(long id) {
		Place place = KisiAPI.getInstance().getPlaceById(placeId);
		if(place != null) {
			List<Lock> locks = place.getLocks();	
			for(int position = 0; position < locks.size(); position++) {
				if(locks.get(position).getId() == (int) id) {
					return position;
				}
					
			}
		}
		return -1;
		
	}
	

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Place place = KisiAPI.getInstance().getPlaceById(placeId);
		LayoutInflater li = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		Button button = (Button) li.inflate(R.layout.place_button, null);
		button.setText(place.getLocks().get(position).getName());
		//disable the clickability of the buttons so that the OnItemClickListner of the ListView handels the clicks 
		button.setFocusable(false);
		button.setClickable(false);
		return button;
	}
	
	
	public Place getPlace() {
		return KisiAPI.getInstance().getPlaceById(placeId);
	}

}
