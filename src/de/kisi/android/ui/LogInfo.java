package de.kisi.android.ui;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import de.kisi.android.R;
import de.kisi.android.api.KisiAPI;
import de.kisi.android.api.LogsCallback;
import de.kisi.android.model.Event;
import de.kisi.android.model.Place;

public class LogInfo extends Activity implements LogsCallback{

	
	private ListView mListView;
	
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.loginfo);
		//add back button to action bar 
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		mListView = (ListView) findViewById(R.id.place_notification_listview);
		int placeId = getIntent().getIntExtra("place_id", 0);
		if(placeId != 0) {
			Place place = KisiAPI.getInstance().getPlaceById(placeId);
			KisiAPI.getInstance().getLogs(place, this);
			getActionBar().setTitle(place.getName());
		}
		

	}
	
	//listener for the backbutton of the action bar
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
       switch (item.getItemId()) {
        case android.R.id.home:
            finish();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
	}

	@Override
	public void onLogsResult(List<Event> events) {
		mListView.setAdapter(new LogAdapter(events));
	}
	
	
	class LogAdapter extends BaseAdapter {

		private List<Event> events;
		private LayoutInflater inflater;
		
		
		public LogAdapter(List<Event> events) {
			super();
			this.events = events;
			inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public int getCount() {
			return events.size();
		}

		@Override
		public Object getItem(int position) {
			return events.get(position);
		}

		@Override
		public long getItemId(int position) {
			return events.get(position).getId();
		}

		@SuppressWarnings("deprecation")
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View vi = convertView;
	        if (vi == null)
	            vi = inflater.inflate(R.layout.loginfo_list_item, null);
	        TextView eventTextView = (TextView) vi.findViewById(R.id.logEventTextView);
	        eventTextView.setText(events.get(position).getMessage());
	        TextView dateTextView = (TextView) vi.findViewById(R.id.logDateTextVIew);
	        dateTextView.setText(events.get(position).getCreatedAt().toLocaleString());
	        return vi;
		}
		
	}
	
}
