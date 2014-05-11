package com.zhaoyan.juyou.backuprestore;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.res.AssetManager;
import android.content.res.Resources;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.zhaoyan.common.util.Log;
import com.zhaoyan.juyou.backuprestore.Constants.ModulePath;

public class AppBackupComposer extends Composer {
    private static final String TAG = "AppBackupComposer";

    private List<ApplicationInfo> mUserAppInfoList = null;
    private int mAppIndex = 0;

    public AppBackupComposer(final Context context) {
        super(context);
    }

    @Override
    public final int getModuleType() {
        return ModuleType.TYPE_APP;
    }

    @Override
    public final int getCount() {
        int count = 0;
        if (mUserAppInfoList != null && mUserAppInfoList.size() > 0) {
            count = mUserAppInfoList.size();
        }

        Log.d(TAG,  "getCount():" + count);
        return count;
    }

    @Override
    public final boolean init() {
        boolean result = false;

        if(mParams != null) {
            List<ApplicationInfo> tmpList = getUserAppInfoList(mContext);
            HashMap tmpMap = new HashMap<String, ApplicationInfo>();
            if(tmpList != null) {
                for(ApplicationInfo appInfo : tmpList) {
                    tmpMap.put(appInfo.packageName, appInfo);
                }
            }

            mUserAppInfoList = new ArrayList<ApplicationInfo>();
            for(int i = 0; i < mParams.size(); ++i) {
                ApplicationInfo appInfo = (ApplicationInfo)tmpMap.get(mParams.get(i));
                if(appInfo != null) {
                    mUserAppInfoList.add(appInfo);
                }
            }

            result = true;
            mAppIndex = 0;
        }

        Log.d(TAG,  "init():" + result);
        return result;
    }

    @Override
    public final boolean isAfterLast() {
        boolean result = true;
        if (mUserAppInfoList != null && mAppIndex < mUserAppInfoList.size()) {
            result = false;
        }

        Log.d(TAG,  "isAfterLast():" + result);
        return result;
    }

    @Override
    public final boolean implementComposeOneEntity() {
        boolean result = false;
        if (mUserAppInfoList != null && mAppIndex < mUserAppInfoList.size()) {
            ApplicationInfo appInfo = mUserAppInfoList.get(mAppIndex);
            String appSrc = appInfo.publicSourceDir;
            String appDest = mParentFolderPath + File.separator + appInfo.packageName + ModulePath.FILE_EXT_APP;
            CharSequence tmpLable = "";
            if (appInfo.uid == -1) {
                tmpLable = getApkFileLabel(mContext, appInfo.sourceDir, appInfo);
            } else {
                tmpLable = appInfo.loadLabel(mContext.getPackageManager());
            }
            String label = (tmpLable == null) ? appInfo.packageName : tmpLable.toString();
            Log.d(TAG, 
                    mAppIndex + ":" + appSrc + ",pacageName:" + appInfo.packageName
                            + ",sourceDir:" + appInfo.sourceDir + ",dataDir:" + appInfo.dataDir
                            + ",lable:" + label);

            try {
                copyFile(appSrc, appDest);
                //mZipHandler.addFileByFileName(appSrc, appDest);
                // mZipHandler.addFolder(
                //         appInfo.dataDir,
                //         ModulePath.FOLDER_APP
                //                 + File.separator
                //                 + appInfo.dataDir.subSequence(
                //                         appInfo.dataDir.lastIndexOf(File.separator) + 1,
                //                         appInfo.dataDir.length()).toString());

                Log.d(TAG,  "addFile " + appSrc + "success");
                result = true;
            } catch (IOException e) {
                if (super.mReporter != null) {
                    super.mReporter.onErr(e);
                }
                Log.d(TAG,  "addFile:" + appSrc + "fail");
                e.printStackTrace();
            }

            ++mAppIndex;
        }

        return result;
    }

    public static List<ApplicationInfo> getUserAppInfoList(final Context context) {
        List<ApplicationInfo> userAppInfoList = null;
        if (context != null) {
            List<ApplicationInfo> allAppInfoList = context.getPackageManager()
                    .getInstalledApplications(0);
            userAppInfoList = new ArrayList<ApplicationInfo>();
            for (ApplicationInfo appInfo : allAppInfoList) {
                if (!((appInfo.flags & ApplicationInfo.FLAG_SYSTEM) == ApplicationInfo.FLAG_SYSTEM)
                        && !appInfo.packageName.equalsIgnoreCase(context.getPackageName())) {
                    userAppInfoList.add(appInfo);
                }
            }
        }
        return userAppInfoList;
    }

    private CharSequence getApkFileLabel(final Context context, final String apkPath,
            final ApplicationInfo appInfo) {
        if (context == null || appInfo == null || apkPath == null || !(new File(apkPath).exists())) {
            return null;
        }

        Resources contextResources = mContext.getResources();
        //modify by yuri
        AssetManager assetManager = context.getAssets();
//        assetManager.
//        assetManager.addAssetPath(apkPath);

        Resources resources = new Resources(assetManager, contextResources.getDisplayMetrics(),
                contextResources.getConfiguration());

        CharSequence label = null;
        if (0 != appInfo.labelRes) {
            label = (String) resources.getText(appInfo.labelRes);
        }

        return label;
    }

    @Override
    public final void onEnd() {
        super.onEnd();
        if (mUserAppInfoList != null) {
            mUserAppInfoList.clear();
        }
        Log.d(TAG,  "onEnd()");
    }

    private void copyFile(String srcFile, String destFile) throws IOException {
		try {
			File f1 = new File(srcFile);
			if (f1.exists() && f1.isFile()) {
				InputStream inStream = new FileInputStream(srcFile);
				FileOutputStream outStream = new FileOutputStream(destFile);
				byte[] buf = new byte[1024];
                int byteRead = 0;
				while ((byteRead = inStream.read(buf)) != -1) {
					outStream.write(buf, 0, byteRead);
				}
				outStream.flush();
				outStream.close();
				inStream.close();
			}
        } catch(IOException e) {
            throw e;
		} catch (Exception e) {
            e.printStackTrace();
		}
	}

    /**
     * Describe <code>onStart</code> method here.
     *
     */
    public final void onStart() {
        super.onStart();
        if(getCount() > 0) {
            File path = new File(mParentFolderPath);
            if(!path.exists()) {
                path.mkdirs();
            }
        }
    }

}