package fr.redmoon.tictac.gui.activities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import fr.redmoon.tictac.R;
import fr.redmoon.tictac.bus.DateUtils;
import fr.redmoon.tictac.bus.TimeUtils;
import fr.redmoon.tictac.bus.bean.DayBean;
import fr.redmoon.tictac.bus.bean.PreferencesBean;
import fr.redmoon.tictac.db.DbAdapter;
import fr.redmoon.tictac.gui.adapters.WorkedDaysByTypeAdapter;

public class StatisticsResultsActivity extends Activity {
	public static final String EXTRA_FIRST_DAY = StatisticsResultsActivity.class.getCanonicalName() + "FirstDay";
	public static final String EXTRA_LAST_DAY = StatisticsResultsActivity.class.getCanonicalName() + "LastDay";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    
	    // Création de l'interface graphique
        setContentView(R.layout.view_manage_statistics);
        
        // Récupération des jours
        final long firstDay = getIntent().getExtras().getLong(EXTRA_FIRST_DAY);
        final long lastDay = getIntent().getExtras().getLong(EXTRA_LAST_DAY);
        final List<DayBean> days = new ArrayList<DayBean>();
        DbAdapter.getInstance().fetchDays(firstDay, lastDay, days);
		
		// Calcul des statistiques et affichage des résultats
		computePeriodStatistics(days, firstDay, lastDay);
	}
	
	/**
	 * Calcule les statistiques de la période et met à jour la boîte de dialogue
	 * spécifiée.
	 * @param firstDay
	 * @param lastDay
	 * @param dialogView
	 */
	private void computePeriodStatistics(final List<DayBean> days, final long firstDay, final long lastDay) {
		// Nombre de jours travaillés de chaque type
		final Map<String, Float> nbWorkedDaysByType = new HashMap<String, Float>();
		for (String dayTypeId : PreferencesBean.instance.dayTypes.keySet()) {
			nbWorkedDaysByType.put(dayTypeId, Float.valueOf(0.0f));
		}
		Float nbWorkedDaysForThisType;
		// Nombre de jours travaillés (= nombre de jour où il a fallu aller au travail, donc
		// nombre de jours comptant au moins 1 pointage)
		int nbWorkedDays = 0;
		// Nombre d'heures travaillées (utile pour les indépendants)
		int workTime;
		int totalWorkedTime = 0; // En minutes
		// Nombre d’heures écrêtées
		int totalLostTime = 0; // En minutes
		// Temps de travail moyen par jour (en comptant le temps perdu)
		int meanDayWorkTime = 0;
		
		// Parcours des jours et calcul des statistiques
		for (DayBean day : days) {
			// Nombre de jours travaillés de chaque type
			nbWorkedDaysForThisType = nbWorkedDaysByType.get(day.typeMorning);
			nbWorkedDaysByType.put(day.typeMorning, nbWorkedDaysForThisType.floatValue() + 0.5f);
			nbWorkedDaysForThisType = nbWorkedDaysByType.get(day.typeAfternoon);
			nbWorkedDaysByType.put(day.typeAfternoon, nbWorkedDaysForThisType.floatValue() + 0.5f);
			
			// Nombre de jours travaillés
			if (!day.checkings.isEmpty()) {
				nbWorkedDays++;
			}
			
			// Nombre d'heures travaillées
			workTime = TimeUtils.computeTotal(day, false);
			totalWorkedTime += workTime;
			
			// Nombre d’heures écrêtées
			totalLostTime += Math.max(0, workTime - PreferencesBean.instance.dayMax);
		}
		// Temps de travail moyen par jour (en comptant le temps perdu)
		if (nbWorkedDays > 0) {
			meanDayWorkTime = totalWorkedTime / nbWorkedDays;
		}
		
		// Mise à jour de la boîte de dialogue
		final String caption = getResources().getString(
			R.string.period_statistics_caption,
			DateUtils.formatDateDDMMYYYY(firstDay),
			DateUtils.formatDateDDMMYYYY(lastDay));
		final TextView txtCaption = (TextView)findViewById(R.id.txt_period_statistics);
		txtCaption.setText(caption);
		final TextView txtTotalWorkedDays = (TextView)findViewById(R.id.txt_total_worked_days);
		txtTotalWorkedDays.setText(String.valueOf(nbWorkedDays));
		final TextView txtTotalWorkedTime = (TextView)findViewById(R.id.txt_total_worked_time);
		txtTotalWorkedTime.setText(TimeUtils.formatMinutes(totalWorkedTime));
		final TextView txtTotalLostTime = (TextView)findViewById(R.id.txt_total_lost_time);
		txtTotalLostTime.setText(TimeUtils.formatMinutes(totalLostTime));
		final TextView txtMeanDayWorkTime = (TextView)findViewById(R.id.txt_mean_day_work_time);
		txtMeanDayWorkTime.setText(TimeUtils.formatMinutes(meanDayWorkTime));
		
		final List<String[]> items = new ArrayList<String[]>();
		String[] data;
		for (Map.Entry<String, Float> entry : nbWorkedDaysByType.entrySet()) {
			data = new String[2];
			data[0] = PreferencesBean.getLabelByDayType(entry.getKey());
			data[1] = entry.getValue().toString();
			items.add(data);
		}
		final ListAdapter adapter = new WorkedDaysByTypeAdapter(this, R.layout.itm_worked_days_by_type, items);
		final ListView lstWorkedDaysByTypes = (ListView)findViewById(R.id.lst_days_by_type);
		lstWorkedDaysByTypes.setAdapter(adapter);		
	}
}
