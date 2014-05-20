package com.zhaoyan.juyou.backuprestore;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Contacts.Data;
import android.provider.MediaStore.Audio;
import com.android.vcard.VCardComposer;
import com.android.vcard.VCardConfig;
import com.zhaoyan.common.util.Log;
import com.zhaoyan.juyou.backuprestore.Constants.ContactType;
import com.zhaoyan.juyou.backuprestore.Constants.ModulePath;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ContactBackupComposer extends Composer {
    private static final String TAG = "ContactBackupComposer";
    private int mIndex;
    private VCardComposer mVCardComposer;
    private int mCount;
    FileOutputStream mOutStream;
    
    private static final Uri mContactUri = ContactsContract.Data.CONTENT_URI;
    private Cursor mContactCursor = null;
    
    private static final String[] mProjection = new String[] { Data.RAW_CONTACT_ID };

    public ContactBackupComposer(Context context) {
        super(context);
    }

    public int getModuleType() {
        return ModuleType.TYPE_CONTACT;
    }
    public int getCount() {
//        int count = 0;
//		if (mContactCursor != null && !mContactCursor.isClosed() && mContactCursor.getCount() > 0) {
//			count = mContactCursor.getCount();
//		}
    	Log.d(TAG, "getCount():" + mCount);
        return mCount;
    }

    public boolean init() {
        boolean result = false;
        mCount = 0;
        mVCardComposer = new VCardComposer(mContext, VCardConfig.VCARD_TYPE_V21_GENERIC, true);
        String condition = getCondition();
        if (mVCardComposer.init(condition, null)) {
            result = true;
            mCount = mVCardComposer.getCount();
        } else {
            mVCardComposer = null;
        }

//        mContactCursor = mContext.getContentResolver().query(mContactUri, mProjection,
//    			Data.MIMETYPE + "= ?", new String[] { StructuredName.CONTENT_ITEM_TYPE }, null);
//        if (mContactCursor != null) {
//        	mContactCursor.moveToFirst();
//			result = true;
//		}
        
        Log.d(TAG, "init():" + result + ",count:" + mCount);

        return result;
    }

    public boolean isAfterLast() {
        boolean result = true;
        if (mVCardComposer != null) {
            result = mVCardComposer.isAfterLast();
        }
//		if (mContactCursor != null) {
//			result = mContactCursor.isAfterLast();
//		}

        Log.d(TAG, "isAfterLast():" + result);
        return result;
    }

    protected boolean implementComposeOneEntity() {
        boolean result = false;
        if (mVCardComposer != null && !mVCardComposer.isAfterLast()) {
            String tmpVcard = mVCardComposer.createOneEntry();
            if (tmpVcard != null && tmpVcard.length() > 0) {
                if(mOutStream != null) {
                    try {
                        byte[] buf = tmpVcard.getBytes();
                        mOutStream.write(buf, 0, buf.length);
                        result = true;
                    } catch(IOException e) {
                        if (super.mReporter != null) {
                            super.mReporter.onErr(e);
                        }
                    } catch(Exception e) {
                    }
                }
            }
        }
//            if (mContactCursor != null && !mContactCursor.isAfterLast()) {
//            	List<Integer> list = new ArrayList<Integer>();
//            	do {
//            		int id = mContactCursor.getInt(0);
//            		list.add(id);
//				} while (mContactCursor.moveToNext());
//                
//            	for (int i = 0; i < list.size(); i++) {
//					packageVcf(list.get(i));
//					result = true;
//				}
//            }

        Log.d(TAG, "add result:" + result);
        return result;
    }

    /**
     * Describe <code>onStart</code> method here.
     *
     */
    public final void onStart() {
        super.onStart();
        if(getCount() > 0) {
            String path = mParentFolderPath + File.separator + ModulePath.FOLDER_CONTACT;
            File folder = new File(path);
            if(!folder.exists()) {
                folder.mkdirs();
            }

            String fileName = path + File.separator + ModulePath.NAME_CONTACT;
            try {
                mOutStream = new FileOutputStream(fileName);
            } catch(IOException e) {
                mOutStream = null;
            } catch(Exception e) {
            }

        }

    }

    public void onEnd() {
        super.onEnd();
        if (mVCardComposer != null) {
            mVCardComposer.terminate();
            mVCardComposer = null;
        }
//        if (mContactCursor != null) {
//			mContactCursor.close();
//			mContactCursor = null;
//		}

        if(mOutStream != null) {
            try {
                mOutStream.flush();
                mOutStream.close();
            } catch(IOException e) {
            } catch(Exception e) {
            }
        }
    }

     private String getCondition() {
    	 Log.d(TAG, "getCondition.params:" + mParams);
         if(mParams != null) {
             List<String> conditionList = new ArrayList<String>();

             if(mParams.contains(ContactType.PHONE)) {
                 conditionList.add("-1");
             }

             int len = conditionList.size();
             if(len > 0) {
                 StringBuilder condition = new StringBuilder();
                 return condition.toString();
             }
         }

         return null;
    }
}
