package fr.redmoon.tictac;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.Spinner;
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
import fr.redmoon.tictac.gui.listeners.PeriodCheckinListener;

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
            		// Il reste des jours � �crire : on �crit le suivant
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
            		// On a lu tous les jours : on arr�te le thread
            		newState = DbInserterThread.STATE_DONE;
            	}
            	
            	// Mise � jour de la barre de progression
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
	private final static int POS_CHECKIN_PERIOD = 4;
	
	private static final int PROGRESS_DIALOG = 0;
	private static final int PERIOD_CHECKIN_DIALOG = 1;
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
		
		// Ouverture d'una acc�s � la base
		mDb = new DbAdapter(this);
        mDb.openDatabase();

		// Pr�paration du tableau des op�rations
		final Resources resources = getResources();
		final String[][] operations = new String[][] {
				resources.getStringArray(R.array.export_data),
				resources.getStringArray(R.array.import_data),
				resources.getStringArray(R.array.export_prefs),
				resources.getStringArray(R.array.import_prefs),
				resources.getStringArray(R.array.period_checkin)
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
            return createProgressDialog();
        case PERIOD_CHECKIN_DIALOG:
        	return createPeriodCheckinDialog();
        default:
            return null;
        }
    }
	
	private Dialog createProgressDialog() {
		progressDialog = new ProgressDialog(this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setMessage("Import des jours en cours...");
        return progressDialog;
	}

	private Dialog createPeriodCheckinDialog() {
		//On instancie notre layout en tant que View
        LayoutInflater factory = LayoutInflater.from(this);
        final View dialogView = factory.inflate(R.layout.period_checkin, null);
 
        //Cr�ation de l'AlertDialog
        AlertDialog.Builder adb = new AlertDialog.Builder(this);
 
        //On affecte la vue personnalis� que l'on a cr�e � notre AlertDialog
        adb.setView(dialogView);
        
      //On donne un titre � l'AlertDialog
        adb.setTitle(R.string.period_checkin_title);
 
        //On modifie l'ic�ne de l'AlertDialog pour le fun ;)
        //adb.setIcon(android.R.drawable.ic_dialog_alert);
        
        final Spinner spinner = (Spinner)dialogView.findViewById(R.id.day_type);
	    final ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.dayTypesEntries, android.R.layout.simple_spinner_item);
	    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	    spinner.setAdapter(adapter);
 
        //On affecte un bouton "OK" � notre AlertDialog et on lui affecte un �v�nement
        adb.setPositiveButton(R.string.btn_checkin, new PeriodCheckinListener(
        		this,
        		mDb,
        		(DatePicker)dialogView.findViewById(R.id.date1),
        		(DatePicker)dialogView.findViewById(R.id.date2),
        		(Spinner)dialogView.findViewById(R.id.day_type))
        );
 
        //On cr�e un bouton "Annuler" � notre AlertDialog et on lui affecte un �v�nement
        adb.setNegativeButton(R.string.btn_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
          } });
		return adb.create();
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
			// On pourrait appeler directement telle ou telle page de pr�f�rence,
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
				exportPrefs();
				break;
			case POS_IMPORT_PREFS:
				importPrefsPhase1();
				break;
			case POS_CHECKIN_PERIOD:
				showDialog(PERIOD_CHECKIN_DIALOG);
				break;
		}
	}

	/**
	 * Prompte l'utilisateur pour d�terminer la p�riode � exporter puis lance
	 * l'export.
	 */
	private void exportData() {
		// Cr�ation du contr�leur qui va ordonner l'appel aux diff�rentes bo�tes de dialogue.
		final ChoosePeriodControler controler = new ChoosePeriodControler(this, new OnPeriodChoosedListener() {
			
			@Override
			public void onPeriodSet(final long start, final long end) {
				// R�cup�ration des jours � extraire
				final List<DayBean> days = new ArrayList<DayBean>();
				mDb.fetchDays(start, end, days);
				
				// Export des donn�es vers le fichier texte
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
		
		// D�marrage du flux
		controler.prompt();
	}
	
	/**
	 * Exporte les pr�f�rences
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
	 * Prompte l'utilisateur pour choisir le fichier � importer puis lance l'import.
	 */
	private void requestFileChooserActivity(final String type, final int requestId){
		// Demande � l'utilisateur de choisir le fichier � importer
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT); 
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        if (type != null) {
        	intent.setType(type);
        }
        
        // D�marre une activit� dont on attend un r�sultat. La suite des op�rations
        // sera donc lanc�e dans onActivityResult
        startActivityForResult(Intent.createChooser(intent, getString(R.string.import_filechooser_title)), requestId); 
	}
	
	private void importDataPhase1() {
		requestFileChooserActivity(CsvDayBeanImporter.MIME_TYPE, POS_IMPORT_DATA);
	}
	
	private void importPrefsPhase1() {
		requestFileChooserActivity(BinPreferencesBeanImporter.MIME_TYPE, POS_IMPORT_PREFS);
	}
	
	/**
	 * Derni�re phase de l'import : r�alise effectivement l'import en lisant le fichier
	 * CSV et en �crivant en base les donn�es lues.
	 * @param filename
	 */
	private void importDataPhase2(final String filename){
		// Lit les donn�es dans le fichier CSV
		final List<DayBean> days = new ArrayList<DayBean>();
		final FileImporter<List<DayBean>> importer = new CsvDayBeanImporter(this);
		if (importer.importData(filename, days)) {
			// Ecrit ces donn�es dans la base en utilisant un Thread et une belle
			// bo�te de dialogue avec une barre de progression pour montrer o� on
			// en est.
			progressThread = new DbInserterThread(handler, days);
			showDialog(PROGRESS_DIALOG);
		}
	}
	
	/**
	 * Derni�re phase de l'import : r�alise effectivement l'import en lisant le fichier
	 * binaire et en �crivant les donn�es lues dans PreferencesBean.instance.
	 * @param filename
	 */
	private void importPrefsPhase2(final String filename){
		final FileImporter<PreferencesBean> importer = new BinPreferencesBeanImporter(this);
		if (importer.importData(filename, PreferencesBean.instance)) {
			// Les donn�es ont bien �t� lues. On va � pr�sent les �crire dans le vrai
			// fichier de pr�f�rences pour une relecture au prochain d�marrage de l'application
			PreferencesUtils.savePreferencesBean(this);
			
			Toast.makeText(this, R.string.import_prefs_success, Toast.LENGTH_LONG).show();
		} else {
			Toast.makeText(this, R.string.import_prefs_fail, Toast.LENGTH_LONG).show();
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// Import des donn�es
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

