package it.unipd.dei.esp1415;


import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.*;
import android.graphics.drawable.shapes.RectShape;
/**
 * Questa activity conterrà il dettaglio su una caduta
 */
public class DettaglioCadutaActivity extends ActionBarActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_dettaglio_caduta);
		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.dettaglio_caduta, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(
					R.layout.fragment_dettaglio_caduta, container, false);
			return rootView;
		}
	}
	
	/*public class ThumbnailBox extends View{
		private ShapeDrawable box;
		
		public ThumbnailBox(Context context){
			super(context);
			
			int x=10;
			int y=10;
			int width = 200;
			int height = 200;
			
			box = new ShapeDrawable(new RectShape());
			box.getPaint().setColor(0x0);
			box.setBounds(x, y, x+width, y+height);
		}
		
		protected void onDraw(Canvas canvas){
			box.draw(canvas);
		}
	}*/
}
