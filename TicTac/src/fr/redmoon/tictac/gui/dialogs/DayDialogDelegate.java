package fr.redmoon.tictac.gui.dialogs;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import fr.redmoon.tictac.R;
import fr.redmoon.tictac.TicTacActivity;
import fr.redmoon.tictac.bus.DateUtils;
import fr.redmoon.tictac.gui.dialogs.listeners.AddCheckingListener;
import fr.redmoon.tictac.gui.dialogs.listeners.EditNoteListener;
import fr.redmoon.tictac.gui.dialogs.listeners.ShowDayListener;
import fr.redmoon.tictac.gui.dialogs.listeners.EditCheckingListener;
import fr.redmoon.tictac.gui.dialogs.listeners.EditExtraListener;

public class DayDialogDelegate extends AbsDialogDelegate {
	/**
	 * Listeners pour les bo�tes de dialogue
	 */
	private AddCheckingListener mAddCheckingListener;
	private EditCheckingListener mUpdateCheckingListener;
	private EditExtraListener mUpdateExtraListener;
	private EditNoteListener mEditNoteListener;
	private ShowDayListener mShowDayListener;
	
	public DayDialogDelegate(final TicTacActivity activity) {
		super(activity);
		
		// Pr�paration des listeners pour les bo�tes de dialogue
        mAddCheckingListener = new AddCheckingListener(mActivity);
        mUpdateCheckingListener = new EditCheckingListener(mActivity);
        mUpdateExtraListener = new EditExtraListener(mActivity);
        mEditNoteListener = new EditNoteListener(mActivity);
        mShowDayListener = new ShowDayListener(mActivity);
	}
	
	@Override
	public Dialog createDialog(final int id) {
		switch (id) {
    	case DialogTypes.TIMEPICKER_ADD_CHECKING:
    		return TimePickerDialogHelper.createDialog(mActivity, mAddCheckingListener);
		case DialogTypes.TIMEPICKER_EDIT_CHECKING:
			return TimePickerDialogHelper.createDialog(mActivity, mUpdateCheckingListener);
		case DialogTypes.TIMEPICKER_EDIT_EXTRA:
			return TimePickerDialogHelper.createDialog(mActivity, mUpdateExtraListener);
		case DialogTypes.TEXTINPUT_EDIT_NOTE:
			return createEditNoteDialog();
		case DialogTypes.DATEPICKER_SHOW_DAY:
			final long today = mActivity.getToday();
			return DatePickerDialogHelper.createDialog(mActivity, mShowDayListener, DateUtils.extractYear(today), DateUtils.extractMonth(today), DateUtils.extractDayOfMonth(today));
		}
		return null;
	}
	
	@Override
	public void prepareDialog(final int id, final Dialog dialog, final Bundle args) {
		switch (id) {
			case DialogTypes.TIMEPICKER_ADD_CHECKING:
				mAddCheckingListener.prepare(args.getLong(DialogArgs.DATE));
				TimePickerDialogHelper.prepare((TimePickerDialog) dialog, args.getInt(DialogArgs.TIME));
				break;
			case DialogTypes.TIMEPICKER_EDIT_CHECKING:
				mUpdateCheckingListener.prepare(args.getLong(DialogArgs.DATE), args.getInt(DialogArgs.TIME));
				TimePickerDialogHelper.prepare((TimePickerDialog) dialog, args.getInt(DialogArgs.TIME));
				break;
			case DialogTypes.TIMEPICKER_EDIT_EXTRA:
				mUpdateExtraListener.prepare(args.getLong(DialogArgs.DATE));
				TimePickerDialogHelper.prepare((TimePickerDialog) dialog, args.getInt(DialogArgs.TIME));
				break;
			case DialogTypes.TEXTINPUT_EDIT_NOTE:
				mEditNoteListener.prepare(args.getLong(DialogArgs.DATE));
				final String initialNote = args.getString(DialogArgs.NOTE);
				final EditText input = (EditText)dialog.findViewById(R.id.input);
				input.setText(initialNote);
				if (initialNote != null) {
					input.setSelection(initialNote.length());
				}
				break;
			case DialogTypes.DATEPICKER_SHOW_DAY:
				// C'est un peu inutile car on passe toujours le jour courant.
				final long date = args.getLong(DialogArgs.DATE);
				DatePickerDialogHelper.prepare((DatePickerDialog) dialog, DateUtils.extractYear(date), DateUtils.extractMonth(date), DateUtils.extractDayOfMonth(date));
				break;
		}
	}
	
	protected Dialog createEditNoteDialog() {
		//On instancie notre layout en tant que View
		LayoutInflater factory = LayoutInflater.from(mActivity);
        final View dialogView = factory.inflate(R.layout.text_input, null);
        
        //Cr�ation de l'AlertDialog
        AlertDialog.Builder adb = new AlertDialog.Builder(mActivity);
 
        //On affecte la vue personnalis� que l'on a cr�e � notre AlertDialog
        adb.setView(dialogView);
        
        //On donne un titre � l'AlertDialog
		adb.setTitle(R.string.dlg_title_edit_day_note);
		
		//On modifie l'ic�ne de l'AlertDialog pour le fun ;)
        //adb.setIcon(android.R.drawable.ic_dialog_alert);
		
		final EditText input = (EditText)dialogView.findViewById(R.id.input);
		adb.setPositiveButton(R.string.btn_save, new DialogInterface.OnClickListener() {
			@Override
    		public void onClick(DialogInterface dialog, int id) {
				mEditNoteListener.onNoteSaved(input.getText().toString());
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
