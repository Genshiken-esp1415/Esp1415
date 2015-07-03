package it.unipd.dei.esp1415;
import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

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
	
	public interface renameDialogListener {
        public void onDialogPositiveClick(RenameDialog dialog);
        public void onDialogNegativeClick(RenameDialog dialog);
    }
    
    // Use this instance of the interface to deliver action events
	renameDialogListener mListener;

	 // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (renameDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement NoticeDialogListener");
        }
    }
    
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {

		//setting the db
		mMyDbmanager = new DBManager(getActivity());
		mMyDbmanager.open();
		//Importing the bundle of task info
		Bundle args;
		args = this.getArguments();
		mSessionId=new Date(args.getLong("id"));


		// Use the Builder class for convenient dialog construction
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		LayoutInflater inflater = getActivity().getLayoutInflater();
		View layout = inflater.inflate(R.layout.dialog_rename,null);

		mSessionName=(EditText)layout.findViewById(R.id.renameField);
		mCurrentSession = mMyDbmanager.getSession(mSessionId);
		mOldName = mCurrentSession.getName();
		
		//create done, advancedview and cancel buttons
		builder.setView(layout)
		.setMessage(mOldName)
		.setPositiveButton(R.string.rename, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				mCurrentSession.setName(mSessionName.getText().toString());
				mMyDbmanager.renameSession(mCurrentSession);
				mListener.onDialogPositiveClick(RenameDialog.this);
			}
		})
		.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				mListener.onDialogNegativeClick(RenameDialog.this);
			}
		})
		;
		
		// Create the AlertDialog object and return it
		return builder.create();
	}
	

}
