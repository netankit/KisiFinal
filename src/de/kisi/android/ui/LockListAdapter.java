package de.kisi.android.ui;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
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
		
		LinearLayout buttonAndMessageGroup = null;
		Button button;
		TextView message;
		
		if(convertView == null) {
			LayoutInflater li = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			
			buttonAndMessageGroup = (LinearLayout) li.inflate(R.layout.lock_group, parent, false);
			
			button = (Button) li.inflate(R.layout.lock_button, buttonAndMessageGroup, false);
			button.setText(place.getLocks().get(position).getName());
			//disable the clickability of the buttons so that the OnItemClickListner of the ListView handels the clicks 
			button.setFocusable(false);
			button.setClickable(false);
			buttonAndMessageGroup.addView(button);
			
			message = (TextView) li.inflate(R.layout.lock_message, buttonAndMessageGroup, false);
			String messageText = place.getLocks().get(position).getActionMessage();
			if(messageText !=null && messageText.length()>0){
				message.setText(messageText);
				buttonAndMessageGroup.addView(message);
			}
		}
		else {
			 buttonAndMessageGroup = (LinearLayout) convertView;
		}
		
		return buttonAndMessageGroup;
	}
	
	
	public Place getPlace() {
		return KisiAPI.getInstance().getPlaceById(placeId);
	}

}
