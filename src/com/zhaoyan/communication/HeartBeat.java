package com.zhaoyan.communication;

import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

import com.dreamlink.communication.lib.util.ArrayUtil;
import com.zhaoyan.common.util.ArraysCompat;
import com.zhaoyan.common.util.Log;
import com.zhaoyan.communication.protocol.pb.PBBaseProtos.PBType;

/**
 * 1. Server and client every {@link #INTERVAL_TIME} send
 * {@value #HEART_BEAT_MESSAGE}. </br>
 * 
 * 2. Server and client every {@link #INTERVAL_TIME} check the last received
 * heart beat. If the last heart beat interval bigger than
 * {@link #LOST_CONNECTION_TIME}, the connection is lost.</br>
 * 
 * 3. For usage, refer {@link #SocketCommunication}.</br>
 * 
 * 4. Stop heart beat when screen is off. This is because that when screen off,
 * WiFi will go into power save mode and socket may be sent with a long
 * delay(longer than 1 minute). So stop heart beat.
 */
public class HeartBeat {
	private static final String TAG = "HeartBeat";
	/**
	 * Time interval of send heart beat message.
	 */
	private static final int INTERVAL_TIME = 8 * 1000;
	/**
	 * The time of not receiving heart beat to decide connection lost.
	 */
	private static final int LOST_CONNECTION_TIME = INTERVAL_TIME * 3;
	private static final byte[] HEART_BEAT_MESSAGE = ArrayUtil
			.int2ByteArray(PBType.HEART_BEAT_VALUE);
	private static final byte MODE_SCREEN_ON = 0;
	private static final byte MODE_SCREEN_OFF = 1;

	private byte mModeOfMine = MODE_SCREEN_ON;
	private byte mModeOfTheOther = MODE_SCREEN_ON;
	private byte mLastMode = MODE_SCREEN_ON;

	private SocketCommunication mCommunication;
	private Timer mSendTimer;
	private TimerTask mSendTimerTask;
	private Timer mReceiveTimer;
	private TimerTask mReceiveTimerTask;
	private long mLastHeartBeatTime;
	private HeartBeatListener mHeartBeatListener;
	private boolean mIsStoped = false;
	private boolean mIsStarted = false;

	public HeartBeat(SocketCommunication communication,
			HeartBeatListener listener) {
		mCommunication = communication;
		mHeartBeatListener = listener;
	}

	public void start() {
		Log.d(TAG, "start");
		if (mIsStarted) {
			return;
		}
		mIsStarted = true;
		mIsStoped = false;

		mSendTimer = new Timer();
		mSendTimerTask = new SendTimerTask();
		mReceiveTimer = new Timer();
		mReceiveTimerTask = new ReceiveTimerTask();
		mLastHeartBeatTime = System.currentTimeMillis();
		mSendTimer.schedule(mSendTimerTask, 0, INTERVAL_TIME);
		mReceiveTimer.schedule(mReceiveTimerTask, 0, INTERVAL_TIME);
	}

	public void stop() {
		Log.d(TAG, "stop");
		if (mIsStoped) {
			return;
		}
		mIsStarted = false;
		mIsStoped = true;
		mSendTimer.cancel();
		mReceiveTimer.cancel();
		mSendTimer = null;
		mReceiveTimer = null;
	}

	public boolean processHeartBeat(byte[] msg) {
		if (msg.length < HEART_BEAT_MESSAGE.length) {
			return false;
		}
		byte[] typeData = ArraysCompat.copyOfRange(msg, 0,
				HEART_BEAT_MESSAGE.length);
		if (Arrays.equals(typeData, HEART_BEAT_MESSAGE)) {
			mModeOfTheOther = msg[HEART_BEAT_MESSAGE.length];
			mLastHeartBeatTime = System.currentTimeMillis();
			checkMode();
			return true;
		} else {
			return false;
		}
	}

	private class SendTimerTask extends TimerTask {
		@Override
		public void run() {
			sendHeartBeatMessage();
		}
	}

	private void sendHeartBeatMessage() {
		byte[] msg = ArrayUtil.join(HEART_BEAT_MESSAGE,
				new byte[] { mModeOfMine });
		mCommunication.sendMessage(msg, false);
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

	public void setScreenOn() {
		mModeOfMine = MODE_SCREEN_ON;
		checkMode();
		sendHeartBeatMessage();
	}

	public void setScreenOff() {
		mModeOfMine = MODE_SCREEN_OFF;
		checkMode();
		sendHeartBeatMessage();
	}

	private void checkMode() {
		if (MODE_SCREEN_ON == mModeOfMine && MODE_SCREEN_ON == mModeOfTheOther) {
			setMode(MODE_SCREEN_ON);
		} else {
			setMode(MODE_SCREEN_OFF);
		}
	}

	private void setMode(byte mode) {
		if (mLastMode == mode) {
			return;
		}
		Log.d(TAG, "setMode mode = " + mode);
		mLastMode = mode;
		if (mode == MODE_SCREEN_ON) {
			start();
		} else {
			stop();
		}
	}

}
