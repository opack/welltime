package fr.redmoon.tictac.gui.listadapter;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import fr.redmoon.tictac.R;
import fr.redmoon.tictac.bus.TimeUtils;

public class DayAdapter extends ArrayAdapter<String[]> {
	private final List<String[]> items;

	public DayAdapter(
			final Context context,
			final int textViewResourceId,
			final List<String[]> items) {
		super(context, textViewResourceId, items);
		this.items = items;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		if (v == null) {
			final Activity activity = (Activity)getContext();
			final LayoutInflater vi = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = vi.inflate(R.layout.day_item, null);
			
			// Ajout de la vue comme �tant int�ress�e par les �v�nements de menu contextuel 
			activity.registerForContextMenu(v.findViewById(R.id.clockin_time));
			activity.registerForContextMenu(v.findViewById(R.id.clockout_time));
		}
		final View viewOut = v.findViewById(R.id.time_out);
		viewOut.setVisibility(View.VISIBLE);
		final String[] times = items.get(position);
		if (times != null) {
			initTextView((TextView)v.findViewById(R.id.clockin_time), times[0]);
			if (times[1] == null) {
				viewOut.setVisibility(View.GONE);
			} else {
				initTextView((TextView)v.findViewById(R.id.clockout_time), times[1]);
			}
		}
		return v;
	}

	private void initTextView(final TextView view, String text) {
		if (view != null) {
			// D�finition du texte affich�
			if (text == null) {
				text = TimeUtils.UNKNOWN_TIME_STRING;
			}
			view.setText(text);
			view.setTag(TimeUtils.parseTime(text));
		}
	}
}