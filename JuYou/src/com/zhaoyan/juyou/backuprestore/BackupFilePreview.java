package com.zhaoyan.juyou.backuprestore;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import android.content.Context;

import com.zhaoyan.common.util.Log;
import com.zhaoyan.juyou.backuprestore.Constants.ModulePath;

public class BackupFilePreview {
    private final String TAG = "BackupFilePreview";
    private final int UN_PARESED_TYPE = -1;

    private File mFolderName = null;
    private long mSize = 0;
    private int mTypes = UN_PARESED_TYPE;
    private String backupTime;
    private boolean mIsRestored = false;
    private boolean mIsOtherBackup = true;
    private boolean mIsSelfBackup = true;
    private HashMap<Integer, Integer> mNumberMap = new HashMap<Integer, Integer>();

    public BackupFilePreview(File file) {
        if (file == null) {
            Log.e(TAG, "constractor error! file is null");
            return;
        }
        mNumberMap.clear();
        mFolderName = file;
        Log.d(TAG,  "new BackupFilePreview: file is " + file.getAbsolutePath());
        computeSize();
        checkRestored();
    }

	private void computeSize() {
        mSize = FileUtils.computeAllFileSizeInFolder(mFolderName);
    }

    private void checkRestored() {
        mIsRestored = false;
        mIsOtherBackup = true;
        mIsSelfBackup = true;
        String xmlFilePath = mFolderName + File.separator + Constants.RECORD_XML;
        File recordXmlFile = new File(xmlFilePath);
        if (!recordXmlFile.exists()) {
        	addToCurrentBackupHistory(xmlFilePath);
            return;
        }

        String content = Utils.readFromFile(xmlFilePath);
        ArrayList<RecordXmlInfo> recordList = new ArrayList<RecordXmlInfo>();
        if (content != null) {
            recordList = RecordXmlParser.parse(content.toString());
			if (recordList != null && recordList.size() > 0) {
				if (recordList.size() > 1) {
					mIsSelfBackup = false;
				}
				String currentDevice = Utils.getPhoneSearialNumber();
				for (RecordXmlInfo record : recordList) {
					if (record.getDevice().equals(currentDevice)) {
						mIsOtherBackup = false;
						if (record.isRestore()) {
							mIsRestored = true;
						}
					}
				}
				if (mIsOtherBackup) {
					addCurrentSN(recordList, xmlFilePath);
				}
			}
        }

    }
    
    public boolean isSelfBackup() {
		return mIsSelfBackup;
	}


	private void addToCurrentBackupHistory(String xmlFilePath) {
		// TODO Auto-generated method stub
    	RecordXmlInfo backupInfo = new RecordXmlInfo();
        backupInfo.setRestore(false);
        backupInfo.setDevice(Utils.getPhoneSearialNumber());
        backupInfo.setTime(""+System.currentTimeMillis());
        RecordXmlComposer xmlCompopser = new RecordXmlComposer();
        xmlCompopser.startCompose();
        xmlCompopser.addOneRecord(backupInfo);
        xmlCompopser.endCompose();
        if (xmlFilePath != null && xmlFilePath.length()>0) {
            Utils.writeToFile(xmlCompopser.getXmlInfo(), xmlFilePath);
        }
	}

	/**
     * add this phone's SN info to record.xml
     * @return add success return true, otherwise false.
     */
    private boolean addCurrentSN(List<RecordXmlInfo> recordList, String path) {
		// TODO Auto-generated method stub
    	boolean success = false;
    	try {
			RecordXmlInfo restoreInfo = new RecordXmlInfo();
			restoreInfo.setRestore(false);
			restoreInfo.setDevice(Utils.getPhoneSearialNumber());
			restoreInfo.setTime(String.valueOf(System.currentTimeMillis()));
			
			recordList.add(restoreInfo);
			
			RecordXmlComposer xmlCompopser = new RecordXmlComposer();
			xmlCompopser.startCompose();
			for(RecordXmlInfo record : recordList){
				xmlCompopser.addOneRecord(record);
			}
			xmlCompopser.endCompose();
			Utils.writeToFile(xmlCompopser.getXmlInfo(), path);
			success = true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return success;
		}
    	return success;
	}

	public boolean isRestored() {
        return mIsRestored;
    }
    public boolean isOtherDeviceBackup() {
        return mIsOtherBackup;
    }

    public File getFile() {
        return mFolderName;
    }

    public String getFileName() {
    	String showNameString = mFolderName.getName();
    	if(showNameString!=null&&showNameString.length()==14&&showNameString.trim().length()==14){
    		try {
    			Double.parseDouble(showNameString);
				return showNameString = formatString(showNameString);
			} catch (NumberFormatException e) {
				return showNameString;
			}
    	}
    	return showNameString;
    }

    private String formatString(String showNameString) {
		// TODO Auto-generated method stub
    	if(showNameString!=null){
    		String yearString = showNameString.substring(0, 4);
    		String monthString = showNameString.substring(4,6);
    		String dayString = showNameString.substring(6,8);
    		String hourString = showNameString.substring(8,10);
    		String minString = showNameString.substring(10,12);
    		String secrString = showNameString.substring(12,14);
    		return yearString+"-"+monthString+"-"+dayString+"  "+hourString+":"+minString+":"+secrString;
    	}
    	return null;
	}

	public String getBackupTime() {
        if (backupTime == null) {
            Long time = mFolderName.lastModified();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
            backupTime = dateFormat.format(new Date(time));
        }
        return backupTime;
    }

    public void setBackupTime(String backupTime) {
        this.backupTime = backupTime;
    }

    public long getFileSize() {
        return mSize;
    }

    public int getBackupModules(Context context) {
        if (mTypes == UN_PARESED_TYPE) {
            mTypes = peekBackupModules(context);
        }
        return mTypes;
    }

    /**
     * parse backup items.
     */
    private int peekBackupModules(Context context) {

        File[] files = mFolderName.listFiles();
        mTypes = 0;
        if (files != null) {
            for (File file : files) {
                String[] moduleFolders = new String[] { 
                        ModulePath.FOLDER_CONTACT, ModulePath.FOLDER_MUSIC,
                        ModulePath.FOLDER_PICTURE, ModulePath.FOLDER_SMS};

                int[] moduleTypes = new int[] { ModuleType.TYPE_CONTACT,
                         ModuleType.TYPE_MUSIC, ModuleType.TYPE_PICTURE,
                        ModuleType.TYPE_SMS };

                if (file.isDirectory() && !FileUtils.isEmptyFolder(file)) {
                    String name = file.getName();
                    
                    Log.d(TAG, "peekBackupModules.name=" + name);
                    int count = moduleFolders.length;
                    for (int index = 0; index < count; index++) {
                        if (moduleFolders[index].equalsIgnoreCase(name)) {
                        	initNumByType(context, moduleTypes[index]);
                        	if(getItemCount(moduleTypes[index])>0){
                        		mTypes |= moduleTypes[index];
                        	}
                        }
                    }
                }
            }
        }
        Log.e(TAG, "parseItemTypes: mTypes =  " + mTypes);
        return mTypes;
    }

    private void initNumByType(Context context, final int type) {
        Composer composer = null;
        switch (type) {
        case ModuleType.TYPE_CONTACT:
            composer = new ContactRestoreComposer(context);
            break;

//        case ModuleType.TYPE_CALENDAR:
////            composer = new CalendarRestoreComposer(context);
//            break;

        case ModuleType.TYPE_SMS:
//            composer = new MessageRestoreComposer(context);
        	composer = new SmsRestoreComposer(context);
            break;

        case ModuleType.TYPE_MUSIC:
            composer = new MusicRestoreComposer(context);
            break;

        case ModuleType.TYPE_PICTURE:
            composer = new PictureRestoreComposer(context);
            break;

        default:
            break;
        }
        if (composer != null) {
            composer.setParentFolderPath(mFolderName.getAbsolutePath());
            composer.init();
            int count = composer.getCount();
            Log.v(TAG, "initNumByType: count = " + count);
            mNumberMap.put(type, count);
        }
    }

    public int getItemCount(int type) {
        int count = mNumberMap.get(type);
        Log.v(TAG, "getItemCount: type = " + type + ",count = " + count);
        return count;
    }
}
