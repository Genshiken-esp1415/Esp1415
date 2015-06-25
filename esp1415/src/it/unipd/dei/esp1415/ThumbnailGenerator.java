package it.unipd.dei.esp1415;

import java.util.Date;
import java.util.Random;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;

/**
 * Questa classe verrà utilizzata per generare una thumbnail unica per ogni sessione
 * @author Andrea, Laura
 *
 */
public class ThumbnailGenerator {
	
	public ThumbnailGenerator()
		{		}
	
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
    
}
