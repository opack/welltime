package fr.redmoon.tictac.bus.export;

import java.io.File;
import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Environment;
import android.widget.Toast;
import fr.redmoon.tictac.R;

public abstract class FileExporter<DataType> {
	
	protected final Activity mActivity;
	protected final Resources mResources;
	protected DataType mData;
	
	public FileExporter(final Activity activity){
		mActivity = activity;
		mResources = activity.getResources();
	}
	
	private boolean checkSDCardWritable() {

		final String auxSDCardStatus = Environment.getExternalStorageState();
		
		// Tout va bien !
		if (auxSDCardStatus.equals(Environment.MEDIA_MOUNTED)) {
			return true;
		}
		
		// Il y a un probl�me avec la carte SD.
		int errorMsgId = R.string.error_sdcard_unknown;
		if (auxSDCardStatus.equals(Environment.MEDIA_MOUNTED_READ_ONLY)) {
			errorMsgId = R.string.error_sdcard_read_only;
		} else if (auxSDCardStatus.equals(Environment.MEDIA_NOFS)) {
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
	
	public boolean exportData(final DataType data) {
		// V�rifie l'accessibilit� de la carte SD
		if (!checkSDCardWritable()) {
			return false;
		}
		mData = data;
		boolean result = false;
		try {
			// Cr�ation du r�pertoire, si n�cessaire
			final String rootDirName = mResources.getString(R.string.app_name);
			final File root = new File(Environment.getExternalStorageDirectory(), rootDirName);
			if (!root.exists() && !root.mkdirs()){
				Toast.makeText(
					mActivity,
					mResources.getString(R.string.error_mkdir_failed, rootDirName),
					Toast.LENGTH_LONG).show();
			}
			
			// Ecriture du fichier
			final File file = new File(root, getFilename());
			result = performExport(file);
		} catch (IOException e) {
			e.printStackTrace();
			Toast.makeText(mActivity, e.getMessage(), Toast.LENGTH_LONG).show();
			return false;
		}
		
		return result;
	}
	
	protected abstract boolean performExport(final File file) throws IOException;
	
	protected abstract String getFilename();

	private void sendMail(
			final Context context,
			final String mimeType,
			final String recipiant,
			final String subject,
			final String attachmentFilePath) {
		final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
		emailIntent.setType(mimeType);
		emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{recipiant});
		emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);
		emailIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(attachmentFilePath)); // i.e. "file:///sdcard/mysong.mp3"
		context.startActivity(Intent.createChooser(emailIntent, "Send mail..."));
	}
}
