package fr.redmoon.tictac;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import fr.redmoon.tictac.bus.PreferencesUtils;

public class PreferencesActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {
	public static final String URI_PAGE_MAIN = "preferences://main";
	public static final String URI_PAGE_LIMITS = "preferences://limits";
	public static final String URI_PAGE_FLEX = "preferences://flex";
	public static final String URI_PAGE_DAYS = "preferences://days";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Pour contourner le bug Android 4611, on d�cide ici quel layout de pr�f�rences on va utiliser
		// si on souhaite afficher un sous-menu de pr�f�rences.
		String target = null;
		final Uri uri = getIntent().getData();
		if (uri != null) {
			target = uri.toString();
		}
		if(URI_PAGE_LIMITS.equals(target)) {
            addPreferencesFromResource(R.xml.preferences_limits);
        } else if(URI_PAGE_FLEX.equals(target)) {
            addPreferencesFromResource(R.xml.preferences_flex);
        } else if(URI_PAGE_DAYS.equals(target)) {
            addPreferencesFromResource(R.xml.preferences_days);
        } else {
            addPreferencesFromResource(R.xml.preferences);
        }
		
		final SharedPreferences prefs = getPreferenceManager().getSharedPreferences();
		prefs.registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		PreferencesUtils.updatePreferencesBean(this);
	}
}
