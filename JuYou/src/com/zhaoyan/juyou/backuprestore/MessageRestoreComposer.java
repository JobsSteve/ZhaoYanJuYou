
package com.zhaoyan.juyou.backuprestore;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import java.util.ArrayList;
import java.util.List;

import com.zhaoyan.common.util.Log;

public class MessageRestoreComposer extends Composer {
    private static final String TAG = "MessageRestoreComposer";
    private List<Composer> mMessageComposers;
    private long mTime;

    public MessageRestoreComposer(Context context) {
        super(context);
        mMessageComposers = new ArrayList<Composer>();
        mMessageComposers.add(new SmsRestoreComposer(context));
//        mMessageComposers.add(new MmsRestoreComposer(context));
    }

    // @Override
    // public void setZipFileName(String fileName) {
    //     super.setZipFileName(fileName);
    //     for (Composer composer : mMessageComposers) {
    //         composer.setZipFileName(fileName);
    //     }
    // }

    @Override
    public int getModuleType() {
        return ModuleType.TYPE_MESSAGE;
    }

    @Override
    public int getCount() {
        int count = 0;
        for (Composer composer : mMessageComposers) {
            if (composer != null) {
                count += composer.getCount();
            }
        }

        Log.d(TAG, "getCount():" + count);
        return count;
    }

    public boolean init() {
        boolean result = false;
        mTime = System.currentTimeMillis();
        for (Composer composer : mMessageComposers) {
            if (composer != null) {
                if (composer.init()) {
                    result = true;
                }
            }
        }

        Log.d(TAG, "init():" + result + ",count:" + getCount());
        return result;
    }

    @Override
    public boolean isAfterLast() {
        boolean result = true;
        for (Composer composer : mMessageComposers) {
            if (composer != null && !composer.isAfterLast()) {
                result = false;
                break;
            }
        }

        Log.d(TAG, "isAfterLast():" + result);
        return result;
    }

    @Override
    public boolean implementComposeOneEntity() {
        for (Composer composer : mMessageComposers) {
            if (composer != null && !composer.isAfterLast()) {
                return composer.composeOneEntity();
            }
        }

        return false;
    }

    private boolean deleteAllMessage() {
        boolean result = false;
        int count = 0;
        if (mContext != null) {
            Log.d(TAG, "begin delete:" + System.currentTimeMillis());
            count = mContext.getContentResolver().delete(Uri.parse(Constants.URI_MMS_SMS),
                    "date < ?", new String[] { Long.toString(mTime) });
            Log.d(TAG, "end delete:" + System.currentTimeMillis());

            result = true;
        }

        Log.d(TAG, "deleteAllMessage(),result" + result + "," + count + " deleted!");
        return result;
    }

    @Override
    public void onStart() {
        super.onStart();
        // for (Composer composer : mComposers) {
        // if (composer != null) {
        // composer.onStart();
        // }
        // }
//        deleteAllMessage();
        Log.d(TAG, "onStart()");
    }

    @Override
    public void onEnd() {
        super.onEnd();
        for (Composer composer : mMessageComposers) {
            if (composer != null) {
                composer.onEnd();
            }
        }
	Intent intent = new Intent();
        intent.setAction("com.mediatek.backuprestore.module.MessageRestoreComposer.RESTORE_END");
        Log.d(TAG, "message restore end,sendBroadcast to updata UI ");
        mContext.sendBroadcast(intent);
        Log.d(TAG, "onEnd()");
    }

    /**
     * Describe <code>setParentFolderPath</code> method here.
     *
     * @param string a <code>String</code> value
     */
    public final void setParentFolderPath(final String path) {
        mParentFolderPath = path;
        for(Composer composer : mMessageComposers) {
            composer.setParentFolderPath(path);
        }
    }


}
