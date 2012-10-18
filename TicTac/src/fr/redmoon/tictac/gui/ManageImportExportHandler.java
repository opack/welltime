package fr.redmoon.tictac.gui;

import static fr.redmoon.tictac.gui.activities.ManageActivity.PROGRESS_DIALOG;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;
import fr.redmoon.tictac.R;
import fr.redmoon.tictac.bus.DateUtils;
import fr.redmoon.tictac.bus.PreferencesUtils;
import fr.redmoon.tictac.bus.TimeUtils;
import fr.redmoon.tictac.bus.bean.DayBean;
import fr.redmoon.tictac.bus.bean.PreferencesBean;
import fr.redmoon.tictac.bus.bean.WeekBean;
import fr.redmoon.tictac.bus.export.FileExporter;
import fr.redmoon.tictac.bus.export.FileImporter;
import fr.redmoon.tictac.bus.export.ZipDecompress;
import fr.redmoon.tictac.bus.export.tobinary.BinPreferencesBeanExporter;
import fr.redmoon.tictac.bus.export.tobinary.BinPreferencesBeanImporter;
import fr.redmoon.tictac.bus.export.tocsv.CsvDayBeanImporter;
import fr.redmoon.tictac.bus.export.tocsv.CsvWeekBeanImporter;
import fr.redmoon.tictac.gui.ProgressDialogHandler.OnProgressFinishedListener;
import fr.redmoon.tictac.gui.activities.PreferencesActivity;
import fr.redmoon.tictac.gui.dialogs.DialogArgs;
import fr.redmoon.tictac.gui.dialogs.ExportPeriodFragment;
import fr.redmoon.tictac.gui.dialogs.SelectDayTypeReplacementFragment;
import fr.redmoon.tictac.gui.dialogs.SelectDayTypeReplacementFragment.OnDayTypeReplacedListener;

public class ManageImportExportHandler implements OnItemClickListener, OnDayTypeReplacedListener, OnProgressFinishedListener {
	private final static int POS_EXPORT_DATA = 0;
	private final static int POS_IMPORT_DATA = 1;
	private final static int POS_EXPORT_PREFS = 2;
	private final static int POS_IMPORT_PREFS = 3;
	
	private ProgressDialogHandler progressHandler;
	private DbInserterThread progressThread;
	private ProgressDialog progressDialog;
	
	private final FragmentActivity activity;
	
	private Map<String, String> unknownDayTypes;
	
	public ManageImportExportHandler(final FragmentActivity activity) {
		this.activity = activity;
	}
	
	@Override
	public void onItemClick(AdapterView<?> adapter, View v, int position, long id) {
		switch (position) {
			case POS_EXPORT_DATA:
				final DialogFragment fragment = new ExportPeriodFragment();
				fragment.show(activity.getSupportFragmentManager(), ExportPeriodFragment.TAG);
				break;
			case POS_IMPORT_DATA:
				importData();
				break;
			case POS_EXPORT_PREFS:
				exportPrefs();
				break;
			case POS_IMPORT_PREFS:
				importPreferences();
				break;
		}
	}
	
	@TargetApi(11)
	public Dialog createProgressDialog() {
		progressDialog = new ProgressDialog(activity);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        if (Integer.parseInt(Build.VERSION.SDK) >= 11) {
        	progressDialog.setProgressNumberFormat("%1d sur %2d");
        }
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
        progressHandler = new ProgressDialogHandler(activity, progressDialog, PROGRESS_DIALOG, this);
        progressThread.setHandler(progressHandler);
        return progressDialog;
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
	
	abstract class FilePickedListener implements DialogInterface.OnClickListener {
		private File[] mFiles;
		
		public void setFiles(File[] files) {
			mFiles = files;
		}
		
		public String getFilePath(final int index) {
			return mFiles[index].getAbsolutePath();
		}
	}
	
	private void importDataFile(final FilenameFilter filter, final int extensionLength, final FilePickedListener onFilePickedListener) {
		// Chargement des fichiers trouvés
		final String rootDirName = activity.getResources().getString(R.string.app_name);
		final File root = new File(Environment.getExternalStorageDirectory(), rootDirName);
		if (!root.exists() && !root.mkdirs()){
			// Erreur
			Log.e("Welltime", "Une erreur s'est produite lors de l'accès à la carte SD.");
			Toast.makeText(activity, "Une erreur s'est produite lors de l'accès à la carte SD.", Toast.LENGTH_SHORT).show();
			return;
		}
		final File[] files = root.listFiles(filter);
		onFilePickedListener.setFiles(files);
		
		// Affichage des possibilités à l'utilisateur pour qu'il choisisse
		if (files.length == 0) {
			Toast.makeText(activity, "Aucun fichier corresondant n'a été trouvé dans le répertoire Welltime.", Toast.LENGTH_SHORT).show();
			return;
		}
		final CharSequence[] items = new String[files.length];
		String filenameWithExtension;
		for (int curFile = 0; curFile < files.length; curFile++) {
			filenameWithExtension = files[curFile].getName();
			// Retourne une chaine au format "dd/MM/yyyy - dd/MM/yyyy" à partir d'un 
			// nom de fichier au format "yyyyMMdd_yyyyMMdd.zip"
			items[curFile] = activity.getResources().getString(
				R.string.import_days_filechooser_item,
				DateUtils.formatDateDDMMYYYY(filenameWithExtension.substring(0, 8)),
				DateUtils.formatDateDDMMYYYY(filenameWithExtension.substring(9, 17)));
		}
		final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		builder.setTitle(R.string.import_days_filechooser_title);
		builder.setItems(items, onFilePickedListener);
		builder.show();
	}
	
	private void importPrefsFile(final FilenameFilter filter, final int extensionLength, final FilePickedListener onFilePickedListener) {
		// Chargement des fichiers trouvés
		final String rootDirName = activity.getResources().getString(R.string.app_name);
		final File root = new File(Environment.getExternalStorageDirectory(), rootDirName);
		if (!root.exists() && !root.mkdirs()){
			// Erreur
			Log.e("Welltime", "Une erreur s'est produite lors de l'accès à la carte SD.");
			Toast.makeText(activity, "Une erreur s'est produite lors de l'accès à la carte SD.", Toast.LENGTH_SHORT).show();
			return;
		}
		final File[] files = root.listFiles(filter);
		onFilePickedListener.setFiles(files);
		
		// Affichage des possibilités à l'utilisateur pour qu'il choisisse
		if (files.length == 0) {
			Toast.makeText(activity, "Aucun fichier corresondant n'a été trouvé dans le répertoire Welltime.", Toast.LENGTH_SHORT).show();
			return;
		}
		final CharSequence[] items = new String[files.length];
		String filenameWithExtension;
		for (int curFile = 0; curFile < files.length; curFile++) {
			filenameWithExtension = files[curFile].getName();
			// Retourne une chaine au format "dd/MM/yyyy hh:mm:ss" à partir d'un 
			// nom de fichier au format "yyyyMMdd_hhmmss.prefs"
			items[curFile] = activity.getResources().getString(
				R.string.import_prefs_filechooser_item,
				DateUtils.formatDateDDMMYYYY(filenameWithExtension.substring(0, 8)),
				TimeUtils.formatTimeWithSeconds(filenameWithExtension.substring(9, 15)));
		}
		final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		builder.setTitle(R.string.import_prefs_filechooser_title);
		builder.setItems(items, onFilePickedListener);
		builder.show();
	}
	
	private void importPreferences() {
		final FilenameFilter filter = new FilenameFilter() {
			@Override
			public boolean accept(File dir, String filename) {
				return filename.endsWith("." + BinPreferencesBeanExporter.EXTENSION);
			}
		};
		final int extensionLength = BinPreferencesBeanExporter.EXTENSION.length() + 1;
		final FilePickedListener onFilePickedListener = new FilePickedListener() {
		    public void onClick(DialogInterface dialog, int item) {
		    	final String filename = getFilePath(item);
		    	
		    	// Chargement du fichier de son choix
		    	final FileImporter<PreferencesBean> importer = new BinPreferencesBeanImporter(activity);
				if (importer.importData(filename, PreferencesBean.instance)) {
					// Les données ont bien été lues. On va à présent les écrire dans le vrai
					// fichier de préférences pour une relecture au prochain démarrage de l'application
					// Suppression des préférences stockées liées aux types de jour
					final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(activity);
					final SharedPreferences.Editor editor = preferences.edit();
					for (String prefKey : preferences.getAll().keySet()) {
						// S'il s'agit d'une préférence liée aux types de jour, alors sa clé
						// commence par PREF_DAYTYPE_PREFIX. Le cas échéant, on la supprime.
						if (prefKey.startsWith(PreferencesActivity.PREF_DAYTYPE_PREFIX)) {
							editor.remove(prefKey);
						}
					}
					editor.commit();
					PreferencesUtils.savePreferencesBean(activity);
					Toast.makeText(activity, R.string.import_prefs_success, Toast.LENGTH_LONG).show();
				} else {
					Toast.makeText(activity, R.string.import_prefs_fail, Toast.LENGTH_LONG).show();
				}
		    }
		};
		importPrefsFile(filter, extensionLength, onFilePickedListener);
	}
	
	private void importData() {
		final FilenameFilter filter = new FilenameFilter() {
			@Override
			public boolean accept(File dir, String filename) {
				return filename.endsWith("." + "zip");
			}
		};
		final int extensionLength = "zip".length() + 1;
		final FilePickedListener onFilePickedListener = new FilePickedListener() {
		    public void onClick(DialogInterface dialog, int item) {
		    	final String filename = getFilePath(item);
		    	// Chargement du fichier de son choix
		    	// Décompression des données dans un répertoire temporaire
				final long timestamp = System.currentTimeMillis();
				final File zipFile = new File(filename);
				final String dir = zipFile.getParent() + "/" + timestamp + "/"; // Ajout d'un timestamp pour éviter d'écrire sur des fichiers existants 
				ZipDecompress d = new ZipDecompress(filename, dir); 
				d.unzip(); 
						
				final File unzippedDaysCsvFile = new File(dir, ExportPeriodFragment.FILE_DAYS_CSV);
				final File unzippedWeeksCsvFile = new File(dir, ExportPeriodFragment.FILE_WEEKS_CSV);
				
				// Lit les jours dans le fichier CSV
				final List<DayBean> days = new ArrayList<DayBean>();
				final CsvDayBeanImporter daysImporter = new CsvDayBeanImporter(activity);
				if (!daysImporter.importData(unzippedDaysCsvFile, days)) {
					Toast.makeText(
							activity, 
							activity.getString(R.string.import_io_exception, unzippedDaysCsvFile.getAbsolutePath()),
							Toast.LENGTH_LONG)
						.show();
				}

				// On conserve les jours inconnus pour les importer à la fin du traitement
				unknownDayTypes = daysImporter.getUnknownDayTypes();
				
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
				progressThread = new DbInserterThread(activity, days, weeks);
				// Si le handler existe déjà (c'est le cas si la boîte de dialogue a déjà été créée
				// via createDialog) alors on l'assigne ici. Sinon, ce sera fait lors de la création
				// de la boîte de dialogue.
				progressThread.setHandler(progressHandler);
				activity.showDialog(PROGRESS_DIALOG);
		    }
		};
		importDataFile(filter, extensionLength, onFilePickedListener);
	}
	
	public void prepareProgressDialog() {
		progressDialog.setProgress(0);
        progressDialog.setSecondaryProgress(0);
        progressDialog.setMax(progressThread.getMax());
        progressThread.start();
	}
	
	/**
	 * Affiche une boîte de dialogue demandant à l'utilisateur de choisir un type
	 * de jour pour remplacer le type passé en paramètre, qui n'est pas connu.
	 * @param unknownDayTypeId
	 * @return identifiant du type de jour remplaçant celui qui n'est pas connu
	 */
	private void promptForUnknownDayTypes() {
		// S'il n'y a plus de types de jour à traiter, on arrête
		if (unknownDayTypes.isEmpty()) {
			return;
		}
		
		// Récupère un des types de jour inconnus
		final Iterator<Map.Entry<String, String>> iterator = unknownDayTypes.entrySet().iterator();
		final Map.Entry<String, String> unknownDayType = iterator.next();
		final String unknownDayTypeLabel = unknownDayType.getKey();
		final String tempDayTypeId = unknownDayType.getValue();
		iterator.remove();
		
		// Affichage d'une boîte de dialogue
		final Bundle args = new Bundle();
		args.putString(DialogArgs.UNKNOWN_DAYTYPE.name(), unknownDayTypeLabel);
		args.putString(DialogArgs.TEMP_DAYTYPE_ID.name(), tempDayTypeId);

		final SelectDayTypeReplacementFragment newFragment = new SelectDayTypeReplacementFragment();
		newFragment.setListener(this);
		newFragment.setArguments(args);
	    newFragment.show(activity.getSupportFragmentManager(), SelectDayTypeReplacementFragment.TAG);
	}
	
	@Override
	public void onDayTypeReplaced() {
		// On recommence avec le prochain type de jour inconnu
		promptForUnknownDayTypes();
	}
	
	@Override
	public void onProgressFinished() {
		// S'il y a eut des types de jour inconnus, on affiche une boîte de dialogue pour
		// leur trouver une correspondance
		if (!unknownDayTypes.isEmpty()) {
			promptForUnknownDayTypes();
		}
	}
}
