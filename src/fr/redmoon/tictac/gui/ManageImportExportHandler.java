package fr.redmoon.tictac.gui;

import static fr.redmoon.tictac.gui.activities.ManageActivity.PERIOD_EXPORT_DIALOG;
import static fr.redmoon.tictac.gui.activities.ManageActivity.PROGRESS_DIALOG;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.DatePicker;
import android.widget.Toast;
import fr.redmoon.tictac.R;
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
import fr.redmoon.tictac.bus.export.ZipDecompress;
import fr.redmoon.tictac.db.DbAdapter;
import fr.redmoon.tictac.gui.dialogs.listeners.PeriodExporterListener;

public class ManageImportExportHandler implements OnItemClickListener {
	private final static int POS_EXPORT_DATA = 0;
	private final static int POS_IMPORT_DATA = 1;
	private final static int POS_EXPORT_PREFS = 2;
	private final static int POS_IMPORT_PREFS = 3;
	
	private ProgressDialogHandler progressHandler;
	private DbInserterThread progressThread;
	private ProgressDialog progressDialog;
	
	private final Activity activity;
	private final DbAdapter db;
	
	public ManageImportExportHandler(final Activity activity, final DbAdapter db) {
		this.activity = activity;
		this.db = db;
	}

	@Override
	public void onItemClick(AdapterView<?> adapter, View v, int position, long id) {
		switch (position) {
			case POS_EXPORT_DATA:
				activity.showDialog(PERIOD_EXPORT_DIALOG);
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
		}
	}
	
	public Dialog createProgressDialog() {
		progressDialog = new ProgressDialog(activity);
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
        progressHandler = new ProgressDialogHandler(activity, progressDialog, PROGRESS_DIALOG);
        progressThread.setHandler(progressHandler);
        return progressDialog;
	}
	
	public Dialog createPeriodExportDialog() {
		//On instancie notre layout en tant que View
        LayoutInflater factory = LayoutInflater.from(activity);
        final View dialogView = factory.inflate(R.layout.dlg_period_chooser, null);
 
        //Cr�ation de l'AlertDialog
        AlertDialog.Builder adb = new AlertDialog.Builder(activity);
 
        //On affecte la vue personnalis� que l'on a cr�e � notre AlertDialog
        adb.setView(dialogView);
        
        //On donne un titre � l'AlertDialog
        adb.setTitle(R.string.export_data_title);
 
        //On modifie l'ic�ne de l'AlertDialog pour le fun ;)
        //adb.setIcon(android.R.drawable.ic_dialog_alert);
        
        //On affecte un bouton "OK" � notre AlertDialog et on lui affecte un �v�nement
        adb.setPositiveButton(R.string.btn_ok, new PeriodExporterListener(
        	activity, 
        	db, 
        	(DatePicker)dialogView.findViewById(R.id.date1), 
        	(DatePicker)dialogView.findViewById(R.id.date2))
        );
 
        //On cr�e un bouton "Annuler" � notre AlertDialog et on lui affecte un �v�nement
        adb.setNegativeButton(R.string.btn_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {}
        });
        
		return adb.create();
	}
	
	/**
	 * Exporte les pr�f�rences
	 */
	private void exportPrefs() {
		final FileExporter<PreferencesBean> exporter = new BinPreferencesBeanExporter(activity);
		if (exporter.exportData(PreferencesBean.instance)) {
			Toast.makeText(activity, R.string.export_prefs_success, Toast.LENGTH_LONG).show();
		} else {
			Toast.makeText(activity, R.string.export_prefs_fail, Toast.LENGTH_LONG).show();
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
        activity.startActivityForResult(Intent.createChooser(intent, activity.getString(R.string.import_filechooser_title)), requestId); 
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
		// D�compression des donn�es dans un r�pertoire temporaire
		final long timestamp = System.currentTimeMillis();
		final File zipFile = new File(filename);
		final String dir = zipFile.getParent() + "/" + timestamp + "/"; // Ajout d'un timestamp pour �viter d'�crire sur des fichiers existants 
		ZipDecompress d = new ZipDecompress(filename, dir); 
		d.unzip(); 
				
		final File unzippedDaysCsvFile = new File(dir, PeriodExporterListener.FILE_DAYS_CSV);
		final File unzippedWeeksCsvFile = new File(dir, PeriodExporterListener.FILE_WEEKS_CSV);
		
		// Lit les jours dans le fichier CSV
		final List<DayBean> days = new ArrayList<DayBean>();
		final FileImporter<List<DayBean>> daysImporter = new CsvDayBeanImporter(activity);
		if (!daysImporter.importData(unzippedDaysCsvFile, days)) {
			Toast.makeText(
					activity, 
					activity.getString(R.string.import_io_exception, unzippedDaysCsvFile.getAbsolutePath()),
					Toast.LENGTH_LONG)
				.show();
		}
		
		// Lit les semaines dans le fichier CSV
		final List<WeekBean> weeks = new ArrayList<WeekBean>();
		final FileImporter<List<WeekBean>> weeksImporter = new CsvWeekBeanImporter(activity);
		if (!weeksImporter.importData(unzippedWeeksCsvFile, weeks)) {
			Toast.makeText(
					activity, 
					activity.getString(R.string.import_io_exception, unzippedWeeksCsvFile.getAbsolutePath()),
					Toast.LENGTH_LONG)
				.show();
		}
		
		// Suppression des fichiers et du repertoire temporaires
		unzippedDaysCsvFile.delete();
		unzippedWeeksCsvFile.delete();
		final File unzipDirectory = new File(dir);
		unzipDirectory.delete();
		
		// Ecrit ces donn�es dans la base en utilisant un Thread et une belle
		// bo�te de dialogue avec une barre de progression pour montrer o� on
		// en est.
		progressThread = new DbInserterThread(days, weeks, db);
		// Si le handler existe d�j� (c'est le cas si la bo�te de dialogue a d�j� �t� cr��e
		// via createDialog) alors on l'assigne ici. Sinon, ce sera fait lors de la cr�ation
		// de la bo�te de dialogue.
		progressThread.setHandler(progressHandler);
		activity.showDialog(PROGRESS_DIALOG);
	}
	
	/**
	 * Derni�re phase de l'import : r�alise effectivement l'import en lisant le fichier
	 * binaire et en �crivant les donn�es lues dans PreferencesBean.instance.
	 * @param filename
	 */
	private void importPrefsPhase2(final String filename){
		final FileImporter<PreferencesBean> importer = new BinPreferencesBeanImporter(activity);
		if (importer.importData(filename, PreferencesBean.instance)) {
			// Les donn�es ont bien �t� lues. On va � pr�sent les �crire dans le vrai
			// fichier de pr�f�rences pour une relecture au prochain d�marrage de l'application
			PreferencesUtils.savePreferencesBean(activity);
			
			Toast.makeText(activity, R.string.import_prefs_success, Toast.LENGTH_LONG).show();
		} else {
			Toast.makeText(activity, R.string.import_prefs_fail, Toast.LENGTH_LONG).show();
		}
	}

	public void prepareProgressDialog() {
		progressDialog.setProgress(0);
        progressDialog.setSecondaryProgress(0);
        progressDialog.setMax(progressThread.getMax());
        progressThread.start();
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
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
	}
}
