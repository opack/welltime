package fr.redmoon.tictac.gui;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import fr.redmoon.tictac.gui.activities.PreferencesActivity;

public class ManagePreferencesHandler implements OnItemClickListener {
	private final static int POS_SHOW_PREFS = 0;
	
	private final Activity activity;
	
	public ManagePreferencesHandler(final Activity activity) {
		this.activity = activity;
	}

	@Override
	public void onItemClick(AdapterView<?> adapter, View v, int position, long id) {
		switch (position) {
		case POS_SHOW_PREFS:
			final Intent prefsActivity = new Intent(activity, PreferencesActivity.class);
			// On pourrait appeler directement telle ou telle page de préférence,
			// mais comme on veut afficher la page principale inutile de mettre une URI.
			//intent.setData(Uri.parse("preferences://main"));
			activity.startActivity(prefsActivity);
			break;
		}
	}
}
