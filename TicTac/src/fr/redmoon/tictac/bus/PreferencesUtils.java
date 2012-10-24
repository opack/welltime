package fr.redmoon.tictac.bus;

import static fr.redmoon.tictac.gui.activities.PreferencesActivity.PATTERN_DAY_TYPE_LABEL;

import java.util.Map;
import java.util.regex.Matcher;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.preference.PreferenceManager;
import fr.redmoon.tictac.R;
import fr.redmoon.tictac.bus.bean.DayType;
import fr.redmoon.tictac.bus.bean.PreferencesBean;
import fr.redmoon.tictac.gui.activities.PreferencesActivity;

public class PreferencesUtils {
	public static void updatePreferencesBean(final Context context) {
		// Met � jour le bean PreferencesBean avec les valeurs sauvegard�es
		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
	// Flag de premier lancement
		PreferencesBean.instance.isFirstLaunch = prefs.getBoolean(PreferenceKeys.isFirstLaunch.getKey(), true);
	
	// Le widget doit-il afficher un timepicker ou pointer � l'heure courante ?
		PreferencesBean.instance.widgetDisplayTimePicker = prefs.getBoolean(PreferenceKeys.widgetDisplayTimePicker.getKey(), false);

	// D�calage pointeuse/t�l�phone
		PreferencesBean.instance.clockShift = TimeUtils.parseMinutes(prefs.getString(PreferenceKeys.clockShift.getKey(), "00:00"));
		
	// Synchronisation avec la calendrier
		PreferencesBean.instance.syncCalendar = prefs.getBoolean(PreferenceKeys.syncCalendar.getKey(), false);
		
	// Limites
		// Temps de travail quotidien
		PreferencesBean.instance.dayMin = TimeUtils.parseMinutes(prefs.getString(PreferenceKeys.dayMin.getKey(), "07:00"));
		PreferencesBean.instance.dayMax = TimeUtils.parseMinutes(prefs.getString(PreferenceKeys.dayMax.getKey(), "09:00"));
		
		// Pause repas
		PreferencesBean.instance.lunchMin = TimeUtils.parseMinutes(prefs.getString(PreferenceKeys.lunchMin.getKey(), "00:45"));
		PreferencesBean.instance.lunchStart = TimeUtils.parseTime(prefs.getString(PreferenceKeys.lunchStart.getKey(), "11:45"));
		PreferencesBean.instance.lunchEnd = TimeUtils.parseTime(prefs.getString(PreferenceKeys.lunchEnd.getKey(), "14:00"));
		
		// Temps de travail hebdomadaire
		PreferencesBean.instance.weekMin = TimeUtils.parseMinutes(prefs.getString(PreferenceKeys.weekMin.getKey(), "35:00"));
		PreferencesBean.instance.weekMax = TimeUtils.parseMinutes(prefs.getString(PreferenceKeys.weekMax.getKey(), "39:00"));
		
		// Nombre de jours dans la semaine
		PreferencesBean.instance.nbDaysInWeek = Integer.valueOf(prefs.getString(PreferenceKeys.nbDaysInWeek.getKey(), "5"));
		
		// Horaire variable
		PreferencesBean.instance.flexMin = TimeUtils.parseMinutes(prefs.getString(PreferenceKeys.flexMin.getKey(), "00:00"));
		PreferencesBean.instance.flexMax = TimeUtils.parseMinutes(prefs.getString(PreferenceKeys.flexMax.getKey(), "07:00"));
		
	// Types de jour
		final Resources res = context.getResources();
		final Map<String, DayType> dayTypes = PreferencesBean.instance.dayTypes;
		dayTypes.clear();
		// Les types de jour "normal" et "non travaill�" sont toujours pr�sents.
		if (!dayTypes.containsKey(StandardDayTypes.normal.name())) {
			dayTypes.put(StandardDayTypes.normal.name(), new DayType(
				StandardDayTypes.normal.name(),
				"Normal",
				0,
				res.getColor(R.color.daytype_normal_default))
			);
		}
		if (!dayTypes.containsKey(StandardDayTypes.not_worked.name())) {
			dayTypes.put(StandardDayTypes.not_worked.name(), new DayType(
				StandardDayTypes.not_worked.name(),
				"Non travaill�",
				0,
				res.getColor(R.color.daytype_not_worked_default))
			);
		}
		
		// C'est le premier lancement de l'application : on ajoute quelques types de jour par d�faut
		// Pour des raisons de compatibilit�s avec l'existant, on modifie les id pour que ce soient
		// des entiers plut�t que des chaines.
		if (PreferencesBean.instance.isFirstLaunch) {
			addDefaultDayTypes(dayTypes, res);
		}
		// Si ce n'est pas le premier lancement, on charge les types de jour �ventuellement d�finis
		// par l'utilisateur
		else {
			String id;
			String label;
			String time;
			int color;
			DayType dayType;
			for (String prefKey : prefs.getAll().keySet()) {
				// Si la cl� n'est pas un type de jour, on passe
				Matcher matcher = PATTERN_DAY_TYPE_LABEL.matcher(prefKey);
				if (!matcher.matches()){
					continue;
				}
				
				// Extracton de l'id, du temps et et de la couleurdu type de jour
				id = matcher.group(1);
				label = prefs.getString(PreferenceKeys.dayTypeLabel.getKey() + id, id);
				time = prefs.getString(PreferenceKeys.dayTypeTime.getKey() + id, "00:00");
				color = prefs.getInt(PreferenceKeys.dayTypeColor.getKey() + id, Color.rgb(245, 245, 245));
				
				// Ajout de la pr�f�rence � la liste
				dayType = new DayType(
					id,
					label,
					TimeUtils.parseMinutes(time),
					color);
				dayTypes.put(id, dayType);
			}
		}
	}
	
	public static void addDefaultDayTypes(final Map<String, DayType> dayTypes, final Resources res) {
		// Les types de jour "normal" et "non travaill�" sont toujours pr�sents.
		dayTypes.put(StandardDayTypes.normal.name(), new DayType(
			StandardDayTypes.normal.name(),
			res.getString(R.string.daytype_normal_default),
			0,
			res.getColor(R.color.daytype_normal_default))
		);
		dayTypes.put(StandardDayTypes.not_worked.name(), new DayType(
			StandardDayTypes.not_worked.name(),
			res.getString(R.string.daytype_not_worked_default),
			0,
			res.getColor(R.color.daytype_not_worked_default))
		);
		// Cong� pay�
		dayTypes.put(StandardDayTypes.vacation.name(), new DayType(
			StandardDayTypes.vacation.name(),
			res.getString(R.string.daytype_vacation_default),
			420,
			res.getColor(R.color.daytype_vacation_default))
		);
		
		// RTT
		dayTypes.put(StandardDayTypes.personaltime.name(), new DayType(
			StandardDayTypes.personaltime.name(),
			res.getString(R.string.daytype_personal_time_default),
			420,
			res.getColor(R.color.daytype_personal_time_default))
		);
		
		// Maladie
		dayTypes.put(StandardDayTypes.illness.name(), new DayType(
			StandardDayTypes.illness.name(),
			res.getString(R.string.daytype_illness_default),
			420,
			res.getColor(R.color.daytype_illness_default))
		);
		
		// F�ri�
		dayTypes.put(StandardDayTypes.publicholiday.name(), new DayType(
			StandardDayTypes.publicholiday.name(),
			res.getString(R.string.daytype_public_holiday_default),
			420,
			res.getColor(R.color.daytype_public_holiday_default))
		);
	}

	public static void savePreference(final Context context, final String key, final boolean value) {
		final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		final SharedPreferences.Editor editor = preferences.edit();
		editor.putBoolean(key, value);
      	editor.commit();
	}
	
	public static void savePreference(final Context context, final String key, final int value) {
		final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		final SharedPreferences.Editor editor = preferences.edit();
		editor.putInt(key, value);
      	editor.commit();
	}
	
	public static void savePreference(final Context context, final String key, final long value) {
		final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		final SharedPreferences.Editor editor = preferences.edit();
		editor.putLong(key, value);
      	editor.commit();
	}
	
	public static boolean isFirstLaunch(final Context context) {
		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		return prefs.getBoolean(PreferenceKeys.isFirstLaunch.getKey(), true);
	}
	
	public static int getPreference(final Context context, final String key, final int defaultValue) {
		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		return prefs.getInt(key, defaultValue);
	}

	/**
	 * Met � jour les pr�f�rences avec les valeurs du bean
	 * @param context
	 */
	public static void savePreferencesBean(final Context context) {
		final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		final SharedPreferences.Editor editor = preferences.edit();
		
	// Flag de premier lancement
		editor.putBoolean(PreferenceKeys.isFirstLaunch.getKey(), PreferencesBean.instance.isFirstLaunch);
		
	// Le widget doit-il afficher un timepicker ou pointer � l'heure courante ?
		editor.putBoolean(PreferenceKeys.widgetDisplayTimePicker.getKey(), PreferencesBean.instance.widgetDisplayTimePicker);
		
	// D�calage pointeuse/t�l�phone
		editor.putString(PreferenceKeys.clockShift.getKey(), TimeUtils.formatMinutes(PreferencesBean.instance.clockShift));

	// Synchronisation avec la calendrier
		editor.putBoolean(PreferenceKeys.syncCalendar.getKey(), PreferencesBean.instance.syncCalendar);
		
	// Limites
		// Temps de travail quotidien
		editor.putString(PreferenceKeys.dayMin.getKey(), TimeUtils.formatMinutes(PreferencesBean.instance.dayMin));
		editor.putString(PreferenceKeys.dayMax.getKey(), TimeUtils.formatMinutes(PreferencesBean.instance.dayMax));
		
		// Pause repas
		editor.putString(PreferenceKeys.lunchMin.getKey(), TimeUtils.formatMinutes(PreferencesBean.instance.lunchMin));
		editor.putString(PreferenceKeys.lunchStart.getKey(), TimeUtils.formatTime(PreferencesBean.instance.lunchStart));
		editor.putString(PreferenceKeys.lunchEnd.getKey(), TimeUtils.formatTime(PreferencesBean.instance.lunchEnd));
		
		// Temps de travail hebdomadaire
		editor.putString(PreferenceKeys.weekMin.getKey(), TimeUtils.formatMinutes(PreferencesBean.instance.weekMin));
		editor.putString(PreferenceKeys.weekMax.getKey(), TimeUtils.formatMinutes(PreferencesBean.instance.weekMax));
		
		// Nombre de jours dans la semaine
		editor.putString(PreferenceKeys.nbDaysInWeek.getKey(), String.valueOf(PreferencesBean.instance.nbDaysInWeek));
				
		// Horaire variable
		editor.putString(PreferenceKeys.flexMin.getKey(), TimeUtils.formatMinutes(PreferencesBean.instance.flexMin));
		editor.putString(PreferenceKeys.flexMax.getKey(), TimeUtils.formatMinutes(PreferencesBean.instance.flexMax));
		
	// Types de jour
		final Map<String, DayType> dayTypes = PreferencesBean.instance.dayTypes;
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
	}

	/**
	 * Affiche les pr�f�rences
	 * @param activity
	 */
	public static void showPreferences(final Activity activity) {
		final Intent prefsActivity = new Intent(activity, PreferencesActivity.class);
		activity.startActivity(prefsActivity);
	}

	/**
	 * R�initialise les pr�f�rences comme au premier d�marrage de l'application
	 * @param activity
	 */
	public static void resetPreferences(final Activity activity) {
		// Note : les 3 lignes qui suivent devraient pouvoir �tre remplac�es par l'appel
    	// � PreferenceManager.setDefaultValues(this, R.xml.preferences, true), mais �a
    	// ne fonctionne pas donc on doit �crire les pr�f�rences � la main :'(
		PreferencesUtils.updatePreferencesBean(activity);
    	PreferencesBean.instance.isFirstLaunch = false;
    	PreferencesUtils.savePreferencesBean(activity);
	}
}
