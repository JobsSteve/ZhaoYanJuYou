package com.zhaoyan.juyou.common;

import java.io.File;

import com.zhaoyan.common.file.FileManager;

public class FileCategoryHelper {

    public enum FileCategory {
        Audio, Video, Image, Word, Ppt, Excel, Pdf, Archive, Apk, Ebook, Other
    }
    
    public static final String[] IMAGE_EXTS = {"png","jpg","bmp","jpeg","gif","mpo"};
    public static final String[] AUDIO_EXTS = {"mp3","wav","ogg","midi","wma","amr"};
    public static final String[] VIDEO_EXTS = {"mp4","rm","rmvb","3gpp","3gp","avi","mpeg","mpg","mov","mkv","flv","swf","asf","wmv"};
    public static final String[] ARCHIVE_EXTS = {"rar","zip"};
    public static final String[] EBOOK_EXTS = {"txt","uml","ebk","chm"};
    public static final String[] WORD_EXTS = {"doc","docx"};
    public static final String[] PPT_EXTS = {"ppt","pptx","pps","ppsx"};
    public static final String[] EXCEL_EXTS = {"xls","xlsx"};
    public static final String[] PDF_EXTS = {"pdf"};
    public static final String[] APK_EXTS = {"apk"};

    public static FileCategory getCategoryFromPath(String filepath){
    	// 首先取得文件名
    	String fileName = new File(filepath).getName();
    	return getCategoryByName(fileName);
    }
    
    public static FileCategory getCategoryByName(String fileName) {
    	String ext = FileManager.getExtFromFilename(fileName);
		if (matchExts(ext, EBOOK_EXTS)) return FileCategory.Ebook;
		if (matchExts(ext, IMAGE_EXTS)) return FileCategory.Image;
		if (matchExts(ext, AUDIO_EXTS)) return FileCategory.Audio;
		if (matchExts(ext, VIDEO_EXTS)) return FileCategory.Video;
		if (matchExts(ext, APK_EXTS)) 	return FileCategory.Apk;
		if (matchExts(ext, WORD_EXTS)) 	return FileCategory.Word;
		if (matchExts(ext, PPT_EXTS)) 	return FileCategory.Ppt;
		if (matchExts(ext, EXCEL_EXTS))	return FileCategory.Excel;
		if (matchExts(ext, ARCHIVE_EXTS))return FileCategory.Archive;
		if (matchExts(ext, PDF_EXTS)) return FileCategory.Pdf;
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
