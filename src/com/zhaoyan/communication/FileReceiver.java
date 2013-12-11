package com.zhaoyan.communication;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.HandlerThread;
import android.os.Message;

import com.dreamlink.communication.aidl.User;
import com.zhaoyan.common.util.Log;
import com.zhaoyan.communication.TrafficStaticInterface.TrafficStaticsRxListener;
import com.zhaoyan.communication.protocol.FileTransferInfo;

/**
 * Connect to server socket and get the file from server.
 * 
 */
public class FileReceiver {
	private static final String TAG = "FileReceiver";
	private static final int SOCKET_TIMEOUT = 5000;
	/** To avoid call back to fast, set the minimum interval callback time。 */
	private static final int TIME_CALLBACK_INTERVAL = 1000;
	
	private User mSendUser;
	private InetAddress mServerInetAddress;
	private int mServerPort;
	private FileTransferInfo mFileTransferInfo;
	private File mReceivedFile;
	private OnReceiveListener mListener;

	private ReceiveHandlerThread mHandlerThread;

	private static final int MSG_UPDATE_PROGRESS = 1;
	private static final int MSG_FINISH = 2;
	private static final int MSG_STOP_RECEIVE = 3;
	private Handler mHandler;

	private static final String KEY_RECEIVE_BYTES = "KEY_RECEIVE_BYTES";

	private static final int FINISH_RESULT_SUCCESS = 1;
	private static final int FINISH_RESULT_FAIL = 2;
	
	private Object mKey;

	/** The socket to recieve file. */
	private Socket mSocket;
	
	private TrafficStaticsRxListener mRxListener = TrafficStatics.getInstance();
	
	private boolean mCancelReceiveFlag = false;

	public FileReceiver(User sendUser, byte[] serverAddress, int serverPort,
			FileTransferInfo fileTransferInfo) {
		mSendUser = sendUser;
		try {
			mServerInetAddress = InetAddress.getByAddress(serverAddress);
		} catch (UnknownHostException e) {
			Log.e(TAG, "FileReceiver() get server addresss error. " + e);
		}
		mServerPort = serverPort;
		mFileTransferInfo = fileTransferInfo;
	}

	/**
	 * @return the SendUser
	 */
	public User getSendUser() {
		return mSendUser;
	}

	/**
	 * @return the FileInfo
	 */
	public FileTransferInfo getFileTransferInfo() {
		return mFileTransferInfo;
	}

	/**
	 * Connect the server and receive file from server.
	 * 
	 * @param receivedFile
	 *            the file to save.
	 * @param listener
	 */
	public void receiveFile(File receivedFile, OnReceiveListener listener, Object key) {
		mKey = key;
		Log.d(TAG,
				"receiveFile() received file " + receivedFile.getAbsolutePath());
		mReceivedFile = receivedFile;
		mListener = listener;
		if (mServerInetAddress == null) {
			Log.e(TAG, "receiveFile() Server Address is null.");
			return;
		}

		FileReceiverThread fileReceiverThread = new FileReceiverThread();
		fileReceiverThread.start();

		mHandlerThread = new ReceiveHandlerThread("ReceiveHandlerThread");
		mHandlerThread.start();

		mHandler = new Handler(mHandlerThread.getLooper(), mHandlerThread);
	}
	
	public void cancelReceiveFile() {
		mCancelReceiveFlag = true;
	}

	/**
	 * Stop receiving.
	 */
	public void stopReceive() {
		if (mHandler != null) {
			mHandler.sendEmptyMessage(MSG_STOP_RECEIVE);
		}
	}

	/**
	 * A simple server socket that accepts connection and writes some data on
	 * the stream.
	 */
	class FileReceiverThread extends Thread {

		@Override
		public void run() {
			Log.d(TAG, "FileReceiverThread run()");

			mSocket = new Socket();
			try {
				Log.d(TAG, "Opening client socket - server address: "
						+ mServerInetAddress.getHostAddress() + ", port : "
						+ mServerPort);
				mSocket.bind(null);
				mSocket.connect((new InetSocketAddress(mServerInetAddress,
						mServerPort)), SOCKET_TIMEOUT);
				Log.d(TAG, "Client socket - " + mSocket.isConnected());
				InputStream inputStream = mSocket.getInputStream();
				copyFile(inputStream, new FileOutputStream(mReceivedFile));
				Log.d(TAG, "Client: Data written");
			} catch (Exception e) {
				Log.e(TAG, "FileReceiverThread:[" + e.toString() + "]");
			} finally {
				if (mSocket != null) {
					if (mSocket.isConnected()) {
						try {
							mSocket.close();
						} catch (IOException e) {
							// Give up
							Log.e(TAG, "Close socket error." + e);
						}
					}
				}
			}
			Log.d(TAG, "FileReceiverThread file: [" + mReceivedFile.getName()
					+ "] finished.");
		}
	}

	private void copyFile(InputStream inputStream, OutputStream out) {
		byte buf[] = new byte[4096];
		int len;
		long receiveBytes = 0;
		long totalBytes = mFileTransferInfo.getFileSize();
		long start = System.currentTimeMillis();
		long lastCallbackTime = start;
		long currentTime = start;
		mCancelReceiveFlag = false;
		
		try {
			while ((len = inputStream.read(buf)) != -1 && mCancelReceiveFlag == false) {
				out.write(buf, 0, len);
				receiveBytes += len;
				
				currentTime = System.currentTimeMillis();
				if (currentTime - lastCallbackTime >= TIME_CALLBACK_INTERVAL || receiveBytes >= totalBytes) {
					notifyProgress(receiveBytes, totalBytes);
					lastCallbackTime = currentTime;
				}
				
				mRxListener.addRxBytes(len);
			}
			
			if (mCancelReceiveFlag == true) {
				notifyFinish(false);
			} else if (receiveBytes != totalBytes) {
				notifyFinish(false);
			} else {
			    notifyFinish(true);
			}
			out.close();
			inputStream.close();
		} catch (IOException e) {
			Log.d(TAG, "copyFile:[" + e.toString() + "]");
			notifyFinish(false);
		}
		long time = System.currentTimeMillis() - start;
		Log.d(TAG, "Total size = " + receiveBytes + "bytes time = " + time
				+ ", speed = " + (receiveBytes / time) + "KB/s");
	}

	class ReceiveHandlerThread extends HandlerThread implements Callback {

		public ReceiveHandlerThread(String name) {
			super(name);
		}

		@Override
		public boolean handleMessage(Message msg) {

			switch (msg.what) {
			case MSG_UPDATE_PROGRESS:
				Bundle data = msg.getData();
				long receiveBytes = data.getLong(KEY_RECEIVE_BYTES);
				if (mListener != null) {
					mListener.onReceiveProgress(receiveBytes, mReceivedFile, mKey);
				}
				break;
			case MSG_FINISH:
				if (mListener != null) {
					if (msg.arg1 == FINISH_RESULT_SUCCESS) {
						mListener.onReceiveFinished(true, mReceivedFile, mKey);
					} else {
						mListener.onReceiveFinished(false, mReceivedFile, mKey);
					}
				}
				// Quit the HandlerThread.
				quit();
				mHandler = null;
				break;

			case MSG_STOP_RECEIVE:
				Log.d(TAG, "MSG_STOP_RECEIVE");
				if (mSocket != null && mSocket.isConnected()) {
					try {
						mSocket.close();
					} catch (IOException e) {
						Log.e(TAG, "Create socket error." + e);
					}
				}
				break;
			default:
				break;
			}
			return true;
		}

	}

	private void notifyProgress(long sentBytes, long totalBytes) {
		Message message = mHandler.obtainMessage();
		message.what = MSG_UPDATE_PROGRESS;
		Bundle data = new Bundle();
		data.putLong(KEY_RECEIVE_BYTES, sentBytes);
		message.setData(data);
		mHandler.sendMessage(message);
	}

	private void notifyFinish(boolean result) {
		Message message = mHandler.obtainMessage();
		message.what = MSG_FINISH;
		if (result) {
			message.arg1 = FINISH_RESULT_SUCCESS;
		} else {
			message.arg1 = FINISH_RESULT_FAIL;
		}
		mHandler.sendMessage(message);
	}

	/**
	 * Callback to get the receive status.
	 * 
	 */
	public interface OnReceiveListener {
		/**
		 * Every {@link #TIME_CALLBACK_INTERVAL} time, this method is invoked.
		 * 
		 * @param receivedBytes
		 * @param totalBytes
		 */
		void onReceiveProgress(long receivedBytes, File file, Object key);

		/**
		 * The file is received.
		 * 
		 * @param success
		 */
		void onReceiveFinished(boolean success, File file, Object key);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "FileReceiver [mSendUser=" + mSendUser + ", mServerInetAddress="
				+ mServerInetAddress + ", mServerPort=" + mServerPort
				+ ", mFileInfo=" + mFileTransferInfo + "]";
	}

}
