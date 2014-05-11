
package com.zhaoyan.juyou.backuprestore;

import android.content.Context;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.zhaoyan.common.util.Log;

public class BackupEngine {
	private static final String TAG = "BackupEngine";
    public interface OnBackupDoneListner {
        public void onFinishBackup(BackupResultType result);
    }

    public enum BackupResultType {
        Success, Fail, Error, Cancel
    }

    private Context mContext;
    private ProgressReporter mProgressReporter;
    private List<Composer> mComposerList;
    private OnBackupDoneListner mBackupDoneListner;
    private boolean mIsRunning = false;
    private boolean mIsPause = false;
    private boolean mIsCancel = false;
    private Object mLock = new Object();
    private String mBackupFolder;
    HashMap<Integer, ArrayList<String>> mParasMap = new HashMap<Integer, ArrayList<String>>();

    private static BackupEngine mSelfInstance;

    public static BackupEngine getInstance(final Context context, final ProgressReporter reporter) {
        if (mSelfInstance == null) {
            new BackupEngine(context, reporter);
        } else {
            mSelfInstance.updateInfo(context, reporter);
        }

        return mSelfInstance;
    }

    public BackupEngine(final Context context, final ProgressReporter reporter) {
        mContext = context;
        mProgressReporter = reporter;
        mComposerList = new ArrayList<Composer>();
        mSelfInstance = this;
    }

    ArrayList<Integer> mModuleList;

    public void setBackupModelList(ArrayList<Integer> moduleList) {
        reset();
        mModuleList = moduleList;
    }

    public void setBackupItemParam(int itemType, ArrayList<String> paraList) {
        mParasMap.put(itemType, paraList);
    }

    public boolean startBackup(final String folderName) {
        boolean startSuccess = true;
        mBackupFolder = folderName;
        Log.d(TAG, "startBackup():" + folderName);

        if (setupComposer(mModuleList)) {
        	Utils.isBackingUp = mIsRunning = true;
            new BackupThread().start();
        } else {
            startSuccess = false;
        }
        return startSuccess;
    }

    public final boolean isRunning() {
        return mIsRunning;
    }

    private final void updateInfo(final Context context, final ProgressReporter reporter) {
        mContext = context;
        mProgressReporter = reporter;
    }

    public final void pause() {
        mIsPause = true;
    }

    public final boolean isPaused() {
        return mIsPause;
    }

    public final void continueBackup() {
        if (mIsPause) {
            synchronized (mLock) {
                mIsPause = false;
                mLock.notify();
            }
        }
    }

    public final void cancel() {
        if (mComposerList != null && mComposerList.size() > 0) {
            for (Composer composer : mComposerList) {
                composer.setCancel(true);
            }
            mIsCancel = true;
            continueBackup();
        }
    }

    public final void setOnBackupDoneListner(final OnBackupDoneListner listner) {
        mBackupDoneListner = listner;
    }

    private void addComposer(final Composer composer) {
        if (composer != null) {
            int type = composer.getModuleType();
            ArrayList<String> params = mParasMap.get(type);
            if (params != null) {
                composer.setParams(params);
            }
            composer.setReporter(mProgressReporter);
            composer.setParentFolderPath(mBackupFolder);
            mComposerList.add(composer);
        }
    }

    private void reset() {
        if (mComposerList != null) {
            mComposerList.clear();
        }

        if (mParasMap != null) {
            mParasMap.clear();
        }

        mIsPause = false;
        mIsCancel = false;
    }

    private boolean setupComposer(final ArrayList<Integer> list) {
        Log.d(TAG, "setupComposer begin...");

        boolean result = true;
        File path = new File(mBackupFolder);
        if (!path.exists()) {
            result = path.mkdirs();
        }

        if (result) {
            Log.d(TAG, "create folder " + mBackupFolder
                    + " success");

            for (int type : list) {
                switch (type) {
                case ModuleType.TYPE_CONTACT:
                    addComposer(new ContactBackupComposer(mContext));
                    break;
//                case ModuleType.TYPE_SMS:
//                    addComposer(new SmsBackupComposer(mContext));
//                    break;
//
//                case ModuleType.TYPE_MMS:
//                    addComposer(new MmsBackupComposer(mContext));
//                    break;
                case ModuleType.TYPE_MESSAGE:
                    addComposer(new MessageBackupComposer(mContext));
                    break;
                case ModuleType.TYPE_PICTURE:
                    addComposer(new PictureBackupComposer(mContext));
                    break;

                case ModuleType.TYPE_MUSIC:
                    addComposer(new MusicBackupComposer(mContext));
                    break;
                case ModuleType.TYPE_APP:
                	addComposer(new AppBackupComposer(mContext));
                	break;
                default:
                    result = false;
                    break;
                }
            }

            Log.d(TAG, "setupComposer finish");
        } else {
            Log.e(TAG, "setupComposer failed");
            result = false;
        }
        return result;
    }

    private class BackupThread extends Thread {
        @Override
        public void run() {
            BackupResultType result = BackupResultType.Fail;

            Log.d(TAG, "BackupThread begin...");
            for (Composer composer : mComposerList) {
                Log.d(TAG, "BackupThread->composer:"
                        + composer.getModuleType() + " start...");
                if (!composer.isCancel()) {
                    composer.init();
                    composer.onStart();
                    Log.d(TAG, "BackupThread-> composer:"
                            + composer.getModuleType() + " init finish");
                    while (!composer.isAfterLast() && !composer.isCancel()) {
                        if (mIsPause) {
                            synchronized (mLock) {
                                try {
                                    Log.d(TAG,"BackupThread wait...");
                                    mLock.wait();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        if(!composer.isCancel()){
                        	composer.composeOneEntity();
                        	Log.d(TAG, "BackupThread->composer:"
                        			+ composer.getModuleType() + " compose one entiry");
                        }
                    }
                }

                try {
                    sleep(Constants.TIME_SLEEP_WHEN_COMPOSE_ONE);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                composer.onEnd();

                // generateModleXmlInfo(composer);

                Log.d(TAG, "BackupThread-> composer:"
                        + composer.getModuleType() + " finish");
            }

            Log.d(TAG, "BackupThread run finish, result:"
                    + result);
            Utils.isBackingUp = mIsRunning = false;
            if (mIsCancel) {
                result = BackupResultType.Cancel;
                if (!mModuleList.contains(ModuleType.TYPE_APP)) {
                    deleteFolder(new File(mBackupFolder));
                }
            } else {
                result = BackupResultType.Success;
            }

            if (mBackupDoneListner != null) {
                if (mIsPause) {
                    synchronized (mLock) {
                        try {
                            Log.d(TAG, "BackupThread wait before end...");
                            mLock.wait();
                            if(mIsCancel){
                            	result = BackupResultType.Cancel;
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                Log.d(TAG, "result:" + result);
                mBackupDoneListner.onFinishBackup(result);
            }
            
        }
    }

    // public void deleteFile() {
    // File file = new File(mBackupFolder);
    // FileUtils.deleteFileOrFolder(file);
    // Log.d(TAG, "deleteFile "
    // + mBackupFolder);
    // }

    private void deleteFolder(File file) {
        if (file.exists()) {
            if (file.isFile()) {
                file.delete();
            } else if (file.isDirectory()) {
                File files[] = file.listFiles();
                for (int i = 0; i < files.length; ++i) {
                    this.deleteFolder(files[i]);
                }
            }

            file.delete();
        }
    }

}
