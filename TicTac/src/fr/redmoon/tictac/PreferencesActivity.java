package fr.redmoon.tictac;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import fr.redmoon.tictac.bus.PreferenceKeys;
import fr.redmoon.tictac.bus.PreferencesUtils;
import fr.redmoon.tictac.bus.bean.PreferencesBean;

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
		
		// Si on a modifi� une pr�f�rence en rapport avec l'HV, on met � 0 les flexCurXXX
		// pour qu'ils soient recalcul�s d�s que possible (dans WeekActivity)
		if (PreferenceKeys.flexInitDate.getKey().equals(key)
		|| PreferenceKeys.flexInitTime.getKey().equals(key)) {
			PreferencesBean.instance.flexCurDate = 0;
			PreferencesBean.instance.flexCurTime = 0;
			PreferencesUtils.savePreference(this, PreferenceKeys.flexCurDate.getKey(), (long)0);
			PreferencesUtils.savePreference(this, PreferenceKeys.flexCurTime.getKey(), 0);
		}
	}
}
