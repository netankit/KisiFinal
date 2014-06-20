package de.kisi.android.ui;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import de.kisi.android.R;
import de.kisi.android.api.KisiAPI;
import de.kisi.android.model.Lock;
import de.kisi.android.model.Place;


public class LockListAdapter extends BaseAdapter {

	private Context mContext;
	private int placeId;
	private String trigger;
	private HashSet<Integer> suggestedNFC;
	private Hashtable<Integer,Button> buttonList;

	public LockListAdapter(Context context, int  placeId) {
		this.mContext = context;
		this.placeId = placeId;
		suggestedNFC = new HashSet<Integer>();
		buttonList = new Hashtable<Integer,Button>();
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
	public String getTrigger(){
		String result = trigger;
		trigger = null;
		return result;
	}
	public void setTrigger(String t){
		trigger = t;
	}
	public boolean isSuggestedNFC(int lockId){
		return suggestedNFC.contains(lockId);
	}
	public void clearSuggestedNFC(){
		suggestedNFC.clear();
	}
	public void addSuggestedNFC(int lockId){
		suggestedNFC.add(lockId);
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
		Lock lock = place.getLocks().get(position);
		Log.i("getView",""+position);
		Button button;
		if(convertView == null) {
			if(buttonList.containsKey(lock.getId())){
				button = buttonList.get(lock.getId());
			}else{
				LayoutInflater li = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				button = (Button) li.inflate(R.layout.lock_button, parent, false);
				buttonList.put(lock.getId(), button);
			}
		}
		else {
			button = (Button) convertView;
			buttonList.put(lock.getId(), button);
		}
		button.setText(lock.getName());
		//disable the clickability of the buttons so that the OnItemClickListner of the ListView handels the clicks 
		button.setFocusable(false);
		button.setClickable(false);
		
		
		return button;
	}
	
	
	public Place getPlace() {
		return KisiAPI.getInstance().getPlaceById(placeId);
	}

}
