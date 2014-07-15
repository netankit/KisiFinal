package de.kisi.android.takeatour;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import de.kisi.android.R;

public class TakeTourFragment extends Fragment {
	ImageView image;
	private int idPicture;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View rootView = inflater.inflate(R.layout.fragment_taketour,
				container, false);
		Bundle bundle = this.getArguments();
		idPicture = bundle.getInt("IMG_ID", R.drawable.one);
		ImageView image = (ImageView) rootView.findViewById(R.id.imageView1);
		image.setImageResource(idPicture);
		image.setScaleType(ScaleType.CENTER_CROP);
		return rootView;
	}
}