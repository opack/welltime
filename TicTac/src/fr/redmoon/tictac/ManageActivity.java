package fr.redmoon.tictac;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;
import fr.redmoon.tictac.bus.PreferencesUtils;
import fr.redmoon.tictac.bus.bean.DayBean;
import fr.redmoon.tictac.bus.bean.PreferencesBean;
import fr.redmoon.tictac.bus.bean.WeekBean;
import fr.redmoon.tictac.bus.export.BinPreferencesBeanExporter;
import fr.redmoon.tictac.bus.export.BinPreferencesBeanImporter;
import fr.redmoon.tictac.bus.export.CsvDayBeanImporter;
import fr.redmoon.tictac.bus.export.CsvWeekBeanImporter;
import fr.redmoon.tictac.bus.export.FileExporter;
import fr.redmoon.tictac.bus.export.FileImporter;
import fr.redmoon.tictac.db.DbAdapter;
import fr.redmoon.tictac.gui.DbInserterThread;
import fr.redmoon.tictac.gui.ProgressDialogHandler;
import fr.redmoon.tictac.gui.dialogs.listeners.PeriodCheckinListener;
import fr.redmoon.tictac.gui.dialogs.listeners.PeriodExporterListener;
import fr.redmoon.tictac.gui.listadapter.ManageAdapter;

public class ManageActivity extends ListActivity {
	private final static int POS_EXPORT_DATA = 0;
	private final static int POS_IMPORT_DATA = 1;
	private final static int POS_EXPORT_PREFS = 2;
	private final static int POS_IMPORT_PREFS = 3;
	private final static int POS_CHECKIN_PERIOD = 4;
	
	private static final int PROGRESS_DIALOG = 0;
	private static final int PERIOD_CHECKIN_DIALOG = 1;
	private static final int PERIOD_EXPORT_DIALOG = 2;
	
	private ProgressDialogHandler progressHandler;
	private DbInserterThread progressThread;
	private ProgressDialog progressDialog;
	
	private DbAdapter mDb;
	
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
		
		this.setListAdapter(new ManageAdapter(this, R.layout.lst_itm_manage_operation, operations));
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
        case PERIOD_EXPORT_DIALOG:
        	return createPeriodExportDialog();
        default:
            return null;
        }
    }
	
	private Dialog createProgressDialog() {
		progressDialog = new ProgressDialog(this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setProgressNumberFormat("%1d sur %2d");
        progressDialog.setMessage("Import des jours en cours...");
        progressDialog.setOnDismissListener(new OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface dialog) {
				if (progressThread != null && progressThread.isRunning()) {
					progressThread.setState(DbInserterThread.STATE_CANCEL);
				}
			}
		});
        
        // On profite de cette �tape pour cr�er le handler
        progressHandler = new ProgressDialogHandler(this, progressDialog, PROGRESS_DIALOG);
        progressThread.setHandler(progressHandler);
        return progressDialog;
	}

	private Dialog createPeriodCheckinDialog() {
		//On instancie notre layout en tant que View
        LayoutInflater factory = LayoutInflater.from(this);
        final View dialogView = factory.inflate(R.layout.dlg_period_checkin, null);
 
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
        		(Spinner)dialogView.findViewById(R.id.day_type),
        		(EditText)dialogView.findViewById(R.id.note))
        );
 
        //On cr�e un bouton "Annuler" � notre AlertDialog et on lui affecte un �v�nement
        adb.setNegativeButton(R.string.btn_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
          } });
		return adb.create();
	}
	
	private Dialog createPeriodExportDialog() {
		//On instancie notre layout en tant que View
        LayoutInflater factory = LayoutInflater.from(this);
        final View dialogView = factory.inflate(R.layout.dlg_period_chooser, null);
 
        //Cr�ation de l'AlertDialog
        AlertDialog.Builder adb = new AlertDialog.Builder(this);
 
        //On affecte la vue personnalis� que l'on a cr�e � notre AlertDialog
        adb.setView(dialogView);
        
        //On donne un titre � l'AlertDialog
        adb.setTitle(R.string.export_data_title);
 
        //On modifie l'ic�ne de l'AlertDialog pour le fun ;)
        //adb.setIcon(android.R.drawable.ic_dialog_alert);
        
        //On affecte un bouton "OK" � notre AlertDialog et on lui affecte un �v�nement
        adb.setPositiveButton(R.string.btn_ok, new PeriodExporterListener(
        	this, 
        	mDb, 
        	(DatePicker)dialogView.findViewById(R.id.date1), 
        	(DatePicker)dialogView.findViewById(R.id.date2))
        );
 
        //On cr�e un bouton "Annuler" � notre AlertDialog et on lui affecte un �v�nement
        adb.setNegativeButton(R.string.btn_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {}
        });
        
		return adb.create();
	}

	@Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        switch(id) {
        case PROGRESS_DIALOG:
            progressDialog.setProgress(0);
            progressDialog.setSecondaryProgress(0);
            progressDialog.setMax(100);
            progressThread.start();
            break;
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
				showDialog(PERIOD_EXPORT_DIALOG);
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
		requestFileChooserActivity(PeriodExporterListener.MIME_TYPE, POS_IMPORT_DATA);
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
		// Ouverture du fichier properties pour r�cup�rer le nom des fichiers � importer
		final Properties importData = new Properties();
		Reader read = null;
		try {
			read = new FileReader(filename);
			importData.load(read);
		} catch (FileNotFoundException e) {
			Toast.makeText(
					this, 
					getString(R.string.import_file_not_found, filename),
					Toast.LENGTH_LONG)
				.show();
		} catch (IOException e) {
			Toast.makeText(
					this, 
					getString(R.string.import_io_exception, filename),
					Toast.LENGTH_LONG)
				.show();
		} finally {
			try {
				read.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		// R�cup�ration du r�pertoire courant
		final File infosFile = new File(filename);
		final String dir = infosFile.getParent() + "/";
		final String daysCsv = dir + importData.getProperty(PeriodExporterListener.PROPERTY_DAYS_CSV);
		final String weeksCsv = dir + importData.getProperty(PeriodExporterListener.PROPERTY_WEEKS_CSV);	
		
		// Lit les jours dans le fichier CSV
		final List<DayBean> days = new ArrayList<DayBean>();
		final FileImporter<List<DayBean>> daysImporter = new CsvDayBeanImporter(this);
		if (!daysImporter.importData(daysCsv, days)) {
			Toast.makeText(
					this, 
					getString(R.string.import_io_exception, daysCsv),
					Toast.LENGTH_LONG)
				.show();
		}
		
		// Lit les semaines dans le fichier CSV
		final List<WeekBean> weeks = new ArrayList<WeekBean>();
		final FileImporter<List<WeekBean>> weeksImporter = new CsvWeekBeanImporter(this);
		if (!weeksImporter.importData(weeksCsv, weeks)) {
			Toast.makeText(
					this, 
					getString(R.string.import_io_exception, weeksCsv),
					Toast.LENGTH_LONG)
				.show();
		}
		
		// Ecrit ces donn�es dans la base en utilisant un Thread et une belle
		// bo�te de dialogue avec une barre de progression pour montrer o� on
		// en est.
		progressThread = new DbInserterThread(days, weeks, mDb);
		// Si le handler existe d�j� (c'est le cas si la bo�te de dialogue a d�j� �t� cr��e
		// via createDialog) alors on l'assigne ici. Sinon, ce sera fait lors de la cr�ation
		// de la bo�te de dialogue.
		progressThread.setHandler(progressHandler);
		showDialog(PROGRESS_DIALOG);
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

