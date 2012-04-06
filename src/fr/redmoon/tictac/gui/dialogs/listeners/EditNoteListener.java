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
    	 * M�thode appel�e lorsque le bouton "Sauvegarder" a �t� cliqu�.
    	 * @param note Note � sauvegarder
    	 */
		public void onNoteSaved(final String note) {
			// Mise � jour de la base de donn�es
			boolean dbUpdated = mDb.updateDayNote(mDate, note);
			
			// Mise � jour de l'affichage
			if (dbUpdated) {
				mActivity.populateView(mDate);
			}
		}
	}