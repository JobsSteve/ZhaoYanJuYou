package com.zhaoyan.juyou.backuprestore;

import android.util.Xml;

import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.io.StringWriter;

/**
 * Describe class RecordXmlComposer here.
 * 
 * @author
 * @version 1.0
 */
public class RecordXmlComposer {
    private XmlSerializer mSerializer = null;
    private StringWriter mStringWriter = null;

    /**
     * Creates a new <code>RecordXmlComposer</code> instance.
     * 
     */
    public RecordXmlComposer() {
    }

    public boolean startCompose() {
        boolean result = false;
        mSerializer = Xml.newSerializer();
        mStringWriter = new StringWriter();
        try {
            mSerializer.setOutput(mStringWriter);
            // serializer.startDocument("UTF-8", null);
            mSerializer.startDocument(null, false);
            mSerializer.startTag("", RecordXmlInfo.ROOT);
            result = true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    public boolean endCompose() {
        boolean result = false;
        try {
            mSerializer.endTag("", RecordXmlInfo.ROOT);
            mSerializer.endDocument();
            result = true;
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    public boolean addOneRecord(RecordXmlInfo record) {
        boolean result = false;
        try {
            if (record.isRestore()) {
                mSerializer.startTag("", RecordXmlInfo.RESTORE);
            } else {
                mSerializer.startTag("", RecordXmlInfo.BACKUP);
            }
            
            if (record.getDevice() != null) {
                mSerializer.attribute("", RecordXmlInfo.DEVICE, record.getDevice());
            }

            if (record.getTime() != null) {
                mSerializer.attribute("", RecordXmlInfo.TIME, record.getTime());
            }

            if (record.isRestore()) {
                mSerializer.endTag("", RecordXmlInfo.RESTORE);
            } else {
                mSerializer.endTag("", RecordXmlInfo.BACKUP);
            }
            result = true;
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    public String getXmlInfo() {
        if (mStringWriter != null) {
            return mStringWriter.toString();
        }
        return null;
    }

}
