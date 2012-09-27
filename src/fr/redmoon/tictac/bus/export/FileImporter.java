package fr.redmoon.tictac.bus.export;

import java.io.File;
import java.io.IOException;

import android.app.Activity;
import android.os.Environment;
import android.widget.Toast;
import fr.redmoon.tictac.R;

public abstract class FileImporter<DataType> {
	
	protected final Activity mActivity;
	protected DataType mData;
	
	public FileImporter(final Activity activity){
		mActivity = activity;
	}
	
	private boolean checkSDCardReadable() {

		final String auxSDCardStatus = Environment.getExternalStorageState();
		
		// Tout va bien !
		if (auxSDCardStatus.equals(Environment.MEDIA_MOUNTED)
		|| auxSDCardStatus.equals(Environment.MEDIA_MOUNTED_READ_ONLY)) {
			return true;
		}
		
		// Il y a un problème avec la carte SD.
		int errorMsgId = R.string.error_sdcard_unknown;
		if (auxSDCardStatus.equals(Environment.MEDIA_NOFS)) {
			errorMsgId = R.string.error_sdcard_nofs;
		} else if (auxSDCardStatus.equals(Environment.MEDIA_REMOVED)) {
			errorMsgId = R.string.error_sdcard_removed;
		} else if (auxSDCardStatus.equals(Environment.MEDIA_SHARED)) {
			errorMsgId = R.string.error_sdcard_shared;
		} else if (auxSDCardStatus.equals(Environment.MEDIA_UNMOUNTABLE)) {
			errorMsgId = R.string.error_sdcard_unmountable;
		} else if (auxSDCardStatus.equals(Environment.MEDIA_UNMOUNTED)) {
			errorMsgId = R.string.error_sdcard_unmounted;
		} else if (auxSDCardStatus.equals(Environment.MEDIA_CHECKING)) {
			errorMsgId = R.string.error_sdcard_checking;
		}
		
		if (mActivity != null) {
			Toast.makeText(mActivity, mActivity.getString(errorMsgId), Toast.LENGTH_LONG).show();
		}

		return false;
	}
	
	public boolean importData(final String filename, final DataType readData) {
		return importData(new File(filename), readData);
	}
	
	public boolean importData(final File source, final DataType readData) {
		// Vérifie l'accessibilité de la carte SD
		if (!checkSDCardReadable()) {
			return false;
		}
		
		mData = readData;
		
		// Lecture des données depuis le fichier
		try {
			performImport(source);
		} catch (IOException e) {
			e.printStackTrace();
			Toast.makeText(mActivity, e.getMessage(), Toast.LENGTH_LONG).show();
			return false;
		}
		
		return true;
	}
	
	protected abstract boolean performImport(final File file) throws IOException;
}
