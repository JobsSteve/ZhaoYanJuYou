package com.zhaoyan.juyou.backuprestore;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Handler;
import android.os.Message;

import com.zhaoyan.common.util.Log;
import com.zhaoyan.juyou.backuprestore.Constants.MessageID;
import com.zhaoyan.juyou.backuprestore.Constants.ModulePath;

public class BackupFileScanner {

    private static final String TAG = "BackupFileScanner";
    private Handler mHandler;
    private Context mContext;
    private Object object = new Object();
    private LinkedHashSet<BackupsHandler> scanTaskHandlers = new LinkedHashSet<BackupsHandler>();

    public BackupFileScanner(Context context, Handler handler) {
        mHandler = handler;
        mContext = context;
        scanTaskHandlers.clear();
        if (mHandler == null) {
           Log.e(TAG, "constuctor maybe failed!cause mHandler is null");
        }
    }
    
    public boolean addScanHandler(BackupsHandler backupsHandler){
    	return scanTaskHandlers.add(backupsHandler);
    }
    
    public void setHandler(Handler handler) {
        synchronized (object) {
            mHandler = handler;
        }
    }

    ScanThread mScanThread;

    public void startScan() {
        mScanThread = new ScanThread();
        mScanThread.start();
    }

    public void quitScan() {
        synchronized (object) {
            if (mScanThread != null) {
                mScanThread.cancel();
                mScanThread = null;
                Log.d(TAG,  "quitScan");
            }
        }
    }
    
    public boolean isRunning(){
    	if (mScanThread != null) {
    		return mScanThread.isCanceled;
    	}
    	return false;
    }

    private class ScanThread extends Thread {
        boolean isCanceled = false;

        public void cancel() {
            isCanceled = true;
        }

        private File[] filterFile(File[] fileList) {
            if (fileList == null) {
                return null;
            }
            List<File> list = new ArrayList<File>();
            for (File file : fileList) {
                if (isCanceled) {
                    break;
                }

                if (!FileUtils.isEmptyFolder(file)) {
                	Log.d(TAG, "filterFile.filepath:" + file.getAbsolutePath());
                    list.add(file);
                }
            }
            if (isCanceled) {
                return null;
            } else {
                return (File[]) list.toArray(new File[0]);
            }
        }

        private File[] scanBackupFiles(String path) {
            if (path != null && !isCanceled) {
                return filterFile(new File(path).listFiles());
            } else {
                return null;
            }
        }
        
        private File[] scanPersonalBackupFiles(){
            String path = SDCardUtils.getPersonalDataBackupPath(mContext);
            Log.d(TAG, "scanPersonalBackupFiles.path:" + path);
            return scanBackupFiles(path);
        }

        @Override
		public void run() {
        	isCanceled = false;
			if (scanTaskHandlers != null && !scanTaskHandlers.isEmpty()) {
				Log.d(TAG, "run.detectSDcardForOtherBackups");
				detectSDcardForOtherBackups();
			} else {
				Log.d(TAG, "run.initDataTransferBackupForList");
				initDataTransferBackupForList();
			}
		}

		private void detectSDcardForOtherBackups() {
			int notifyType = NotifyManager.FP_NEW_DETECTION_NOTIFY_TYPE_DEAFAULT;
			List<File> result = new ArrayList<File>();
			for (BackupsHandler backupsHandler : scanTaskHandlers) {
				
				if (isCanceled) {
					backupsHandler.cancel();
					continue;
				}
				
				backupsHandler.reset();
				
				if (!backupsHandler.init(mContext)) {
					continue;
				}
				
				backupsHandler.onStart();
				
				List<File> oneResult = backupsHandler.onEnd();
				if(oneResult!=null){
					result.addAll(oneResult);
				}
				
				String backupType = backupsHandler.getBackupType();
				if (backupType.equals(Constants.BackupScanType.DATATRANSFER)) {
					Log.d(TAG,  "Type is DATATRANSFER");
					notifyType = isOtherBackup();
				}
			}
			String path = null;
			if(!result.isEmpty()){
				List<String> newDetectList = checkMD5(result);
					if(result!=null){
						try {
							path = makeOneBackupHistory(result);
							addMD5toPreference(newDetectList);
						} catch (Exception e) {
							Log.e(TAG, "we must do some rollback option here.");
							e.printStackTrace();
							if(path!=null&&!path.equals("")){
								FileUtils.deleteFileOrFolder(new File(path));
							}
							return;
						}
					}
			}
			if (notifyType == NotifyManager.FP_NEW_DETECTION_NOTIFY_TYPE_LIST||!result.isEmpty()) {
				Log.e(TAG, "notifyType = " + notifyType+"  path = " +path);
				NotifyManager.getInstance(mContext).showNewDetectionNotification(notifyType,path);
			}
			isCanceled = (isCanceled ? !isCanceled : isCanceled);
		}

		private int isOtherBackup() {
			File[] files = scanPersonalBackupFiles();
			Log.d(TAG, "isOtherBackup.fileSize=" + files.length);
			BackupFilePreview temPreview ;
			if (files != null && files.length > 0) {
				for(File file : files){
					temPreview = new BackupFilePreview(file);
					Log.d(TAG,  "temPreview.isOtherDeviceBackup() = "+temPreview.isOtherDeviceBackup());
					/*there are two kind cases need to notify list
					 *1.Found other Phone's backup on SDCard
					 *2.When (boot reset or clear application data) && some non-self backup history exist
					 */
					if(temPreview.isOtherDeviceBackup()){
						return NotifyManager.FP_NEW_DETECTION_NOTIFY_TYPE_LIST;
					}else if((isBootReset(false)&&!temPreview.isSelfBackup())){
						Log.d(TAG,  "Find other Phone's backup on SDCard && boot has been reseted "+ !temPreview.isSelfBackup());
						isBootReset(true);
						return NotifyManager.FP_NEW_DETECTION_NOTIFY_TYPE_LIST;
					}
				}
			}
			return NotifyManager.FP_NEW_DETECTION_NOTIFY_TYPE_DEAFAULT;
		}

		/**
		 * find the tag file in data,it'll be delete when phone reset.
		 * @return true is reset
		 */
		private boolean isBootReset(boolean insert) {
			// TODO Auto-generated method stub
			SharedPreferences sp = mContext.getSharedPreferences("boot_reset_flag", Context.MODE_PRIVATE);
			if(insert){
				sp.edit().putBoolean("boot_reset", false).commit();
			}
			if(sp.getBoolean("boot_reset", true)){
				return true;
			}
			return false;
		}

		private void addMD5toPreference(List<String> newDetectList) {
			// TODO Auto-generated method stub
			SharedPreferences preferences = mContext.getSharedPreferences("md5_info", Context.MODE_PRIVATE);;
			Editor editor = preferences.edit();
			for(String md5 : newDetectList){
				editor.putInt(md5, Constants.BackupScanType.MD5_EXIST);
			}
			editor.commit();
		}

		private String makeOneBackupHistory(List<File> result)  throws IOException{
			// TODO Auto-generated method stub
			File newestFile = FileUtils.getNewestFile(result);
			if(newestFile==null){
				return null;
			}
			long historySecond = newestFile.lastModified();
			SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
			String historyName = format.format(new Date(historySecond));
			String path = SDCardUtils.getPersonalDataBackupPath(mContext)+File.separator+historyName;
			File destFile =  new File(path);
			Log.d(TAG,  "makeOneBackupHistory <=====>path  " +path);
			if(destFile!=null&&destFile.exists()&&destFile.isDirectory()&&!FileUtils.isEmptyFolder(destFile)){
				Log.d(TAG,  "makeOneBackupHistory <=====>file has been exist ");
				return path;
			}
			destFile = FileUtils.createFile(path);			
			File contactDestFile = FileUtils.createFile(path+File.separator+Constants.ModulePath.FOLDER_CONTACT);
			File mmsDestFile = FileUtils.createFile(path+File.separator+Constants.ModulePath.FOLDER_MMS);
			File smsDestFile = FileUtils.createFile(path+File.separator+Constants.ModulePath.FOLDER_SMS);
			for(File file : result){
				String name = file.getName();
				if(name.endsWith(".vcf")){		//find it is a contact file
					org.apache.commons.io.FileUtils.copyFileToDirectory(file,contactDestFile );
					Log.d(TAG,  "makeOneBackupHistory <=====>file = " + file.getName()+ "TO: "+destFile.getName());
				}else if(name.endsWith(".s")||name.endsWith(".m")){
					org.apache.commons.io.FileUtils.copyFileToDirectory(file,mmsDestFile );
				}else if(name.equals("sms.vmsg")){
					org.apache.commons.io.FileUtils.copyFileToDirectory(file,smsDestFile );
				}
			}
			
			FileUtils.combineFiles(Arrays.asList(contactDestFile.listFiles()),contactDestFile.getAbsolutePath()+File.separator + Constants.ModulePath.NAME_CONTACT);
			for(File file : contactDestFile.listFiles()){
				if(file.getName().equals(Constants.ModulePath.NAME_CONTACT)){
					continue;
				}
				FileUtils.deleteFileOrFolder(file);
			}
			//add MMS record xml file
			if (mmsDestFile != null && mmsDestFile.isDirectory()
					&& mmsDestFile.listFiles().length > 0){
				MmsXmlComposer composer = new MmsXmlComposer();
				composer.startCompose();
				for(File file : mmsDestFile.listFiles()){
					MmsXmlInfo record = new MmsXmlInfo();
					record.setID(file.getName());
					record.setIsRead("1");
					record.setMsgBox(file.getName().endsWith(".m")?"1":"2");
					record.setDate(""+file.lastModified());
					record.setSize(""+file.length());
					record.setSimId("0");
					record.setIsLocked("0");
					composer.addOneMmsRecord(record);
				}
				composer.endCompose();
				String xmlInfoString = composer.getXmlInfo();
				FileUtils.writeToFile(path+File.separator + ModulePath.FOLDER_MMS + File.separator + ModulePath.MMS_XML,xmlInfoString.getBytes());
			}
			
			makeRootRecordXml(historySecond, path, destFile);
			FileUtils.deleteEmptyFolder(destFile);
			return path;
		}

		

		private void makeRootRecordXml(long historySecond, String path,
				File destFile) {
			RecordXmlInfo backupInfo = new RecordXmlInfo();
            backupInfo.setRestore(false);
            backupInfo.setDevice(Utils.getPhoneSearialNumber());
            backupInfo.setTime(""+historySecond);
            RecordXmlComposer xmlCompopser = new RecordXmlComposer();
            xmlCompopser.startCompose();
            xmlCompopser.addOneRecord(backupInfo);
            xmlCompopser.endCompose();
            if (destFile != null && destFile.canWrite()) {
                Utils.writeToFile(xmlCompopser.getXmlInfo(), path + File.separator
                        + Constants.RECORD_XML);
            }
		}

		private List<String> checkMD5(List<File> result) {
			SharedPreferences preferences = mContext.getSharedPreferences("md5_info", Context.MODE_PRIVATE);;
			List<String> newDetectList = new ArrayList<String>();
			List<File> removeList = new ArrayList<File>();
			for(File file : result){
				if(file==null){
					Log.e(TAG, "[checkMD5] file is null ,continue!");
					continue;
				}
				String md5 = FileUtils.getFileMD5(file.getAbsolutePath());
				int md5_flag = preferences.getInt(md5, Constants.BackupScanType.MD5_NOT_EXIST);
				if(md5_flag == Constants.BackupScanType.MD5_NOT_EXIST){
					newDetectList.add(md5);
				}else if(md5_flag == Constants.BackupScanType.MD5_EXIST){
					removeList.add(file);
				}
			}
			result.removeAll(removeList);
			return newDetectList;
		}

		private void initDataTransferBackupForList() {
			HashMap<String, List<BackupFilePreview>> result = getDataTransferBackups();
			synchronized (object) {
				if (!isCanceled && mHandler != null) {
					List<BackupFilePreview> items = result.get(Constants.SCAN_RESULT_KEY_PERSONAL_DATA);
					Log.d(TAG, "initDataTransferBackupForList.SCANNER_FINISH.size=" + items.size());
					Message msg = mHandler.obtainMessage(MessageID.SCANNER_FINISH, result);
					mHandler.sendMessage(msg);
				}
			}
			mScanThread = null;
		}
        
		private HashMap<String, List<BackupFilePreview>> getDataTransferBackups() {
			File[] files = scanPersonalBackupFiles();
			Log.d(TAG, "getDataTransferBackups.filesize=" + files.length);
            HashMap<String, List<BackupFilePreview>> result = new HashMap<String, List<BackupFilePreview>>();
            List<BackupFilePreview> backupItems = generateBackupFileItems(files);
            Log.d(TAG, "getDataTransferBackups.backupItems=" + backupItems.size());
            result.put(Constants.SCAN_RESULT_KEY_PERSONAL_DATA, backupItems);
            //delete by yuri,do not scan app
            /*String path = SDCardUtils.getAppsBackupPath(mContext);
            if(path == null || path.trim().equals("")){
            	return null;
            }
            File appFolderFile = new File(path);
            backupItems = new ArrayList<BackupFilePreview>();
            BackupFilePreview appBackupFile = null;
            if(appFolderFile.exists()){
                appBackupFile = new BackupFilePreview(appFolderFile);
                backupItems.add(appBackupFile);
            }
            result.put(Constants.SCAN_RESULT_KEY_APP_DATA, backupItems);*/
			return result;
		}

        private List<BackupFilePreview> generateBackupFileItems(File[] files) {
            if (files == null || isCanceled) {
                return null;
            }
            List<BackupFilePreview> list = new ArrayList<BackupFilePreview>();
            for (File file : files) {
                if (isCanceled) {
                    break;
                }
                BackupFilePreview backupFile = new BackupFilePreview(file);
                Log.d(TAG, "====generateBackupFileItems.begin====");
                Log.d(TAG, "backupFile.name=" + backupFile.getFileName());
                Log.d(TAG, "backupFile.size=" + backupFile.getFileSize());
                Log.d(TAG, "====generateBackupFileItems.end====");
                if (backupFile != null) {
                	if(backupFile.getFileSize()>1024*2){
                		list.add(backupFile);
                	}else{
                		if(backupFile.getBackupModules(mContext)>0){
                			list.add(backupFile);
                		}
                	}
                }
            }
            if (!isCanceled) {
                sort(list);
                return list;
            } else {
                return null;
            }
        }

        private void sort(List<BackupFilePreview> list) {
            Collections.sort(list, new Comparator<BackupFilePreview>() {
                public int compare(BackupFilePreview object1, BackupFilePreview object2) {
                    String dateLeft = object1.getBackupTime();
                    String dateRight = object2.getBackupTime();
                    if (dateLeft != null && dateRight != null) {
                        return dateRight.compareTo(dateLeft);
                    } else {
                        return 0;
                    }
                }
            });
        }
    }

}
