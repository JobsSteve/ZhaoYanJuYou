package com.zhaoyan.juyou.common;

import java.io.File;

import android.content.Context;

import com.zhaoyan.common.file.FileManager;
import com.zhaoyan.common.util.Log;
import com.zhaoyan.juyou.R;

public class FileCategoryHelper {
    private static final String TAG = "FileCategoryHelper";

    public enum FileCategory {
        Audio, Video, Image, Doc, Ppt, Xls, Pdf, Archive, Apk, Ebook, Other
    }

    public static FileCategory getCategoryFromPath(Context context, String filepath){
    	// 首先取得文件名
    	String fileName = new File(filepath).getName();
    	return getCategoryByName(context, fileName);
    }
    
    public static FileCategory getCategoryByName(Context context, String fileName) {
    	String ext = FileManager.getExtFromFilename(fileName);
    	Log.d(TAG, "getCategoryByName.ext:" + ext);
		if (matchExts(ext, context.getResources()
				.getStringArray(R.array.fileEndingEbook))) {
			return FileCategory.Ebook;
		}
		
		if (matchExts(ext, context.getResources()
				.getStringArray(R.array.fileEndingImage))) {
			return FileCategory.Image;
		}
		
		if (matchExts(ext, context.getResources()
				.getStringArray(R.array.fileEndingAudio))) {
			return FileCategory.Audio;
		}
		
		if (matchExts(ext, context.getResources()
				.getStringArray(R.array.fileEndingVideo))) {
			return FileCategory.Video;
		}
		
		if (matchExts(ext, context.getResources()
				.getStringArray(R.array.fileEndingApk))) {
			return FileCategory.Apk;
		} 
		
		if (matchExts(ext, context.getResources()
				.getStringArray(R.array.fileEndingWord))) {
			return FileCategory.Doc;
		} 
		
		if (matchExts(ext, context.getResources()
				.getStringArray(R.array.fileEndingPpt))) {
			return  FileCategory.Ppt;
		} 
		
		if (matchExts(ext, context.getResources()
				.getStringArray(R.array.fileEndingExcel))) {
			return FileCategory.Xls;
		} 
		
		if (matchExts(ext, context.getResources()
				.getStringArray(R.array.fileEndingArchive))) {
			return FileCategory.Archive;
		} 
		
		if (matchExts(ext, context.getResources()
				.getStringArray(R.array.fileEndingPdf))) {
			return FileCategory.Pdf;
		} 

		return FileCategory.Other;
	}

	private static boolean matchExts(String ext,String[] exts) {
		for (String ex : exts) {
			if (ex.equalsIgnoreCase(ext))
				return true;
		}
		return false;
	}
}
