package com.zhaoyan.juyou.adapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.R.integer;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.zhaoyan.juyou.R;
import com.zhaoyan.juyou.common.AsyncPictureLoader;
import com.zhaoyan.juyou.common.ImageInfo;
import com.zhaoyan.juyou.common.ZYConstant;

public class ImageItemAdapter extends BaseAdapter {
	private static final String TAG = "PictureItemAdapter";
	private LayoutInflater mInflater = null;
	private Resources mResources = null;
	private List<ImageInfo> mDataList;
	private ContentResolver contentResolver;
	private AsyncPictureLoader pictureLoader;
    private List<Integer> lstPosition = new ArrayList<Integer>();
    private List<Boolean> lstLoadedBitmap = new ArrayList<Boolean>();  
    private List<View> lstView = new ArrayList<View>();
	private boolean mIdleFlag = true;
	public static int MAX_CACHED_VIEW_NUM = 70;
	public static boolean USE_VIEW_CACHE = false;
	public static HashMap<Long, View> sViewCaches;
	/**save status of item selected*/
	private SparseBooleanArray mSelectArray;
	/**current menu mode,ZYConstant.MENU_MODE_NORMAL,ZYConstant.MENU_MODE_EDIT*/
	private int mMenuMode = ZYConstant.MENU_MODE_NORMAL;

	public ImageItemAdapter(Context context, List<ImageInfo> itemList){
		mInflater = LayoutInflater.from(context);
		mResources = context.getResources();
		mDataList = itemList;
		contentResolver = context.getContentResolver();
		pictureLoader = new AsyncPictureLoader(context);
		sViewCaches = new HashMap<Long, View>();
		mSelectArray = new SparseBooleanArray();
	}
	
	public void changeMode(int mode){
		mMenuMode = mode;
	}
	
	public boolean isMode(int mode){
		return mMenuMode == mode;
	}
	
	/**
	 * Select All or not
	 * @param isSelected true or false
	 */
	public void selectAll(boolean isSelected){
		int count = this.getCount();
		for (int i = 0; i < count; i++) {
			setSelected(i, isSelected);
		}
	}
	
	/**
	 * set the item is selected or not
	 * @param position the position that clicked
	 * @param isSelected selected or not
	 */
	public void setSelected(int position, boolean isSelected){
		mSelectArray.put(position, isSelected);
	}
	
	public void setSelected(int position){
		mSelectArray.put(position, !isSelected(position));
	}
	
	public boolean isSelected(int position){
		return mSelectArray.get(position);
	}
	
	public int getSelectedItemsCount(){
		int count = 0;
		for (int i = 0; i < mSelectArray.size(); i++) {
			if (mSelectArray.valueAt(i)) {
				count ++;
			}
		}
		return count;
	}
	
	public List<Integer> getSelectedItemsPos(){
		List<Integer> list = new ArrayList<Integer>();
		for (int i = 0; i < mSelectArray.size(); i++) {
			if (mSelectArray.valueAt(i)) {
				list.add(i);
			}
		}
		return list;
	}
	
	public List<String> getSelectedItemsNameList(){
		List<String> list = new ArrayList<String>();
		for (int i = 0; i < mSelectArray.size(); i++) {
			if (mSelectArray.valueAt(i)) {
				list.add(mDataList.get(i).getDisplayName());
			}
		}
		return list;
	}
	
	public List<String> getSelectedItemsPathList(){
		List<String> list = new ArrayList<String>();
		for (int i = 0; i < mSelectArray.size(); i++) {
			if (mSelectArray.valueAt(i)) {
				list.add(mDataList.get(i).getPath());
			}
		}
		return list;
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mDataList.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		View view = null;

/*
		if (sViewCaches.containsKey(id)) {
			System.out.println("$$$$$$$$ getView $$$ 333 $$$ sViewCaches.containsKey ="+id);
			view = sViewCaches.get(id);
			if (null != view) {
				System.out.println("bindView $$$$$$$$ containsKey $$$ before return $$$ id = "+id);
			}
			return;
		} else {
			System.out.println("$$$$$$$$ bindView $$$ 444 $$$ !!sViewCaches.containsKey = "+id);
			sViewCaches.put(id, view);
		}
*/

/*
		if (position != parent.getChildCount()) {
			return view;
		}
	*/	
		long id = mDataList.get(position).getImage_id();
		if (mIdleFlag) {
			int count = lstPosition.size();
			if (USE_VIEW_CACHE) {
				if(count > MAX_CACHED_VIEW_NUM) { //�������û����Item����	
					int startPos = lstPosition.get(0);
					int endPos = lstPosition.get(count-1);
					int total = mDataList.size();
					boolean atLeftPos = (position - startPos) < (endPos - position);
					if (!atLeftPos) {					   //��ǰλ�ÿ���
						lstPosition.remove(0);			  //ɾ��ǰ��2�������Ӻ���һ��
						lstPosition.remove(0);
						lstView.remove(0);
						lstView.remove(0);
						lstLoadedBitmap.remove(0);
						lstLoadedBitmap.remove(0);
						if (endPos + 1 < total) {
							view = mInflater.inflate(R.layout.image_item, null);
							ImageView imageView = (ImageView) view.findViewById(R.id.iv_picture_item);
							long addedId = mDataList.get(endPos+1).getImage_id();
							pictureLoader.loadBitmap(addedId, imageView);
							lstPosition.add(position);
							lstView.add(view);
							lstLoadedBitmap.add(true);
						} 
					} else {							//��ǰλ�ÿ�ǰ
						lstPosition.remove(count-1);	//ɾ���������������ǰ��һ��
						lstPosition.remove(count-2);
						if (startPos - 1 > 0) {
							view = mInflater.inflate(R.layout.image_item, null);
							ImageView imageView = (ImageView) view.findViewById(R.id.iv_picture_item);
							long addedId = mDataList.get(startPos-1).getImage_id();
							pictureLoader.loadBitmap(addedId, imageView);
							lstPosition.add(0,position);
							lstView.add(0,view);
							lstLoadedBitmap.add(0,true);
						}
					}
					long id2 = lstPosition.get(0);
					lstPosition.remove(0);//ɾ���һ��	
					lstView.remove(0);//ɾ���һ��	
					id2 = lstPosition.get(0); 
				}		

				if (lstPosition.contains(position) == false) {
					view = mInflater.inflate(R.layout.image_item, null);
					ImageView imageView = (ImageView) view.findViewById(R.id.iv_picture_item);
					pictureLoader.loadBitmap(id, imageView);
					lstPosition.add(position);//���������	
					lstView.add(view);//���������  
				} else {
					view = lstView.get(lstPosition.indexOf(position));
					if (lstLoadedBitmap.get(lstPosition.indexOf(position))) {
						ImageView imageView = (ImageView) view.findViewById(R.id.iv_picture_item);
						pictureLoader.loadBitmap(id, imageView);	
					}
				}
			
			} else {
				if (convertView != null) {
					view = convertView;
				} else {
					view = mInflater.inflate(R.layout.image_item, null);
				}
				ImageView imageView = (ImageView) view.findViewById(R.id.iv_picture_item);
				pictureLoader.loadBitmap(id, imageView);
				updateViewBackground(view, position);
			}
			
		} else {
			if (USE_VIEW_CACHE) {
				if (lstPosition.contains(position) == false) {
					view = mInflater.inflate(R.layout.image_item, null);		
					ImageView imageView = (ImageView) view.findViewById(R.id.iv_picture_item);
					lstPosition.add(position);
					lstView.add(view);
					lstLoadedBitmap.add(false);
				} else {
					view = lstView.get(lstPosition.indexOf(position));
				}
			} else {
				if (convertView != null) {
					view = convertView;
				} else {
					view = mInflater.inflate(R.layout.image_item, null);
				}
				ImageView imageView = (ImageView) view.findViewById(R.id.iv_picture_item);
				if (AsyncPictureLoader.bitmapCaches.size() >0 &&
						AsyncPictureLoader.bitmapCaches.get(id) != null) {
					imageView.setImageBitmap(AsyncPictureLoader.bitmapCaches.get(id).get());
				}else {
					imageView.setImageResource(R.drawable.photo_l);
				}
				updateViewBackground(view, position);
			}

		}

		return view;
	}

	public void setIdleFlag(boolean flag){
		this.mIdleFlag = flag;
	}
	
	public void updateViewBackground(View view, int position){
		boolean isSelected = mSelectArray.get(position);
		if (isSelected) {
			view.setBackgroundResource(R.drawable.photo_l_frame);
		}else {
			view.setBackgroundResource(R.drawable.photo_l);
		}
	}
	
	private void removeLists(int pos){
		lstPosition.remove(pos);            //ɾ��ǰ��2�������Ӻ���һ��
		lstView.remove(pos);
		lstLoadedBitmap.remove(pos);		
	}
	
	private class ViewHolder{
		ImageView imageView;//folder icon
		TextView nameView;//folder name
		TextView sizeView;//picture num
	}

	
	class AsyncLoadImage extends AsyncTask<Object, Object, Void> {	
		@Override  
		protected Void doInBackground(Object... params) {  
	
			ImageView imageView=(ImageView) params[0];
			long id = Long.valueOf(String.valueOf(params[1])).longValue();
			System.out.println("AsyncLoadImage $$$$$$$$ doInBackground $$$ $$$ id = "+id);
			//long id = (long) params[1];  
			
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inDither = false;//采用默认值
			options.inPreferredConfig = Bitmap.Config.ARGB_8888;//采用默认值
			// get images thumbail
			Bitmap bitmap = MediaStore.Images.Thumbnails.getThumbnail(contentResolver, id, Images.Thumbnails.MICRO_KIND, options);

			publishProgress(new Object[] {imageView, bitmap});  
			return null;  
		}  
	
		protected void onProgressUpdate(Object... progress) {  
			ImageView imageView = (ImageView) progress[0];
			imageView.setImageBitmap((Bitmap) progress[1]); 		  
		}  
	} 


}
 

