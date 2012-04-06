package fr.redmoon.tictac.gui.dialogs.listeners;

import android.widget.EditText;
import fr.redmoon.tictac.TicTacActivity;

public class EditNoteListener extends TicTacListener {
	
    	public EditNoteListener(final TicTacActivity activity) {
    		super(activity);
		}
    	
    	public void prepare(final long date, final EditText input) {
    		super.prepare(date);
    	}

    	/**
    	 * Méthode appelée lorsque le bouton "Sauvegarder" a été cliqué.
    	 * @param note Note à sauvegarder
    	 */
		public void onNoteSaved(final String note) {
			// Mise à jour de la base de données
			boolean dbUpdated = mDb.updateDayNote(mDate, note);
			
			// Mise à jour de l'affichage
			if (dbUpdated) {
				mActivity.populateView(mDate);
			}
		}
	}