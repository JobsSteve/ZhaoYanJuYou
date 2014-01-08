package com.zhaoyan.juyou.common;

import com.zhaoyan.common.util.ZYUtils;

import android.graphics.Bitmap;

public class MediaInfo {
	//db id
	private long id;
	/**media file name*/
	private String displayName;
	/**artist*/
	private String artist;
	/**专辑*/
	private String album;
	private long albumId;
	/**时长(Audio/Video)*/
	private long duration;
	/**file length*/
	private long size;
	/**file path*/
	private String url;
	/**true:audio,false,video*/
	private boolean isAudio;
	private Bitmap icon;
	/**modify date*/
	private long date;
	/**image folder*/
	private String folder;
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getDisplayName() {
		return displayName;
	}
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	public String getArtist() {
		return artist;
	}
	public void setArtist(String artist) {
		this.artist = artist;
	}
	public String getAlbum() {
		return album;
	}
	public void setAlbum(String album) {
		this.album = album;
	}
	public long getAlbumId() {
		return albumId;
	}
	public void setAlbumId(long ablumId) {
		this.albumId = ablumId;
	}
	public long getDuration() {
		return duration;
	}
	public void setDuration(long duration) {
		this.duration = duration;
	}
	public long getSize() {
		return size;
	}
	public void setSize(long size) {
		this.size = size;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	
	public boolean isAudio() {
		return isAudio;
	}
	
	public void setMediaType(boolean isAudio) {
		this.isAudio = isAudio;
	}
	
	public Bitmap getIcon() {
		return icon;
	}
	public void setIcon(Bitmap icon) {
		this.icon = icon;
	}
	
	public long getDate() {
		return date;
	}
	public void setDate(long date) {
		this.date = date;
	}
	public String getFolder() {
		return folder;
	}
	public void setFolder(String folder) {
		this.folder = folder;
	}
	
    public String formatTime() {  
    	return ZYUtils.mediaTimeFormat(duration);
    }  
    
	public String getFormatSize(){
		return ZYUtils.getFormatSize(size);
	}
	
	public String getFormatDate(){
		return ZYUtils.getFormatDate(date);
	}
}
