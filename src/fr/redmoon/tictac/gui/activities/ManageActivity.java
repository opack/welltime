package fr.redmoon.tictac.gui.activities;

import android.app.Dialog;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import fr.redmoon.tictac.R;
import fr.redmoon.tictac.db.DbAdapter;
import fr.redmoon.tictac.gui.ManageImportExportHandler;
import fr.redmoon.tictac.gui.dialogs.fragments.CleanDaysFragment;
import fr.redmoon.tictac.gui.dialogs.fragments.PeriodCheckinFragment;
import fr.redmoon.tictac.gui.dialogs.fragments.StatisticsFragment;
import fr.redmoon.tictac.gui.dialogs.fragments.SyncCalendarPeriodFragment;
import fr.redmoon.tictac.gui.listadapter.ManageAdapter;

public class ManageActivity extends TicTacActivity {
	
	public static final int PROGRESS_DIALOG = 0;
	public static final int PERIOD_EXPORT_DIALOG = 2;
	
	// Position des éléments dans la liste d'outils
	private final static int POS_CHECKIN_PERIOD = 0;
	private final static int POS_SYNC_CALENDAR_PERIOD = 1;
	private final static int POS_CLEAN_DAYS = 2;
	private final static int POS_STATISTICS = 3;
	
	// Position des éléments dans la liste préférences
	private final static int POS_SHOW_PREFS = 0;
	
	private DbAdapter mDb;
	private ManageImportExportHandler importExportHandler;
	
	/** Called when the activity is first created. */
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
    	
        // Initialisation de l'affichage
        setContentView(R.layout.view_common_frame);
        findViewById(R.id.lyt_navigation).setVisibility(View.GONE);
        findViewById(R.id.lyt_total_worked).setVisibility(View.GONE);
        findViewById(R.id.lyt_clockin).setVisibility(View.GONE);
		
		// Ouverture d'una accès à la base
		mDb = new DbAdapter(this);
        mDb.openDatabase();
        
        // Initialisation du gestionnaire de pages
        final View pageOperations = View.inflate(this, R.layout.view_manage_operations, null);
        final View pageImportExport = View.inflate(this, R.layout.view_manage_import_export, null);
        final View pagePreferences = View.inflate(this, R.layout.view_manage_preferences, null);
        initPages(getResources().getStringArray(R.array.manage_page_titles), pageOperations, pageImportExport, pagePreferences);

        // Préparation de la liste des opérations de gestion
        final Resources resources = getResources();
 		prepareList(pageOperations, new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> adapter, View v, int position, long id) {
		 			switch (position) {
		 			case POS_CHECKIN_PERIOD:
		 				showFragment(new PeriodCheckinFragment(), PeriodCheckinFragment.TAG);
		 				break;
		 			case POS_SYNC_CALENDAR_PERIOD:
		 				showFragment(new SyncCalendarPeriodFragment(), SyncCalendarPeriodFragment.TAG);
		 				break;
		 			case POS_CLEAN_DAYS:
		 				showFragment(new CleanDaysFragment(), CleanDaysFragment.TAG);
		 				break;
		 			case POS_STATISTICS:
		 				showFragment(new StatisticsFragment(), StatisticsFragment.TAG);
		 				break;
		 			}
				}
			},
			resources.getStringArray(R.array.period_checkin),
 			resources.getStringArray(R.array.period_sync_calendar),
			resources.getStringArray(R.array.period_cleaner),
			resources.getStringArray(R.array.period_statistics)
 		);
 		
		// Préparation de la liste des opérations d'import / export
 		importExportHandler = new ManageImportExportHandler(this, mDb);
 		prepareList(pageImportExport,
 			importExportHandler,
 			resources.getStringArray(R.array.export_data),
			resources.getStringArray(R.array.import_data),
			resources.getStringArray(R.array.export_prefs),
			resources.getStringArray(R.array.import_prefs)
		);
		
		// Préparation de la liste des préférences
 		prepareList(pagePreferences,
 				new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> adapter, View v, int position, long id) {
		 			switch (position) {
			 			case POS_SHOW_PREFS:
			 				final Intent prefsActivity = new Intent(ManageActivity.this, PreferencesActivity.class);
			 				// On pourrait appeler directement telle ou telle page de préférence,
			 				// mais comme on veut afficher la page principale inutile de mettre une URI.
			 				//intent.setData(Uri.parse("preferences://main"));
			 				ManageActivity.this.startActivity(prefsActivity);
			 				break;
		 			}
				}
			},
			resources.getStringArray(R.array.show_preferences)
		);
	}
	
	private void prepareList(final View page, final OnItemClickListener listener, final String[]... labels) {
		final ListView list = (ListView)page.findViewById(R.id.list);
 		list.setAdapter(new ManageAdapter(this, R.layout.itm_manage_operation, labels));
 		list.setOnItemClickListener(listener);
	}

	@Override
	protected void onDestroy() {
		mDb.closeDatabase();
		super.onDestroy();
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
        switch(id) {
        case PROGRESS_DIALOG:
            return importExportHandler.createProgressDialog();
        default:
            return null;
        }
    }

	@Override
    protected void onPrepareDialog(int id, Dialog dialog, Bundle args) {
        switch(id) {
        case PROGRESS_DIALOG:
        	importExportHandler.prepareProgressDialog();
            break;
        }
    }
        
	@Override
	public void populateView(long day) {
		// Rien à faire pour afficher des données d'un jour particulier
	}
	
	private void showFragment(final DialogFragment fragment, final String tag) {
		fragment.show(getSupportFragmentManager(), tag);
	}
}

