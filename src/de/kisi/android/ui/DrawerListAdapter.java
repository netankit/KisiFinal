package de.kisi.android.ui;

import de.kisi.android.R;
import de.kisi.android.api.KisiAPI;
import de.kisi.android.model.Place;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class DrawerListAdapter extends BaseAdapter {

	private KisiAPI mKisiAPI;
	private Context mContext;
	private int selectedItem = 0;
	
	public DrawerListAdapter(Context context) {
		mKisiAPI = KisiAPI.getInstance();
		mContext = context;
	}
	
	@Override
	public int getCount() {
		Place[] places = mKisiAPI.getPlaces();
		if(places != null) {
			return places.length;
		}
		else {
			return 0;
		}
	}

	@Override
	public Object getItem(int position) {
		Place place = mKisiAPI.getPlaceAt(position);
		if(place != null) {
			return place;
		}
		else {
			return null;
		}
	}

	@Override
	public long getItemId(int position) {
		Place place = mKisiAPI.getPlaceAt(position);
		if(place != null) {
			return place.getId();
		}
		else {
			return 0;
		}
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater li = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		TextView mView = (TextView) li.inflate(R.layout.drawer_list_item, null);
		mView.setText(mKisiAPI.getPlaceAt(position).getName());
		if(position == selectedItem)
			mView.setTextColor(Color.WHITE);
		return mView;
	}

	
	public void selectItem(int position) {
		selectedItem = position;
		this.notifyDataSetChanged();
	}
	

}
