package com.zhaoyan.communication;


public interface TrafficStaticInterface {
	public interface TrafficStaticsRxListener {
		void addRxBytes(int byteCount);
	}

	public interface TrafficStaticsTxListener {
		void addTxBytes(int byteCount);
	}
}
