package com.zhaoyan.juyou.backuprestore;

import android.net.Uri;

public class SmsField {
	public static final String ADDRESS = "address";  
    public static final String PERSON = "person";  
    public static final String DATE = "date";  
    public static final String PROTOCOL = "protocol";  
    public static final String READ = "read";  
    public static final String STATUS = "status";  
    public static final String TYPE = "type";  
    public static final String REPLY_PATH_PRESENT = "reply_path_present";  
    public static final String BODY = "body";  
    public static final String LOCKED = "locked";  
    public static final String ERROR_CODE = "error_code";  
    public static final String SEEN = "seen";  
    public static final String SC = "service_center";//��������
    
    public static final String[] PROJECTION = new String[]{
			SmsField.ADDRESS, SmsField.PERSON, SmsField.DATE, SmsField.PROTOCOL,   
            SmsField.READ, SmsField.STATUS, SmsField.TYPE, SmsField.REPLY_PATH_PRESENT,  
            SmsField.BODY,SmsField.LOCKED,SmsField.ERROR_CODE, SmsField.SEEN,SmsField.SC
            // type=1���ռ��䣬==2�Ƿ�����;read=0��ʾδ����read=1��ʾ����seen=0��ʾδ����seen=1��ʾ����
            };
    
//    public static final String SMS_URI = "content://sms/";
    public static final Uri SMS_URI = Uri.parse("content://sms/");
    public static final Uri SMS_INBOX_URI = Uri.parse("content://mms/inbox");//�ռ���
    public static final Uri SMS_SENT_URI = Uri.parse("content://mms/sent");  //������
    
    public static final String sms_bakup = "sms_backup.xml";
}
