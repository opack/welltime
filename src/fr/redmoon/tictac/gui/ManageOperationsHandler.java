package fr.redmoon.tictac.gui;

import static fr.redmoon.tictac.gui.activities.ManageActivity.PERIOD_CHECKIN_DIALOG;
import static fr.redmoon.tictac.gui.activities.ManageActivity.PERIOD_SYNC_CALENDAR_DIALOG;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
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
import fr.redmoon.tictac.gui.dialogs.listeners.PeriodCheckinListener;
import fr.redmoon.tictac.gui.dialogs.listeners.PeriodSyncCalendarListener;

public class ManageOperationsHandler implements OnItemClickListener {
	private final static int POS_CHECKIN_PERIOD = 0;
	private final static int POS_SYNC_CALENDAR_PERIOD = 1;
	
	private final Activity activity;
	private final DbAdapter db;
	
	public ManageOperationsHandler(final Activity activity, final DbAdapter db) {
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
		}
	}
	
	public Dialog createPeriodCheckinDialog() {
		//On instancie notre layout en tant que View
        LayoutInflater factory = LayoutInflater.from(activity);
        final View dialogView = factory.inflate(R.layout.dlg_period_checkin, null);
 
        //Cr�ation de l'AlertDialog
        AlertDialog.Builder adb = new AlertDialog.Builder(activity);
 
        //On affecte la vue personnalis� que l'on a cr�e � notre AlertDialog
        adb.setView(dialogView);
        
        //On donne un titre � l'AlertDialog
        adb.setTitle(R.string.period_checkin_title);
 
        //On modifie l'ic�ne de l'AlertDialog pour le fun ;)
        //adb.setIcon(android.R.drawable.ic_dialog_alert);
        
        final List<DayType> dayTypes = new ArrayList<DayType>(PreferencesBean.instance.dayTypes.values());
		ArrayAdapter<DayType> adapter = new ArrayAdapter<DayType>(activity, android.R.layout.simple_spinner_item, dayTypes);
        final Spinner spinner = (Spinner)dialogView.findViewById(R.id.day_type);
	    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	    spinner.setAdapter(adapter);
	    spinner.setPromptId(R.string.dlg_title_edit_day_type);
 
        //On affecte un bouton "OK" � notre AlertDialog et on lui affecte un �v�nement
        adb.setPositiveButton(R.string.btn_checkin, new PeriodCheckinListener(
        		activity,
        		db,
        		(DatePicker)dialogView.findViewById(R.id.date1),
        		(DatePicker)dialogView.findViewById(R.id.date2),
        		(Spinner)dialogView.findViewById(R.id.day_type),
        		(EditText)dialogView.findViewById(R.id.note))
        );
 
        //On cr�e un bouton "Annuler" � notre AlertDialog et on lui affecte un �v�nement
        adb.setNegativeButton(R.string.btn_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
          } });
		return adb.create();
	}

	public Dialog createPeriodSyncCalendarDialog() {
		//On instancie notre layout en tant que View
        LayoutInflater factory = LayoutInflater.from(activity);
        final View dialogView = factory.inflate(R.layout.dlg_period_chooser, null);
 
        //Cr�ation de l'AlertDialog
        AlertDialog.Builder adb = new AlertDialog.Builder(activity);
 
        //On affecte la vue personnalis� que l'on a cr�e � notre AlertDialog
        adb.setView(dialogView);
        
        //On donne un titre � l'AlertDialog
        adb.setTitle(R.string.period_sync_calendar_title);
 
        //On modifie l'ic�ne de l'AlertDialog pour le fun ;)
        //adb.setIcon(android.R.drawable.ic_dialog_alert);
        
        //On affecte un bouton "OK" � notre AlertDialog et on lui affecte un �v�nement
        adb.setPositiveButton(R.string.btn_ok, new PeriodSyncCalendarListener(
        	activity, 
        	db, 
        	(DatePicker)dialogView.findViewById(R.id.date1), 
        	(DatePicker)dialogView.findViewById(R.id.date2))
        );
 
        //On cr�e un bouton "Annuler" � notre AlertDialog et on lui affecte un �v�nement
        adb.setNegativeButton(R.string.btn_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {}
        });
        
		return adb.create();
	}
}
