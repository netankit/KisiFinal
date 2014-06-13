package de.kisi.android.takeatour;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import de.kisi.android.R;
 
public class TopRatedFragment extends Fragment  {
	ImageView image;
 
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
    	
        View rootView = inflater.inflate(R.layout.fragment_top_rated, container, false);
        //image = (ImageView) findViewById(R.id.imageView1);         
        return rootView;
    }
}