package fr.redmoon.tictac.gui.listadapter;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import fr.redmoon.tictac.R;
import fr.redmoon.tictac.gui.DayBiColorDrawableHelper;

public class WeekAdapter extends ArrayAdapter<WeekAdapterEntry> {
	private List<WeekAdapterEntry> items;

	public WeekAdapter(
			final Context context,
			final int textViewResourceId,
			final List<WeekAdapterEntry> items) {
		super(context, textViewResourceId, items);
		this.items = items;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		final Activity activity = (Activity)getContext();
		if (v == null) {
			// Création de la vue et de son contenu
			LayoutInflater vi = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = vi.inflate(R.layout.lst_itm_week_day, null);
			
			// On conserve la position du jour dans la liste. Pratique pour mapper avec la liste de jours ;)
			v.setTag(position);
			
			// Activation du menu contextuel
			activity.registerForContextMenu(v);
		}
		final WeekAdapterEntry infos = items.get(position);
		if (infos != null) {
			TextView txtDate = (TextView) v.findViewById(R.id.date);
			if (infos.date != null && txtDate != null) {
				txtDate.setText(infos.date);
			}
			
			TextView txtTime = (TextView) v.findViewById(R.id.time);
			if (infos.total != null && txtTime != null) {
				txtTime.setText(infos.total);
			}
			
			// Définition de la couleur de fond
			Drawable background = DayBiColorDrawableHelper.getInstance().getDrawableForDayTypes(infos.morningDayType, infos.afternoonDayType);
	        v.setBackgroundDrawable(background);
		}
		return v;
	}
}