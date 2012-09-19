package fr.redmoon.tictac.gui.activities;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.AlertDialog;
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
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import fr.redmoon.tictac.R;
import fr.redmoon.tictac.bus.PreferencesUtils;
import fr.redmoon.tictac.bus.export.tocalendar.CalendarAccess;

public class PreferencesActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {
	public static final String URI_PAGE_MAIN = "preferences://main";
	public static final String URI_PAGE_LIMITS = "preferences://limits";
	public static final String URI_PAGE_DAYS = "preferences://days";
	public static final Pattern PATTERN_DAY_TYPE = Pattern.compile("daytype_(.*)_title");
	
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
		final List<String> titles = new ArrayList<String>();
		final List<String> ids = new ArrayList<String>();
		
		final PreferenceCategory cat = new PreferenceCategory(this);
        cat.setTitle("Types de jour");
        
        // Ajout de la cat�gorie � l'�cran, et ajout des pr�f�rences �
        // la cat�gorie.
        // Attention : cet ordre doit �tre respect� !
        getPreferenceScreen().addPreference(cat);
		
		// R�cup�ration des jours actuellement d�finis
		final SharedPreferences prefs = getPreferenceManager().getSharedPreferences();
		String title;
		String id;
		for (String prefKey : prefs.getAll().keySet()) {
			// Si la cl� n'est pas un type de jour, on passe
			Matcher matcher = PATTERN_DAY_TYPE.matcher(prefKey);
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
			
			// Sauvegarde du titre et de l'indice pour la cr�ation des actions de gestion
			titles.add(title);
			ids.add(id);
		}
		
		// Ajout de la cat�gorie de gestion des types de jour
		final String[] fakeArrayForType = new String[]{};
		addDayTypeManagementPreferences(titles.toArray(fakeArrayForType), ids.toArray(fakeArrayForType));
	}

	/**
	 * Ajoute une pr�f�rence � l'�cran de pr�f�rence pour le type de jour
	 * indiqu�.
	 * @param curDayType
	 */
	private void customizeDayTypeEditPreferences(final String id) {
		// Modification du titre de la cat�gorie
		final PreferenceScreen prefScreen = getPreferenceScreen();
		final Preference category = prefScreen.findPreference("daytype_title");
		final String title = getPreferenceManager().getSharedPreferences().getString("daytype_" + id + "_title", "");
		category.setTitle(title);
		
		// Modification de la cl� et valeur par d�faut de la pr�f�rence Time
		final Preference timePref = prefScreen.findPreference("daytype_time");
		timePref.setKey("daytype_" + id + "_time");
		timePref.setDefaultValue("00:00");
		
		// Modification de la cl� et valeur par d�faut de la pr�f�rence Color
		final Preference colorPref = prefScreen.findPreference("daytype_color");
		colorPref.setKey("daytype_" + id + "_color");
		colorPref.setDefaultValue(-657931);
		
		// Modification de la cl� et valeur par d�faut de la pr�f�rence Label
		final Preference labelPref = prefScreen.findPreference("daytype_label");
		labelPref.setKey("daytype_" + id + "_title");
		labelPref.setDefaultValue(title);
	}
	
//	/**
//	 * Ajoute les entr�es permettant la gestion (ajout, modification, suppression) des types de jour
//	 * @param dayTitles
//	 * @param dayIndexes
//	 */
//	private void addDayTypeManagementPreferences(final String[] dayTitles, final String[] dayIndexes) {
//        // Ajout de la pr�f�rence permettant l'ajout d'un type de jour
//		final EditTextPreference addPref = new EditTextPreference(this);
//		addPref.setTitle("Ajouter");
//		addPref.setSummary("Ajoute un type de jour");
//		addPref.setKey("daytype_add");
//		addPref.setDialogTitle("Choisissez un libell� pour ce type de jour :");
//		
//		// Ajout de la pr�f�rence permettant la modification du nom d'un type de jour
//		final ListPreference modifyPref = new ListPreference(this);
//		modifyPref.setTitle("Modifier");
//		modifyPref.setSummary("Modifie le libell� d'un type de jour");
//		modifyPref.setKey("daytype_modify");
//		modifyPref.setDialogTitle("Choisissez le type de jour � modifier :");
//		modifyPref.setEntries(dayTitles);
//		modifyPref.setEntryValues(dayIndexes);
//		modifyPref.setEnabled(dayTitles.length != 0);
//		
//		// Ajout de la pr�f�rence permettant la suppression d'un type de jour
//		final ListPreference removePref = new ListPreference(this);
//		removePref.setTitle("Supprimer");
//		removePref.setSummary("Supprime un type de jour");
//		removePref.setKey("daytype_remove");
//		removePref.setDialogTitle("Choisissez le type de jour � supprimer :");
//		removePref.setEntries(dayTitles);
//		removePref.setEntryValues(dayIndexes);
//		removePref.setEnabled(dayTitles.length != 0);
//		
//		// Pr�paration de la cat�gorie regroupant ces deux pr�f�rences
//        final PreferenceCategory cat = new PreferenceCategory(this);
//        cat.setTitle("Gestion des types de jour");
//        
//        // Ajout de la cat�gorie � l'�cran, et ajout des pr�f�rences �
//        // la cat�gorie.
//        // Attention : cet ordre doit �tre respect� !
//        getPreferenceScreen().addPreference(cat);
//        cat.addPreference(addPref);
//        cat.addPreference(modifyPref);
//        cat.addPreference(removePref);
//	}


	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		// Si on a demand� � ajouter un type de jour
		if ("daytype_add".equals(key)) {
			addDayType(sharedPreferences);
		}
		// Si on a demand� � modifier un type de jour
		else if ("daytype_modify".equals(key)) {
			modifyDayType(sharedPreferences);
		}
		// Si on a demand� � supprimer un type de jour, on le supprime
		else if ("daytype_remove".equals(key)) {
			removeDayType(sharedPreferences);
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
		final String title = sharedPreferences.getString("daytype_add", null);
		
		// Ajout des pr�f�rences associ�es � ce jour
		final Editor editor = sharedPreferences.edit();
		if (title != null) {
			// Construction de l'identifiant : c'est le titre sans espaces en minuscules
			final String id = title.toLowerCase().replaceAll(" ", "");
			
			// Ajout des pr�f�rences pour ce jour
			editor.putString("daytype_" + id + "_title", title);
			editor.putString("daytype_" + id + "_time", "00:00");
			editor.putInt("daytype_" + id + "_color", -657931);
		}
		
		// Suppression de la valeur du jour � ajouter
		editor.putString("daytype_add", null);
		
		// Enregistrement des modifications
		editor.commit();
		
		sharedPreferences.registerOnSharedPreferenceChangeListener(this);
		
		refreshContent();
	}
	
	/**
	 * Modifie le libell� du type de jour d'indice sp�cifi� par la pr�f�rence daytype_remove.
	 * Ne fait rien si cette valeur vaut null.
	 * @param sharedPreferences
	 */
	private void modifyDayType(final SharedPreferences sharedPreferences) {
		// R�cup�ration de l'identifiant du jour � supprimer
		final String id = sharedPreferences.getString("daytype_modify", null);
		
		final Editor editor = sharedPreferences.edit();
		if (id != null) {
			// R�cup�ration de l'ancien libell�
			final String oldTitle = sharedPreferences.getString("daytype_" + id + "_title", "");
			
			// Saisie du nouveau libell�
			//On instancie notre layout en tant que View
			LayoutInflater factory = LayoutInflater.from(this);
	        final View dialogView = factory.inflate(R.layout.dlg_text_input, null);
	        
	        //Cr�ation de l'AlertDialog
	        AlertDialog.Builder adb = new AlertDialog.Builder(this);
	 
	        //On affecte la vue personnalis� que l'on a cr�e � notre AlertDialog
	        adb.setView(dialogView);
	        
	        //On donne un titre � l'AlertDialog
			adb.setTitle(R.string.dlg_title_edit_day_note);
			
			//On modifie l'ic�ne de l'AlertDialog pour le fun ;)
	        //adb.setIcon(android.R.drawable.ic_dialog_alert);
			
			final EditText input = (EditText)dialogView.findViewById(R.id.input);
			input.setText(oldTitle);
			input.setSelection(oldTitle.length());
			
			adb.setPositiveButton(R.string.btn_save, new DialogInterface.OnClickListener() {
				@Override
	    		public void onClick(DialogInterface dialog, int arg) {
					sharedPreferences.unregisterOnSharedPreferenceChangeListener(PreferencesActivity.this);					
					// Modification du libell�
					editor.putString("daytype_" + id + "_title", input.getText().toString());
					// Suppression de la valeur du jour � supprimer
	        		editor.putString("daytype_modify", null);
					// Enregistrement des modifications
					editor.commit();
					sharedPreferences.registerOnSharedPreferenceChangeListener(PreferencesActivity.this);
					refreshContent();
	        	}
			});
			adb.setNegativeButton(R.string.btn_cancel, new DialogInterface.OnClickListener() {
				@Override
	    		public void onClick(DialogInterface dialog, int id) {
	            	dialog.dismiss();
	            	sharedPreferences.unregisterOnSharedPreferenceChangeListener(PreferencesActivity.this);
	            	// Suppression de la valeur du jour � supprimer
	        		editor.putString("daytype_modify", null);
	        		editor.commit();
					sharedPreferences.registerOnSharedPreferenceChangeListener(PreferencesActivity.this);
	        	}
			});
			
			adb.show();
		}
	}
	
	/**
	 * Supprime le type de jour d'indice sp�cifi� par la pr�f�rence daytype_remove.
	 * Ne fait rien si cette valeur vaut null.
	 * @param sharedPreferences
	 */
	private void removeDayType(final SharedPreferences sharedPreferences) {
		sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
		
		// R�cup�ration de l'identifiant du jour � supprimer
		final String id = sharedPreferences.getString("daytype_remove", null);
		
		// Suppression des pr�f�rences associ�es � ce jour
		final Editor editor = sharedPreferences.edit();
		if (id != null) {
			editor.remove("daytype_" + id + "_title");
			editor.remove("daytype_" + id + "_time");
			editor.remove("daytype_" + id + "_color");
		}
		
		// Suppression de la valeur du jour � supprimer
		editor.putString("daytype_remove", null);
		
		// Enregistrement des modifications
		editor.commit();
		
		sharedPreferences.registerOnSharedPreferenceChangeListener(this);
		
		refreshContent();
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
