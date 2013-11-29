package com.zhaoyan.juyou.common;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.zhaoyan.common.util.ZYUtils;
import com.zhaoyan.juyou.R;

public class FileListItem {
    public static void setupFileListItemInfo(Context context, View view,
            FileInfo fileInfo, FileIconHelper fileIcon) {

        setText(view, R.id.file_name_textview, fileInfo.fileName);
        
    	String size = ZYUtils.getFormatSize(fileInfo.fileSize);
		String date = ZYUtils.getFormatDate(fileInfo.fileDate);
        setText(view, R.id.file_info_textview, fileInfo.isDir ? date : date + " | " + size);

        ImageView lFileImage = (ImageView) view.findViewById(R.id.file_icon_imageview);

        if (fileInfo.isDir) {
            lFileImage.setImageResource(R.drawable.icon_folder);
        } else {
            fileIcon.setIcon(fileInfo, lFileImage);
        }
    }
    
    private static boolean setText(View view, int resId, String text){
    	 TextView textView = (TextView) view.findViewById(resId);
         if (textView == null)
             return false;

         textView.setText(text);
         return true;
    }
}
