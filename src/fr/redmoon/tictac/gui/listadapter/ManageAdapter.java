package fr.redmoon.tictac.gui.listadapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import fr.redmoon.tictac.R;

public class ManageAdapter extends ArrayAdapter<String[]> {
	private final String[][] items;

	public ManageAdapter(
			final Context context,
			final int textViewResourceId,
			final String[][] items) {
		super(context, textViewResourceId, items);
		this.items = items;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		if (v == null) {
			final Activity activity = (Activity)getContext();
			final LayoutInflater vi = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = vi.inflate(R.layout.itm_manage_operation, null);
		}
		
		final String[] data = items[position];
		if (data != null) {
			initTextView((TextView)v.findViewById(R.id.item_caption), data[0]);
			initTextView((TextView)v.findViewById(R.id.item_summary), data[1]);
		}
		return v;
	}

	private void initTextView(final TextView view, String text) {
		if (view != null) {
			// Définition du texte affiché
			view.setText(text);
		}
	}
}