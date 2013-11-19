package com.zhaoyan.communication;

import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

import com.dreamlink.communication.lib.util.ArrayUtil;
import com.zhaoyan.common.util.Log;
import com.zhaoyan.communication.protocol.Protocol;

/**
 * 1. Server and client every {@link #INTERVAL_TIME} send
 * {@value #HEART_BEAT_MESSAGE}. </br>
 * 
 * 2. Server and client every {@link #INTERVAL_TIME} check the last received
 * heart beat. If the last heart beat interval bigger than
 * {@link #LOST_CONNECTION_TIME}, the connection is lost.</br>
 * 
 * 3. For usage, refer {@link #SocketCommunication}.
 */
public class HeartBeat {
	private static final String TAG = "HeartBeat";
	/**
	 * Time interval of send heart beat message.
	 */
	private static final int INTERVAL_TIME = 10 * 1000;
	/**
	 * The time of not receiving heart beat to decide connection lost.
	 */
	private static final int LOST_CONNECTION_TIME = INTERVAL_TIME * 2;
	private static final byte[] HEART_BEAT_MESSAGE = ArrayUtil
			.int2ByteArray(Protocol.DATA_TYPE_HEADER_HEART_BEAT);
	private SocketCommunication mCommunication;
	private Timer mSendTimer;
	private TimerTask mSendTimerTask;
	private Timer mReceiveTimer;
	private TimerTask mReceiveTimerTask;
	private long mLastHeartBeatTime;
	private HeartBeatListener mHeartBeatListener;
	private boolean mIsStoped = false;

	public HeartBeat(SocketCommunication communication,
			HeartBeatListener listener) {
		mCommunication = communication;
		mSendTimer = new Timer();
		mSendTimerTask = new SendTimerTask();
		mReceiveTimer = new Timer();
		mReceiveTimerTask = new ReceiveTimerTask();

		mHeartBeatListener = listener;
	}

	public void start() {
		Log.d(TAG, "start");
		if (mIsStoped) {
			throw new IllegalStateException("HeartBeat is stoped!");
		}
		mLastHeartBeatTime = System.currentTimeMillis();
		mSendTimer.schedule(mSendTimerTask, 0, INTERVAL_TIME);
		mReceiveTimer.schedule(mReceiveTimerTask, 0, INTERVAL_TIME);
	}

	public void stop() {
		Log.d(TAG, "stop");
		if (mIsStoped) {
			return;
		}
		mIsStoped = true;
		mSendTimer.cancel();
		mReceiveTimer.cancel();
		mSendTimer = null;
		mReceiveTimer = null;
	}

	public static boolean isHeartBeatMessage(byte[] msg) {
		return Arrays.equals(msg, HEART_BEAT_MESSAGE);
	}

	public void receivedHeartBeat() {
		mLastHeartBeatTime = System.currentTimeMillis();
	}

	private class SendTimerTask extends TimerTask {
		@Override
		public void run() {
			mCommunication.sendMessage(HEART_BEAT_MESSAGE);
		}
	}

	private class ReceiveTimerTask extends TimerTask {
		@Override
		public void run() {
			long intervalFromLastBeat = System.currentTimeMillis()
					- mLastHeartBeatTime;
			Log.v(TAG, "ReceiveTimerTask intervalFromLastBeat = "
					+ intervalFromLastBeat);
			if (intervalFromLastBeat >= LOST_CONNECTION_TIME) {
				if (mHeartBeatListener != null) {
					mHeartBeatListener.onHeartBeatTimeOut();
				}
			}
		}
	}

	public interface HeartBeatListener {
		void onHeartBeatTimeOut();
	}

}
