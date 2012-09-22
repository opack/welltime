package fr.redmoon.tictac.gui;

import static fr.redmoon.tictac.gui.activities.ManageActivity.PERIOD_CHECKIN_DIALOG;
import static fr.redmoon.tictac.gui.activities.ManageActivity.PERIOD_SYNC_CALENDAR_DIALOG;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import fr.redmoon.tictac.R;
import fr.redmoon.tictac.bus.bean.DayType;
import fr.redmoon.tictac.bus.bean.PreferencesBean;
import fr.redmoon.tictac.db.DbAdapter;
import fr.redmoon.tictac.gui.dialogs.fragments.CleanDaysFragment;
import fr.redmoon.tictac.gui.dialogs.fragments.StatisticsFragment;
import fr.redmoon.tictac.gui.dialogs.listeners.PeriodCheckinListener;
import fr.redmoon.tictac.gui.dialogs.listeners.PeriodSyncCalendarListener;

public class ManageOperationsHandler implements OnItemClickListener {
	private final static int POS_CHECKIN_PERIOD = 0;
	private final static int POS_SYNC_CALENDAR_PERIOD = 1;
	private final static int POS_CLEAN_DAYS = 2;
	private final static int POS_STATISTICS = 3;
	
	private final FragmentActivity activity;
	private final DbAdapter db;
	
	public ManageOperationsHandler(final FragmentActivity activity, final DbAdapter db) {
		this.activity = activity;
		this.db = db;
	}

	@Override
	public void onItemClick(AdapterView<?> adapter, View v, int position, long id) {
		switch (position) {
		case POS_CHECKIN_PERIOD:
			activity.showDialog(PERIOD_CHECKIN_DIALOG);
			break;
		case POS_SYNC_CALENDAR_PERIOD:
			activity.showDialog(PERIOD_SYNC_CALENDAR_DIALOG);
			break;
		case POS_CLEAN_DAYS:
			promptCleanDays();
			break;
		case POS_STATISTICS:
			promptStatistics();
			break;
		}
	}
	
	/**
	 * Propose une boîte de dialogue de saisie de période pour retirer les
	 * données de la base
	 * @param date 
	 */
	protected void promptCleanDays() {
		final DialogFragment newFragment = new CleanDaysFragment();
	    newFragment.show(activity.getSupportFragmentManager(), CleanDaysFragment.TAG);
	}
	
	/**
	 * Propose une boîte de dialogue de saisie de période pour 
	 * afficher des statistiques sur la période
	 * @param date 
	 */
	protected void promptStatistics() {
		final DialogFragment newFragment = new StatisticsFragment();
	    newFragment.show(activity.getSupportFragmentManager(), StatisticsFragment.TAG);
	}
	
	public Dialog createPeriodCheckinDialog() {
		//On instancie notre layout en tant que View
        LayoutInflater factory = LayoutInflater.from(activity);
        final View dialogView = factory.inflate(R.layout.dlg_period_checkin, null);
 
        //Création de l'AlertDialog
        AlertDialog.Builder adb = new AlertDialog.Builder(activity);
 
        //On affecte la vue personnalisé que l'on a crée à notre AlertDialog
        adb.setView(dialogView);
        
        //On donne un titre à l'AlertDialog
        adb.setTitle(R.string.period_checkin_title);
 
        //On modifie l'icône de l'AlertDialog pour le fun ;)
        //adb.setIcon(android.R.drawable.ic_dialog_alert);
        
        final List<DayType> dayTypes = new ArrayList<DayType>(PreferencesBean.instance.dayTypes.values());
		ArrayAdapter<DayType> adapter = new ArrayAdapter<DayType>(activity, android.R.layout.simple_spinner_item, dayTypes);
        final Spinner spinner = (Spinner)dialogView.findViewById(R.id.day_type);
	    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	    spinner.setAdapter(adapter);
	    spinner.setPromptId(R.string.dlg_title_edit_day_type);
 
        //On affecte un bouton "OK" à notre AlertDialog et on lui affecte un évènement
        adb.setPositiveButton(R.string.btn_checkin, new PeriodCheckinListener(
        		activity,
        		db,
        		(DatePicker)dialogView.findViewById(R.id.date1),
        		(DatePicker)dialogView.findViewById(R.id.date2),
        		(Spinner)dialogView.findViewById(R.id.day_type),
        		(EditText)dialogView.findViewById(R.id.note))
        );
 
        //On crée un bouton "Annuler" à notre AlertDialog et on lui affecte un évènement
        adb.setNegativeButton(R.string.btn_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
          } });
		return adb.create();
	}

	public Dialog createPeriodSyncCalendarDialog() {
		//On instancie notre layout en tant que View
        LayoutInflater factory = LayoutInflater.from(activity);
        final View dialogView = factory.inflate(R.layout.dlg_period_chooser, null);
 
        //Création de l'AlertDialog
        AlertDialog.Builder adb = new AlertDialog.Builder(activity);
 
        //On affecte la vue personnalisé que l'on a crée à notre AlertDialog
        adb.setView(dialogView);
        
        //On donne un titre à l'AlertDialog
        adb.setTitle(R.string.period_sync_calendar_title);
 
        //On modifie l'icône de l'AlertDialog pour le fun ;)
        //adb.setIcon(android.R.drawable.ic_dialog_alert);
        
        //On affecte un bouton "OK" à notre AlertDialog et on lui affecte un évènement
        adb.setPositiveButton(R.string.btn_ok, new PeriodSyncCalendarListener(
        	activity, 
        	db, 
        	(DatePicker)dialogView.findViewById(R.id.date1), 
        	(DatePicker)dialogView.findViewById(R.id.date2))
        );
 
        //On crée un bouton "Annuler" à notre AlertDialog et on lui affecte un évènement
        adb.setNegativeButton(R.string.btn_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {}
        });
        
		return adb.create();
	}
}
