package com.zhaoyan.juyou.backuprestore;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;

import com.zhaoyan.common.util.Log;
import com.zhaoyan.juyou.backuprestore.Constants.ModulePath;

public class SmsRestoreComposer extends Composer {
    private static final String TAG = "SmsRestoreComposer";
    private static final String COLUMN_NAME_IMPORT_SMS = "import_sms";
    private static final Uri[] mSmsUriArray = {
    	//yuri
    	Uri.parse("content://sms/inbox"),
    	Uri.parse("content://sms/sent"),
    };
    
    private static final Uri mSmsUri = Uri.parse("content://sms/");
    private int mIndex;
    private long mTime;
    private ArrayList<ContentProviderOperation> mOperationList;
//    private ArrayList<SmsRestoreEntry> mVmessageList;
    private ArrayList<SmsItem> mVmessageList;

    public SmsRestoreComposer(Context context) {
        super(context);
    }

    public int getModuleType() {
        return ModuleType.TYPE_SMS;
    }

    public int getCount() {
        int count = 0;
        if (mVmessageList != null) {
            count = mVmessageList.size();
        }

        Log.d(TAG, "getCount():" + count);
        return count;
    }

    public boolean init() {
        boolean result = false;

        Log.d(TAG, "begin init:" + System.currentTimeMillis());
        mOperationList = new ArrayList<ContentProviderOperation>();
        try {
            mTime = System.currentTimeMillis();
//            mVmessageList = getSmsRestoreEntry();
            mVmessageList = getSmsRestoreItem();
            result = true;
        } catch (Exception e) {
            Log.d(TAG, "init failed");
        }

        Log.d(TAG, "end init:" + System.currentTimeMillis());
        Log.d(TAG, "init():" + result + ",count:" + mVmessageList.size());
        return result;
    }

    @Override
    public boolean isAfterLast() {
        boolean result = true;
        if (mVmessageList != null) {
            result = (mIndex >= mVmessageList.size()) ? true : false;
        }

        Log.d(TAG, "isAfterLast():" + result);
        return result;
    }

    @Override
    public boolean implementComposeOneEntity() {
        boolean result = false;
//        SmsRestoreEntry vMsgFileEntry = mVmessageList.get(mIndex++);
        SmsItem item = mVmessageList.get(mIndex++);

        if (item != null) {
            ContentValues values = parsePdu(item);
            if (values == null) {
                Log.d(TAG, "parsePdu():values=null");
            } else {
                Log.d(TAG, "begin restore:" + System.currentTimeMillis());
//                int mboxType = vMsgFileEntry.getBoxType().equals("INBOX") ? 1 : 2;
//                Log.d(TAG, "mboxType:" + mboxType);
//                ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(mSmsUriArray[mboxType - 1]);
                ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(mSmsUri);
                builder.withValues(values);
                mOperationList.add(builder.build());
                if ((mIndex % Constants.NUMBER_IMPORT_SMS_EACH != 0) && !isAfterLast()) {
                    return true;
                }

                if (isAfterLast()) {
                    values.remove("import_sms");
                }

                if (mOperationList.size() > 0) {
                    try {
                        mContext.getContentResolver().applyBatch("sms", mOperationList);
                    } catch (android.os.RemoteException e) {
                    } catch (android.content.OperationApplicationException e) {
                    } catch(Exception e) {
                    } finally {
                        mOperationList.clear();
                    }
                }

                Log.d(TAG, "end restore:" + System.currentTimeMillis());
                result = true;
            }
        } else {
            if (super.mReporter != null) {
                super.mReporter.onErr(new IOException());
            }
        }

        return result;
    }

    private ContentValues parsePdu(SmsRestoreEntry pdu) {

        ContentValues values = new ContentValues();
        //yuri
//        values.put(Sms.ADDRESS, pdu.getSmsAddress());
        values.put("address", pdu.getSmsAddress());
        //       values.put(Sms.SUBJECT, null);
//        values.put(Sms.BODY, pdu.getBody());
        values.put("body", pdu.getBody());
        Log.d(TAG, "readorunread :"+pdu.getReadByte());
        
//        values.put(Sms.READ, (pdu.getReadByte().equals("UNREAD") ? 0 : 1));
        values.put("read", (pdu.getReadByte().equals("UNREAD") ? 0 : 1));
//        values.put(Sms.SEEN,pdu.getSeen());
        values.put("seen",pdu.getSeen());
//        values.put(Sms.LOCKED,(pdu.getLocked().equals("LOCKED") ? "1" : "0"));
        values.put("locked",(pdu.getLocked().equals("LOCKED") ? "1" : "0"));
        String simCardid = "-1";
//        if (FeatureOption.MTK_GEMINI_SUPPORT == true) {
//            int soltid = Integer.parseInt(pdu.getSimCardid())-1;
//            if (soltid < 0) {
//                simCardid = "-1";
//            } else {
//            	SimInfoRecord si = SimInfoManager.getSimInfoBySlot(mContext, soltid);
//                if (si == null) {
//                    simCardid = "-1";
//                } else {
//                    simCardid = String.valueOf(si.mSimInfoId);
//                }
//            }
//        } else {
//            simCardid = "-1";
//        }
//        values.put(Sms.SIM_ID,simCardid);
//        values.put("sim_id", simCardid);
                
//        values.put(Sms.DATE, pdu.getTimeStamp());
        values.put("date", pdu.getTimeStamp());
//        values.put(Sms.TYPE, (pdu.getBoxType().equals("INBOX") ? 1 : 2));
        values.put("type", (pdu.getBoxType().equals("INBOX") ? 1 : 2));
        //       values.put("import_sms", true);

        return values;
    }
    
    private ContentValues parsePdu(SmsItem pdu) {

        ContentValues values = new ContentValues();
        values.put(SmsField.ADDRESS, pdu.getAddress());
        values.put(SmsField.BODY, pdu.getBody());
        Log.d(TAG, "parsePdu:"+pdu.getReadByte());
        values.put(SmsField.READ, (pdu.getReadByte().equals("UNREAD")) ? 0 : 1);
        values.put(SmsField.SEEN,pdu.getSeen());
        values.put(SmsField.LOCKED,(pdu.getLocked().equals("LOCKED") ? "1" : "0"));
        Log.d(TAG, "parsePdu.date:" + pdu.getDate());
        values.put(SmsField.DATE, pdu.getDate());
        values.put(SmsField.TYPE, (pdu.getBoxType().equals("INBOX") ? 1 : 2));
//        values.put(SmsField.PERSON, pdu.getPerson());
//        values.put(SmsField.PROTOCOL, pdu.getProtocol());
//        values.put(SmsField.STATUS, pdu.getStatus());
//        values.put(SmsField.REPLY_PATH_PRESENT, pdu.getReplyPathPresent());
//        values.put(SmsField.ERROR_CODE, pdu.getErrorcode());
        //       values.put("import_sms", true);

        return values;
    }

    // private ContentValues extractContentValues(SmsMessage sms) {
    //     ContentValues values = new ContentValues();
    //     values.put(Sms.PROTOCOL, sms.getProtocolIdentifier());

    //     if (sms.getPseudoSubject().length() > 0) {
    //         values.put(Sms.SUBJECT, sms.getPseudoSubject());
    //     }

    //     String address = sms.getDestinationAddress();
    //     if (TextUtils.isEmpty(address)) {
    //         address = "unknown";
    //     }
    //     values.put(Sms.ADDRESS, address);
    //     values.put(Sms.REPLY_PATH_PRESENT, sms.isReplyPathPresent() ? 1 : 0);
    //     values.put(Sms.SERVICE_CENTER, sms.getServiceCenterAddress());

    //     return values;
    // }

    private boolean deleteAllPhoneSms() {
        boolean result = false;
        if (mContext != null) {
            Log.d(TAG, "begin delete:" + System.currentTimeMillis());
            int count = mContext.getContentResolver().delete(Uri.parse(Constants.URI_SMS),
                    "type <> ?", new String[] { Constants.MESSAGE_BOX_TYPE_INBOX });
            count += mContext.getContentResolver().delete(Uri.parse(Constants.URI_SMS), "date < ?",
                    new String[] { Long.toString(mTime) });
            Log.d(TAG, "deleteAllPhoneSms():" + count + " sms deleted!");
            //yuri.WapPush is mtk added
//            int count2 = mContext.getContentResolver().delete(WapPush.CONTENT_URI, null, null);
//            Log.d(TAG, "deleteAllPhoneSms():" + count + " sms deleted!" + count2
//                    + "wappush deleted!");
            result = true;
            Log.d(TAG, "end delete:" + System.currentTimeMillis());
        }

        return result;
    }

    @Override
    public void onStart() {
        super.onStart();
        //deleteAllPhoneSms();

        Log.d(TAG, "onStart()");
    }

    @Override
    public void onEnd() {
        super.onEnd();
        if (mVmessageList != null) {
        	mVmessageList.clear();
        }

        if (mOperationList != null) {
            mOperationList = null;
        }

        Log.d(TAG, "onEnd()");
    }

    private static final String VMESSAGE_END_OF_LINE = "\r\n";
    private static final String BEGIN_VMSG = "BEGIN:VMSG";
    private static final String END_VMSG = "END:VMSG";
    private static final String VERSION = "VERSION:";
    private static final String BEGIN_VCARD = "BEGIN:VCARD";
    private static final String END_VCARD = "END:VCARD";
    private static final String BEGIN_VBODY = "BEGIN:VBODY";
    private static final String END_VBODY = "END:VBODY";
    private static final String FROMTEL = "FROMTEL:";
    private static final String XBOX = "X-BOX:";
    private static final String XREAD = "X-READ:";
    private static final String XSEEN = "X-SEEN:";
    private static final String XSIMID = "X-SIMID:";
    private static final String XLOCKED = "X-LOCKED:";
    private static final String XTYPE = "X-TYPE:";
    private static final String DATE = "Date:";
    private static final String SUBJECT = "Subject";
    //add by yuri
    private static final String PERSON = "PERSON:";//person
    private static final String PROTOCOL = "PROTOCOL:";//protocol
    private static final String STATUS = "STATUS:";//status
    private static final String REPLY_PATH_PRESENT = "REPLY_PATH_PRESENT:";//reply
    private static final String ERROR_CODE = "ERROR_CODE:";//error code
    
    private static final String VMESSAGE_END_OF_COLON = ":";

    class SmsRestoreEntry implements Comparable<SmsRestoreEntry>{
        private String timeStamp;

        private String readByte;
    
        private String seen;

        private String boxType;

        private String simCardid;
    
        private String locked;
    
        private String smsAddress;
    
        private String body;

        public  String getTimeStamp() {
            return timeStamp;
        }

        public  void setTimeStamp(String timeStamp) {
            this.timeStamp = timeStamp;
        }

        public  String getReadByte() {
            return readByte == null ? "READ" : readByte;
        }

        public  void setReadByte(String readByte) {
            this.readByte = readByte;
        }

        public String getSeen() {
            return seen == null ? "1" : seen;
        }

        public void setSeen(String seen) {
            this.seen = seen;
        }

        public  String getBoxType() {
            return boxType;
        }

        public  void setBoxType(String boxType) {
            this.boxType = boxType;
        }

        public  String getSimCardid() {
            return simCardid;
        }

        public  void setSimCardid(String simCardid) {
            this.simCardid = simCardid;
        }

        public  String getLocked() {
            return locked;
            
        }

        public  void setLocked(String locked) {
            this.locked = locked;
        }

        public  String getSmsAddress() {
            return smsAddress;
        }

        public  void setSmsAddress(String smsAddress) {
            this.smsAddress = smsAddress;
        }

        public  String getBody() {
            return body;
        }

        public  void setBody(String body) {
            this.body = body;
        }

		@Override
		public int compareTo(SmsRestoreEntry another) {
			// TODO Auto-generated method stub
			return this.timeStamp.compareTo(another.timeStamp);
		}
    }

    public ArrayList<SmsRestoreEntry> getSmsRestoreEntry() {
        ArrayList<SmsRestoreEntry> smsEntryList = new ArrayList<SmsRestoreEntry>();
        SimpleDateFormat sd = new SimpleDateFormat("yyyy/MM/dd kk:mm:ss");
        try {
            File file = new File(mParentFolderPath + File.separator + ModulePath.FOLDER_SMS + File.separator + ModulePath.SMS_VMSG);
            InputStream instream = new FileInputStream(file);
            InputStreamReader inreader = new InputStreamReader(instream);
            BufferedReader buffreader = new BufferedReader(inreader);
            String line = null;
            StringBuffer tmpbody = new StringBuffer();
            boolean appendbody = false;
            SmsRestoreEntry smsentry = null;
            while ((line = buffreader.readLine()) != null) {
                if (line.startsWith(BEGIN_VMSG) && !appendbody) {
                    smsentry = new SmsRestoreEntry();
                    Log.d(TAG,"startsWith(BEGIN_VMSG)");
                }
                if (line.startsWith(FROMTEL) && !appendbody) {
                    smsentry.setSmsAddress(line.substring(FROMTEL.length()));
                    Log.d(TAG,"startsWith(FROMTEL)");
                }
                if (line.startsWith(XBOX) && !appendbody) {
                    smsentry.setBoxType(line.substring(XBOX.length()));
                    Log.d(TAG,"startsWith(XBOX)");
                }
                if (line.startsWith(XREAD) && !appendbody) {
                    smsentry.setReadByte(line.substring(XREAD.length()));
                    Log.d(TAG,"startsWith(XREAD)");
                }
                if (line.startsWith(XSEEN) && !appendbody) {
                    smsentry.setSeen(line.substring(XSEEN.length()));
                    Log.d(TAG,"startsWith(XSEEN)");
                }
                if (line.startsWith(XSIMID) && !appendbody) {
                    smsentry.setSimCardid(line.substring(XSIMID.length()));
                    Log.d(TAG,"startsWith(XSIMID)");
                }
                if (line.startsWith(XLOCKED) && !appendbody) {
                    smsentry.setLocked(line.substring(XLOCKED.length()));
                    Log.d(TAG,"startsWith(XLOCKED)");
                }
                //                if (line.startsWith(XTYPE)) {
                //                    smsentry.set(line.substring(XTYPE.length()));
                //                    Log.d(TAG, "startsWith(XTYPE)  line.substring(XTYPE.length()) ="+line.substring(XTYPE.length()));
                //                }
                if (line.startsWith(DATE) && !appendbody) {
                    long result = sd.parse(line.substring(DATE.length())).getTime();
                    Log.d(TAG, "result.date:" + result);
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            		String dateString = format.format(new Date(result));
            		Log.d(TAG, "result.formatedate:" + result);
                    smsentry.setTimeStamp(String.valueOf(result));
                    Log.d(TAG,"startsWith(DATE)");
                }

                if (line.startsWith(SUBJECT) && !appendbody) {
                   // String bodySlash = line.substring(SUBJECT.length());
                    String bodySlash = line.substring(line.indexOf(VMESSAGE_END_OF_COLON)+1);
                    int m = bodySlash.indexOf("END:VBODY");
                    if (m > 0) {
                        StringBuffer tempssb = new StringBuffer(bodySlash);
                        do {
                            if (m > 0) {
                                tempssb.deleteCharAt(m - 1);
                            } else { 
                                break;
                            }
                        } while ((m = tempssb.indexOf("END:VBODY",m+"END:VBODY".length())) > 0 );
                        bodySlash = tempssb.toString();
                    }
                    
                    tmpbody.append(bodySlash);
                    appendbody = true;
                    Log.d(TAG,"startsWith(SUBJECT)");
                    continue;
                }
                if (line.startsWith(END_VBODY)) {
                    appendbody = false;
                    smsentry.setBody(tmpbody.toString());
                    smsEntryList.add(smsentry);
                    tmpbody.setLength(0);
                    Log.d(TAG, "startsWith(END_VBODY)");
                    continue;
                }
                if (appendbody) {
                    tmpbody.append(VMESSAGE_END_OF_LINE);
                    int n = line.indexOf("END:VBODY");
                    if (n > 0) {
                        StringBuffer tempsub = new StringBuffer(line);
                        do {
                            if (n > 0) {
                                tempsub.deleteCharAt(n - 1);
                            } else { 
                                break;
                            }
                        } while ((n = tempsub.indexOf("END:VBODY",n+"END:VBODY".length())) > 0 );
                        line = tempsub.toString();
                    }
                    tmpbody.append(line);
                    Log.d(TAG, "appendbody=true,tmpbody="+tmpbody.toString());
                }
                
            }
            instream.close();
        } catch (Exception e) {
            Log.e(TAG, "init failed");
        }
        if(smsEntryList!=null&&smsEntryList.size()>0){
        	Collections.sort(smsEntryList);
        }
        return smsEntryList;
    }
    
	public int getBackupSmsCount() {
		int result = 0;
		try {
			File file = new File(mParentFolderPath + File.separator
					+ ModulePath.FOLDER_SMS + File.separator
					+ ModulePath.SMS_VMSG);
			InputStream instream = new FileInputStream(file);
			InputStreamReader inreader = new InputStreamReader(instream);
			BufferedReader buffreader = new BufferedReader(inreader);
			String line = buffreader.readLine();
			if (line != null) {
				result = Integer.parseInt(line);
			} 
			instream.close();
		} catch (Exception e) {
			Log.e(TAG, "getBackupSmsCount.ERROR:" + e.toString());
			result = 0;
		}
		
		return result;
	}
    
    public ArrayList<SmsItem> getSmsRestoreItem() {
        ArrayList<SmsItem> smsEntryList = new ArrayList<SmsItem>();
        SimpleDateFormat sd = new SimpleDateFormat("yyyy/MM/dd kk:mm:ss");
        try {
            File file = new File(mParentFolderPath + File.separator + ModulePath.FOLDER_SMS + File.separator + ModulePath.SMS_VMSG);
            InputStream instream = new FileInputStream(file);
            InputStreamReader inreader = new InputStreamReader(instream);
            BufferedReader buffreader = new BufferedReader(inreader);
            String line = null;
            StringBuffer tmpbody = new StringBuffer();
            boolean appendbody = false;
            SmsItem smsentry = null;
            while ((line = buffreader.readLine()) != null) {
                if (line.startsWith(BEGIN_VMSG) && !appendbody) {
                    smsentry = new SmsItem();
                    Log.d(TAG,"startsWith(BEGIN_VMSG)");
                }
                if (line.startsWith(FROMTEL) && !appendbody) {
                    smsentry.setAddress(line.substring(FROMTEL.length()));
                    Log.d(TAG,"startsWith(FROMTEL)");
                }
                if (line.startsWith(XBOX) && !appendbody) {
                    smsentry.setBoxType(line.substring(XBOX.length()));
                    Log.d(TAG,"startsWith(XBOX)");
                }
                if (line.startsWith(XREAD) && !appendbody) {
                    smsentry.setReadByte(line.substring(XREAD.length()));
                    Log.d(TAG,"startsWith(XREAD)");
                }
                if (line.startsWith(XSEEN) && !appendbody) {
                    smsentry.setSeen(line.substring(XSEEN.length()));
                    Log.d(TAG,"startsWith(XSEEN)");
                }
//                if (line.startsWith(PERSON) && !appendbody) {
//                    smsentry.setPerson(line.substring(PERSON.length()));
//                    Log.d(TAG,"startsWith(PERSON)");
//                }
                if (line.startsWith(XLOCKED) && !appendbody) {
                    smsentry.setLocked(line.substring(XLOCKED.length()));
                    Log.d(TAG,"startsWith(XLOCKED)");
                }
//                if (line.startsWith(PROTOCOL) && !appendbody) {
//					smsentry.setProtocol(line.substring(PROTOCOL.length()));
//					Log.d(TAG,"startsWith(PROTOCOL)");
//				}
//                if (line.startsWith(STATUS) && !appendbody) {
//					smsentry.setStatus(line.substring(STATUS.length()));
//					Log.d(TAG,"startsWith(STATUS)");
//				}
//                if (line.startsWith(REPLY_PATH_PRESENT) && !appendbody) {
//					smsentry.setReplyPathPresent(line.substring(REPLY_PATH_PRESENT.length()));
//					Log.d(TAG,"startsWith(REPLY_PATH_PRESENT)");
//				}
//                if (line.startsWith(ERROR_CODE) && !appendbody) {
//					smsentry.setErrorCode(line.substring(ERROR_CODE.length()));
//					Log.d(TAG,"startsWith(ERROR_CODE)");
//				}
                if (line.startsWith(DATE) && !appendbody) {
                    long result = sd.parse(line.substring(DATE.length())).getTime();
                    smsentry.setDate(String.valueOf(result));
                    Log.d(TAG,"startsWith(DATE)");
                }

                if (line.startsWith(SUBJECT) && !appendbody) {
                   // String bodySlash = line.substring(SUBJECT.length());
                    String bodySlash = line.substring(line.indexOf(VMESSAGE_END_OF_COLON)+1);
                    int m = bodySlash.indexOf("END:VBODY");
                    if (m > 0) {
                        StringBuffer tempssb = new StringBuffer(bodySlash);
                        do {
                            if (m > 0) {
                                tempssb.deleteCharAt(m - 1);
                            } else { 
                                break;
                            }
                        } while ((m = tempssb.indexOf("END:VBODY",m+"END:VBODY".length())) > 0 );
                        bodySlash = tempssb.toString();
                    }
                    
                    tmpbody.append(bodySlash);
                    appendbody = true;
                    Log.d(TAG,"startsWith(SUBJECT)");
                    continue;
                }
                if (line.startsWith(END_VBODY)) {
                    appendbody = false;
                    smsentry.setBody(tmpbody.toString());
                    smsEntryList.add(smsentry);
                    tmpbody.setLength(0);
                    Log.d(TAG, "startsWith(END_VBODY)");
                    continue;
                }
                if (appendbody) {
                    tmpbody.append(VMESSAGE_END_OF_LINE);
                    int n = line.indexOf("END:VBODY");
                    if (n > 0) {
                        StringBuffer tempsub = new StringBuffer(line);
                        do {
                            if (n > 0) {
                                tempsub.deleteCharAt(n - 1);
                            } else { 
                                break;
                            }
                        } while ((n = tempsub.indexOf("END:VBODY",n+"END:VBODY".length())) > 0 );
                        line = tempsub.toString();
                    }
                    tmpbody.append(line);
                    Log.d(TAG, "appendbody=true,tmpbody="+tmpbody.toString());
                }
                
            }
            instream.close();
        } catch (Exception e) {
            Log.e(TAG, "init failed");
        }
        if(smsEntryList!=null&&smsEntryList.size()>0){
        	Collections.sort(smsEntryList);
        }
        return smsEntryList;
    }

}




