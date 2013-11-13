package com.zhaoyan.communication;

import java.net.Socket;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.util.Log;

import com.dreamlink.communication.lib.util.Notice;

/**
 * This class is a AsyncTask, used for creating client socket and connecting to
 * server.</br>
 * 
 * After connected server, start communication with server socket.</br>
 * 
 */
@SuppressLint("UseValueOf")
public class SocketClientTask extends AsyncTask<String, Void, Socket> {
	private static final String TAG = "SocketClientTask";

	public interface OnConnectedToServerListener {
		/**
		 * Connected to server.
		 * 
		 * @param socket
		 */
		void onConnectedToServer(Socket socket);
	}

	private Context mContext;
	private ProgressDialog progressDialog;
	private Notice notice;

	private SocketClient client;

	private OnConnectedToServerListener mOnConnectedToServerListener;

	public SocketClientTask(Context context) {
		this.mContext = context;

		client = new SocketClient();
		notice = new Notice(mContext);
	}

	public void setOnConnectedToServerListener(
			OnConnectedToServerListener listener) {
		mOnConnectedToServerListener = listener;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();

		progressDialog = new ProgressDialog(mContext);
		progressDialog.setTitle("Server");
		progressDialog.setMessage("Connecting to server...");
		progressDialog.setButton(ProgressDialog.BUTTON_POSITIVE, "Stop",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						cancel(true);
						progressDialog.dismiss();
					}
				});
		progressDialog.setCancelable(false);
//		progressDialog.show();
	}

	@Override
	protected Socket doInBackground(String... arg) {
		return client.startClient(arg[0], new Integer(arg[1]));
	}

	@Override
	protected void onPostExecute(Socket result) {
		super.onPostExecute(result);

		closeDialog();

		if (result == null) {
			notice.showToast("Connect to server fail.");
		} else {
			if (mOnConnectedToServerListener != null) {
				mOnConnectedToServerListener.onConnectedToServer(result);
			}
		}
	}

	private void closeDialog() {
		if (progressDialog != null && progressDialog.isShowing()) {
			try {
				progressDialog.dismiss();
			} catch (Exception e) {
				Log.e(TAG, "closeDialog(), " + e);
				// There is exception sometimes.
			}

		}
	}

}