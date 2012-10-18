package fr.redmoon.tictac.gui.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import fr.redmoon.tictac.R;
import fr.redmoon.tictac.db.DbAdapter;
import fr.redmoon.tictac.gui.activities.TicTacActivity;

public class EditNoteFragment extends DialogFragment implements DialogInterface.OnClickListener {
	public final static String TAG = EditNoteFragment.class.getName();
	
	private long mDate;
	private String mOldNote;
	private EditText mInputField;
	
	@Override
	public void setArguments(Bundle args) {
		mDate = args.getLong(DialogArgs.DATE.name());
		mOldNote = args.getString(DialogArgs.NOTE.name());
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		//On instancie notre layout en tant que View
		LayoutInflater factory = LayoutInflater.from(getActivity());
        final View dialogView = factory.inflate(R.layout.dlg_text_input, null);
        
        //Création de l'AlertDialog
        AlertDialog.Builder adb = new AlertDialog.Builder(getActivity());
 
        //On affecte la vue personnalisé que l'on a crée à notre AlertDialog
        adb.setView(dialogView);
        
        //On donne un titre à l'AlertDialog
		adb.setTitle(R.string.dlg_title_edit_day_note);
		
		//On modifie l'icône de l'AlertDialog pour le fun ;)
        //adb.setIcon(android.R.drawable.ic_dialog_alert);
		
		mInputField = (EditText)dialogView.findViewById(R.id.input);
		mInputField.setText(mOldNote);
		if (mOldNote != null) {
			mInputField.setSelection(mOldNote.length());
		}
		
		adb.setPositiveButton(R.string.btn_save, this);
		adb.setNegativeButton(R.string.btn_cancel, new DialogInterface.OnClickListener() {
			@Override
    		public void onClick(DialogInterface dialog, int id) {
            	dialog.dismiss();
        	}
		});
		
		return adb.create();
	}
	
	@Override
	public void onClick(DialogInterface dialog, int id) {
		final String newNote = mInputField.getText().toString();
		if (newNote == null || newNote.equals(mOldNote)) {
			return;
		}
		
		// Mise à jour de la base de données
		final TicTacActivity activity = (TicTacActivity)getActivity();
		final DbAdapter db = DbAdapter.getInstance(activity);
		db.openDatabase();
		boolean dbUpdated = db.updateDayNote(mDate, newNote);
		db.closeDatabase();
		
		// Mise à jour de l'affichage
		if (dbUpdated) {
			activity.populateView(mDate);
		}
	}
}