package com.zhaoyan.juyou.backuprestore;

import java.io.File;
import java.util.List;

import com.zhaoyan.common.util.Log;

import android.content.Context;

public class PlutoBackupHandler implements BackupsHandler {
	private String mPath = "";
	private static final String TAG = "PlutoBackupHandler";
	public PlutoBackupHandler() {
		super();
	}

	@Override
	public boolean init(Context context) {
		String externalStoragePath = SDCardUtils.getExternalStoragePath(context);
		if(externalStoragePath == null){
			return false;
		}
		mPath = externalStoragePath +File.separator+ Constants.BackupScanType.PLUTO_PATH;
		Log.d(TAG, "init()=====>>mPath is "+ mPath );
		return true;
	}
	@Override
	public void onStart() {
		// TODO Auto-generated method stub
		Log.d(TAG, "onStart()");
		File file = new File(mPath);
		if(file.exists()&&file.isFile()&&file.canRead()){
			result.add(file);
		}else{
			Log.e(TAG, "file is not exist====>>>mPath is "+mPath);
		}
	}

	@Override
	public void cancel() {
		// TODO Auto-generated method stub
		Log.d(TAG, "cancel()");
		
	}

	@Override
	public List<File> onEnd() {
		// TODO Auto-generated method stub
		Log.d(TAG, "onEnd()");
		return result;
	}

	@Override
	public String getBackupType() {
		// TODO Auto-generated method stub
		return Constants.BackupScanType.PLUTO;
	}

	@Override
	public void reset() {
		// TODO Auto-generated method stub
		result.clear();
	}
}
