package fr.redmoon.tictac.gui.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import fr.redmoon.tictac.TicTacActivity;

public abstract class AbsDialogDelegate {
	protected final TicTacActivity mActivity;
	
	public AbsDialogDelegate(final TicTacActivity _activity) {
		mActivity = _activity;
	}
	
	public abstract Dialog createDialog(final int id);
	
	public abstract void prepareDialog(final int id, final Dialog dialog, final Bundle args);
}
