package it.unipd.dei.esp1415;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Date;
import java.util.Random;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.widget.Toast;

/**
 * Questa classe verrà utilizzata per generare una thumbnail unica per ogni sessione
 * @author Andrea, Laura
 *
 */
public class ThumbnailGenerator {

	

	public static Bitmap createThumbnail(Date inizioSessione)
	{
		long timestamp = inizioSessione.getTime();
		Random random = new Random();
		int a = (int)((timestamp >> 32)) + random.nextInt();
		long secondaParte = timestamp  & 0xffffffff;
		int b = (int)(secondaParte);
		Bitmap.Config conf = Bitmap.Config.ARGB_4444;
		Bitmap left = Bitmap.createBitmap(35, 70, conf);
		left.eraseColor(a);
		Bitmap right = Bitmap.createBitmap(35, 70, conf);
		right.eraseColor(b);
		Bitmap thumbnail = Bitmap.createBitmap(70, 70, conf);
		Canvas canvas = new Canvas(thumbnail);
		canvas.drawBitmap(left, null, new Rect(0, 0, canvas.getWidth() / 2, canvas.getHeight()), null);
		canvas.drawBitmap(right, null, new Rect(canvas.getWidth() / 2, 0, canvas.getWidth(), canvas.getHeight()), null);   
		return thumbnail;  
	}

	public static Bitmap loadImageFromStorage(String filename, Context context) {

		Bitmap thumbnail = null;
		FileInputStream fis;
		try {
			String path = "data/data/it.unipd.dei.esp1415/app_Thumbnails";
			path = path + "/" + filename;
			File f = new File(path);
			fis = new FileInputStream(f);
			thumbnail = BitmapFactory.decodeStream(fis);
			fis.close();
		} catch (Exception ex) {
			Toast.makeText(context, "errore lettura file",
					Toast.LENGTH_SHORT).show();
			return null;
		}
		return thumbnail;
	}

	public static boolean saveToInternalStorage(Bitmap image, String name, Context context) {


		try {
			// Creo la directory nell'archivio interno
			File mydir = context.getDir("Thumbnails", Context.MODE_PRIVATE); 
			// Metto il file nella directory
			File fileWithinMyDir = new File(mydir, name); 
			// Stream per scrivere nel file
			FileOutputStream out = new FileOutputStream(fileWithinMyDir); 
			// Scrivo la bitmap nello stream
			image.compress(Bitmap.CompressFormat.PNG, 100, out);
			out.close();
			return true;
		} catch (Exception e) {
			return false;
		}
	}
    
}
