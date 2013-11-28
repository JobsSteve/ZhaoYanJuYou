/*
 * Copyright (c) 2010-2011, The MiCode Open Source Community (www.micode.net)
 *
 * This file is part of FileExplorer.
 *
 * FileExplorer is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FileExplorer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with SwiFTP.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.zhaoyan.juyou.common;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.zhaoyan.common.net.http.Util;
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
