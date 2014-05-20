package com.zhaoyan.juyou.backuprestore;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;

public interface BackupsHandler {
	List<File> result = new ArrayList<File>();
	public boolean init(Context context);
	public void onStart();
	public void reset();
	public void cancel();
	public List<File> onEnd();
	public String getBackupType();
}
