package fr.redmoon.tictac.gui.dialogs;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;
import android.widget.Toast;
import fr.redmoon.tictac.R;
import fr.redmoon.tictac.bus.DateUtils;
import fr.redmoon.tictac.bus.bean.DayBean;
import fr.redmoon.tictac.bus.bean.WeekBean;
import fr.redmoon.tictac.bus.export.FileExporter;
import fr.redmoon.tictac.bus.export.ZipCompress;
import fr.redmoon.tictac.bus.export.tocsv.CsvDayBeanExporter;
import fr.redmoon.tictac.bus.export.tocsv.CsvWeekBeanExporter;
import fr.redmoon.tictac.db.DbAdapter;
import fr.redmoon.tictac.gui.activities.TicTacActivity;

public class ExportPeriodFragment extends DialogFragment implements OnClickListener {
	public final static String TAG = ExportPeriodFragment.class.getName();
	
	public static final String FILE_DAYS_CSV = "days.csv";
	public static final String FILE_WEEKS_CSV = "weeks.csv";
	public static final String MIME_TYPE = "application/zip";
	
	private DatePicker mDate1; 
	private DatePicker mDate2;
	
	private TicTacActivity mActivity;
	private String mRootDir;
	private String mDaysFilename;
	private String mWeeksFilename;
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		//On instancie notre layout en tant que View
        LayoutInflater factory = LayoutInflater.from(getActivity());
        final View dialogView = factory.inflate(R.layout.dlg_period_chooser, null);
 
        // Sauvegarde des contrôles pour lecture lors de la validation
    	mDate1 = (DatePicker)dialogView.findViewById(R.id.date1);
		mDate2 = (DatePicker)dialogView.findViewById(R.id.date2);
		
        //Création de l'AlertDialog
        AlertDialog.Builder adb = new AlertDialog.Builder(getActivity());
 
        //On affecte la vue personnalisé que l'on a crée à notre AlertDialog
        adb.setView(dialogView);
        
        //On donne un titre à l'AlertDialog
        adb.setTitle(R.string.export_data_title);
 
        //On modifie l'icône de l'AlertDialog pour le fun ;)
        //adb.setIcon(android.R.drawable.ic_dialog_alert);
        
        //On affecte un bouton "OK" à notre AlertDialog et on lui affecte un évènement
        adb.setPositiveButton(R.string.btn_ok, this);
 
        //On crée un bouton "Annuler" à notre AlertDialog et on lui affecte un évènement
        adb.setNegativeButton(R.string.btn_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {}
        });
        
		return adb.create();
	}
	
	@Override
	public void onClick(final DialogInterface dialog, final int which) {
		// Récupération de l'activity et de l'accès à la base
		mActivity = (TicTacActivity)getActivity();
		
		// Identifiants des jours saisis
		final long firstDay = DateUtils.getDayId(mDate1.getYear(), mDate1.getMonth(), mDate1.getDayOfMonth());
		final long lastDay = DateUtils.getDayId(mDate2.getYear(), mDate2.getMonth(), mDate2.getDayOfMonth());
		
		// Export des jours et des semaines
		final boolean resExportDays = exportDays(firstDay, lastDay);
		final boolean resExportSemaines = exportWeeks(firstDay, lastDay);
		
		// Compression des fichiers dans un zip
		final Map<String, String> files = new HashMap<String, String>();
		files.put(mRootDir + "/" + mDaysFilename, FILE_DAYS_CSV);
		files.put(mRootDir + "/" + mWeeksFilename, FILE_WEEKS_CSV);
		final String zipFilename = mActivity.getString(R.string.export_zip_filename_pattern, firstDay, lastDay);
		final ZipCompress zip = new ZipCompress(files, mRootDir + "/" + zipFilename);
		zip.zip();
		
		// Suppression des fichiers source
		final File exportedDaysCsvFile = new File(mRootDir, mDaysFilename);
		exportedDaysCsvFile.delete();
		final File exportedWeeksCsvFile = new File(mRootDir, mWeeksFilename);
		exportedWeeksCsvFile.delete();
		
		// Affichage du résultat
		String message = "";
		if (resExportDays && resExportSemaines) {
			message = mActivity.getString(
				R.string.export_data_success,
				DateUtils.formatDateDDMMYYYY(firstDay),
				DateUtils.formatDateDDMMYYYY(lastDay));
		} else {
			message = mActivity.getString(R.string.export_data_fail);
		}
		Toast.makeText(mActivity, message, Toast.LENGTH_LONG).show();
	}

	private boolean exportDays(final long firstDay, final long lastDay) {
		// Récupération des jours à extraire
		final List<DayBean> days = new ArrayList<DayBean>();
		DbAdapter.getInstance().fetchDays(firstDay, lastDay, days);
		
		// Export des jours vers le fichier texte
		final FileExporter<List<DayBean>> daysExporter = new CsvDayBeanExporter(mActivity, firstDay, lastDay);
		boolean result = daysExporter.exportData(days);
		mRootDir = daysExporter.getRootDir(); // A faire après exportData car la valeur n'est pas renseignée avant !
		mDaysFilename = daysExporter.getFilename();
		return result;
	}
	
	private boolean exportWeeks(final long firstDay, final long lastDay) {
		// Récupération des jours à extraire
		final List<WeekBean> weeks = new ArrayList<WeekBean>();
		DbAdapter.getInstance().fetchWeeks(firstDay, lastDay, weeks);
		
		// Export des jours vers le fichier texte
		final FileExporter<List<WeekBean>> weeksExporter = new CsvWeekBeanExporter(mActivity, firstDay, lastDay);
		boolean result = weeksExporter.exportData(weeks);
		mWeeksFilename = weeksExporter.getFilename();
		return result;
	}
}