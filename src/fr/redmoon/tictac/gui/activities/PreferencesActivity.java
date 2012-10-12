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
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import fr.redmoon.tictac.R;
import fr.redmoon.tictac.bus.FlexUtils;
import fr.redmoon.tictac.bus.PreferenceKeys;
import fr.redmoon.tictac.bus.PreferencesUtils;
import fr.redmoon.tictac.bus.StandardDayTypes;
import fr.redmoon.tictac.bus.export.tocalendar.CalendarAccess;
import fr.redmoon.tictac.db.DbAdapter;

public class PreferencesActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {
	public static final String URI_PAGE_MISC = "preferences://misc";
	public static final String URI_PAGE_LIMITS = "preferences://limits";
	public static final String URI_PAGE_DAYS = "preferences://days";

	// Nom des préférences dans les écrans de prefs, ou en tant que préfixe dans les préférences stockées
	public static final String PREF_DAYTYPE_PREFIX = "daytype_";
	public static final String PREF_DAYTYPE_TITLE = PREF_DAYTYPE_PREFIX + "title";
	public static final String PREF_DAYTYPE_TIME = PREF_DAYTYPE_PREFIX + "time";
	public static final String PREF_DAYTYPE_COLOR = PREF_DAYTYPE_PREFIX + "color";
	private static final String PREF_DAYTYPE_LABEL = PREF_DAYTYPE_PREFIX + "label";
	private static final String PREF_DAYTYPE_ADD = PREF_DAYTYPE_PREFIX + "add";
	private static final String PREF_DAYTYPE_REMOVE = PREF_DAYTYPE_PREFIX + "remove";
	private static final String PREF_DAYTYPE_RESET = PREF_DAYTYPE_PREFIX + "reset";	
	
	public static final Pattern PATTERN_DAY_TYPE_TITLE = Pattern.compile(PreferenceKeys.dayTypeLabel.getKey() + "(.*)");
	
	private Bundle mLastSavedInstanceState;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mLastSavedInstanceState = savedInstanceState;
		
		// Pour contourner le bug Android 4611, on décide ici quel layout de préférences on va utiliser
		// si on souhaite afficher un sous-menu de préférences.
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
            addPreferencesFromResource(R.xml.preferences_misc);
        }
		
		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.registerOnSharedPreferenceChangeListener(this);
	}

	private void addDayTypesPreferences() {
		final PreferenceCategory cat = (PreferenceCategory)getPreferenceScreen().findPreference("daytype_types");
		
		// Récupération des jours actuellement définis
		final SharedPreferences prefs = getPreferenceManager().getSharedPreferences();
		String title;
		String id;
		for (String prefKey : prefs.getAll().keySet()) {
			// Si la clé n'est pas un type de jour, on passe
			Matcher matcher = PATTERN_DAY_TYPE_TITLE.matcher(prefKey);
			if (!matcher.matches()){
				continue;
			}
			
			// Extracton du titre et de l'id du type de jour
			id = matcher.group(1);
			title = prefs.getString(prefKey, "");
			
			// Ajout de la préférence à l'écran
			Preference pref = new Preference(this);
			pref.setTitle(title);
			Intent intent = new Intent("android.intent.action.VIEW", Uri.parse(URI_PAGE_DAYS + "/" + id), this, PreferencesActivity.class);
			pref.setIntent(intent);
			cat.addPreference(pref);
		}
		
		// Préparation du bouton de remise à zéro
		final Preference resetPref = getPreferenceScreen().findPreference(PREF_DAYTYPE_RESET);
		resetPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				final Builder adb = new AlertDialog.Builder(PreferencesActivity.this);
				adb.setTitle(R.string.app_name);
				final String message = getResources().getString(R.string.pref_daytype_reset_dialogmessage);
				adb.setMessage(message);
				adb.setIcon(android.R.drawable.ic_dialog_alert);
				adb.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
				    public void onClick(DialogInterface dialog, int whichButton) {
				    	// Mise à zéro des jours
				        PreferencesUtils.resetDayTypesPreferences(PreferencesActivity.this);
				        
				        // Rafraîchissement de l'interface
						refreshContent();
				    }
				});
				adb.setNegativeButton(android.R.string.no, null);
				adb.show();
				return false;
			}
		});
	}
		
	/**
	 * Ajoute une préférence à l'écran de préférence pour le type de jour
	 * indiqué.
	 * @param curDayType
	 */
	private void customizeDayTypeEditPreferences(final String id) {
		// Modification du titre de la catégorie
		final PreferenceScreen prefScreen = getPreferenceScreen();
		final Preference category = prefScreen.findPreference(PREF_DAYTYPE_TITLE);
		final String title = getPreferenceManager().getSharedPreferences().getString(PreferenceKeys.dayTypeLabel.getKey() + id, "");
		category.setTitle(title);
		
		// Modification de la clé et valeur par défaut de la préférence Time
		final Preference timePref = prefScreen.findPreference(PREF_DAYTYPE_TIME);
		timePref.setKey(PreferenceKeys.dayTypeTime.getKey() + id);
		timePref.setDefaultValue("00:00");
		
		// Modification de la clé et valeur par défaut de la préférence Color
		final Preference colorPref = prefScreen.findPreference(PREF_DAYTYPE_COLOR);
		colorPref.setKey(PreferenceKeys.dayTypeColor.getKey() + id);
		colorPref.setDefaultValue(getResources().getColor(R.color.daytype_normal_default));
		
		// Modification de la clé et valeur par défaut de la préférence Label
		final EditTextPreference labelPref = (EditTextPreference)prefScreen.findPreference(PREF_DAYTYPE_LABEL);
		labelPref.setKey(PreferenceKeys.dayTypeLabel.getKey() + id);
		labelPref.setDefaultValue(title);
		labelPref.setText(title);
		
		// Préparation du bouton de suppression
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
				        // Affichage de la page précédente
						onBackPressed();
						
						// Suppression des préférences liées à ce type de jour
				        removeDayType(id);
				    }
				});
				adb.setNegativeButton(android.R.string.no, null);
				adb.show();
				return false;
			}
		});
		// On n'active le bouton que s'il s'agit d'un jour supprimable.
		final boolean isRemovable = 
			!StandardDayTypes.normal.name().equals(id)
			&& !StandardDayTypes.not_worked.name().equals(id);
		removePref.setEnabled(isRemovable);
	}
	
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		// Si on a demandé à ajouter un type de jour
		if (PREF_DAYTYPE_ADD.equals(key)) {
			addDayType(sharedPreferences);
		}
		// Mise à jour de l'affichage si on a modifié les libellés
		else if (key.matches(PATTERN_DAY_TYPE_TITLE.pattern())) {
			refreshContent();
		}
		// S'il y a eut une mise à jour de la durée d'un type de jour, on recalcule l'HV
		else if (key.startsWith(PreferencesActivity.PREF_DAYTYPE_TIME)) {
			final DbAdapter db = new DbAdapter(this);
			final FlexUtils flexUtils = new FlexUtils(db);
		   	flexUtils.updateFlex();
		}
		
		// Mise à jour du bean faisant proxy pour les préférences
		PreferencesUtils.updatePreferencesBean(this);
		
		// Si on a activé la synchro calendrier, il faut s'assurer qu'on a accès au calendrier
		CalendarAccess.getInstance().initAccess(this);
	}

	/**
	 * Ajoute un type de jour d'indice spécifié. Ne fait rien si index == -1.
	 * @param index
	 */
	private void addDayType(final SharedPreferences sharedPreferences) {
		sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
		
		// Récupération de l'identifiant du jour à supprimer
		final String title = sharedPreferences.getString(PREF_DAYTYPE_ADD, null);
		
		// Ajout des préférences associées à ce jour
		final Editor editor = sharedPreferences.edit();
		if (title != null) {
			// Construction de l'identifiant : c'est le titre sans espaces en minuscules
			final String id = title.toLowerCase().replaceAll(" ", "");
			
			// Ajout des préférences pour ce jour
			editor.putString(PreferenceKeys.dayTypeLabel.getKey() + id, title);
			editor.putString(PreferenceKeys.dayTypeTime.getKey() + id, "00:00");
			editor.putInt(PreferenceKeys.dayTypeColor.getKey() + id, getResources().getColor(R.color.daytype_normal_default));
		}
		
		// Suppression de la valeur du jour à ajouter
		editor.remove(PREF_DAYTYPE_ADD);
		
		// Enregistrement des modifications
		editor.commit();
		
		sharedPreferences.registerOnSharedPreferenceChangeListener(this);
		
		refreshContent();
	}
	
	/**
	 * Supprime le type de jour d'indice spécifié par la préférence daytype_remove.
	 * Ne fait rien si cette valeur vaut null.
	 * @param sharedPreferences
	 */
	private void removeDayType(final String id) {
		final SharedPreferences prefs = getPreferenceManager().getSharedPreferences();
		prefs.unregisterOnSharedPreferenceChangeListener(this);
		
		// Suppression des préférences associées à ce jour
		final Editor editor = prefs.edit();
		if (id != null) {
			editor.remove(PreferenceKeys.dayTypeLabel.getKey() + id);
			editor.remove(PreferenceKeys.dayTypeTime.getKey() + id);
			editor.remove(PreferenceKeys.dayTypeColor.getKey() + id);
		}
		
		// Suppression de la valeur du jour à supprimer
		editor.remove(PREF_DAYTYPE_REMOVE);
		
		// Enregistrement des modifications
		editor.commit();
		
		// Ne surtout pas remettre ce listener !!! En effet, une fois la suppression
		// du type de jour effectué, l'Activity n'existera plus (on fait un onBackPressed)
		// et si on la laisse en listener, on va finir par tenter de faire un refreshContent
		// qui échouera pour cause d'activité inexistante.
		//prefs.registerOnSharedPreferenceChangeListener(this);
	}

	private void refreshContent() {
		// Impossible de rafraîchir l'écran. On va donc rappeler onCreate
		// pour qu'il refasse le même boulot d'initialisation qu'au
		// démarrage de l'activité. C'est crade, mais il n'y a que ça qui
		// fonctionne.
		// Une autre technique existe : finir l'activité et la redémarrer
		// avec le même Intent. Le problème est que cette méthode met le
		// bazar dans l'historique du bouton back.
		onCreate(mLastSavedInstanceState);
	}
}
