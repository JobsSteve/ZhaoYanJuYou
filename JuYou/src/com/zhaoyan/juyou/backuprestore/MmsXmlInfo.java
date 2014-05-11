
package com.zhaoyan.juyou.backuprestore;

public class MmsXmlInfo {
    private static final String TAG = "RestoreService";

    public static class MmsXml {
        public static final String ROOT = "mms";
        public static final String RECORD = "record";
        //public static final String CATEGORY = "category";
        public static final String ID = "_id";
        public static final String ISREAD = "isread";
        //public static final String LOCALDATE = "local_date";
        //public static final String ST = "st";
        public static final String MSGBOX = "msg_box";
        public static final String DATE = "date";
        public static final String SIZE = "m_size";
        public static final String SIMID = "sim_id";
        public static final String ISLOCKED = "islocked";
    }

    //private String mCategory;
    private String mId;
    private String mIsRead;
    //private String mLocalDate;
    //private String mST;
    private String mMsgBox;
    private String mDate;
    private String mSize;
    private String mSimId;
    private String mIsLocked;    

    // public void setCategory(String category) {
    //     mCategory = category;
    // }

    // public String getCategory() {
    //     return (mCategory == null) ? "0" : mCategory;
    // }

    public void setID(String id) {
        mId = id;
    }

    public String getID() {
        return (mId == null) ? "" : mId;
    }

    public void setIsRead(String isread) {
        mIsRead = isread;
    }

    public String getIsRead() {
        return ((mIsRead == null) || mIsRead.equals("")) ? "1" : mIsRead;
    }

    // public void setLocalDate(String date) {
    //     mLocalDate = date;
    // }

    // public String getLocalDate() {
    //     return (mLocalDate == null) ? "" : mLocalDate;
    // }

    // public void setST(String st) {
    //     mST = st;
    // }

    // public String getST() {
    //     return (mST == null) ? "" : mST;
    // }

    public void setMsgBox(String msgBox) {
        mMsgBox = msgBox;
    }

    public String getMsgBox() {
        return ((mMsgBox == null) || mMsgBox.equals("")) ? "1" : mMsgBox;
    }

    public void setDate(String date) {
        mDate = date;
    }

    public String getDate() {
        return (mDate == null) ? "" : mDate;
    }

    public void setSize(String size) {
        mSize = size;
    }

    public String getSize() {
        return ((mSize == null) || mSize.equals("")) ? "0" : mSize;
    }

    public void setSimId(String simId) {
        mSimId = simId;
    }

    public String getSimId() {
        return ((mSimId == null) || mSimId.equals("")) ? "0" : mSimId;
    }

    public void setIsLocked(String islocked) {
        mIsLocked = islocked;
    }

    public String getIsLocked() {
        return ((mIsLocked == null) || mIsLocked.equals("")) ? "0" : mIsLocked;
    }
}
