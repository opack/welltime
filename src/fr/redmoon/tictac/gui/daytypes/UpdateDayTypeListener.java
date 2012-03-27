package fr.redmoon.tictac.gui.daytypes;

import android.content.DialogInterface;
import fr.redmoon.tictac.R;
import fr.redmoon.tictac.TicTacActivity;
import fr.redmoon.tictac.gui.listeners.TicTacListener;

public class UpdateDayTypeListener extends TicTacListener implements DialogInterface.OnClickListener {
	public UpdateDayTypeListener(final TicTacActivity activity) {
		super(activity);
	}

	@Override
	public void onClick(final DialogInterface dialog, final int which) {
		// Mise à jour de la base de données
		final String selected = mActivity.getResources().getStringArray(R.array.dayTypesValues)[which];
		final int newType = Integer.parseInt(selected);
		if (mDb.updateDayType(mDate, newType)) {
			// Mise à jour de l'affichage
			mActivity.populateView(mDate);
		}
	}
}