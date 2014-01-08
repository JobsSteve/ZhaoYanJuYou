package com.zhaoyan.communication;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.HandlerThread;
import android.os.Message;

import com.zhaoyan.common.util.Log;
import com.zhaoyan.communication.TrafficStaticInterface.TrafficStaticsTxListener;

/**
 * Create server socket to send file. Send file to the connected client.
 * 
 */
public class FileSender {
	private static final String TAG = "FileSender";
	/** 3 minutes time out. */
	private static final int SEND_SOCKET_TIMEOUT = 3 * 60 * 1000;

	/** To avoid call back to fast, set the minimum interval callback time。 */
	private static final int TIME_CALLBACK_INTERVAL = 1000;

	private OnFileSendListener mListener;
	private File mSendFile;
	private ServerSocket mServerSocket;

	private SendHandlerThread mHandlerThread;

	private static final int MSG_UPDATE_PROGRESS = 1;
	private static final int MSG_FINISH = 2;
	private Handler mHandler;

	private static final String KEY_SENT_BYTES = "KEY_SENT_BYTES";
	private static final String KEY_TOTAL_BYTES = "KEY_TOTAL_BYTES";

	private static final int FINISH_RESULT_SUCCESS = 1;
	private static final int FINISH_RESULT_FAIL = 2;
	private Object mKey;

	private TrafficStaticsTxListener mTxListener = TrafficStatics.getInstance();
	
	private boolean mCancelSendFlag = false;

	public FileSender() {
	};

	public FileSender(Object key) {
		mKey = key;
	}

	/**
	 * Create file send server and send file to the first connected client.
	 * After the file is sent, the server is closed.
	 * 
	 * @param file
	 * @param listener
	 * @return The server socket port.
	 */
	public int sendFile(File file, OnFileSendListener listener) {
		mListener = listener;
		mSendFile = file;

		mServerSocket = createServerSocket();
		if (mServerSocket == null) {
			Log.e(TAG, "sendFile() file: " + file
					+ ", fail. Create server socket error");
			return -1;
		}

		FileSenderThread fileSenderThread = new FileSenderThread();
		fileSenderThread.start();

		mHandlerThread = new SendHandlerThread("HandlerThread-FileSender");
		mHandlerThread.start();

		mHandler = new Handler(mHandlerThread.getLooper(), mHandlerThread);
		return mServerSocket.getLocalPort();
	}
	
	public void cancelSendFile() {
		mCancelSendFlag = true;
	}

	/**
	 * Get an available port.
	 * 
	 * @return
	 */
	private ServerSocket createServerSocket() {
		for (int port : SocketPort.FILE_TRANSPORT_PROT) {
			try {
				ServerSocket serverSocket = new ServerSocket(port);
				Log.d(TAG, "Create port successs. port number: " + port);
				return serverSocket;
			} catch (IOException e) {
				Log.d(TAG, "The server port is in use. port number: " + port);
			}
		}
		return null;
	}

	/**
	 * A simple server socket that accepts connection and writes some data on
	 * the stream.
	 */
	class FileSenderThread extends Thread {

		@Override
		public void run() {
			try {
				mServerSocket.setSoTimeout(SEND_SOCKET_TIMEOUT);
				Socket client = mServerSocket.accept();
				Log.d(TAG, "Client ip: "
						+ client.getInetAddress().getHostAddress());
				Log.d(TAG, "Server: connection done");
				OutputStream outputStream = client.getOutputStream();
				Log.d(TAG, "server: copying files " + mSendFile.toString());
				copyFile(new FileInputStream(mSendFile), outputStream);
				mServerSocket.close();
			} catch (Exception e) {
				Log.e(TAG, "FileSenderThread " + e.toString());
				notifyFinish(false);
			}
			Log.d(TAG, "FileSenderThread file: [" + mSendFile.getName()
					+ "] finished");
		}
	}

	class SendHandlerThread extends HandlerThread implements Callback {

		public SendHandlerThread(String name) {
			super(name);
		}

		@Override
		public boolean handleMessage(Message msg) {

			switch (msg.what) {
			case MSG_UPDATE_PROGRESS:
				Bundle data = msg.getData();
				long sentBytes = data.getLong(KEY_SENT_BYTES);
				long totalBytes = data.getLong(KEY_TOTAL_BYTES);
				if (mListener != null) {
					mListener.onSendProgress(sentBytes, mSendFile, mKey);
				}
				break;
			case MSG_FINISH:
				if (mListener != null) {
					if (msg.arg1 == FINISH_RESULT_SUCCESS) {
						mListener.onSendFinished(true, mSendFile, mKey);
					} else {
						mListener.onSendFinished(false, mSendFile, mKey);
					}
				}

				// Quit the HandlerThread
				quit();
				break;
			default:
				break;
			}
			return true;
		}

	}

	private void copyFile(InputStream inputStream, OutputStream out) {
		byte buf[] = new byte[4096];
		int len;
		long sendBytes = 0;
		long start = System.currentTimeMillis();
		long totalBytes = mSendFile.length();
		long lastCallbackTime = start;
		long currentTime = start;
		mCancelSendFlag = false;

		try {
			while ((len = inputStream.read(buf)) != -1 && mCancelSendFlag == false) {
				out.write(buf, 0, len);
				sendBytes += len;

				currentTime = System.currentTimeMillis();
				if (currentTime - lastCallbackTime >= TIME_CALLBACK_INTERVAL
						|| sendBytes >= totalBytes) {
					notifyProgress(sendBytes, totalBytes);
					lastCallbackTime = currentTime;
				}

				mTxListener.addTxBytes(len);
			}

			if (mCancelSendFlag == true) {
				notifyFinish(false);
			} else {
			    notifyFinish(true);
			}
			out.close();
			inputStream.close();
		} catch (IOException e) {
			notifyFinish(false);
			Log.d(TAG, e.toString());
		}
		long time = System.currentTimeMillis() - start;
		Log.d(TAG, "Total size = " + sendBytes + "bytes time = " + time
				+ ", speed = " + (sendBytes / time) + "KB/s");
	}

	private void notifyProgress(long sentBytes, long totalBytes) {
		Message message = mHandler.obtainMessage();
		message.what = MSG_UPDATE_PROGRESS;
		Bundle data = new Bundle();
		data.putLong(KEY_SENT_BYTES, sentBytes);
		data.putLong(KEY_TOTAL_BYTES, totalBytes);
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
	 * Callback to get the send status.
	 * 
	 */
	public interface OnFileSendListener {
		/**
		 * Every {@link #TIME_CALLBACK_INTERVAL} time, this method is invoked.
		 * 
		 * @param sentBytes
		 * @param file
		 */
		void onSendProgress(long sentBytes, File file, Object key);

		/**
		 * The file is sent.
		 * 
		 * @param success
		 */
		void onSendFinished(boolean success, File file, Object key);
	}

}
