package com.zhaoyan.juyou.backuprestore;

import android.content.Context;

import java.util.List;

import com.zhaoyan.common.util.Log;

public abstract class Composer {
    private static final String TAG = "Composer";

    protected Context mContext;
    protected ProgressReporter mReporter;
    // protected BackupZip mZipHandler;
    // protected String mZipFileName;
    protected boolean mIsCancel = false;
    private int mComposeredCount = 0;
    protected String mParentFolderPath;
    protected List<String> mParams;

    public Composer(Context context) {
        mContext = context;
    }

    // public void setZipHandler(BackupZip handler) {
    // mZipHandler = handler;
    // }

    public void setParentFolderPath(String path) {
        mParentFolderPath = path;
    }

    // public void setZipFileName(String fileName) {
    // mZipFileName = fileName;
    // }

    public void setReporter(ProgressReporter reporter) {
        mReporter = reporter;
    }

    synchronized public void setCancel(boolean cancel) {
        mIsCancel = cancel;
    }

    synchronized public boolean isCancel() {
        return mIsCancel;
    }

    public int getComposed() {
        return mComposeredCount;
    }

    public void increaseComposed(boolean result) {
        if (result) {
            ++mComposeredCount;
        }

        if (mReporter != null) {
            mReporter.onOneFinished(this, result);
        }
    }

    public void onStart() {
        if (mReporter != null) {
            mReporter.onStart(this);
        }
    }

    public void onEnd() {
        if (mReporter != null) {
            boolean bResult = (getCount() == mComposeredCount && mComposeredCount > 0);
            mReporter.onEnd(this, bResult);
            Log.d(TAG, "onEnd: result is " + bResult);
            Log.d(TAG, "onEnd: getCount is " + getCount()
                    + ", and composed count is " + mComposeredCount);
        }
    }

    public void setParams(List<String> params) {
        mParams = params;
    }

    public boolean composeOneEntity() {
        boolean result = implementComposeOneEntity();
        if (result) {
            ++mComposeredCount;
        }

        if (mReporter != null) {
            mReporter.onOneFinished(this, result);
        }
        return result;
    }

    abstract public int getModuleType();

    abstract public int getCount();

    abstract public boolean isAfterLast();

    abstract public boolean init();

    abstract protected boolean implementComposeOneEntity();
}
