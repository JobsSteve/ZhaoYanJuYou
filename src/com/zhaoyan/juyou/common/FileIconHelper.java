package com.zhaoyan.juyou.common;


import java.util.HashMap;

import android.content.Context;
import android.widget.ImageView;

import com.zhaoyan.common.file.FileManager;
import com.zhaoyan.common.util.Log;
import com.zhaoyan.juyou.R;
import com.zhaoyan.juyou.common.FileCategoryHelper.FileCategory;
import com.zhaoyan.juyou.common.FileIconLoader.IconLoadFinishListener;

public class FileIconHelper implements IconLoadFinishListener {

    private static final String TAG = "FileIconHelper";
    private static HashMap<String, Integer> fileExtToIcons = new HashMap<String, Integer>();

    private FileIconLoader mIconLoader;
    
    private static String[] mImageExts;
    private static String[] mAudioExts;
    private static String[] mVideoExts;
    private static String[] mApkExts;
    private static String[] mEbookExts;
    private static String[] mDocExts;
    private static String[] mPptExts;
    private static String[] mXlsExts;
    private static String[] mPdfExts;
    private static String[] mArchiveExts;
    
    private Context context;

    static {
        addItem(FileCategory.Audio, R.drawable.icon_audio);
        addItem(FileCategory.Video, R.drawable.icon_video);
        addItem(FileCategory.Image, R.drawable.icon_image);
        addItem(FileCategory.Ebook, R.drawable.icon_txt);
        addItem(FileCategory.Doc, R.drawable.icon_doc);
        addItem(FileCategory.Ppt, R.drawable.icon_ppt);
        addItem(FileCategory.Xls, R.drawable.icon_xls);
        addItem(FileCategory.Apk, R.drawable.icon_apk);
        addItem(FileCategory.Archive, R.drawable.icon_rar);
        addItem(FileCategory.Pdf, R.drawable.icon_pdf);
    }

    public FileIconHelper(Context context) {
    	this.context = context;
    	
        mIconLoader = new FileIconLoader(context, this);
        //init Exts
        mImageExts = context.getResources().getStringArray(R.array.fileEndingImage);
        mAudioExts = context.getResources().getStringArray(R.array.fileEndingAudio);
        mVideoExts = context.getResources().getStringArray(R.array.fileEndingVideo);
        mApkExts = context.getResources().getStringArray(R.array.fileEndingApk);
        mEbookExts = context.getResources().getStringArray(R.array.fileEndingEbook);
        mDocExts = context.getResources().getStringArray(R.array.fileEndingWord);
        mPptExts = context.getResources().getStringArray(R.array.fileEndingPpt);
        mXlsExts = context.getResources().getStringArray(R.array.fileEndingExcel);
        mPdfExts = context.getResources().getStringArray(R.array.fileEndingPdf);
        mArchiveExts = context.getResources().getStringArray(R.array.fileEndingArchive);
    }

	private static void addItem(FileCategory cate, int resId) {
		String[] exts = null;
		switch (cate) {
		case Image:
			exts = mImageExts;
			break;
		case Audio:
			exts = mAudioExts;
			break;
		case Video:
			exts = mVideoExts;
			break;
		case Apk:
			exts = mApkExts;
			break;
		case Ebook:
			exts = mEbookExts;
			break;
		case Doc:
			exts = mDocExts;
			break;
		case Ppt:
			exts = mPptExts;
			break;
		case Xls:
			exts = mXlsExts;
			break;
		case Pdf:
			exts = mPdfExts;
			break;
		case Archive:
			exts = mArchiveExts;
			break;
		case Other:
			break;
		default:
			break;
		}
		
		if (exts != null) {
			for (String ext : exts) {
				fileExtToIcons.put(ext.toLowerCase(), resId);
			}
		}
	}

    public static int getFileIcon(String ext) {
        Integer i = fileExtToIcons.get(ext.toLowerCase());
        if (i != null) {
            return i.intValue();
        } else {
            return R.drawable.icon_file;
        }

    }

    public void setIcon(FileInfo fileInfo, ImageView fileImage) {
        String filePath = fileInfo.filePath;
        String ext = FileManager.getExtFromFilename(fileInfo.fileName);
        Log.d(TAG, "setIcon.ext:" + ext);
        FileCategory fc = FileCategoryHelper.getCategoryByName(context, fileInfo.fileName);
        boolean set = false;
        int id = getFileIcon(ext);
        fileImage.setImageResource(id);
        Log.d(TAG, "setIcon.fc:" + fc);
        mIconLoader.cancelRequest(fileImage);
        switch (fc) {
            case Apk:
                set = mIconLoader.loadIcon(fileImage, filePath, fc);
                break;
            case Image:
            case Video:
                set = mIconLoader.loadIcon(fileImage, filePath, fc);
                if (!set){
                    fileImage.setImageResource(fc == FileCategory.Image ? R.drawable.icon_image
                            : R.drawable.icon_video);
                    set = true;
                }
                break;
            default:
                set = true;
                break;
        }

        if (!set)
            fileImage.setImageResource(R.drawable.icon_file);
    }

    @Override
    public void onIconLoadFinished(ImageView view) {
    	Log.d(TAG, "onIconLoadFinished");
    }

}
