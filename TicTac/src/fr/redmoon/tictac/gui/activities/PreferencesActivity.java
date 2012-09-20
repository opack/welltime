package fr.redmoon.tictac.gui.activities;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.net.Uri;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import fr.redmoon.tictac.R;
import fr.redmoon.tictac.bus.PreferenceKeys;
import fr.redmoon.tictac.bus.PreferencesUtils;
import fr.redmoon.tictac.bus.export.tocalendar.CalendarAccess;

public class PreferencesActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {
	public static final String URI_PAGE_MAIN = "preferences://main";
	public static final String URI_PAGE_LIMITS = "preferences://limits";
	public static final String URI_PAGE_DAYS = "preferences://days";

	// Nom des pr�f�rences dans les �crans de prefs, ou en tant que pr�fixe dans les pr�f�rences stock�es
	public static final String PREF_DAYTYPE_TITLE = "daytype_title";
	public static final String PREF_DAYTYPE_TIME = "daytype_time";
	public static final String PREF_DAYTYPE_COLOR = "daytype_color";
	private static final String PREF_DAYTYPE_ADD = "daytype_add";
	private static final String PREF_DAYTYPE_REMOVE = "daytype_remove";
	private static final String PREF_DAYTYPE_LABEL = "daytype_label";
	
	public static final Pattern PATTERN_DAY_TYPE_TITLE = Pattern.compile(PreferenceKeys.dayTypeLabel.getKey() + "(.*)");
	
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
		if (URI_PAGE_LIMITS.equals(target)) {
            addPreferencesFromResource(R.xml.preferences_limits);
        } else if (URI_PAGE_DAYS.equals(target)) {
            addPreferencesFromResource(R.xml.preferences_days);
            addDayTypesPreferences();
        } else if (target != null && target.startsWith(URI_PAGE_DAYS + "/")) {
        	addPreferencesFromResource(R.xml.preferences_days_edit);
        	customizeDayTypeEditPreferences(target.substring(URI_PAGE_DAYS.length() + 1));
        } else {
            addPreferencesFromResource(R.xml.preferences);
        }
		
		final SharedPreferences prefs = getPreferenceManager().getSharedPreferences();
		prefs.registerOnSharedPreferenceChangeListener(this);
	}

	private void addDayTypesPreferences() {
		final PreferenceCategory cat = (PreferenceCategory)getPreferenceScreen().findPreference("daytype_types");
		
		// R�cup�ration des jours actuellement d�finis
		final SharedPreferences prefs = getPreferenceManager().getSharedPreferences();
		String title;
		String id;
		for (String prefKey : prefs.getAll().keySet()) {
			// Si la cl� n'est pas un type de jour, on passe
			Matcher matcher = PATTERN_DAY_TYPE_TITLE.matcher(prefKey);
			if (!matcher.matches()){
				continue;
			}
			
			// Extracton du titre et de l'id du type de jour
			id = matcher.group(1);
			title = prefs.getString(prefKey, "");
			
			// Ajout de la pr�f�rence � l'�cran
			Preference pref = new Preference(this);
			pref.setTitle(title);
			Intent intent = new Intent("android.intent.action.VIEW", Uri.parse(URI_PAGE_DAYS + "/" + id), this, PreferencesActivity.class);
			pref.setIntent(intent);
			cat.addPreference(pref);
		}
	}
		
	/**
	 * Ajoute une pr�f�rence � l'�cran de pr�f�rence pour le type de jour
	 * indiqu�.
	 * @param curDayType
	 */
	private void customizeDayTypeEditPreferences(final String id) {
		// Modification du titre de la cat�gorie
		final PreferenceScreen prefScreen = getPreferenceScreen();
		final Preference category = prefScreen.findPreference(PREF_DAYTYPE_TITLE);
		final String title = getPreferenceManager().getSharedPreferences().getString(PreferenceKeys.dayTypeLabel.getKey() + id, "");
		category.setTitle(title);
		
		// Modification de la cl� et valeur par d�faut de la pr�f�rence Time
		final Preference timePref = prefScreen.findPreference(PREF_DAYTYPE_TIME);
		timePref.setKey(PreferenceKeys.dayTypeTime.getKey() + id);
		timePref.setDefaultValue("00:00");
		
		// Modification de la cl� et valeur par d�faut de la pr�f�rence Color
		final Preference colorPref = prefScreen.findPreference(PREF_DAYTYPE_COLOR);
		colorPref.setKey(PreferenceKeys.dayTypeColor.getKey() + id);
		colorPref.setDefaultValue(-657931);
		
		// Modification de la cl� et valeur par d�faut de la pr�f�rence Label
		final EditTextPreference labelPref = (EditTextPreference)prefScreen.findPreference(PREF_DAYTYPE_LABEL);
		labelPref.setKey(PreferenceKeys.dayTypeLabel.getKey() + id);
		labelPref.setDefaultValue(title);
		labelPref.setText(title);
		
		// Pr�paration du bouton de suppression
		final Preference removePref = prefScreen.findPreference(PREF_DAYTYPE_REMOVE);
		removePref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				final Builder adb = new AlertDialog.Builder(PreferencesActivity.this);
				adb.setTitle(R.string.app_name);
				final String message = getResources().getString(R.string.pref_daytype_remove_dialogmessage, title);
				adb.setMessage(message);
				adb.setIcon(android.R.drawable.ic_dialog_alert);
				adb.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
				    public void onClick(DialogInterface dialog, int whichButton) {
				    	// Suppression des pr�f�rences li�es � ce type de jour
				        removeDayType(id);
				        
				        // Affichage de la page pr�c�dente
						onBackPressed();
				    }
				});
				adb.setNegativeButton(android.R.string.no, null);
				adb.show();
				return false;
			}
		});
	}
	
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		// Si on a demand� � ajouter un type de jour
		if (PREF_DAYTYPE_ADD.equals(key)) {
			addDayType(sharedPreferences);
		}
		// Mise � jour de l'affichage si on a modifi� les libell�s
		else if (key.matches(PATTERN_DAY_TYPE_TITLE.pattern())) {
			refreshContent();
		}
		
		// Mise � jour du bean faisant proxy pour les pr�f�rences
		PreferencesUtils.updatePreferencesBean(this);
		
		// Si on a activ� la synchro calendrier, il faut s'assurer qu'on a acc�s au calendrier
		CalendarAccess.getInstance().initAccess(this);
	}

	/**
	 * Ajoute un type de jour d'indice sp�cifi�. Ne fait rien si index == -1.
	 * @param index
	 */
	private void addDayType(final SharedPreferences sharedPreferences) {
		sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
		
		// R�cup�ration de l'identifiant du jour � supprimer
		final String title = sharedPreferences.getString(PREF_DAYTYPE_ADD, null);
		
		// Ajout des pr�f�rences associ�es � ce jour
		final Editor editor = sharedPreferences.edit();
		if (title != null) {
			// Construction de l'identifiant : c'est le titre sans espaces en minuscules
			final String id = title.toLowerCase().replaceAll(" ", "");
			
			// Ajout des pr�f�rences pour ce jour
			editor.putString(PreferenceKeys.dayTypeLabel.getKey() + id, title);
			editor.putString(PreferenceKeys.dayTypeTime.getKey() + id, "00:00");
			editor.putInt(PreferenceKeys.dayTypeColor.getKey() + id, -657931);
		}
		
		// Suppression de la valeur du jour � ajouter
		editor.remove(PREF_DAYTYPE_ADD);
		
		// Enregistrement des modifications
		editor.commit();
		
		sharedPreferences.registerOnSharedPreferenceChangeListener(this);
		
		refreshContent();
	}
	
	/**
	 * Supprime le type de jour d'indice sp�cifi� par la pr�f�rence daytype_remove.
	 * Ne fait rien si cette valeur vaut null.
	 * @param sharedPreferences
	 */
	private void removeDayType(final String id) {
		final SharedPreferences prefs = getPreferenceManager().getSharedPreferences();
		prefs.unregisterOnSharedPreferenceChangeListener(this);
		
		// Suppression des pr�f�rences associ�es � ce jour
		final Editor editor = prefs.edit();
		if (id != null) {
			editor.remove(PreferenceKeys.dayTypeLabel.getKey() + id);
			editor.remove(PreferenceKeys.dayTypeTime.getKey() + id);
			editor.remove(PreferenceKeys.dayTypeColor.getKey() + id);
		}
		
		// Suppression de la valeur du jour � supprimer
		editor.remove(PREF_DAYTYPE_REMOVE);
		
		// Enregistrement des modifications
		editor.commit();
		
		prefs.registerOnSharedPreferenceChangeListener(this);
	}

	private void refreshContent() {
		// Impossible de rafra�chir l'�cran. On va donc rappeler onCreate
		// pour qu'il refasse le m�me boulot d'initialisation qu'au
		// d�marrage de l'activit�. C'est crade, mais il n'y a que �a qui
		// fonctionne.
		// Une autre technique existe : finir l'activit� et la red�marrer
		// avec le m�me Intent. Le probl�me est que cette m�thode met le
		// bazar dans l'historique du bouton back.
		onCreate(null);
	}
}