package com.zhaoyan.juyou.backuprestore;

import java.io.File;
import java.util.List;

import android.content.Context;

import com.zhaoyan.common.util.Log;

public class AppRestoreComposer extends Composer {
    private static final String TAG = "AppRestoreComposer";
    private int mIndex;
    private List<String> mFileNameList;
    private Object mLock = new Object();

    public AppRestoreComposer(Context context) {
        super(context);
    }

    public int getModuleType() {
        return ModuleType.TYPE_APP;
    }

    public int getCount() {
        int count = 0;
        if (mFileNameList != null) {
            count = mFileNameList.size();
        }
        Log.d(TAG,  "getCount():" + count);
        return count;
    }

    public boolean init() {
        boolean result = false;
        if(mParams != null) {
            mFileNameList = mParams;
            result = true;
        }

        Log.d(TAG,  "init():" + result + ", count:" + getCount());
        return result;
    }

    public boolean isAfterLast() {
        boolean result = true;
        if (mFileNameList != null) {
            result = (mIndex >= mFileNameList.size());
        }

        Log.d(TAG,  "isAfterLast():" + result);
        return result;
    }

    public boolean implementComposeOneEntity() {
        boolean result = false;
        if(mFileNameList != null && mIndex < mFileNameList.size()){
            try {
                String apkFileName = mFileNameList.get(mIndex++);
                File apkFile = new File(apkFileName);
                if (apkFile != null && apkFile.exists()) {
//                    PackageManager packageManager = mContext.getPackageManager();
                    result = true;
//                    PackageInstallObserver installObserver = new PackageInstallObserver();

//                    packageManager.installPackage(Uri.fromFile(apkFile), installObserver,
//                                                  PackageManager.INSTALL_REPLACE_EXISTING, "test");

                    synchronized (mLock) {
//                        while (!installObserver.finished) {
//                            try {
//                                mLock.wait();
//                            } catch (InterruptedException e) {
//                            }
//                        }

//                        if (installObserver.result == PackageManager.INSTALL_SUCCEEDED) {
//                            result = true;
//                            Log.d(TAG,  "install success");
//                        } else {
//                            Log.d(TAG,  "install fail, result:" + installObserver.result);
//                        }
                    }
                } else {
                    Log.d(TAG,  "install failed");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return result;
    }

    // private void delteTempFolder() {
    //     String folder = SDCardUtils.getStoragePath() + File.separator + ModulePath.FOLDER_TEMP;
    //     File file = new File(folder);
    //     if (file.exists() && file.isDirectory()) {
    //         File files[] = file.listFiles();
    //         for (File f : files) {
    //             if (f.getName().matches(ModulePath.ALL_APK_FILES)) {
    //                 f.delete();
    //             }
    //         }
    //         file.delete();
    //     }
    // }

    public void onStart() {
        super.onStart();
        //delteTempFolder();
    }

    public void onEnd() {
        super.onEnd();
        if (mFileNameList != null) {
            mFileNameList.clear();
        }
        //delteTempFolder();
        Log.d(TAG,  "onEnd()");
    }

//    class PackageInstallObserver extends IPackageInstallObserver.Stub {
//        private boolean finished = false;
//        private int result;
//
//        @Override
//        public void packageInstalled(String name, int status) {
//            synchronized (mLock) {
//                finished = true;
//                result = status;
//                mLock.notifyAll();
//            }
//        }
//    }
    
}
