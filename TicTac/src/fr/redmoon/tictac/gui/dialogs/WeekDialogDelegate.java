package fr.redmoon.tictac.gui.dialogs;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TimePicker;
import fr.redmoon.tictac.R;
import fr.redmoon.tictac.bus.DateUtils;
import fr.redmoon.tictac.bus.TimeUtils;
import fr.redmoon.tictac.gui.activities.TicTacActivity;
import fr.redmoon.tictac.gui.dialogs.listeners.AddDayListener;
import fr.redmoon.tictac.gui.dialogs.listeners.EditFlexTimeListener;
import fr.redmoon.tictac.gui.dialogs.listeners.ShowDayListener;

public class WeekDialogDelegate extends AbsDialogDelegate {
	private final CharSequence NEGATIVE_SIGN = "-";
	private final CharSequence POSITIVE_SIGN = "+";
	
	/**
	 * Listeners pour les boîtes de dialogue
	 */
	private AddDayListener mAddDayListener;
	private ShowDayListener mShowDayListener;
	private EditFlexTimeListener mEditFlexTimeListener;
	
	public WeekDialogDelegate(final TicTacActivity activity) {
		super(activity);
		
		// Préparation des listeners pour les boîtes de dialogue
        mAddDayListener = new AddDayListener(mActivity);
        mShowDayListener = new ShowDayListener(mActivity);
        mEditFlexTimeListener = new EditFlexTimeListener(mActivity);
	}
	
	@Override
	public Dialog createDialog(final int id) {
		final long today = mActivity.getToday();
		switch (id) {
		case DialogTypes.DATEPICKER_ADD_DAY:
			return DatePickerDialogHelper.createDialog(mActivity, mAddDayListener, DateUtils.extractYear(today), DateUtils.extractMonth(today), DateUtils.extractDayOfMonth(today));
		case DialogTypes.DATEPICKER_SHOW_DAY:
			return DatePickerDialogHelper.createDialog(mActivity, mShowDayListener, DateUtils.extractYear(today), DateUtils.extractMonth(today), DateUtils.extractDayOfMonth(today));
		case DialogTypes.DATETIMEPICKER_EDIT_FLEXTIME:
			return createEditFlexTimeDialog();
		}
		return null;
	}
	
	@Override
	public void prepareDialog(final int id, final Dialog dialog, final Bundle args) {
		final long date = args.getLong(DialogArgs.DATE);
		switch (id) {
			case DialogTypes.DATEPICKER_ADD_DAY:
			case DialogTypes.DATEPICKER_SHOW_DAY:
				// C'est un peu inutile car on passe toujours le jour courant.
				DatePickerDialogHelper.prepare((DatePickerDialog) dialog, DateUtils.extractYear(date), DateUtils.extractMonth(date), DateUtils.extractDayOfMonth(date));
				break;
			case DialogTypes.DATETIMEPICKER_EDIT_FLEXTIME:
				int time = args.getInt(DialogArgs.TIME);
				// Si le temps est inconnu, alors on initialise la boîte de dialogue à 0
				if (time == TimeUtils.UNKNOWN_TIME) {
					time = 0;
				}
				
				mEditFlexTimeListener.prepare(date, time);
				final DatePicker datePicker = (DatePicker)dialog.findViewById(R.id.date);
				datePicker.updateDate(DateUtils.extractYear(date), DateUtils.extractMonth(date), DateUtils.extractDayOfMonth(date));
				final Button btnSign = (Button)dialog.findViewById(R.id.btn_sign);
				if (time < 0) {
					btnSign.setText(NEGATIVE_SIGN);
					btnSign.setTag(-1);
					
					// Pour ne pas perturber l'initialisation du TimePicker, on fait comme si time
					// était positif
					time = -time;
				} else {
					btnSign.setText(POSITIVE_SIGN);
					btnSign.setTag(1);
				}
				final TimePicker timePicker = (TimePicker)dialog.findViewById(R.id.time);
				timePicker.setCurrentHour(time / 60);
				timePicker.setCurrentMinute(time % 60);
				break;
		}
	}
	
	protected Dialog createEditFlexTimeDialog() {
		//On instancie notre layout en tant que View
		LayoutInflater factory = LayoutInflater.from(mActivity);
        final View dialogView = factory.inflate(R.layout.dlg_date_time, null);
        
        //Création de l'AlertDialog
        AlertDialog.Builder adb = new AlertDialog.Builder(mActivity);
 
        //On affecte la vue personnalisé que l'on a crée à notre AlertDialog
        adb.setView(dialogView);
        
        //On donne un titre à l'AlertDialog
		adb.setTitle(R.string.dlg_title_edit_flex_time);
		
		//On modifie l'icône de l'AlertDialog pour le fun ;)
        //adb.setIcon(android.R.drawable.ic_dialog_alert);
		
		final Button btnSign = (Button)dialogView.findViewById(R.id.btn_sign);
		btnSign.setText(POSITIVE_SIGN);
		btnSign.setTag(1);
		btnSign.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if ((Integer)btnSign.getTag() == -1) {
					btnSign.setText(POSITIVE_SIGN);
					btnSign.setTag(1);
				} else {
					btnSign.setText(NEGATIVE_SIGN);
					btnSign.setTag(-1);
				}
			}
		});
		
		final DatePicker datePicker = (DatePicker)dialogView.findViewById(R.id.date);
		final TimePicker timePicker = (TimePicker)dialogView.findViewById(R.id.time);
		timePicker.setIs24HourView(DateFormat.is24HourFormat(mActivity));
		adb.setPositiveButton(R.string.btn_save, new DialogInterface.OnClickListener() {
			@Override
    		public void onClick(DialogInterface dialog, int id) {
				// Suppression du focus pour conserver la valeur éventuellement saisie
				// par l'utilisateur à la main dans les champs
				datePicker.clearFocus();
				timePicker.clearFocus();
				
				final long date = DateUtils.getDayId(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth());
				final int time = timePicker.getCurrentHour() * 60 + timePicker.getCurrentMinute();
				final int sign = (Integer)btnSign.getTag();
				mEditFlexTimeListener.onDateAndTimeSet(date, sign * time);
        	}
		});
		
		adb.setNegativeButton(R.string.btn_cancel, new DialogInterface.OnClickListener() {
			@Override
    		public void onClick(DialogInterface dialog, int id) {
            	dialog.dismiss();
        	}
		});
		
		return adb.create();
	}
}
