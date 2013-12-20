package com.zhaoyan.communication;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Arrays;

import android.annotation.SuppressLint;

import com.dreamlink.communication.lib.util.ArrayUtil;
import com.zhaoyan.common.util.Log;
import com.zhaoyan.communication.HeartBeat.HeartBeatListener;
import com.zhaoyan.communication.TrafficStaticInterface.TrafficStaticsRxListener;
import com.zhaoyan.communication.TrafficStaticInterface.TrafficStaticsTxListener;

/**
 * Thread for send and receive message.</br>
 * 
 * 1. Process packet size</br>
 * 
 * Before send packet, add packet size header to ensure the received packet is
 * the right packet. The packet with size header is like [packet size][packet
 * data]. After received packet, remove the packet size header to get the
 * original packet. For Details, see {@link #encode(byte[])} and
 * {@link #decode(DataInputStream)} </br>
 * 
 * 2. Process large packet</br>
 * 
 * Because of the size limit of receive message. We break the large message into
 * some small message to send. Before send message, add a header to check
 * whether there is more packet for the message. When receive a message, check
 * the header to get all packets of a message.</br>
 * 
 * For Details, see {@link #encode(byte[])} and {@link #decode(DataInputStream)}
 * </br>
 * 
 * 3. Process heart beat. For Details, see {@link #HeartBeat}</br>
 */
@SuppressLint("NewApi")
public class SocketCommunication extends Thread implements HeartBeatListener {
	private static final String TAG = "SocketCommunication";
	/** Socket server port */
	public static final int PORT = SocketPort.COMMUNICATION_SERVER_PORT;

	/**
	 * Listen socket connect and disconnect event.
	 * 
	 */
	public interface OnCommunicationChangedListener {
		/**
		 * There is a new socket connected, and the communication is ready.
		 * 
		 * @param communication
		 *            The established communication.
		 */
		void OnCommunicationEstablished(SocketCommunication communication);

		/**
		 * There is socket disconnected, and the commnunication is lost.
		 * 
		 * @param communication
		 */
		void OnCommunicationLost(SocketCommunication communication);
	}

	/**
	 * Call back listener for notify there is a message received.
	 * 
	 */
	public interface OnReceiveMessageListener {
		void onReceiveMessage(byte[] msg,
				SocketCommunication socketCommunication);
	}

	private Socket mSocket;
	private OnReceiveMessageListener mOnReceiveMessageListener;

	private DataInputStream mDataInputStream = null;
	private DataOutputStream mDataOutputStream = null;

	private OnCommunicationChangedListener mListener;

	/** The size of head */
	private static final int HEAD_SIZE = 4;
	/** Buffer of head */
	private byte[] mHeadBuffer = new byte[HEAD_SIZE];
	/** One receive max size */
	private static final int RECEIVE_BUFFER_SIZE = 10 * 1024;
	/** Buffer of one time receive. */
	private byte[] mReceiveBuffer;

	/** The remain head of last receive. */
	private byte[] mRemainHeader;
	/** The last packet length. It is use for next read */
	private int mLastPacketLength;
	/** The remain packet of last receive. */
	private byte[] mRemainPacket;

	private static final int HEAD_IS_LAST_PACKET_SIZE = 1;
	private static final byte HEAD_LAST_PACKET = 0;
	private static final byte HEAD_NOT_LAST_PACKET = 1;
	/**
	 * The max message size of one send. Bigger than this will cause receive
	 * fail.
	 */
	private static final int MAX_SEND_SIZE_ONE_TIME = RECEIVE_BUFFER_SIZE
			- HEAD_SIZE - HEAD_IS_LAST_PACKET_SIZE;
	/** Store the large message. */
	private byte[] mLargeMessageBuffer;

	private TrafficStaticsRxListener mRxListener = TrafficStatics.getInstance();
	private TrafficStaticsTxListener mTxListener = TrafficStatics.getInstance();

	private HeartBeat mHeartBeat;

	public SocketCommunication(Socket socket, OnReceiveMessageListener listener) {
		if (socket == null) {
			// This is a fake one, used by UserManager.
			return;
		}
		this.mSocket = socket;
		this.mOnReceiveMessageListener = listener;
		mHeartBeat = new HeartBeat(this, this);
	}

	public InetAddress getConnectedAddress() {
		return mSocket.getInetAddress();
	}

	public void setOnCommunicationChangedListener(
			OnCommunicationChangedListener listener) {
		mListener = listener;
	}

	@Override
	public void run() {

		try {
			mDataInputStream = new DataInputStream(mSocket.getInputStream());
			mDataOutputStream = new DataOutputStream(mSocket.getOutputStream());
			if (mListener != null) {
				mListener.OnCommunicationEstablished(this);
			}
			mHeartBeat.start();
			mReceiveBuffer = new byte[RECEIVE_BUFFER_SIZE];
			while (!mSocket.isClosed()) {
				boolean isContinue = decode(mDataInputStream);
				if (!isContinue) {
					communicationLost();
					break;
				}
			}
		} catch (IOException e) {
			Log.e(TAG, "Read socket error. " + e);
			communicationLost();
		}
	}

	/**
	 * There is error cause communication lost.
	 */
	private void communicationLost() {
		Log.d(TAG, "communicationLost "
				+ getConnectedAddress().getHostAddress());
		if (mSocket == null) {
			// This is a fake one, used by UserManager.
			return;
		}
		mHeartBeat.stop();

		if (mListener != null) {
			mListener.OnCommunicationLost(this);
			mListener = null;
		}

		closeSocket();
	}

	/**
	 * Decode the encoded package.</br>
	 * 
	 * 1. Before decode:</br>
	 * 
	 * 1.1 Normal received data is:
	 * [header1+msg1][header2+msg2][header3+msg3].</br>
	 * 
	 * 1.2 But in some conditions received data may break or more than one
	 * packet like: [hearder1+msg1.1][msg1.2+header2+msg2+header3+msg3]</br>
	 * 
	 * After decode: [msg1][msg2][msg3]</br>
	 * 
	 * Decode process:</br>
	 * 
	 * 1. Read 4 bit header to get the packet length.</br>
	 * 
	 * 2. Based on the packet length, read the input to get the packet
	 * data.</br>
	 * 
	 * @param in
	 * @return true: continue decode, false : stop decode.
	 * @throws IOException
	 */
	private synchronized boolean decode(DataInputStream in) throws IOException {
		int dataReceivedLength = 0;
		if (mRemainPacket == null) {
			// There is no remain packet.
			// 1. Get the first 4 bit header for packet length.
			int packetLength = 0;
			if (mRemainHeader == null) {
				// There is no remain head.
				dataReceivedLength = in.read(mHeadBuffer);
				if (dataReceivedLength == -1) {
					Log.d(TAG, "Connection lost. dataReceivedLength = -1");
					return false;
				}
				if (dataReceivedLength < HEAD_SIZE) {
					// Received data is less than one header data, Save it and
					// return for next read.
					mRemainHeader = Arrays.copyOfRange(mHeadBuffer, 0,
							dataReceivedLength);
					return true;
				} else if (dataReceivedLength == HEAD_SIZE) {
					// Get the packet length.
					packetLength = ArrayUtil.byteArray2Int(mHeadBuffer);
					mRemainHeader = null;
				}
			} else {
				// There is remain header data left.
				// Read the remain header data to get the header data.
				dataReceivedLength = in.read(mHeadBuffer, 0, HEAD_SIZE
						- mRemainHeader.length);
				if (dataReceivedLength == -1) {
					Log.d(TAG, "Connection lost. dataReceivedLength = -1");
					return false;
				}
				if (mRemainHeader.length + dataReceivedLength < HEAD_SIZE) {
					// remain head + data received less than one header data.
					// Save the header data and return for next read.
					mRemainHeader = ArrayUtil.join(mRemainHeader, Arrays
							.copyOfRange(mHeadBuffer, 0, dataReceivedLength));
					return true;
				} else if (mRemainHeader.length + dataReceivedLength == HEAD_SIZE) {
					// remain head + data received is one header
					mHeadBuffer = ArrayUtil.join(mRemainHeader, Arrays
							.copyOfRange(mHeadBuffer, 0, dataReceivedLength));
					packetLength = ArrayUtil.byteArray2Int(mHeadBuffer);
					mRemainHeader = null;
				}
			}
			if (packetLength > RECEIVE_BUFFER_SIZE || packetLength < 1) {
				// Decode header error.
				Log.e(TAG,
						"Decode header error. packageLength is bad number. packageLength = "
								+ packetLength);
				return true;
			}

			// 2. Read received data.
			dataReceivedLength = in.read(mReceiveBuffer, 0, packetLength);
			if (dataReceivedLength == -1) {
				Log.d(TAG, "Connection lost. dataReceivedLength = -1");
				return false;
			}
			if (dataReceivedLength == packetLength) {
				// Received data is just one packet. Return for next read.
				receiveMessage(Arrays.copyOfRange(mReceiveBuffer, 0,
						dataReceivedLength));
				return true;
			} else if (dataReceivedLength < packetLength) {
				// Received data is less than one packet.
				// 1. Save last packet length.
				mLastPacketLength = packetLength;
				// 2. Save the received data into buff.
				mRemainPacket = Arrays.copyOfRange(mReceiveBuffer, 0,
						dataReceivedLength);
			}
		} else {
			// There is remain packet left.
			if (mLastPacketLength > RECEIVE_BUFFER_SIZE) {
				// Packet length error. drop it.
				mReceiveBuffer = null;
				return true;
			}
			// Read the remain packet data.
			dataReceivedLength = in.read(mReceiveBuffer, 0, mLastPacketLength
					- mRemainPacket.length);
			if (dataReceivedLength == -1) {
				Log.d(TAG, "Connection lost. dataReceivedLength = -1");
				return false;
			}
			if (dataReceivedLength + mRemainPacket.length == mLastPacketLength) {
				// remain packet + received data is one packet.
				receiveMessage(ArrayUtil.join(mRemainPacket, Arrays
						.copyOfRange(mReceiveBuffer, 0, dataReceivedLength)));
				mRemainPacket = null;
				return true;
			} else if (dataReceivedLength + mRemainPacket.length < mLastPacketLength) {
				// remain packet + received data is less than one packet.
				// Save the received data into buff.
				mRemainPacket = ArrayUtil.join(mRemainPacket, Arrays
						.copyOfRange(mReceiveBuffer, 0, dataReceivedLength));
			}
		}
		return true;
	}

	private void receiveMessage(byte[] msg) {
		byte isLastPacket = msg[0];
		msg = Arrays.copyOfRange(msg, 1, msg.length);
		switch (isLastPacket) {
		case HEAD_LAST_PACKET:
			receiveLastPacket(msg);
			break;
		case HEAD_NOT_LAST_PACKET:
			receiveNotLastPacket(msg);
			break;

		default:
			Log.e(TAG,
					"receiveMessage formate error. Unkown is_last_packet_head byte.");
			break;
		}
	}

	private void receiveLastPacket(byte[] msg) {
		if (mLargeMessageBuffer != null) {
			msg = ArrayUtil.join(mLargeMessageBuffer, msg);
			mLargeMessageBuffer = null;
		}

		/**
		 * Heart beat message is only for {@link #HeartBeat}.
		 */
		if (!mHeartBeat.processHeartBeat(msg)) {
			mOnReceiveMessageListener.onReceiveMessage(msg, this);
			mRxListener.addRxBytes(msg.length);
		}
	}

	private void receiveNotLastPacket(byte[] msg) {
		if (mLargeMessageBuffer != null) {
			mLargeMessageBuffer = ArrayUtil.join(mLargeMessageBuffer, msg);
		} else {
			mLargeMessageBuffer = msg;
		}
	}

	/**
	 * Add 4 bytes msg length header and 1 byte last packet hearder before
	 * msg.</br>
	 * 
	 * Before encode: [msg1][msg2][msg3]</br>
	 * 
	 * After encode:
	 * [sizeHeader1+lastPacketHeader1+msg1][sizeHeader2+lastPacketHeader2
	 * +msg2][sizeHeader3+lastPacketHeader3+msg3]</br>
	 * 
	 * @param msg
	 * @return encoded msg.
	 */
	private byte[] encode(byte[] msg, boolean isLastPacket) {
		byte headLastPacket = isLastPacket ? HEAD_LAST_PACKET
				: HEAD_NOT_LAST_PACKET;
		byte[] result = ArrayUtil.join(
				ArrayUtil.int2ByteArray(msg.length + HEAD_IS_LAST_PACKET_SIZE),
				new byte[] { headLastPacket }, msg);
		return result;
	}

	/**
	 * Send message to the connected socket
	 * 
	 * @param msg
	 * @return return true if send success, return false if send fail.
	 */
	public boolean sendMessage(byte[] msg) {
		return sendMessage(msg, true);
	}

	/**
	 * Used for some message that without traffic statics.
	 * 
	 * @param msg
	 * @param doTrafficStatic
	 * @return
	 */
	boolean sendMessage(byte[] msg, boolean doTrafficStatic) {
		int messageLength = msg.length;
		if (messageLength > MAX_SEND_SIZE_ONE_TIME) {
			return sendMultiPacketMessage(msg, doTrafficStatic);
		} else {
			return sendSinglePacketMessage(msg, doTrafficStatic);
		}
	}

	/**
	 * The msg length length bigger than {@link #MAX_SEND_SIZE_ONE_TIME}
	 * 
	 * @param msg
	 * @param doTrafficStatic
	 * @return
	 */
	private boolean sendMultiPacketMessage(byte[] msg, boolean doTrafficStatic) {
		if (msg.length <= MAX_SEND_SIZE_ONE_TIME) {
			return sendSinglePacketMessage(msg, doTrafficStatic);
		}

		int totalLength = msg.length;
		int sentLength = 0;

		int start = 0;
		int end = MAX_SEND_SIZE_ONE_TIME;
		boolean isLastPacket = false;
		while (sentLength < totalLength) {
			boolean sendResult = sendPacketMessage(
					Arrays.copyOfRange(msg, start, end), isLastPacket,
					doTrafficStatic);
			if (!sendResult) {
				return false;
			}
			sentLength += end - start;
			start = end;
			if (totalLength - sentLength <= MAX_SEND_SIZE_ONE_TIME) {
				end = totalLength;
				isLastPacket = true;
			} else {
				end += MAX_SEND_SIZE_ONE_TIME;
				isLastPacket = false;
			}
		}
		return false;
	}

	private boolean sendSinglePacketMessage(byte[] msg, boolean doTrafficStatic) {
		return sendPacketMessage(msg, true, doTrafficStatic);
	}

	private boolean sendPacketMessage(byte[] msg, boolean isLastPacket,
			boolean doTrafficStatic) {
		try {
			if (mDataOutputStream != null) {
				mDataOutputStream.write(encode(msg, isLastPacket));
				mDataOutputStream.flush();

				if (doTrafficStatic) {
					mTxListener.addTxBytes(msg.length);
				}
			} else {
				communicationLost();
				return false;
			}
		} catch (IOException e) {
			Log.e(TAG, "sendMessage fail. msg = " + msg + ", error = " + e);
			communicationLost();
		}
		return true;
	}

	private void closeSocket() {
		try {
			if (mDataInputStream != null) {
				mDataInputStream.close();
			}
			if (mDataOutputStream != null) {
				mDataOutputStream.close();
			}
			if (!mSocket.isClosed()) {
				mSocket.close();
			}
		} catch (IOException e) {
			Log.e(TAG, "closeSocket fail." + e);
		} catch (Exception e) {
			Log.e(TAG, "closeSocket fail." + e);
		}
	}

	/**
	 * Disconnect from the connected socket and stop communication.
	 */
	public void stopComunication() {
		Log.d(TAG, "stopComunication()");
		mListener = null;
		mHeartBeat.stop();

		closeSocket();
	}

	@Override
	public void onHeartBeatTimeOut() {
		Log.d(TAG, "onHeartBeatTimeOut");
		if (mListener != null) {
			mListener.OnCommunicationLost(this);
			mListener = null;
		}

		closeSocket();
	}

	public void setScreenOn() {
		mHeartBeat.setScreenOn();
	}

	public void setScreenOff() {
		mHeartBeat.setScreenOff();
	}

	@Override
	public String toString() {
		return super.toString() + ", connected ip = "
				+ mSocket.getInetAddress().getHostAddress();
	}
}