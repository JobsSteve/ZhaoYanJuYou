package com.zhaoyan.juyou.common;

import java.io.File;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Environment;

import com.zhaoyan.common.util.Log;
import com.zhaoyan.juyou.common.DevMountInfo.DevInfo;
import com.zhaoyan.juyou.common.ZYConstant.Extra;

/*
 * (PS:以下方法仅为个人理解，不能保证100%准确，因为没有足够的机器或者资料证明，仅根据Google nexus，phiee机器验证而得)
 * Q1：如何判断该手机是否有支持外置SDCARD？ </br>
 * A：猜测，目前无法得知是否正确，通过判断/etc/vold.fstab文件是否存在，如果不存在，表示不支持外置sdcard，存在表示支持sdcard  </br>
 * Q2：如何判断是否存在内置sdcard？ </br>
 * A: 1.如果不支持外置sdcard，直接通过Environment.getExternalStorageState()判断是否mounted，如果mount表示有内存sdcard，否则没有</br>
 * 	2.如果支持外置sdcard，第一步也是直接判断Environment.getExternalStorageState()如果unmounted，则肯定不存在内置sdcard，而且外置sdcard也未挂载</br>
 *		如果是mount，只能说明有sdcard，但是你不能确定是内置sdcard还是外置sdcard或者两者都有 </br>
 *		到了这里，我们就要执行第二步判断 </br>
 *		第二步，首先我们可以肯定/etc/vold.fstab文件时肯定存在的 </br>
 *		所以我们就要去读取/etc/vold.fstab文件，</br>
 *	 以下便是/etc/vold.fstab(来自phiee850) </br>
 *其中有两句便是sdcard的挂载信息(这个一般是由厂商写入的，当然用户也可以修改，需要root，但不推荐)
dev_mount sdcard /storage/sdcard0 emmc@fat /devices/platform/goldfish_mmc.0 /devices/platform/mtk-msdc.0/mmc </br>
dev_mount sdcard2 /storage/sdcard1 auto /devices/platform/goldfish_mmc.1 /devices/platform/mtk-msdc.1/mmc_ho </br>
 * 你可以读到两行信息，像上面一样，我们可以确定肯定有内置sdcard和外置sdcard，而且第一行表示内置sdcard的信息，第二行的是外置sdcard  </br>
 * (PS:一般情况下，我们都会认为第一行表示的是内置的sdcard，但是谁又能确保呢)
 * 但是，到了这里，你无法确定外置sdcard是否挂载，我们就可以通过new File( /storage/sdcard1).canWrite()来电判断外置sdcard是否挂载 </br>
 * 因为如果外置sdcard挂载了的话，肯定是可写的</br>
 * 如果只有一行的话，那么一是，不存在内置sdcard，二是外置sdcard肯定挂载</br>
 * 不可能有三行吧，没见过，内置一个sdcard，再有两个sdcard扩展卡？或者 不知道，暂时不这么考虑吧</br>
 */
public class MountManager {
	private static final String TAG = "MountManager";
	private  String sdcard_path;
	private  String internal_path;
	public static final String SEPERATOR = "/";
	public static final String NO_EXTERNAL_SDCARD = "no_external_sdcard | no_mounted";
	public static final String NO_INTERNAL_SDCARD = "no_internal_sdcard";
	
	public static final int INTERNAL = 0;
	public static final int SDCARD = 1;
	private DevMountInfo mDevMountInfo;
	private DevInfo devInfo;
	private SharedPreferences sp = null;
	
	public MountManager(Context context) {
		sp = context.getSharedPreferences(Extra.SHARED_PERFERENCE_NAME, Context.MODE_PRIVATE);
	}
	
	public void init() {
		mDevMountInfo = DevMountInfo.getInstance();

		if (mDevMountInfo.isExistExternal()) {
			Log.d(TAG, "isExistExternal");
			if (isSdcardMounted()) {
				//可以肯定存在sdcard，支持外置sdcard
				DevInfo exDevInfo = mDevMountInfo.getExternalInfo();
				DevInfo interDevInfo = mDevMountInfo.getInternalInfo();
				DevInfo devInfo = mDevMountInfo.getDevInfo();
//				SDCARD_PATH = exDevInfo.getPath();
				sdcard_path = devInfo.getExterPath();
				internal_path = devInfo.getInterPath();
				Log.i(TAG, "SDCARD_PATH:" +  sdcard_path);
				Log.i(TAG, "INTERNAL_PATH:" +  internal_path);
				if (!new File(sdcard_path).canWrite()) {
					//外置sdcard未挂载
					sdcard_path = NO_EXTERNAL_SDCARD;
				}
				
				if (!new File(internal_path).canWrite()) {
					internal_path = NO_INTERNAL_SDCARD;
				}
				Log.d(TAG, "SDCARD_PATH=" + sdcard_path);
				Log.d(TAG, "INTERNAL_PATH=" + internal_path);
			} else {
				//不存在内置sdcard，而且外置sdcard也未挂载
				Log.e(TAG, "ther is no sdcard");
				internal_path = NO_INTERNAL_SDCARD;
				sdcard_path = NO_EXTERNAL_SDCARD;
			}
			
		} else {
			//不支持外置sdcard
			if (isSdcardMounted()) {
				//存在内置sdcard
				internal_path = Environment.getExternalStorageDirectory().getAbsolutePath();
				sdcard_path = NO_EXTERNAL_SDCARD;
			}else {
				//不存在内置sdcard
				internal_path = NO_INTERNAL_SDCARD;
				sdcard_path = NO_EXTERNAL_SDCARD;
			}
		}
		Editor editor = sp.edit();
		editor.putString(Extra.SDCARD_PATH, sdcard_path);
		editor.putString(Extra.INTERNAL_PATH, internal_path);
		editor.commit();
	}
	
	/***
	 * is external exist
	 */
	public static  boolean isSdcardMounted(){
		String state = Environment.getExternalStorageState();
		Log.d(TAG, "isSdcardMounted:" + state + "\n" + Environment.getExternalStorageDirectory().getAbsolutePath());
		return Environment.MEDIA_MOUNTED.equals(state);
	}
	
	/**
	 * get show path</br>
	 * like: /mnt/sdcard/JuYou</br>
	 * you can get return /JuYou
	 * @param rootPath current root path    like:/mnt/sdcard/
	 * @param path  current path  like: /mnt/sdcard/JuYou
	 * @return  /JuYou
	 */
	public String getShowPath(String rootPath, String path){
		int len = rootPath.length();
		String result = path.substring(len);
		Log.d(TAG, "getShowPath=" + result);
		return result;
	}
}
