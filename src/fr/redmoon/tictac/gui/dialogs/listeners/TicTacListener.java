package fr.redmoon.tictac.gui.dialogs.listeners;

import fr.redmoon.tictac.TicTacActivity;
import fr.redmoon.tictac.db.DbAdapter;

public abstract class TicTacListener {
	protected final TicTacActivity mActivity;
	protected long mDate;
	protected final DbAdapter mDb;
	
	public TicTacListener(final TicTacActivity activity) {
		mActivity = activity;
		mDb = activity.getDbAdapter();
	}
	
	public void prepare(final long date) {
		mDate = date;
	}
}