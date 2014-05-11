
package com.zhaoyan.juyou.backuprestore;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore.Images;
import android.provider.MediaStore;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import com.zhaoyan.common.util.Log;
import com.zhaoyan.juyou.backuprestore.Constants.ModulePath;

/**
 * Describe class <code>PictureRestoreComposer</code> here.
 *
 * @author 
 * @version 1.0
 */
public class PictureRestoreComposer extends Composer {
    private static final String TAG = "PictureRestoreComposer";
    //private static final String mStoragepath = "/mnt/sdcard2/.backup/Data/picture";
    private int mIndex;
    private File[] mFileList;
    private ArrayList<String> mExistFileList = null;
    private static final String[] mProjection = new String[] { MediaStore.Images.Media._ID,
                                                               MediaStore.Images.Media.DATA };

    /**
     * Creates a new <code>PictureRestoreComposer</code> instance.
     *
     * @param context a <code>Context</code> value
     */
    public PictureRestoreComposer(Context context) {
        super(context);
    }

    public int getModuleType() {
        return ModuleType.TYPE_PICTURE;
    }

    public int getCount() {
        int count = 0;
        if (mFileList != null) {
            count = mFileList.length;
        }

        Log.d(TAG, "getCount():" + count);
        return count;
    }

    public boolean init() {
        boolean result = false;
        String path = mParentFolderPath + File.separator + ModulePath.FOLDER_PICTURE;
        File folder = new File(path);
        if(folder.exists() && folder.isDirectory()) {
            mFileList = folder.listFiles();
            mExistFileList = getExistFileList(path);
            result = true;
        }

        Log.d(TAG, "init():" + result + ",count:" + getCount());
        return result;
    }

    public boolean isAfterLast() {
        boolean result = true;
        if (mFileList != null) {
            result = (mIndex >= mFileList.length) ? true : false;
        }

        Log.d(TAG, "isAfterLast():" + result);
        return result;
    }

    public boolean implementComposeOneEntity() {
        boolean result = false;
        if (mFileList != null) {
            File file = mFileList[mIndex++];
            if(!mExistFileList.contains(file.getAbsolutePath())) {
                ContentValues v = new ContentValues();
                v.put(Images.Media.SIZE, file.length());
                v.put(Images.Media.DATA, file.getAbsolutePath());
                Uri tmpUri = mContext.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, v);
                Log.d(TAG, "tmpUri:" + tmpUri + "," + file.getAbsolutePath());
            } else {
                Log.d(TAG, "already exist");
            }

            result = true;
        }

        Log.d(TAG, "implementComposeOneEntity:" + result);
        return result;
    }

    private ArrayList<String> getExistFileList(String path) {
        ArrayList<String> fileList = new ArrayList<String>();
        Cursor cur = mContext.getContentResolver().query(Images.Media.EXTERNAL_CONTENT_URI,
                                                         mProjection,
                                                         MediaStore.Images.Media.DATA + "  like ?",
                                                         new String[] { "%" + path + "%" },
                                                         null);
        if(cur != null ) {
            if(cur.moveToFirst()) {
                while(!cur.isAfterLast()) {
                    int dataColumn = cur.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                    String data = cur.getString(dataColumn);
                    if(data != null) {
                        fileList.add(data);
                    }
                    cur.moveToNext();
                }
            }

            cur.close();
        }

        return fileList;
    }

    // private void deleteFolder(File file) {
    //     if (file.exists()) {
    //         if (file.isFile()) {
    //             int count = mContext.getContentResolver().delete(
    //                     MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
    //                     MediaStore.Images.Media.DATA + " like ?",
    //                     new String[] { file.getAbsolutePath() });
    //             Log.d(TAG, "deleteFolder():" + count + ":" + file.getAbsolutePath());
    //             file.delete();
    //         } else if (file.isDirectory()) {
    //             File files[] = file.listFiles();
    //             for (int i = 0; i < files.length; ++i) {
    //                 this.deleteFolder(files[i]);
    //             }
    //         }

    //         file.delete();
    //     }
    // }

//     private void deleteRecord(String path) {
//         if(mFileList != null) {
//             int count = 0;
//             for(File file : mFileList) {
//                 count += mContext.getContentResolver().delete(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
//                                                               MediaStore.Images.Media.DATA + " like ?",
//                                                               new String[] { file.getAbsolutePath() });
//                 Log.d(TAG, "deleteRecord():" + count + ":" + file.getAbsolutePath());
//             }
//
//             Log.d(TAG, "deleteRecord():" + count + ":" + path);
//         }
//     }

    public void onStart() {
        super.onStart();

//        deleteRecord(mParentFolderPath + File.separator + ModulePath.FOLDER_PICTURE);

        Log.d(TAG, "onStart()");
    }
}
