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

public class WeekAdapter extends ArrayAdapter<String[]> {

	private List<String[]> items;

	public WeekAdapter(
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
			// Création de la vue et de son contenu
			final Activity activity = (Activity)getContext();
			LayoutInflater vi = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = vi.inflate(R.layout.week_item, null);
			
			// Ajout de la vue comme étant intéressée par les évènements de menu contextuel 
			activity.registerForContextMenu(v);
			
			// On conserve la position du jour dans la liste. Pratique pour mapper avec la liste de jours ;)
			v.setTag(position);
		}
		final String[] infos = items.get(position);
		if (infos != null) {
			String info = infos[0];
			TextView date = (TextView) v.findViewById(R.id.date);
			if (info != null && date != null) {
				date.setText(info);
			}
			
			info = infos[1];
			TextView time = (TextView) v.findViewById(R.id.time);
			if (info != null && time != null) {
				time.setText(info);
			}
			
			// Définition de la couleur de fond
			v.setBackgroundColor(Integer.parseInt(infos[2]));
		}
		return v;
	}
}