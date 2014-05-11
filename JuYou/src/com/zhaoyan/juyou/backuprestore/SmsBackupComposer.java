package com.zhaoyan.juyou.backuprestore;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.telephony.SmsMessage.SubmitPdu;
import android.telephony.SmsMessage;
import android.text.format.DateFormat;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;

import com.zhaoyan.common.util.Log;

public class SmsBackupComposer extends Composer {
	private static final String TAG = "SmsBackupComposer";
    private static final String TRICKY_TO_GET_DRAFT_SMS_ADDRESS = "canonical_addresses.address from sms,threads,canonical_addresses where sms.thread_id=threads._id and threads.recipient_ids=canonical_addresses._id and sms.thread_id =";

    private static final String COLUMN_NAME_DATE = "date";
    private static final String COLUMN_NAME_READ = "read";
    private static final String COLUMN_NAME_SEEN = "seen";
    private static final String COLUMN_NAME_TYPE = "type";
    private static final String COLUMN_NAME_SIM_ID = "sim_id";
    private static final String COLUMN_NAME_LOCKED = "locked";
    private static final String COLUMN_NAME_THREAD_ID = "thread_id";
    private static final String COLUMN_NAME_ADDRESS = "address";
    private static final String COLUMN_NAME_SC = "service_center";
    private static final String COLUMN_NAME_BODY = "body";

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
        //modify by yuri
//        for (Cursor cur : mSmsCursorArray) {
//            if (cur != null && !cur.isClosed() && cur.getCount() > 0) {
//                count += cur.getCount();
//            }
//        }
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
        //modify by yuri
//        for (int i = 0; i < mSmsUriArray.length; ++i) {
//            mSmsCursorArray[i] = mContext.getContentResolver().query(mSmsUriArray[i], null, null,
//                    null, "date ASC");
//            if (mSmsCursorArray[i] != null) {
//                mSmsCursorArray[i].moveToFirst();
//                result = true;
//            }
//        }
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
        //modify by yuri
//        for (Cursor cur : mSmsCursorArray) {
//            if (cur != null && !cur.isAfterLast()) {
//                result = false;
//                break;
//            }
//        }
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
        //modify by yuri
//        for (int i = 0; i < mSmsCursorArray.length; ++i) {
//            if (mSmsCursorArray[i] != null && !mSmsCursorArray[i].isAfterLast()) {
//        if (mSmsCursor != null && !mSmsCursor.isAfterLast()) {
//                Cursor tmpCur = mSmsCursor;
//
//                long mtime = tmpCur.getLong(tmpCur.getColumnIndex(COLUMN_NAME_DATE));
//
//                String timeStamp = formatTimeStampString(mContext, mtime);
//
//                int read = tmpCur.getInt(tmpCur.getColumnIndex(COLUMN_NAME_READ));
//                String readByte = (read == 0 ? "UNREAD" : "READ");
//
//                String seen = tmpCur.getString(tmpCur.getColumnIndex(COLUMN_NAME_SEEN));
//
//
//                int box  = tmpCur.getInt(tmpCur.getColumnIndex(COLUMN_NAME_TYPE));
//                String boxType = null;
//                switch(box) {
//                case 1:
//                    boxType = "INBOX";
//                    break;
//
//                case 2:
//                    boxType = "SENDBOX";
//                    break;
//
//                default:
//                    boxType = "INBOX";
//                    break;
//                }
//
//                int lock = tmpCur.getInt(tmpCur.getColumnIndex(COLUMN_NAME_LOCKED));
//                String locked = (lock == 1 ? "LOCKED" : "UNLOCKED");
//
//                String smsAddress = null;
//                    smsAddress = tmpCur.getString(tmpCur.getColumnIndex(COLUMN_NAME_ADDRESS));
//
//                if (smsAddress == null) {
//                    smsAddress = "";
//                }
//
//                String sc = tmpCur.getString(tmpCur.getColumnIndex(COLUMN_NAME_SC));
//
//                String body = tmpCur.getString(tmpCur.getColumnIndex(COLUMN_NAME_BODY));
//
//                StringBuffer sbf = new StringBuffer(body);
//                int num = 0;
//                num = sbf.indexOf("END:VBODY");    
//                do {
//                    if (num >= 0) {
//                        sbf.insert(num, "/");
//                    } else {
//                        break;
//                    }
//                } while ((num = sbf.indexOf("END:VBODY", num + 1 + "END:VBODY".length())) >= 0);     
//                body = sbf.toString();
//                
//                try {
//                    if(mWriter != null) {
//                        mWriter.write(combineVmsg(timeStamp,readByte,boxType,mSlotid,locked,smsAddress,body,seen));
//                        result = true;
//                    }
//                } catch (Exception e) {
//                    Log.e(TAG, "mWriter.write() failed");
//                } finally {
//                    tmpCur.moveToNext();
//                }
////                break;
//            }
//        }
      if (mSmsCursor != null && !mSmsCursor.isAfterLast()) {
    	    // �鿴��ݿ�sms���֪ subject��service_centerʼ����null��������Ͳ���ȡ���ǵ������
    	  SmsItem item = null;
//    	  do {
    		  String address;  
              String person;  
              String date;  
              String protocol;  
              String read;  
              String status;  
              String type;  
              String reply_path_present;  
              String body;  
              String locked;  
              String error_code;  
              String seen;  
              String sc;
              
           // ���address == null��xml�ļ����ǲ�����ɸ����Ե�,Ϊ�˱�֤����ʱ�������ܹ��������һһ��Ӧ������Ҫ��֤���е�item��ǵ�����������˳����һ�µ�
              address = mSmsCursor.getString(mSmsCursor.getColumnIndex(SmsField.ADDRESS));  
              if (address == null) {  
                  address = "";  
              }  
              person = mSmsCursor.getString(mSmsCursor.getColumnIndex(SmsField.PERSON));  
              if (person == null) {  
                  person = "";  
              }  
              
              long mtime = mSmsCursor.getLong(mSmsCursor.getColumnIndex(SmsField.DATE));
              date = formatTimeStampString(mContext, mtime);
//              date = mSmsCursor.getString(mSmsCursor.getColumnIndex(SmsField.DATE));  
//              if (date == null) {  
//                  date = "";  
//              }  
              Log.d(TAG, "implement.date:" + date);
              protocol = mSmsCursor.getString(mSmsCursor.getColumnIndex(SmsField.PROTOCOL));  
              if (protocol == null) {
                  protocol = "";  
              }  
              read = mSmsCursor.getString(mSmsCursor.getColumnIndex(SmsField.READ));  
              if (read == null) {  
                  read = "";  
              }  
              status = mSmsCursor.getString(mSmsCursor.getColumnIndex(SmsField.STATUS));  
              if (status == null) {  
                  status = "";  
              }  
              type = mSmsCursor.getString(mSmsCursor.getColumnIndex(SmsField.TYPE));  
              if (type == null) {  
                  type = "";  
              }  
              reply_path_present = mSmsCursor.getString(mSmsCursor.getColumnIndex(SmsField.REPLY_PATH_PRESENT));  
              if (reply_path_present == null) {
                  reply_path_present = "";  
              }  
              body = mSmsCursor.getString(mSmsCursor.getColumnIndex(SmsField.BODY));  
              if (body == null) {  
                  body = "";  
              }  
              locked = mSmsCursor.getString(mSmsCursor.getColumnIndex(SmsField.LOCKED));  
              if (locked == null) {  
                  locked = "";  
              }  
              error_code = mSmsCursor.getString(mSmsCursor.getColumnIndex(SmsField.ERROR_CODE));  
              if (error_code == null) {  
                  error_code = "";  
              }  
              seen = mSmsCursor.getString(mSmsCursor.getColumnIndex(SmsField.SEEN));  
              if (seen == null) {  
                  seen = "";  
              } 
              
              sc = mSmsCursor.getString(mSmsCursor.getColumnIndex(SmsField.SC));
              if (sc == null) {
   			sc = "";
              }
              
              item = new SmsItem();
              item.setAddress(address);
              item.setPerson(person);
              item.setDate(date);
              item.setProtocol(protocol);
              item.setRead(read);
              item.setStatus(status);
              item.setType(type);
              item.setReplyPathPresent(reply_path_present);
              item.setBody(body);
              item.setLocked(locked);
              item.setErrorCode(error_code);
              item.setSeen(seen);
              item.setSC(sc);
				try {
					if (mWriter != null) {
						mWriter.write(combineVmsg(item));
						result = true;
					}
				} catch (Exception e) {
					Log.e(TAG, "mWriter.write() failed");
				} finally {
					mSmsCursor.moveToNext();
				}
//		} while (mSmsCursor.moveToNext());
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
    //add by yuri
    private static final String PERSON = "PERSON:";//person
    private static final String PROTOCOL = "PROTOCOL:";//protocol
    private static final String STATUS = "STATUS:";//status
    private static final String REPLY_PATH_PRESENT = "REPLY_PATH_PRESENT:";//reply
    private static final String ERROR_CODE = "ERROR_CODE:";//error code

	public static String combineVmsg(String TimeStamp, String ReadByte, String BoxType,
	                           String mSlotid, String Locked, String SmsAddress, String body ,String mseen){
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
        mBuilder.append(XSIMID);
        mBuilder.append(mSlotid);
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
	
	public static String combineVmsg(SmsItem item) {
		StringBuilder mBuilder = new StringBuilder();
		mBuilder.append(BEGIN_VMSG);
		mBuilder.append(VMESSAGE_END_OF_LINE);
		mBuilder.append(VERSION);
		mBuilder.append("1.1");
		mBuilder.append(VMESSAGE_END_OF_LINE);
		mBuilder.append(BEGIN_VCARD);
		mBuilder.append(VMESSAGE_END_OF_LINE);
		mBuilder.append(FROMTEL);
		mBuilder.append(item.getAddress());
		mBuilder.append(VMESSAGE_END_OF_LINE);
		mBuilder.append(END_VCARD);
		mBuilder.append(VMESSAGE_END_OF_LINE);
		mBuilder.append(BEGIN_VBODY);
		mBuilder.append(VMESSAGE_END_OF_LINE);
		mBuilder.append(XBOX);
		mBuilder.append(item.getType());
		mBuilder.append(VMESSAGE_END_OF_LINE);
		mBuilder.append(XREAD);
		mBuilder.append(item.getRead());
		mBuilder.append(VMESSAGE_END_OF_LINE);
		mBuilder.append(XSEEN);
		mBuilder.append(item.getSeen());
		mBuilder.append(VMESSAGE_END_OF_LINE);
		mBuilder.append(PERSON);
		mBuilder.append(item.getPerson());
		mBuilder.append(VMESSAGE_END_OF_LINE);
		mBuilder.append(XLOCKED);
		mBuilder.append(item.getLocked());
		mBuilder.append(VMESSAGE_END_OF_LINE);
		mBuilder.append(PROTOCOL);
		mBuilder.append(item.getProtocol());
		mBuilder.append(VMESSAGE_END_OF_LINE);
		mBuilder.append(STATUS);
		mBuilder.append(item.getStatus());
		mBuilder.append(VMESSAGE_END_OF_LINE);
		mBuilder.append(REPLY_PATH_PRESENT);
		mBuilder.append(item.getReplyPathPresent());
		mBuilder.append(VMESSAGE_END_OF_LINE);
		mBuilder.append(ERROR_CODE);
		mBuilder.append(item.getErrorcode());
		mBuilder.append(VMESSAGE_END_OF_LINE);
		mBuilder.append(XTYPE);
		mBuilder.append("SMS");
		mBuilder.append(VMESSAGE_END_OF_LINE);
		mBuilder.append(DATE);
		mBuilder.append(item.getDate());
		mBuilder.append(VMESSAGE_END_OF_LINE);
		mBuilder.append(SUBJECT);
		mBuilder.append(ENCODING);
		mBuilder.append(QUOTED);
		mBuilder.append(VMESSAGE_END_OF_SEMICOLON);
		mBuilder.append(CHARSET);
		mBuilder.append(UTF);
		mBuilder.append(VMESSAGE_END_OF_COLON);
		mBuilder.append(item.getBody());
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




