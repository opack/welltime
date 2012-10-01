package fr.redmoon.tictac.gui.adapters;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import fr.redmoon.tictac.R;

public class WorkedDaysByTypeAdapter extends ArrayAdapter<String[]> {
	private final List<String[]> mItems;
	
	public WorkedDaysByTypeAdapter(
			final Context context,
			final int textViewResourceId,
			final List<String[]> items) {
		super(context, textViewResourceId, items);
		mItems = items;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		if (v == null) {
			final Activity activity = (Activity)getContext();
			final LayoutInflater vi = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = vi.inflate(R.layout.itm_worked_days_by_type, null);
		}

		final String[] data = mItems.get(position);
		
		final TextView label = (TextView)v.findViewById(R.id.txt_day_type);
		label.setText(data[0]);
		final TextView count = (TextView)v.findViewById(R.id.txt_count);
		count.setText(data[1]);
		
		return v;
	}
}