package it.unipd.dei.esp1415;
import java.util.Date;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Questa classe definisce una Dialog che permette di rinominare una sessione.
 * Nella classe chiamante sono da implementare i metodi dell'interfaccia
 * renameDialogListener.
 */
public class RenameDialog extends DialogFragment {

	private EditText mSessionName;
	private DBManager mMyDbmanager;
	private String mOldName;
	private Date mSessionId;
	private Session mCurrentSession;
	private Context mActivity;

	public interface renameDialogListener {
		public void onDialogPositiveClick(RenameDialog dialog);

		public void onDialogNegativeClick(RenameDialog dialog);
	}

	renameDialogListener mListener;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mActivity = activity;
		// Verifica che l'activity chiamante implementi i metodi
		// dell'interfaccia
		try {
			mListener = (renameDialogListener) activity;
		} catch (ClassCastException e) {
			// Se l'activity non implementa i metodi dell'interfaccia lancia
			// un'eccezione
			throw new ClassCastException(activity.toString()
					+ " must implement NoticeDialogListener");
		}
	}

	// Warning soppressa poichè sta usando il metodo inflate in una dialog di
	// cui non posso conoscere la rootview poichè verrà creata dal builder
	@SuppressLint("InflateParams")
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {

		mMyDbmanager = new DBManager(getActivity());
		mMyDbmanager.open();
		Bundle args;
		args = this.getArguments();
		mSessionId = new Date(args.getLong("id"));

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		LayoutInflater inflater = getActivity().getLayoutInflater();
		View layout = inflater.inflate(R.layout.dialog_rename, null);

		mSessionName = (EditText) layout.findViewById(R.id.renameField);
		mCurrentSession = mMyDbmanager.getSession(mSessionId);
		mOldName = mCurrentSession.getName();

		builder.setView(layout)
				.setMessage(mOldName)
				.setPositiveButton(R.string.rename,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								// Se l'utente lascia il campo vuoto non fa nessuna modifica
								if (mSessionName.getText().toString()
										.compareTo("") == 0) {
									Toast.makeText(mActivity,
											R.string.insert_a_name,
											Toast.LENGTH_SHORT).show();
									return;
								}
								mCurrentSession.setName(mSessionName.getText()
										.toString());
								mMyDbmanager.renameSession(mCurrentSession);
								mListener
										.onDialogPositiveClick(RenameDialog.this);
							}
						})
				.setNegativeButton(R.string.cancel,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								mListener
										.onDialogNegativeClick(RenameDialog.this);
							}
						});
		return builder.create();
	}

}
