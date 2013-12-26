package com.zhaoyan.juyou.common;

public class FileHomeInfo {
	/**
	 * id:INTERNAL or SDCARD
	 */
	private int storageId;
	/**
	 * current storage root path
	 */
	private String rootPath;
	/**
	 * current storage available size(bytes)
	 */
	private long availableSize;
	/**
	 * current storage total size(bytes)
	 */
	private long totalSize;
	
	public int getStorageId() {
		return storageId;
	}
	public void setStorageId(int storageId) {
		this.storageId = storageId;
	}
	
	public String getRootPath() {
		return rootPath;
	}
	public void setRootPath(String rootPath) {
		this.rootPath = rootPath;
	}
	
	public long getAvailableSize() {
		return availableSize;
	}
	public void setAvailableSize(long availableSize) {
		this.availableSize = availableSize;
	}
	
	public long getTotalSize() {
		return totalSize;
	}
	public void setTotalSize(long totalSize) {
		this.totalSize = totalSize;
	}
	
}
