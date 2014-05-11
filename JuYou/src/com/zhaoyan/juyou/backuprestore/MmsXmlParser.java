
package com.zhaoyan.juyou.backuprestore;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import com.zhaoyan.common.util.Log;
import com.zhaoyan.juyou.backuprestore.MmsXmlInfo.MmsXml;

import java.util.ArrayList;
import java.io.IOException;
import java.io.StringReader;

public class MmsXmlParser {
    private static final String TAG = "MmsXmlParser";

    public static ArrayList<MmsXmlInfo> parse(String mmsString) {
        MmsXmlInfo record = null;
        ArrayList<MmsXmlInfo> list = new ArrayList<MmsXmlInfo>();
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = factory.newPullParser();
            parser.setInput(new StringReader(mmsString));

            int eventType = parser.getEventType();
            String tagName = "";
            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                case XmlPullParser.START_DOCUMENT:
                    break;

                case XmlPullParser.START_TAG:
                    record = new MmsXmlInfo();
                    tagName = parser.getName();
                    if (tagName.equals(MmsXml.RECORD)) {
                        int attrNum = parser.getAttributeCount();
                        for (int i = 0; i < attrNum; ++i) {
                            String name = parser.getAttributeName(i);
                            String value = parser.getAttributeValue(i);
                            if (name.equals(MmsXml.ID)) {
                                record.setID(value);
                            } else if (name.equals(MmsXml.ISREAD)) {
                                record.setIsRead(value);
                            } else if (name.equals(MmsXml.MSGBOX)) {
                                record.setMsgBox(value);
                            } else if (name.equals(MmsXml.DATE)) {
                                record.setDate(value);
                            } else if (name.equals(MmsXml.SIZE)) {
                                record.setSize(value);
                            } else if (name.equals(MmsXml.SIMID)) {
                                record.setSimId(value);
                            } else if (name.equals(MmsXml.ISLOCKED)) {
                                record.setIsLocked(value);
                            }

                            Log.d(TAG,  "name:" + name + ",value:" + value);
                        }
                    }
                    break;

                case XmlPullParser.END_TAG:
                    if (parser.getName().equals(MmsXml.RECORD) && record != null) {
                        list.add(record);
                    }
                    break;

                case XmlPullParser.END_DOCUMENT:
                    break;

                default:
                    break;
                }

                eventType = parser.next();
            }
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return list;
    }
}
