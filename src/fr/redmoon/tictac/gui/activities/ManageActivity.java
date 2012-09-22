package fr.redmoon.tictac.gui.activities;

import android.app.Dialog;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import fr.redmoon.tictac.R;
import fr.redmoon.tictac.db.DbAdapter;
import fr.redmoon.tictac.gui.ManageImportExportHandler;
import fr.redmoon.tictac.gui.ManageOperationsHandler;
import fr.redmoon.tictac.gui.ManagePreferencesHandler;
import fr.redmoon.tictac.gui.listadapter.ManageAdapter;

public class ManageActivity extends TicTacActivity {
	
	public static final int PROGRESS_DIALOG = 0;
	public static final int PERIOD_CHECKIN_DIALOG = 1;
	public static final int PERIOD_EXPORT_DIALOG = 2;
	public static final int PERIOD_SYNC_CALENDAR_DIALOG = 3;
	
	private DbAdapter mDb;
	private ManageImportExportHandler importExportHandler;
	private ManageOperationsHandler operationsHandler;
	private ManagePreferencesHandler preferencesHandler;
	
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
        initPages(pageOperations, pageImportExport, pagePreferences);

        // Préparation de la liste des opérations de gestion
        final Resources resources = getResources();
 		final String[][] lblOperations = new String[][] {
 			resources.getStringArray(R.array.period_checkin),
 			resources.getStringArray(R.array.period_sync_calendar),
			resources.getStringArray(R.array.period_cleaner),
			resources.getStringArray(R.array.period_statistics)
 		};
 		final ListView lstOperations = (ListView)pageOperations.findViewById(R.id.list);
 		lstOperations.setAdapter(new ManageAdapter(this, R.layout.itm_manage_operation, lblOperations));
 		operationsHandler = new ManageOperationsHandler(this, mDb);
 		lstOperations.setOnItemClickListener(operationsHandler);
 		
		// Préparation de la liste des opérations d'import / export
		final String[][] lblImportExport = new String[][] {
			resources.getStringArray(R.array.export_data),
			resources.getStringArray(R.array.import_data),
			resources.getStringArray(R.array.export_prefs),
			resources.getStringArray(R.array.import_prefs)
		};
		final ListView lstImportExport = (ListView)pageImportExport.findViewById(R.id.list);
		lstImportExport.setAdapter(new ManageAdapter(this, R.layout.itm_manage_operation, lblImportExport));
		importExportHandler = new ManageImportExportHandler(this, mDb);
		lstImportExport.setOnItemClickListener(importExportHandler);
		
		// Préparation de la liste des préférences
		final String[][] lblPreferences = new String[][] {
			resources.getStringArray(R.array.show_preferences)
		};
		final ListView lstPreferences = (ListView)pagePreferences.findViewById(R.id.list);
		lstPreferences.setAdapter(new ManageAdapter(this, R.layout.itm_manage_operation, lblPreferences));
		preferencesHandler = new ManagePreferencesHandler(this);
		lstPreferences.setOnItemClickListener(preferencesHandler);
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
        case PERIOD_CHECKIN_DIALOG:
        	return operationsHandler.createPeriodCheckinDialog();
        case PERIOD_EXPORT_DIALOG:
        	return importExportHandler.createPeriodExportDialog();
        case PERIOD_SYNC_CALENDAR_DIALOG:
        	return operationsHandler.createPeriodSyncCalendarDialog();
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
}

