package com.zhaoyan.juyou.backuprestore;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.zhaoyan.common.util.Log;

import android.content.Context;

public class SmartPhoneBackupHandler implements BackupsHandler {
	private String mPath = "";
	private static final String TAG = "SmartPhoneBackupHandler";
	public SmartPhoneBackupHandler() {
		super();
	}

	@Override
	public boolean init(Context context) {
		String externalStoragePath = SDCardUtils.getExternalStoragePath(context);
		if(externalStoragePath == null){
			return false;
		}
		mPath = externalStoragePath +File.separator;
		Log.d(TAG, "init()=====>>mPath is "+ mPath );
		return true;
	}
	@Override
	public void onStart() {
		Log.d(TAG, "onStart()");
		File file = new File(mPath);
		if(file.exists()&&file.isDirectory()&&file.canRead()){
			File[] files = file.listFiles();
			result.addAll(filterFile(files));
		}else{
			Log.e(TAG, "file is not exist====>>>mPath is "+mPath);
		}
	}

	private Collection<? extends File> filterFile(File[] files) {
		ArrayList<File> resultList = new ArrayList<File>();
		if (files == null || files.length == 0){
			return resultList;
		}
		
		for(File file : files){
			String nameString = file.getName();
			if(nameString!=null&&!nameString.isEmpty()){
				if (nameString.endsWith(".vcf") && nameString.length() == 9){
					String fileNameString = nameString.substring(0, nameString.lastIndexOf("."));
					try {
						Integer.parseInt(fileNameString);
						resultList.add(file);
					} catch (NumberFormatException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
		return resultList;
	}

	@Override
	public void cancel() {
		Log.d(TAG, "cancel()");
		
	}

	@Override
	public List<File> onEnd() {
		Log.d(TAG, "onEnd()");
		return result;
	}

	@Override
	public String getBackupType() {
		return Constants.BackupScanType.PLUTO;
	}

	@Override
	public void reset() {
		result.clear();
	}
}
