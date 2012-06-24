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
        
        // On profite de cette étape pour créer le handler
        progressHandler = new ProgressDialogHandler(activity, progressDialog, PROGRESS_DIALOG);
        progressThread.setHandler(progressHandler);
        return progressDialog;
	}
	
	public Dialog createPeriodExportDialog() {
		//On instancie notre layout en tant que View
        LayoutInflater factory = LayoutInflater.from(activity);
        final View dialogView = factory.inflate(R.layout.dlg_period_chooser, null);
 
        //Création de l'AlertDialog
        AlertDialog.Builder adb = new AlertDialog.Builder(activity);
 
        //On affecte la vue personnalisé que l'on a crée à notre AlertDialog
        adb.setView(dialogView);
        
        //On donne un titre à l'AlertDialog
        adb.setTitle(R.string.export_data_title);
 
        //On modifie l'icône de l'AlertDialog pour le fun ;)
        //adb.setIcon(android.R.drawable.ic_dialog_alert);
        
        //On affecte un bouton "OK" à notre AlertDialog et on lui affecte un évènement
        adb.setPositiveButton(R.string.btn_ok, new PeriodExporterListener(
        	activity, 
        	db, 
        	(DatePicker)dialogView.findViewById(R.id.date1), 
        	(DatePicker)dialogView.findViewById(R.id.date2))
        );
 
        //On crée un bouton "Annuler" à notre AlertDialog et on lui affecte un évènement
        adb.setNegativeButton(R.string.btn_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {}
        });
        
		return adb.create();
	}
	
	/**
	 * Exporte les préférences
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
        activity.startActivityForResult(Intent.createChooser(intent, activity.getString(R.string.import_filechooser_title)), requestId); 
	}
	
	private void importDataPhase1() {
		requestFileChooserActivity(PeriodExporterListener.MIME_TYPE, POS_IMPORT_DATA);
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
		// Décompression des données dans un répertoire temporaire
		final long timestamp = System.currentTimeMillis();
		final File zipFile = new File(filename);
		final String dir = zipFile.getParent() + "/" + timestamp + "/"; // Ajout d'un timestamp pour éviter d'écrire sur des fichiers existants 
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
		
		// Ecrit ces données dans la base en utilisant un Thread et une belle
		// boîte de dialogue avec une barre de progression pour montrer où on
		// en est.
		progressThread = new DbInserterThread(days, weeks, db);
		// Si le handler existe déjà (c'est le cas si la boîte de dialogue a déjà été créée
		// via createDialog) alors on l'assigne ici. Sinon, ce sera fait lors de la création
		// de la boîte de dialogue.
		progressThread.setHandler(progressHandler);
		activity.showDialog(PROGRESS_DIALOG);
	}
	
	/**
	 * Dernière phase de l'import : réalise effectivement l'import en lisant le fichier
	 * binaire et en écrivant les données lues dans PreferencesBean.instance.
	 * @param filename
	 */
	private void importPrefsPhase2(final String filename){
		final FileImporter<PreferencesBean> importer = new BinPreferencesBeanImporter(activity);
		if (importer.importData(filename, PreferencesBean.instance)) {
			// Les données ont bien été lues. On va à présent les écrire dans le vrai
			// fichier de préférences pour une relecture au prochain démarrage de l'application
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
