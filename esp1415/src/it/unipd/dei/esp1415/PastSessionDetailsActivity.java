package it.unipd.dei.esp1415;

import java.util.ArrayList;
import java.util.Date;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.support.v7.app.ActionBarActivity;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

/**
 * Questa activity conterr� il dettaglio su una sessione passata
 */
public class PastSessionDetailsActivity extends ActionBarActivity implements
		RenameDialog.renameDialogListener {

	private static Session sCurrentSession;
	private static DBManager sDb;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent intent = getIntent();
		sDb = new DBManager(this);
		sDb.open();

		sCurrentSession = sDb.getSession(new Date(intent.getLongExtra(
				"IDSessione", 0L)));
		setContentView(R.layout.past_session_details_activity);
		if (savedInstanceState == null) {
			FragmentTransaction fm = getSupportFragmentManager()
					.beginTransaction();
			fm.add(R.id.past_session_details_fragment,
					new SessionDetailsFragment());
			fm.add(R.id.fall_list_fragment, new MyListFragment());
			fm.commit();
		}

	}
	
	@Override
	public void onBackPressed () {
		Intent sessionList = new Intent(this, SessionListActivity.class);
		sessionList.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(sessionList);
		}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.past_session_details, menu);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_rename) {
			showRenameDialog();

		}
		return super.onOptionsItemSelected(item);
	}

	public void showRenameDialog() {
		// Create an instance of the dialog fragment and show it
		RenameDialog newFragment = new RenameDialog();
		Bundle args = new Bundle();
		args.putLong("id", sCurrentSession.getSessionBegin().getTime());
		newFragment.setArguments(args);
		newFragment.show(getSupportFragmentManager(), "rename");
	}

	@Override
	public void onDialogPositiveClick(RenameDialog dialog) {
		sCurrentSession = sDb.getSession(sCurrentSession.getSessionBegin());
		setTitle(sCurrentSession.getName());
	}

	@Override
	public void onDialogNegativeClick(RenameDialog dialog) {

	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class SessionDetailsFragment extends Fragment {

		public SessionDetailsFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(
					R.layout.past_session_details_fragment, container,
					false);
			// solo per testing prendo tutte le sessioni dal db

			// ArrayList<Session> sessions =
			// (ArrayList<Session>)db.getAllSessions();
			// currentSession = sessions.get(2);
			sCurrentSession.setFallList((ArrayList<Fall>) sDb
					.getAllFalls(sCurrentSession.getSessionBegin()));
			// Caricamento thumbnail
			String thumbnailName = sCurrentSession.getThumbnail();
			Bitmap thumbnail = Utilities.loadImageFromStorage(thumbnailName,
					getActivity().getApplicationContext());
			ImageView thumbnailImageView = (ImageView) rootView
					.findViewById(R.id.session_thumbnail);
			thumbnailImageView.setImageBitmap(thumbnail);
			TextView sessionTimestampTextView = (TextView) rootView
					.findViewById(R.id.session_timestamp);
			TextView sessionDurationTextView = (TextView) rootView
					.findViewById(R.id.session_length);
			String timestamp = this.getString(R.string.date_and_time) + DateFormat.format("dd/MM/yy - kk:mm",
					sCurrentSession.getSessionBegin());
			sessionTimestampTextView.setText(timestamp);
			String duration = this.getString(R.string.session_duration) + Utilities.millisToHourMinuteSecond(sCurrentSession
					.getDuration(), false);
			sessionDurationTextView.setText(duration);
			this.getActivity().setTitle(sCurrentSession.getName());
			return rootView;
		}
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class MyListFragment extends ListFragment {
		private Fall mSelectedFall;
		private FallAdapter mAdapter;

		public MyListFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {

			return inflater.inflate(R.layout.fall_list_fragment, container,
					false);
		}

		@Override
		public void onActivityCreated(Bundle savedInstanceState) {
			super.onActivityCreated(savedInstanceState);
			// Solo per testing prendo tutte le sessioni dal db
			DBManager db = new DBManager(getActivity().getBaseContext());
			db.open();
			sCurrentSession.setFallList((ArrayList<Fall>) db
					.getAllFalls(sCurrentSession.getSessionBegin()));
			mAdapter = new FallAdapter(getActivity().getBaseContext(),
					sCurrentSession.getFallList());
			setListAdapter(mAdapter);
		}
		
		// Click su elementi della lista
		@Override
		public void onListItemClick(ListView l, View v, int position, long id) {// gestisce

			super.onListItemClick(l, v, position, id);
			mSelectedFall = (Fall) getListAdapter().getItem(position);
			Intent fallDetails = new Intent(getActivity()
					.getApplicationContext(), FallDetailsActivity.class);
			Date idSessione = mAdapter.getItem(position).getFallTimestamp();
			fallDetails.putExtra("IDCaduta", idSessione.getTime());
			fallDetails.putExtra("NomeSessione", sCurrentSession.getName());
			startActivity(fallDetails);

		}
	}

	public static class FallAdapter extends ArrayAdapter<Fall> {
		private final Context context;
		private final ArrayList<Fall> falls;

		public FallAdapter(Context context, ArrayList<Fall> values) {
			super(context, R.layout.row_fall, values);
			this.context = context;
			this.falls = values;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View rowFallView = inflater.inflate(R.layout.row_fall, parent,
					false);
			TextView fallNumberTextView = (TextView) rowFallView
					.findViewById(R.id.fall_number);
			TextView timestampFallTextView = (TextView) rowFallView
					.findViewById(R.id.fall_timestamp);
			ImageView notifiedImageView = (ImageView) rowFallView
					.findViewById(R.id.notified);
			fallNumberTextView.setText(String.valueOf(falls.get(position)
					.getFallNumber()));
			String timestamp = (String) DateFormat.format("dd/MM/yy - kk:mm",
					falls.get(position).getFallTimestamp());
			timestampFallTextView.setText(timestamp);

			if (falls.get(position).isNotified()) {
				notifiedImageView.setImageResource(R.drawable.cross);
			} else {
				notifiedImageView.setImageResource(R.drawable.tick);
			}

			return rowFallView;

		}
	}

}
