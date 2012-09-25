package fr.redmoon.tictac.gui.listadapter;

import java.util.List;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.SpinnerAdapter;
import fr.redmoon.tictac.bus.bean.DayType;
import fr.redmoon.tictac.gui.DayBiColorDrawableHelper;

public class DayTypeAdapter extends ArrayAdapter<DayType> implements SpinnerAdapter {
	private List<DayType> items;

	public DayTypeAdapter(
			final Context context,
			final int textViewResourceId,
			final List<DayType> items) {
		super(context, textViewResourceId, items);
		this.items = items;
	}
	
	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent) {
		// Création de la vue, déléguée au parent
		View v = super.getDropDownView(position, convertView, parent);
			
		// Définition de la couleur de fond à partir du type de jour
		final DayType type = items.get(position);
		final Drawable background = DayBiColorDrawableHelper.getInstance().getDrawableForDayTypes(type.id, type.id);
        v.setBackgroundDrawable(background);
		
		return v;
	}
}