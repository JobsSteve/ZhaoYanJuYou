package com.zhaoyan.juyou.backuprestore;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.format.DateFormat;

import com.zhaoyan.common.util.Log;

public class SmsBackupComposer extends Composer {
	private static final String TAG = "SmsBackupComposer";
    private static final String TRICKY_TO_GET_DRAFT_SMS_ADDRESS = "canonical_addresses.address from sms,threads,canonical_addresses where sms.thread_id=threads._id and threads.recipient_ids=canonical_addresses._id and sms.thread_id =";

    private static final Uri[] mSmsUriArray = {
    	//yuri
    	Uri.parse("content://sms/inbox"),
    	Uri.parse("content://sms/sent"),
//        Sms.Inbox.CONTENT_URI,
//        Sms.Sent.CONTENT_URI,
        //Sms.Outbox.CONTENT_URI,
        //Sms.Draft.CONTENT_URI
    };
    private Cursor[] mSmsCursorArray = { null, null };
    private Cursor mSmsCursor = null;
    
    private static final String mStoragePath = "sms";
    private static final String mStorageName = "sms.vmsg";
    Writer mWriter = null;

    /**
     * Creates a new <code>SmsBackupComposer</code> instance.
     *
     * @param context a <code>Context</code> value
     */
    public SmsBackupComposer(Context context) {
        super(context);
    }

    @Override
    /**
     * Describe <code>getModuleType</code> method here.
     *
     * @return an <code>int</code> value
     */
    public int getModuleType() {
        return ModuleType.TYPE_SMS;
    }

    @Override
    /**
     * Describe <code>getCount</code> method here.
     *
     * @return an <code>int</code> value
     */
    public int getCount() {
        int count = 0;
        if (mSmsCursor != null && !mSmsCursor.isClosed() && mSmsCursor.getCount() > 0) {
			count = mSmsCursor.getCount();
		}

        Log.d(TAG, "getCount():" + count);
        return count;
    }

    @Override
    /**
     * Describe <code>init</code> method here.
     *
     * @return a <code>boolean</code> value
     */
    public boolean init() {
        boolean result = false;
        mSmsCursor = mContext.getContentResolver().query(SmsField.SMS_URI, null, null, null, "date ASC");
        if (mSmsCursor != null) {
			mSmsCursor.moveToFirst();
			result = true;
		}

        Log.d(TAG, "init():" + result + ",count:" + getCount());
        return result;
    }
    
    @Override
    /**
     * Describe <code>isAfterLast</code> method here.
     *
     * @return a <code>boolean</code> value
     */
    public boolean isAfterLast() {
        boolean result = true;
		if (mSmsCursor != null && !mSmsCursor.isAfterLast()) {
			result = false;
		}

        Log.d(TAG, "isAfterLast():" + result);
        return result;
    }


    /**
     * Describe <code>implementComposeOneEntity</code> method here.
     *
     * @return a <code>boolean</code> value
     */
	public boolean implementComposeOneEntity() {
		boolean result = false;
		if (mSmsCursor != null && !mSmsCursor.isAfterLast()) {
			SmsItem item = null;
			String address = mSmsCursor.getString(mSmsCursor.getColumnIndex(SmsField.ADDRESS));
			if (address == null) {
				address = "";
			}
			String person = mSmsCursor.getString(mSmsCursor.getColumnIndex(SmsField.PERSON));
			if (person == null) {
				person = "";
			}

			long mtime = mSmsCursor.getLong(mSmsCursor.getColumnIndex(SmsField.DATE));
			String date = formatTimeStampString(mContext, mtime);
			Log.d(TAG, "implement.date:" + date);
			
//			protocol = mSmsCursor.getString(mSmsCursor
//					.getColumnIndex(SmsField.PROTOCOL));
//			if (protocol == null) {
//				protocol = "";
//			}
			int read = mSmsCursor.getInt(mSmsCursor.getColumnIndex(SmsField.READ));
			String readByte = (read == 0 ? "UNREAD" : "READ");
			
//			status = mSmsCursor.getString(mSmsCursor
//					.getColumnIndex(SmsField.STATUS));
//			if (status == null) {
//				status = "";
//			}
			int box = mSmsCursor.getInt(mSmsCursor.getColumnIndex(SmsField.TYPE));
			String boxType = "INBOX";
			switch (box) {
			case 1:
				boxType = "INBOX";
				break;
			case 2:
				boxType = "SENDBOX";
				break;
			}
			
//			reply_path_present = mSmsCursor.getString(mSmsCursor
//					.getColumnIndex(SmsField.REPLY_PATH_PRESENT));
//			if (reply_path_present == null) {
//				reply_path_present = "";
//			}
			int lock = mSmsCursor.getInt(mSmsCursor.getColumnIndex(SmsField.LOCKED));
			String locked = (lock == 1 ? "LOCKED" : "UNLOCKED");
			
//			error_code = mSmsCursor.getString(mSmsCursor
//					.getColumnIndex(SmsField.ERROR_CODE));
//			if (error_code == null) {
//				error_code = "";
//			}
			String seen = mSmsCursor.getString(mSmsCursor.getColumnIndex(SmsField.SEEN));

			String sc = mSmsCursor.getString(mSmsCursor.getColumnIndex(SmsField.SC));

			String body = mSmsCursor.getString(mSmsCursor.getColumnIndex(SmsField.BODY));
			
			StringBuffer sbf = new StringBuffer(body);
			int num = 0;
			num = sbf.indexOf("END:VBODY");
			do {
				if (num > 0) {
					sbf.insert(num, "/");
				} else {
					break;
				}
			} while ((num = sbf.indexOf("END:VBODY", num + 1 + "END:VBODY".length())) >= 0);
			body = sbf.toString();
			
			try {
				if (mWriter != null) {
					mWriter.write(combineVmsg(date,readByte,boxType,locked,address,body,seen));
					result = true;
				}
			} catch (Exception e) {
				Log.e(TAG, "mWriter.write() failed");
			} finally {
				mSmsCursor.moveToNext();
			}
		}

		return result;
	}

    /**
     * Describe <code>onStart</code> method here.
     *
     */
    public final void onStart() {
        super.onStart();
        Log.e(TAG, "onStart():mParentFolderPath:" + mParentFolderPath);

        if(getCount() > 0) {
            File path = new File(mParentFolderPath + File.separator + mStoragePath);
            if (!path.exists()) {
                path.mkdirs();
            }

            File file = new File(path.getAbsolutePath() + File.separator + mStorageName);
            if (!file.exists()) {
                try {
                    file.createNewFile();
                } catch (Exception e) {
                    Log.e(TAG, "onStart():file:" + file.getAbsolutePath());
                    Log.e(TAG, "onStart():create file failed");
                }
            }

            try {
                mWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));
            } catch (Exception e) {
                Log.e(TAG, "new BufferedWriter failed");
            }
        }
    }


    /**
     * Describe <code>onEnd</code> method here.
     * 
     */
    public final void onEnd() {
        super.onEnd();
        try {
            Log.d(TAG, "SmsBackupComposer onEnd");
            if (mWriter != null) {
                Log.e(TAG, "mWriter.close()");
                mWriter.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "mWriter.close() failed");
        }

        if (mSmsCursor != null) {
			mSmsCursor.close();
			mSmsCursor = null;
		}
//        for (Cursor cur : mSmsCursorArray) {
//            if (cur != null) {
//                cur.close();
//                cur = null;
//            }
//        }
    }

    private static final String UTF = "UTF-8";
    private static final String QUOTED = "QUOTED-PRINTABLE";
    private static final String CHARSET = "CHARSET=";
    private static final String ENCODING = "ENCODING=";
    private static final String VMESSAGE_END_OF_SEMICOLON = ";";
    private static final String VMESSAGE_END_OF_COLON = ":";
    private static final String VMESSAGE_END_OF_LINE = "\r\n";
    private static final String BEGIN_VMSG = "BEGIN:VMSG";
    private static final String END_VMSG = "END:VMSG";
    private static final String VERSION = "VERSION:";
    private static final String BEGIN_VCARD = "BEGIN:VCARD";
    private static final String END_VCARD = "END:VCARD";
    private static final String BEGIN_VBODY = "BEGIN:VBODY";
    private static final String END_VBODY = "END:VBODY";
    private static final String FROMTEL = "FROMTEL:";//Address
    private static final String XBOX = "X-BOX:";//box type
    private static final String XREAD = "X-READ:";//read
    private static final String XSEEN = "X-SEEN:";//seen
    private static final String XSIMID = "X-SIMID:";//
    private static final String XLOCKED = "X-LOCKED:";//locked
    private static final String XTYPE = "X-TYPE:";//type
    private static final String DATE = "Date:";//date
    private static final String SUBJECT = "Subject;";

	public static String combineVmsg(String TimeStamp, String ReadByte, String BoxType,String Locked, String SmsAddress, String body ,String mseen){
	    StringBuilder mBuilder = new StringBuilder();
	    mBuilder.append(BEGIN_VMSG);
        mBuilder.append(VMESSAGE_END_OF_LINE);
        mBuilder.append(VERSION);
        mBuilder.append("1.1");
        mBuilder.append(VMESSAGE_END_OF_LINE);
        mBuilder.append(BEGIN_VCARD);
        mBuilder.append(VMESSAGE_END_OF_LINE);
        mBuilder.append(FROMTEL);
        mBuilder.append(SmsAddress);
        mBuilder.append(VMESSAGE_END_OF_LINE);
        mBuilder.append(END_VCARD);
        mBuilder.append(VMESSAGE_END_OF_LINE);
        mBuilder.append(BEGIN_VBODY);
        mBuilder.append(VMESSAGE_END_OF_LINE);
        mBuilder.append(XBOX);
        mBuilder.append(BoxType);
        mBuilder.append(VMESSAGE_END_OF_LINE);
        mBuilder.append(XREAD);
        mBuilder.append(ReadByte);
        mBuilder.append(VMESSAGE_END_OF_LINE);
        mBuilder.append(XSEEN);
        mBuilder.append(mseen);
        mBuilder.append(VMESSAGE_END_OF_LINE);
        mBuilder.append(XLOCKED);
        mBuilder.append(Locked);
        mBuilder.append(VMESSAGE_END_OF_LINE);
        mBuilder.append(XTYPE);
        mBuilder.append("SMS");
        mBuilder.append(VMESSAGE_END_OF_LINE);
        mBuilder.append(DATE);
        mBuilder.append(TimeStamp);
        mBuilder.append(VMESSAGE_END_OF_LINE);
        mBuilder.append(SUBJECT);
        mBuilder.append(ENCODING);
        mBuilder.append(QUOTED);
        mBuilder.append(VMESSAGE_END_OF_SEMICOLON);
        mBuilder.append(CHARSET);
        mBuilder.append(UTF);
        mBuilder.append(VMESSAGE_END_OF_COLON);
        mBuilder.append(body);
        mBuilder.append(VMESSAGE_END_OF_LINE);
        mBuilder.append(END_VBODY);
        mBuilder.append(VMESSAGE_END_OF_LINE);
        mBuilder.append(END_VMSG);
        mBuilder.append(VMESSAGE_END_OF_LINE);
        return mBuilder.toString();
	}

    private String formatTimeStampString(Context context, long when) {//, boolean fullFormat
        CharSequence  formattor = DateFormat.format("yyyy/MM/dd kk:mm:ss",when);
        return formattor.toString();
    }
}




