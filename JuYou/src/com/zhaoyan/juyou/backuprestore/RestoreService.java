package com.zhaoyan.juyou.backuprestore;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import com.zhaoyan.common.util.Log;
import com.zhaoyan.juyou.backuprestore.Constants.State;
import com.zhaoyan.juyou.backuprestore.RestoreEngine.OnRestoreDoneListner;
import com.zhaoyan.juyou.backuprestore.ResultDialog.ResultEntity;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;


public class RestoreService extends Service implements ProgressReporter, OnRestoreDoneListner {
    private static final String TAG = "RestoreService";

    public interface OnRestoreStatusListener {
        public void onComposerChanged(final int type, final int num);

        public void onProgressChanged(Composer composer, int progress);

        public void onRestoreEnd(boolean bSuccess, ArrayList<ResultEntity> resultRecord);

        public void onRestoreErr(IOException e);
    }

    public static class RestoreProgress {
        int mType;
        int mMax;
        int mCurNum;
    }

    private RestoreBinder mBinder = new RestoreBinder();
    private int mState;
    private RestoreEngine mRestoreEngine;
    private ArrayList<ResultEntity> mResultList;
    private RestoreProgress mCurrentProgress = new RestoreProgress();
    private OnRestoreStatusListener mRestoreStatusListener;
    private ArrayList<ResultEntity>mAppResultList;
    HashMap<Integer, ArrayList<String>> mParasMap = new HashMap<Integer, ArrayList<String>>();

    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "onbind");
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        super.onUnbind(intent);
        Log.i(TAG, "onUnbind");
        Log.d(TAG, "mResotreENigne:" + mRestoreEngine);
        return true;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        moveToState(State.INIT);
        Log.i(TAG, "onCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        Log.i(TAG, "onStartCommand");
        return START_STICKY_COMPATIBILITY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForeground(true);
        Log.i(TAG, "onDestroy.mRestorEninge:" + mRestoreEngine);
        if (mRestoreEngine != null) {
			Log.e(TAG, mRestoreEngine.isRunning() + "," + mRestoreEngine.isPaused());
		} else {
			Log.d(TAG, "onDestroy.mResotrENigne is null");
		}
        
        if (mRestoreEngine != null && mRestoreEngine.isRunning()) {
            mRestoreEngine.setOnRestoreEndListner(null);
            Log.e(TAG, "===========onDestroy.........=========");
            mRestoreEngine.cancel();
        }
    }

    public void moveToState(int state) {
        synchronized (this) {
            mState = state;
        }
    }

    private int getRestoreState() {
        synchronized (this) {
            return mState;
        }
    }

    public class RestoreBinder extends Binder {
        public int getState() {
            return getRestoreState();
        }
        
        public void setRestoreModelList(ArrayList<Integer> list){
            reset();
            if (mRestoreEngine == null) {
                mRestoreEngine = RestoreEngine
                .getInstance(RestoreService.this, RestoreService.this);
            }
            mRestoreEngine.setRestoreModelList(list);
        }
        
        public void setRestoreItemParam(int itemType, ArrayList<String> paraList){
            mParasMap.put(itemType, paraList);
            mRestoreEngine.setRestoreItemParam(itemType, paraList);
        }
        
        public ArrayList<String> getRestoreItemParam(int itemType){
            return mParasMap.get(itemType);
        }
        
        public boolean startRestore(String fileName) {
        	Log.d(TAG, "startRestore");
        	startForeground(1,new Notification());
            if (mRestoreEngine == null) {
                Log.e(TAG, "startRestore Error: engine is not initialed");
                return false;
            }
            Log.d(TAG, "startRestore,mResortEngine:" + mRestoreEngine);
            mRestoreEngine.setOnRestoreEndListner(RestoreService.this);
            mRestoreEngine.startRestore(fileName);
            moveToState(State.RUNNING);
            return true;
        }

        public void pauseRestore() {
            moveToState(State.PAUSE);
            if (mRestoreEngine != null) {
                mRestoreEngine.pause();
            }
            Log.d(TAG,  "pauseRestore");
        }

        public void continueRestore() {
            moveToState(State.RUNNING);
            if (mRestoreEngine != null) {
                mRestoreEngine.continueRestore();
            }
            Log.d(TAG,  "continueRestore");
        }

        public void cancelRestore() {
        	 Log.d(TAG,  "cancelRestore");
            moveToState(State.CANCELLING);
            if (mRestoreEngine != null) {
                mRestoreEngine.cancel();
            } else {
				Log.e(TAG, "NNNNNNNNNNNNNNNNNNNNNNN");
			}
        }

        public void reset() {
            moveToState(State.INIT);
            if (mResultList != null) {
                mResultList.clear();
            }
            if(mAppResultList != null){
                mAppResultList.clear();
            }
            if(mParasMap != null){
                mParasMap.clear();
            }
        }

        public RestoreProgress getCurRestoreProgress() {
            return mCurrentProgress;
        }

        public void setOnRestoreChangedListner(OnRestoreStatusListener listener) {
            mRestoreStatusListener = listener;
        }

        public ArrayList<ResultEntity> getRestoreResult() {
            return mResultList;
        }
        
        public ArrayList<ResultEntity> getAppRestoreResult(){
            return mAppResultList;
        }
    }

    public void onStart(Composer iComposer) {
        mCurrentProgress.mType = iComposer.getModuleType();
        mCurrentProgress.mMax = iComposer.getCount();
        mCurrentProgress.mCurNum = 0;
        if (mRestoreStatusListener != null) {
            mRestoreStatusListener.onComposerChanged(mCurrentProgress.mType, mCurrentProgress.mMax);
        }
        
        if (mCurrentProgress.mMax != 0) {
            NotifyManager.getInstance(RestoreService.this).setMaxPercent(mCurrentProgress.mMax);
        }
    }

    public void onOneFinished(Composer composer, boolean result) {

        mCurrentProgress.mCurNum++;
        if(composer.getModuleType() == ModuleType.TYPE_APP){
            if(mAppResultList == null){
                mAppResultList = new ArrayList<ResultEntity>();
            }
            ResultEntity entity = new ResultEntity(ModuleType.TYPE_APP, 
                                    result ? ResultEntity.SUCCESS : ResultEntity.FAIL);
            entity.setKey(mParasMap.get(ModuleType.TYPE_APP).get(mCurrentProgress.mCurNum-1));
            mAppResultList.add(entity);
        }
        if (mRestoreStatusListener != null) {
            mRestoreStatusListener.onProgressChanged(composer, mCurrentProgress.mCurNum);
        }
        
        if (mCurrentProgress.mMax != 0) {
            NotifyManager.getInstance(RestoreService.this).showRestoreNotification(
                    ModuleType.getModuleStringFromType(this, composer.getModuleType()),
                    composer.getModuleType(), mCurrentProgress.mCurNum);
        }
    }

    public void onEnd(Composer composer, boolean result) {

        if (mResultList == null) {
            mResultList = new ArrayList<ResultEntity>();
        }
        ResultEntity item = new ResultEntity(composer.getModuleType(), 
                    result ? ResultEntity.SUCCESS : ResultEntity.FAIL);
        mResultList.add(item);
    }

    public void onErr(IOException e) {

        if (mRestoreStatusListener != null) {
            mRestoreStatusListener.onRestoreErr(e);
        }
    }

    public void onFinishRestore(boolean bSuccess) {
        moveToState(State.FINISH);
        if (mRestoreStatusListener != null) {
            mRestoreStatusListener.onRestoreEnd(bSuccess, mResultList);
        }
//        NotifyManager.getInstance(RestoreService.this).clearNotification();
        NotifyManager.getInstance(this).showFinishNotification(NotifyManager.NOTIFY_RESTORING,true);
    }
}
