package fr.redmoon.tictac.bus.export;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipCompress {
	private static final int BUFFER = 2048;

	private Map<String, String> mFiles;
	private String mDestZip;

	public ZipCompress(Map<String, String> files, String zipFile) {
		mFiles = files;
		mDestZip = zipFile;
	}

	public void zip() {
		try {
			final FileOutputStream dest = new FileOutputStream(mDestZip);
			final ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));
			final byte data[] = new byte[BUFFER];

			for (Map.Entry<String, String> file : mFiles.entrySet()) {
				// Accès au fichier source
				FileInputStream fi = new FileInputStream(file.getKey());
				BufferedInputStream origin = new BufferedInputStream(fi, BUFFER);
				
				// Ajout d'une entrée dans le zip
				ZipEntry entry = new ZipEntry(file.getValue());
				out.putNextEntry(entry);
				
				// Copie des octets
				int count = 0;
				while ((count = origin.read(data, 0, BUFFER)) != -1) {
					out.write(data, 0, count);
				}
				
				// Fermeture de l'entrée et du fichier source
				out.closeEntry();
				origin.close();
			}

			// Fermeture du zip
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}