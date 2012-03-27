package fr.redmoon.tictac;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import fr.redmoon.tictac.bus.DateUtils;
import fr.redmoon.tictac.bus.PreferencesUtils;
import fr.redmoon.tictac.bus.bean.DayBean;
import fr.redmoon.tictac.bus.bean.PreferencesBean;
import fr.redmoon.tictac.bus.export.BinPreferencesBeanExporter;
import fr.redmoon.tictac.bus.export.BinPreferencesBeanImporter;
import fr.redmoon.tictac.bus.export.CsvDayBeanExporter;
import fr.redmoon.tictac.bus.export.CsvDayBeanImporter;
import fr.redmoon.tictac.bus.export.FileExporter;
import fr.redmoon.tictac.bus.export.FileImporter;
import fr.redmoon.tictac.db.DbAdapter;
import fr.redmoon.tictac.gui.datepicker.ChoosePeriodControler;
import fr.redmoon.tictac.gui.datepicker.ChoosePeriodControler.OnPeriodChoosedListener;
import fr.redmoon.tictac.gui.listadapter.ManageAdapter;

public class ManageActivity extends ListActivity {
	/** Nested class that performs progress calculations (counting) */
    class DbInserterThread extends Thread {
    	private final List<DayBean> mDays;
        private Handler mHandler;
        private final static int STATE_DONE = 0;
        private final static int STATE_RUNNING = 1;
        private int mState;
        private Iterator<DayBean> mDayIterator;
        private int nbDaysUpdated = 0;
        private int nbDaysCreated = 0;
       
        DbInserterThread(final Handler handler, final List<DayBean> days) {
            mHandler = handler;
            mDays = days;
            
            mState = STATE_DONE;
            
            mDayIterator = days.iterator();
            nbDaysUpdated = 0;
            nbDaysCreated = 0;
        }
       
        public void run() {
            mState = STATE_RUNNING;   
            while (mState == STATE_RUNNING) {
            	int newState = DbInserterThread.STATE_RUNNING;
            	if (mDayIterator.hasNext()) {
            		// Il reste des jours à écrire : on écrit le suivant
	            	final DayBean day = mDayIterator.next();
					if (mDb.isDayExisting(day.date)) {
						mDb.updateDay(day);
						if (day.isValid) {
							nbDaysUpdated++;
						}
					} else {
						mDb.createDay(day);
						if (day.isValid) {
							nbDaysCreated++;
						}
	    			}
            	} else {
            		// On a lu tous les jours : on arrête le thread
            		newState = DbInserterThread.STATE_DONE;
            	}
            	
            	// Mise à jour de la barre de progression
                final Message msg = mHandler.obtainMessage();
                msg.what = newState;
                msg.arg1 = nbDaysCreated;
                msg.arg2 = nbDaysUpdated;
                mHandler.sendMessage(msg);
            }
        }
        
        /* sets the current state for the thread,
         * used to stop the thread */
        public void setState(int state) {
            mState = state;
        }

		public int getMax() {
			return mDays.size();
		}
    }
	
	private final static int POS_EXPORT_DATA = 0;
	private final static int POS_IMPORT_DATA = 1;
	private final static int POS_EXPORT_PREFS = 2;
	private final static int POS_IMPORT_PREFS = 3;
	
	private static final int PROGRESS_DIALOG = 0;
	private DbInserterThread progressThread;
	private ProgressDialog progressDialog;
	
	private DbAdapter mDb;
	
	// Define the Handler that receives messages from the thread and update the progress
    final Handler handler = new Handler() {
        public void handleMessage(Message msg) {
        	final int nbDaysCreated = msg.arg1;
        	final int nbDaysUpdated = msg.arg2;
        	if (msg.what == DbInserterThread.STATE_RUNNING) {
        		final int total = nbDaysCreated + nbDaysUpdated;
	            progressDialog.setProgress(total);
        	} else if (msg.what == DbInserterThread.STATE_DONE) {
                dismissDialog(PROGRESS_DIALOG);
                progressThread.setState(DbInserterThread.STATE_DONE);
                Toast.makeText(
        				ManageActivity.this,
        				getString(
        					R.string.import_days_results,
        					nbDaysCreated, 
        					nbDaysUpdated),
        				Toast.LENGTH_LONG)
        			.show();
            }
        }
    };
    
	/** Called when the activity is first created. */
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		
		// Ouverture d'una accès à la base
		mDb = new DbAdapter(this);
        mDb.openDatabase();

		// Préparation du tableau des opérations
		final Resources resources = getResources();
		final String[][] operations = new String[][] {
				resources.getStringArray(R.array.export_data),
				resources.getStringArray(R.array.import_data),
				resources.getStringArray(R.array.export_prefs),
				resources.getStringArray(R.array.import_prefs)
		};
		
		this.setListAdapter(new ManageAdapter(this, R.layout.manage_item, operations));
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
            progressDialog = new ProgressDialog(this);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setMessage("Import des jours en cours...");
            return progressDialog;
        default:
            return null;
        }
    }
	
	@Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        switch(id) {
        case PROGRESS_DIALOG:
            progressDialog.setProgress(0);
            progressDialog.setMax(progressThread.getMax());
            progressThread.start();
        }
    }
        
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.manage_main, menu);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_preferences:
			final Intent prefsActivity = new Intent(this, PreferencesActivity.class);
			// On pourrait appeler directement telle ou telle page de préférence,
			// mais comme on veut afficher la page principale inutile de mettre une URI.
			//intent.setData(Uri.parse("preferences://main"));
			startActivity(prefsActivity);
			return true;
		}

		return super.onMenuItemSelected(featureId, item);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		
		switch (position) {
			case POS_EXPORT_DATA:
				exportData();
				break;
			case POS_IMPORT_DATA:
				importDataPhase1();
				break;
			case POS_EXPORT_PREFS:
				//DBGexportPrefs();
				Context mContext = getApplicationContext();
				Dialog dialog = new Dialog(mContext);

				dialog.setContentView(R.layout.period_checkin);
				dialog.setTitle("Custom Dialog");

				TextView text = (TextView) dialog.findViewById(R.id.text);
				text.setText("Hello, this is a custom dialog!");
				break;
			case POS_IMPORT_PREFS:
				importPrefsPhase1();
				break;
		}
	}

	/**
	 * Prompte l'utilisateur pour déterminer la période à exporter puis lance
	 * l'export.
	 */
	private void exportData() {
		// Création du contrôleur qui va ordonner l'appel aux différentes boîtes de dialogue.
		final ChoosePeriodControler controler = new ChoosePeriodControler(this, new OnPeriodChoosedListener() {
			
			@Override
			public void onPeriodSet(final long start, final long end) {
				// Récupération des jours à extraire
				final List<DayBean> days = new ArrayList<DayBean>();
				mDb.fetchDays(start, end, days);
				
				// Export des données vers le fichier texte
				String message = "";
				final FileExporter<List<DayBean>> exporter = new CsvDayBeanExporter(ManageActivity.this);
				if (days.isEmpty()) {
					message = getString(R.string.export_days_no_data);
				} else if (exporter.exportData(days)) {
					message = getString(
						R.string.export_days_success,
						DateUtils.formatDateDDMMYYYY(start),
						DateUtils.formatDateDDMMYYYY(end)
					);
				} else {
					message = getString(R.string.export_days_fail);
				}
				Toast.makeText(ManageActivity.this, message, Toast.LENGTH_LONG).show();
			}
		});
		
		// Démarrage du flux
		controler.prompt();
	}
	
	/**
	 * Exporte les préférences
	 */
	private void exportPrefs() {
		final FileExporter<PreferencesBean> exporter = new BinPreferencesBeanExporter(this);
		if (exporter.exportData(PreferencesBean.instance)) {
			Toast.makeText(this, R.string.export_prefs_success, Toast.LENGTH_LONG).show();
		} else {
			Toast.makeText(this, R.string.export_prefs_fail, Toast.LENGTH_LONG).show();
		}
	}
	
	/**
	 * Prompte l'utilisateur pour choisir le fichier à importer puis lance l'import.
	 */
	private void requestFileChooserActivity(final String type, final int requestId){
		// Demande à l'utilisateur de choisir le fichier à importer
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT); 
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        if (type != null) {
        	intent.setType(type);
        }
        
        // Démarre une activité dont on attend un résultat. La suite des opérations
        // sera donc lancée dans onActivityResult
        startActivityForResult(Intent.createChooser(intent, getString(R.string.import_filechooser_title)), requestId); 
	}
	
	private void importDataPhase1() {
		requestFileChooserActivity(CsvDayBeanImporter.MIME_TYPE, POS_IMPORT_DATA);
	}
	
	private void importPrefsPhase1() {
		requestFileChooserActivity(BinPreferencesBeanImporter.MIME_TYPE, POS_IMPORT_PREFS);
	}
	
	/**
	 * Dernière phase de l'import : réalise effectivement l'import en lisant le fichier
	 * CSV et en écrivant en base les données lues.
	 * @param filename
	 */
	private void importDataPhase2(final String filename){
		// Lit les données dans le fichier CSV
		final List<DayBean> days = new ArrayList<DayBean>();
		final FileImporter<List<DayBean>> importer = new CsvDayBeanImporter(this);
		if (importer.importData(filename, days)) {
			// Ecrit ces données dans la base en utilisant un Thread et une belle
			// boîte de dialogue avec une barre de progression pour montrer où on
			// en est.
			progressThread = new DbInserterThread(handler, days);
			showDialog(PROGRESS_DIALOG);
		}
	}
	
	/**
	 * Dernière phase de l'import : réalise effectivement l'import en lisant le fichier
	 * binaire et en écrivant les données lues dans PreferencesBean.instance.
	 * @param filename
	 */
	private void importPrefsPhase2(final String filename){
		final FileImporter<PreferencesBean> importer = new BinPreferencesBeanImporter(this);
		if (importer.importData(filename, PreferencesBean.instance)) {
			// Les données ont bien été lues. On va à présent les écrire dans le vrai
			// fichier de préférences pour une relecture au prochain démarrage de l'application
			PreferencesUtils.savePreferencesBean(this);
			
			Toast.makeText(this, R.string.import_prefs_success, Toast.LENGTH_LONG).show();
		} else {
			Toast.makeText(this, R.string.import_prefs_fail, Toast.LENGTH_LONG).show();
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// Import des données
		if (data != null) {
			final Uri uri = data.getData();
			final String path = uri.getPath();
			switch (requestCode) {
				case POS_IMPORT_DATA:
					importDataPhase2(path);
					break;
				case POS_IMPORT_PREFS:
					importPrefsPhase2(path);
					break;
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
}

