package fr.redmoon.tictac.gui.activities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.util.Log;
import fr.redmoon.tictac.R;
import fr.redmoon.tictac.bus.PreferenceKeys;
import fr.redmoon.tictac.bus.PreferencesUtils;
import fr.redmoon.tictac.bus.StandardDayTypes;
import fr.redmoon.tictac.bus.TimeUtils;
import fr.redmoon.tictac.bus.UpdateFlexTask;
import fr.redmoon.tictac.bus.bean.DayType;
import fr.redmoon.tictac.bus.bean.PreferencesBean;
import fr.redmoon.tictac.bus.export.tocalendar.CalendarAccess;

public class PreferencesActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {
	public static final String URI_PAGE_MISC = "preferences://misc";
	public static final String URI_PAGE_LIMITS = "preferences://limits";
	public static final String URI_PAGE_DAYS = "preferences://days";

	// Nom des préférences dans les écrans de prefs, ou en tant que préfixe dans les préférences stockées
	public static final String PREF_LIMITS_PREFIX = "limits_";
	public static final String PREF_DAYTYPE_PREFIX = "daytype_";
	private static final String PREF_DAYTYPE_TITLE = PREF_DAYTYPE_PREFIX + "title";
	public static final String PREF_DAYTYPE_TIME = PREF_DAYTYPE_PREFIX + "time";
	public static final String PREF_DAYTYPE_COLOR = PREF_DAYTYPE_PREFIX + "color";
	public static final String PREF_DAYTYPE_LABEL = PREF_DAYTYPE_PREFIX + "label";
	public static final String PREF_DAYTYPE_ADD = PREF_DAYTYPE_PREFIX + "add";
	public static final String PREF_DAYTYPE_REMOVE = PREF_DAYTYPE_PREFIX + "remove";
	private static final String PREF_DAYTYPE_RESET = PREF_DAYTYPE_PREFIX + "reset";	
	
	public static final Pattern PATTERN_DAY_TYPE_LABEL = Pattern.compile(PreferenceKeys.dayTypeLabel.getKey() + "(.*)");
	
	// Statique, car chaque instance de PreferencesActivity (correspondant à une page dans la profondeur
	// des préférences) doit pouvoir accéder à la liste des titres en fonction de l'identifiant du type
	// de jour.
	private static Map<String, String> mDayTitlesById = new HashMap<String, String>();
	
	private static boolean mMustComputeFlex = false;
	
	private String mCurPage;
	private int mDepth;
	
	public interface OnPreferenceChangedListener{
		/**
		 * Appelée lorsqu'une préférence a été mise à jour
		 * @param date
		 */
		void onPreferenceChanged(final SharedPreferences sharedPreferences, final String key);
	}
	private static List<OnPreferenceChangedListener> sPreferenceChangedListeners;
	
	public static void registerPreferenceChangeListener(final OnPreferenceChangedListener listener) {
		if (sPreferenceChangedListeners == null) {
			sPreferenceChangedListeners = new ArrayList<OnPreferenceChangedListener>();
		}
		sPreferenceChangedListeners.add(listener);
	}
	
	/**
	 * Notifie les listeners qu'une préférence a été mise à jour
	 * @param date
	 */
	private static void fireOnPreferenceChanged(final SharedPreferences sharedPreferences, final String key) {
		for (OnPreferenceChangedListener listener : sPreferenceChangedListeners) {
			listener.onPreferenceChanged(sharedPreferences, key);
		}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Pour contourner le bug Android 4611, on décide ici quel layout de préférences on va utiliser
		// si on souhaite afficher un sous-menu de préférences.
		mCurPage = null;
		final Uri uri = getIntent().getData();
		if (uri != null) {
			mCurPage = uri.toString();
		}
		if (URI_PAGE_LIMITS.equals(mCurPage)) {
			mDepth = 1;
            addPreferencesFromResource(R.xml.preferences_limits);
        } else if (URI_PAGE_DAYS.equals(mCurPage)) {
        	mDepth = 1;
        	mMustComputeFlex = false;
            addPreferencesFromResource(R.xml.preferences_days);
            prepareDayTypesPrefScreen();
        } else if (mCurPage != null && mCurPage.startsWith(URI_PAGE_DAYS + "/")) {
        	mDepth = 2;
        	addPreferencesFromResource(R.xml.preferences_days_edit);
        	customizeDayTypeEditPreferences(mCurPage.substring(URI_PAGE_DAYS.length() + 1));
        } else {
        	mDepth = 1;
            addPreferencesFromResource(R.xml.preferences_misc);
        }
		
		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.registerOnSharedPreferenceChangeListener(this);
	}
	
	@Override
	public void onBackPressed() {
		// On fait toutes les mises à jour si on quitte définitivement les
		// préférences, donc qu'on quitte une page de profondeur 1
		if (mDepth == 1) {
			// Mise à jour du bean faisant proxy pour les préférences
			PreferencesUtils.updatePreferencesBean(this);
			
			// Mise à jour du temps additionnel si un temps a été modifié
			// et qu'on quitte la page principale des types de jour ou des
			// limites.
			// Ainsi on ne le fait qu'une seule fois même si plusieurs temps
			// ont été modifiés.
			if (mMustComputeFlex && 
				(URI_PAGE_DAYS.equals(mCurPage) || URI_PAGE_LIMITS.equals(mCurPage)) ) {
				final UpdateFlexTask task = new UpdateFlexTask(this, new UpdateFlexTask.OnTaskCompleteListener() {
					@Override
					public void onTaskComplete() {
						PreferencesActivity.super.onBackPressed();
					}
				});
				task.execute();
			   	mMustComputeFlex = false;
			   	// On ne veut pas quitter tout de suite car on est en train de faire la mise
			   	// à jour de l'HV. On quittera quand le listener aura reçu l'info de fin
			   	// de traitement.
			   	return;
			}
		}
	   	
		super.onBackPressed();
	}
	
	private void prepareDayTypesPrefScreen() {
		// Ajout des différents types de jour
		addDayTypesPreferences();
		
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
				    	resetDayTypes();
				    }
				});
				adb.setNegativeButton(android.R.string.no, null);
				adb.show();
				return false;
			}
		});
	}

	private void addDayTypesPreferences() {
		final PreferenceCategory cat = (PreferenceCategory)getPreferenceScreen().findPreference("daytype_types");
		cat.removeAll();
		mDayTitlesById.clear();
		
		// Récupération des jours actuellement définis
		final SharedPreferences prefs = getPreferenceManager().getSharedPreferences();
		String title;
		String id;
		for (String prefKey : prefs.getAll().keySet()) {
			// Si la clé n'est pas un type de jour, on passe
			Matcher matcher = PATTERN_DAY_TYPE_LABEL.matcher(prefKey);
			if (!matcher.matches()){
				continue;
			}
			
			// Extracton du titre et de l'id du type de jour
			id = matcher.group(1);
			title = prefs.getString(prefKey, "");
			mDayTitlesById.put(id, title);
			
			// Ajout de la préférence à l'écran
			Preference pref = new Preference(this);
			pref.setKey(id);
			pref.setTitle(title);
			Intent intent = new Intent("android.intent.action.VIEW", Uri.parse(URI_PAGE_DAYS + "/" + id), this, PreferencesActivity.class);
			pref.setIntent(intent);
			cat.addPreference(pref);
		}
		
		// Préparation de la liste pour le bouton de suppression
		updateRemovableDayTypesList();
	}
		
	private void updateRemovableDayTypesList() {
		// On ne peut pas supprimer le type de jour "normal" et "non travaillé"
		// Le max() sert juste à s'assurer qu'on ne tente pas de créer des tableaux
		// avec une taille de -2 s'il n'y a aucun type de jour défini
		final int count = Math.max(mDayTitlesById.size() - 2, 0);
		// Création des tableaux		
		final CharSequence[] ids = new CharSequence[count];
		final CharSequence[] titles = new CharSequence[count];
		
		// Recopie des éléments dedans
		int cur = 0;
		String key;
		for (Entry<String, String> entry : mDayTitlesById.entrySet()) {
			key = entry.getKey();
			
			// On ne peut pas supprimer le type de jour "normal" et "non travaillé"
			if (StandardDayTypes.normal.name().equals(key)
			|| StandardDayTypes.not_worked.name().equals(key)) {
				continue;
			}
			
			// Ajout du type de jour aux jours supprimables
			ids[cur] = entry.getKey();
			titles[cur] = entry.getValue();
			cur++;
		}
		
		// Affectation des tableaux à la ListPreference
		final ListPreference removePref = (ListPreference)getPreferenceScreen().findPreference(PREF_DAYTYPE_REMOVE);
		removePref.setEntries(titles);
		removePref.setEntryValues(ids);
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
	}
	
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		// Si on a demandé à ajouter un type de jour
		if (PREF_DAYTYPE_ADD.equals(key)) {
			addDayType(sharedPreferences);
		}
		// Si on a demandé à supprimer un type de jour
		else if (PREF_DAYTYPE_REMOVE.equals(key)) {
			removeDayType(sharedPreferences);
		}
		// Si on a renommé un type de jour
		else if (key.startsWith(PREF_DAYTYPE_LABEL)) {
			// On passera dans renameDayType() pour toutes les instances de PreferencesActivity :
			// celle actuellement affichée au moment de l'édition du libellé (chargée de l'édition
			// d'un type de jour) mais aussi l'instance précédente (affichant la liste des types de jour).
			renameDayType(sharedPreferences, key);
		}
		// S'il y a eut une mise à jour de la durée d'un type de jour, on recalcule l'HV
		else if (key.startsWith(PREF_DAYTYPE_TIME)
			|| key.startsWith(PREF_LIMITS_PREFIX)) {
			mMustComputeFlex = true;
		}
		// Si on a activé la synchro calendrier, il faut s'assurer qu'on a accès au calendrier
		else if ("sync_calendar".equals(key)) {
			CalendarAccess.getInstance().initAccess(this);
		}
		
		// Mise à jour de l'interface
		fireOnPreferenceChanged(sharedPreferences, key);
	}

	private void renameDayType(final SharedPreferences sharedPreferences, final String key) {
		final String title = sharedPreferences.getString(key, "");		
		final String id = key.substring(PREF_DAYTYPE_LABEL.length() + 1);
		
		// Mise à jour de l'écran actuel (édition des propriétés du type de jour)
		final Preference catEditDayType = getPreferenceScreen().findPreference(PREF_DAYTYPE_TITLE);
		if (catEditDayType != null) {
			// On ne passera ici que dans l'instance de PreferencesActivity gérant
			// l'édition des détails du jour
			catEditDayType.setTitle(title);
		}
		
		// Mise à jour de l'écran précédent (liste des types de jour)
		final PreferenceCategory catDaysTypesList = (PreferenceCategory)getPreferenceScreen().findPreference("daytype_types");
		if (catDaysTypesList != null) {
			// On ne passera ici que dans l'instance de PreferencesActivity gérant
			// la liste des types de jour.
			// On va alors récupérer la Preference liée au type de jour dont le
			// libellé à changé.
			final Preference pref = catDaysTypesList.findPreference(id);
			if (pref != null) {
				pref.setTitle(title);
				
				// Mise à jour de la liste des suppressions
				mDayTitlesById.put(id, title);
				updateRemovableDayTypesList();
			}
		}
	}

	/**
	 * Ajoute un type de jour d'indice spécifié. Ne fait rien si index == -1.
	 * @param index
	 */
	private void addDayType(final SharedPreferences sharedPreferences) {
		sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
		
		// Récupération de l'identifiant du jour à ajouter et construction
		// de l'identifiant (c'est le titre en minuscules sans espaces)
		final String title = sharedPreferences.getString(PREF_DAYTYPE_ADD, "");
		final String id = title.toLowerCase().replaceAll(" ", "");
		
		// Ajout des préférences associées à ce jour
		final Editor editor = sharedPreferences.edit();
		if (id.length() > 0) {
			// Ajout des préférences pour ce jour
			editor.putString(PreferenceKeys.dayTypeLabel.getKey() + id, title);
			editor.putString(PreferenceKeys.dayTypeTime.getKey() + id, "00:00");
			editor.putInt(PreferenceKeys.dayTypeColor.getKey() + id, getResources().getColor(R.color.daytype_normal_default));
			
			// Ajout de la préférence dans la liste
			final PreferenceCategory cat = (PreferenceCategory)getPreferenceScreen().findPreference("daytype_types");
			Preference pref = new Preference(this);
			pref.setKey(id);
			pref.setTitle(title);
			Intent intent = new Intent("android.intent.action.VIEW", Uri.parse(URI_PAGE_DAYS + "/" + id), this, PreferencesActivity.class);
			pref.setIntent(intent);
			cat.addPreference(pref);
			
			// Ajout de la valeur dans la liste des suppressions possibles
			mDayTitlesById.put(id, title);
			updateRemovableDayTypesList();
		}
		
		// Suppression de la valeur du jour à ajouter
		editor.remove(PREF_DAYTYPE_ADD);
		
		// Enregistrement des modifications
		editor.commit();
		
		sharedPreferences.registerOnSharedPreferenceChangeListener(this);
		
		// Si l'identifiant est correct, on va éditer les propriétés du jour
		if (id.length() > 0) {
			Intent intent = new Intent("android.intent.action.VIEW", Uri.parse(URI_PAGE_DAYS + "/" + id), this, PreferencesActivity.class);
			startActivity(intent);
		}
	}
	
	/**
	 * Supprime le type de jour d'indice spécifié par la préférence daytype_remove.
	 * Ne fait rien si cette valeur vaut null.
	 * @param sharedPreferences
	 */
	private void removeDayType(final SharedPreferences prefs) {
		prefs.unregisterOnSharedPreferenceChangeListener(this);
		
		final String id = prefs.getString(PREF_DAYTYPE_REMOVE, "");
		
		// Suppression des préférences associées à ce jour
		final Editor editor = prefs.edit();
		if (id.length() > 0) {
			editor.remove(PreferenceKeys.dayTypeLabel.getKey() + id);
			editor.remove(PreferenceKeys.dayTypeTime.getKey() + id);
			editor.remove(PreferenceKeys.dayTypeColor.getKey() + id);
			
			// Suppression de la préférence dans la liste de jours
			final PreferenceCategory cat = (PreferenceCategory)getPreferenceScreen().findPreference("daytype_types");
			final Preference pref = cat.findPreference(id);
			if (pref != null) {
				cat.removePreference(pref);
				
				// Suppression de la valeur dans la liste des suppressions possibles
				mDayTitlesById.remove(id);
				updateRemovableDayTypesList();
			} else {
				Log.e("TicTac", "Impossible de supprimer le type de jour " + id + " car il n'existe pas.");
			}
		}
		
		// Suppression de la valeur du jour à supprimer
		editor.remove(PREF_DAYTYPE_REMOVE);
		
		// Enregistrement des modifications
		editor.commit();
		
		prefs.registerOnSharedPreferenceChangeListener(this);
	}
	
	private void resetDayTypes() {
		final SharedPreferences prefs = getPreferenceManager().getSharedPreferences();
		prefs.unregisterOnSharedPreferenceChangeListener(this);
		
		// Mise à zéro des jours...
		final Map<String, DayType> dayTypes = PreferencesBean.instance.dayTypes;
		
		// Supprime les préférences des types de jour
		final SharedPreferences.Editor editor = prefs.edit();
		for (String prefKey : prefs.getAll().keySet()) {
			// S'il s'agit d'une préférence liée aux types de jour, alors sa clé
			// commence par PREF_DAYTYPE_PREFIX. Le cas échéant, on la supprime.
			if (prefKey.startsWith(PreferencesActivity.PREF_DAYTYPE_PREFIX)) {
				editor.remove(prefKey);
			}
		}
		
		// Ajoute les préférences par défaut
		dayTypes.clear();
		PreferencesUtils.addDefaultDayTypes(dayTypes, getResources());
		
		// Sauvegarde des préférences
		String id;
		DayType dayType;
		for (Map.Entry<String, DayType> entry : dayTypes.entrySet()) {
			id = entry.getKey();
			dayType = entry.getValue();
			
			editor.putString(PreferenceKeys.dayTypeLabel.getKey() + id, dayType.label);
			editor.putString(PreferenceKeys.dayTypeTime.getKey() + id, TimeUtils.formatMinutes(dayType.time));
			editor.putInt(PreferenceKeys.dayTypeColor.getKey() + id, dayType.color);
		}
		
		editor.commit();
        
        // Rafraîchissement de l'interface
        addDayTypesPreferences();
        
        prefs.registerOnSharedPreferenceChangeListener(this);
	}
}
