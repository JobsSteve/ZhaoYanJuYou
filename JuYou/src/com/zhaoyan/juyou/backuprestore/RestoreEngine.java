package com.zhaoyan.juyou.backuprestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.zhaoyan.common.util.Log;

import android.content.Context;

public class RestoreEngine {
    private static final String TAG = "RestoreEngine";

    public interface OnRestoreDoneListner {
        public void onFinishRestore(boolean bSuccess);
    }

    private Context mContext;
    private String mRestoreFolder;
    private OnRestoreDoneListner mRestoreDoneListner;
    private boolean mIsRunning = false;
    private boolean mIsPause = false;
    private Object mLock = new Object();
    private ProgressReporter mReporter;
    private List<Composer> mComposerList;
    private static RestoreEngine sSelfInstance;

    public static synchronized RestoreEngine getInstance(final Context context,
            final ProgressReporter reporter) {
        if (sSelfInstance == null) {
            sSelfInstance = new RestoreEngine(context, reporter);
        } else {
            sSelfInstance.updateInfo(context, reporter);
        }
        return sSelfInstance;
    }

    private RestoreEngine(Context context, ProgressReporter reporter) {
        mContext = context;
        mReporter = reporter;
        mComposerList = new ArrayList<Composer>();
    }

    private final void updateInfo(final Context context, final ProgressReporter reporter) {
        mContext = context;
        mReporter = reporter;
    }

    public boolean isRunning() {
        return mIsRunning;
    }

    public void pause() {
        mIsPause = true;
    }

    public boolean isPaused() {
        return mIsPause;
    }

    public void continueRestore() {/*
        if (mIsPause) {
            synchronized (mLock) {
                mIsPause = false;
                mLock.notify();
            }
        }
    */}

    public void cancel() {/*
    	MyLogger.logE(CLASS_TAG, "cancel");
        if (mComposerList != null && mComposerList.size() > 0) {
            for (Composer composer : mComposerList) {
                composer.setCancel(true);
            }
            continueRestore();
        }
    */}

    public void setOnRestoreEndListner(OnRestoreDoneListner restoreEndListner) {
        mRestoreDoneListner = restoreEndListner;
    }

    ArrayList<Integer> mModuleList;

    public void setRestoreModelList(ArrayList<Integer> moduleList) {
        reset();
        mModuleList = moduleList;
    }

    HashMap<Integer, ArrayList<String>> mParasMap = new HashMap<Integer, ArrayList<String>>();

    public void setRestoreItemParam(int itemType, ArrayList<String> paraList) {
        mParasMap.put(itemType, paraList);
    }

    public void startRestore(final String path) {
        if (path != null && mModuleList.size() > 0) {
            mRestoreFolder = path;
            setupComposer(mModuleList);
            Utils.isRestoring = mIsRunning = true;
            new RestoreThread().start();
        }
    }

    public void startRestore(String path, List<Integer> list) {
        reset();
        if (path != null && list.size() > 0) {
            mRestoreFolder = path;
            setupComposer(list);
            Utils.isRestoring = mIsRunning = true;
            new RestoreThread().start();
        }
    }

    private void addComposer(Composer composer) {
        if (composer != null) {
            int type = composer.getModuleType();
            ArrayList<String> params = mParasMap.get(type);
            if (params != null) {
                composer.setParams(params);
            }
            composer.setReporter(mReporter);
            composer.setParentFolderPath(mRestoreFolder);
            mComposerList.add(composer);
        }
    }

    private void reset() {
        if (mComposerList != null) {
            mComposerList.clear();
        }
        mIsPause = false;
    }

    private boolean setupComposer(List<Integer> list) {
        boolean bSuccess = true;
        for (int type : list) {
            switch (type) {
            case ModuleType.TYPE_CONTACT:
                addComposer(new ContactRestoreComposer(mContext));
                break;

            case ModuleType.TYPE_MESSAGE:
                addComposer(new MessageRestoreComposer(mContext));
                break;

            case ModuleType.TYPE_SMS:
                addComposer(new SmsRestoreComposer(mContext));
                break;

            case ModuleType.TYPE_MMS:
//                addComposer(new MmsRestoreComposer(mContext));
                break;

            case ModuleType.TYPE_PICTURE:
                addComposer(new PictureRestoreComposer(mContext));
                break;

            case ModuleType.TYPE_CALENDAR:
//                addComposer(new CalendarRestoreComposer(mContext));
                break;

            case ModuleType.TYPE_APP:
                addComposer(new AppRestoreComposer(mContext));
                break;

            case ModuleType.TYPE_MUSIC:
                addComposer(new MusicRestoreComposer(mContext));
                break;

            default:
                bSuccess = false;
                break;
            }
        }

        return bSuccess;
    }

    public class RestoreThread extends Thread {
        @Override
        public void run() {
            Log.d(TAG,  "RestoreThread begin...");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            for (Composer composer : mComposerList) {
                Log.d(TAG,  "RestoreThread composer:" + composer.getModuleType()
                        + " start..");
                Log.d(TAG,  "begin restore:" + System.currentTimeMillis());
                if (!composer.isCancel()) {
                    if(!composer.init()){
                	composer.onEnd();
                	continue;
                    }
                    Log.d(TAG,  "RestoreThread composer:" + composer.getModuleType()
                            + " init finish");
                    composer.onStart();
                    while (!composer.isAfterLast() && !composer.isCancel()) {
                        if (mIsPause) {
                            synchronized (mLock) {
                                try {
                                    Log.d(TAG,  "RestoreThread wait...");
                                    mLock.wait();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        composer.composeOneEntity();
                        Log.d(TAG, 
                                "RestoreThread composer:" + composer.getModuleType()
                                        + "composer one entiry");
                    }
                }

                try {
                    sleep(Constants.TIME_SLEEP_WHEN_COMPOSE_ONE);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                composer.onEnd();
                Log.d(TAG,  "end restore:" + System.currentTimeMillis());
                Log.d(TAG,  "RestoreThread composer:" + composer.getModuleType()
                        + " composer finish");
            }

            Log.d(TAG,  "RestoreThread run finish");
            Utils.isRestoring = mIsRunning = false;

            if (mRestoreDoneListner != null) {
                mRestoreDoneListner.onFinishRestore(true);
            }
        }
    }
}
