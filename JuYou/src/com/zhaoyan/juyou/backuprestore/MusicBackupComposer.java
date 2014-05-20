
package com.zhaoyan.juyou.backuprestore;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore.Audio;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import com.zhaoyan.common.util.Log;
import com.zhaoyan.juyou.backuprestore.Constants.ModulePath;

/**
 * Describe class <code>MusicBackupComposer</code> here.
 * 
 * @author
 * @version 1.0
 */
public class MusicBackupComposer extends Composer {
    private static final String TAG = "MusicBackupComposer";
    private ArrayList<String> mNameList;
    private static final Uri[] mMusicUriArray = {
        //Audio.Media.INTERNAL_CONTENT_URI,
        Audio.Media.EXTERNAL_CONTENT_URI
    };
    private Cursor[] mMusicCursorArray = {
        //null,
        null };

    private static final String[] mProjection = new String[] { Audio.Media._ID, Audio.Media.DATA };

    /**
     * Creates a new <code>MusicBackupComposer</code> instance.
     * 
     * @param context
     *            a <code>Context</code> value
     */
    public MusicBackupComposer(Context context) {
        super(context);
    }

    /**
     * Describe <code>init</code> method here.
     * 
     * @return a <code>boolean</code> value
     */
    public final boolean init() {
    	Log.d(TAG, "init");
        boolean result = false;
        for (int i = 0; i < mMusicCursorArray.length; ++i) {
            if (mMusicUriArray[i] == Audio.Media.EXTERNAL_CONTENT_URI) {
//                String path = SDCardUtils.getStoragePath();
            	String path = SDCardUtils.getStoragePath(mContext);
                Log.d(TAG, "path:" + path);
                if(path!=null&&!path.trim().equals("")){
                	String externalSDPath = "%" + path.subSequence(0, path.lastIndexOf(File.separator)) + "%";
                	 Log.d(TAG, "externalSDPath:" + externalSDPath);
                	mMusicCursorArray[i] = mContext.getContentResolver().query(mMusicUriArray[i],
                			mProjection, Audio.Media.DATA + " not like ?",
                			new String[] { externalSDPath }, null);
                }
            } else {
                mMusicCursorArray[i] = mContext.getContentResolver().query(mMusicUriArray[i],
                        mProjection, null, null, null);
            }
            if (mMusicCursorArray[i] != null) {
                mMusicCursorArray[i].moveToFirst();
                result = true;
            }
        }
        Log.d(TAG, "init():" + result + ",count:" + getCount());
        mNameList = new ArrayList<String>();
        Log.d(TAG, "init():" + result + ",count:" + getCount());
        return result;
    }

    /**
     * Describe <code>getModuleType</code> method here.
     * 
     * @return an <code>int</code> value
     */
    public final int getModuleType() {
        return ModuleType.TYPE_MUSIC;
    }

    /**
     * Describe <code>getCount</code> method here.
     * 
     * @return an <code>int</code> value
     */
    public final int getCount() {
    	Log.d(TAG, "getCOunt()");
        int count = 0;
        for (Cursor cur : mMusicCursorArray) {
            if (cur != null && !cur.isClosed() && cur.getCount() > 0) {
                count += cur.getCount();
            }
        }

        Log.d(TAG, "getCount():" + count);
        return count;
    }

    /**
     * Describe <code>isAfterLast</code> method here.
     * 
     * @return a <code>boolean</code> value
     */
    public final boolean isAfterLast() {
    	Log.d(TAG, "isAfterLast");
        boolean result = true;
        for (Cursor cur : mMusicCursorArray) {
            if (cur != null && !cur.isAfterLast()) {
                result = false;
                break;
            }
        }

        Log.d(TAG, "isAfterLast():" + result);
        return result;
    }

    /**
     * Describe <code>implementComposeOneEntity</code> method here.
     * 
     * @return a <code>boolean</code> value
     */
    public final boolean implementComposeOneEntity() {
    	Log.d(TAG, "implementComposeOneEntity");
        boolean result = false;
        for (int i = 0; i < mMusicCursorArray.length; ++i) {
            if (mMusicCursorArray[i] != null && !mMusicCursorArray[i].isAfterLast()) {
                int dataColumn = mMusicCursorArray[i].getColumnIndexOrThrow(Audio.Media.DATA);
                String data = mMusicCursorArray[i].getString(dataColumn);
                String destFileName = null;
                try {
                    String tmpName = mParentFolderPath + File.separator + ModulePath.FOLDER_MUSIC +
                        data.subSequence(data.lastIndexOf(File.separator), data.length()).toString();
                    destFileName = getDestinationName(tmpName);
                    if (destFileName != null) {
                        try {
                            copyFile(data, destFileName);
                            mNameList.add(destFileName);
                            result = true;
                        } catch (IOException e) {
                            if (super.mReporter != null) {
                                super.mReporter.onErr(e);
                            }
                            Log.d(TAG, "copy file fail");
                        }
                    }

                    Log.d(TAG, data + ",destFileName:" + destFileName);
                } catch (StringIndexOutOfBoundsException e) {
                    Log.e(TAG, " StringIndexOutOfBoundsException");
                    e.printStackTrace();
                } catch(Exception e) {
                    e.printStackTrace();
                }


                mMusicCursorArray[i].moveToNext();
                break;
            }
        }

        return result;
    }

    /**
     * Describe <code>onStart</code> method here.
     * 
     */
    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
        if(getCount() > 0) {
            File path = new File(mParentFolderPath + File.separator + ModulePath.FOLDER_MUSIC);
            if (path.exists()) {
                deleteFolder(path);
            }

            path.mkdirs();
        }
    }

    /**
     * Describe <code>onEnd</code> method here.
     * 
     */
    @Override
    public void onEnd() {
        super.onEnd();
        Log.d(TAG, "onEnd");
        if (mNameList != null) {
            mNameList.clear();
        }

        for (Cursor cur : mMusicCursorArray) {
            if (cur != null) {
                cur.close();
                cur = null;
            }
        }
    }


    /**
     * Describe <code>getDestinationName</code> method here.
     *
     * @param name a <code>String</code> value
     * @return a <code>String</code> value
     */
    private String getDestinationName(String name) {
    	Log.d(TAG, "getDestinationName");
        if (!mNameList.contains(name)) {
            return name;
        } else {
            return rename(name);
        }
    }

    /**
     * Describe <code>rename</code> method here.
     * 
     * @param name
     *            a <code>String</code> value
     * @return a <code>String</code> value
     */
    private String rename(String name) {
    	Log.d(TAG, "rename");
        String tmpName;
        int id = name.lastIndexOf(".");
        int id2, leftLen;
        for (int i = 1; i < (1 << 12); ++i) {
            leftLen = 255 - (1 + Integer.toString(i).length() + name.length() - id);
            id2 = id <= leftLen ? id : leftLen;
            tmpName = name.subSequence(0, id2) + "~" + i + name.subSequence(id, name.length());
            if (!mNameList.contains(tmpName)) {
                return tmpName;
            }
        }

        return null;
    }

    private void deleteFolder(File file) {
    	Log.d(TAG, "deleteFolder");
        if (file.exists()) {
            if (file.isFile()) {
                int count = mContext.getContentResolver().delete(Audio.Media.EXTERNAL_CONTENT_URI,
                                                                 Audio.Media.DATA + " like ?", new String[] { file.getAbsolutePath() });
                Log.d(TAG, "deleteFolder():" + count + ":" + file.getAbsolutePath());
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

    private void copyFile(String srcFile, String destFile) throws IOException {
    	Log.d(TAG, "copyFile");
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
}
