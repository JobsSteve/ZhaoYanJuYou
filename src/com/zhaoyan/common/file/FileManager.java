package com.zhaoyan.common.file;

import java.io.File;

import android.content.Context;

import com.zhaoyan.juyou.R;

public class FileManager {
	private static final String TAG = "FileManager";
	public static final int TEXT = 0x01;
	public static final int HTML = 0x02;
	public static final int WORD = 0x03;
	public static final int EXCEL = 0x04;
	public static final int PPT = 0x05;
	public static final int PDF = 0x06;
	public static final int AUDIO = 0x07;
	public static final int VIDEO = 0x08;
	public static final int CHM = 0x09;
	public static final int APK = 0x10;
	public static final int ARCHIVE = 0x11;
	public static final int IMAGE = 0x12;
	public static final int UNKNOW = 0x20;

	public static final int TYPE_DEFAULT = 0x20;// 32
	public static final int TYPE_EBOOK = 0x21;
	public static final int TYPE_VIDEO = 0x22;
	public static final int TYPE_DOC = 0x23;
	public static final int TYPE_APK = 0x24;// 36
	public static final int TYPE_ARCHIVE = 0x25;
	public static final int TYPE_BIG_FILE = 0x26;
	public static final int TYPE_IMAGE = 0x27;
	public static final int TYPE_AUDIO = 0x28;// 40

	public static int getFileType(Context context, String filepath) {
		return getFileType(context, new File(filepath));
	}

	public static int getFileType(Context context, File file) {
		int fileNameExtension = fileFilter(context, file);
		return getFileTypeByFileNameExtension(fileNameExtension);
	}

	private static int fileFilter(Context context, File file) {
		String fileName = file.getName();
		int ret;

		if (checkEndsWithInStringArray(fileName, context.getResources()
				.getStringArray(R.array.fileEndingEbook))) {
			// text
			ret = TEXT;
		} else if (checkEndsWithInStringArray(fileName, context.getResources()
				.getStringArray(R.array.fileEndingImage))) {
			// Images
			ret = IMAGE;
		} else if (checkEndsWithInStringArray(fileName, context.getResources()
				.getStringArray(R.array.fileEndingAudio))) {
			// audios
			ret = AUDIO;
		} else if (checkEndsWithInStringArray(fileName, context.getResources()
				.getStringArray(R.array.fileEndingVideo))) {
			// videos
			ret = VIDEO;
		} else if (checkEndsWithInStringArray(fileName, context.getResources()
				.getStringArray(R.array.fileEndingApk))) {
			// apk
			ret = APK;
		} else if (checkEndsWithInStringArray(fileName, context.getResources()
				.getStringArray(R.array.fileEndingWord))) {
			// word
			ret = WORD;
		} else if (checkEndsWithInStringArray(fileName, context.getResources()
				.getStringArray(R.array.fileEndingPpt))) {
			// ppt
			ret = PPT;
		} else if (checkEndsWithInStringArray(fileName, context.getResources()
				.getStringArray(R.array.fileEndingExcel))) {
			// excel
			ret = EXCEL;
		} else if (checkEndsWithInStringArray(fileName, context.getResources()
				.getStringArray(R.array.fileEndingArchive))) {
			// packages
			ret = ARCHIVE;
		} else if (checkEndsWithInStringArray(fileName, context.getResources()
				.getStringArray(R.array.fileEndingPdf))) {
			// pdf
			ret = PDF;
		} else {
			ret = UNKNOW;
		}

		return ret;
	}

	/**
	 * Check the given file name whether match the file suffix name array.
	 * 
	 * @param checkItsEnd
	 * @param fileEndings
	 * @return
	 */
	private static boolean checkEndsWithInStringArray(String checkItsEnd,
			String[] fileEndings) {
		String str = checkItsEnd.toLowerCase();
		for (String aEnd : fileEndings) {
			if (str.endsWith(aEnd))
				return true;
		}
		return false;
	}

	private static int getFileTypeByFileNameExtension(int fileNameExtension) {
		int fileType = TYPE_DEFAULT;
		switch (fileNameExtension) {
		case TEXT:
			fileType = TYPE_EBOOK;
			break;
		case IMAGE:
			fileType = TYPE_IMAGE;
			break;
		case AUDIO:
			fileType = TYPE_AUDIO;
			break;
		case VIDEO:
			fileType = TYPE_VIDEO;
			break;
		case WORD:
			fileType = TYPE_DOC;
			break;
		case PPT:
			fileType = TYPE_DOC;
			break;
		case EXCEL:
			fileType = TYPE_DOC;
			break;
		case PDF:
			fileType = TYPE_DOC;
			break;
		case ARCHIVE:
			fileType = TYPE_ARCHIVE;
			break;
		case APK:
			fileType = TYPE_APK;
			break;
		default:
			break;
		}
		return fileType;
	}
}
