package com.zhaoyan.juyou.backuprestore;

import com.zhaoyan.juyou.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class BRAdapter extends BaseAdapter {

	private LayoutInflater mInflater = null;

	public BRAdapter(Context context) {
		mInflater = LayoutInflater.from(context);
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return 10;
	}
	
	@Override
	public boolean isEnabled(int position) {
		if (position == 0 || position == 5) {
			return false;
		}
		return super.isEnabled(position);
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
		View view = mInflater.inflate(R.layout.backup_restore_item, null);
		View itemView = view.findViewById(R.id.ll_item);
		View headerView = view.findViewById(R.id.ll_item_header);
		ImageView imageView = (ImageView) view.findViewById(R.id.iv_br_icon);
		TextView textView = (TextView) view.findViewById(R.id.tv_br_title);
		TextView headerTextView = (TextView) view.findViewById(R.id.tv_item_header);
		boolean bShow = true;
		switch (position) {
		case 0:
			bShow = false;
			headerTextView.setText(R.string.local);
			headerView.setEnabled(false);
			break;
		case 1:
			bShow = true;
			imageView.setImageResource(R.drawable.ic_backupdata_sdcard);
			textView.setText(R.string.backup_data);
			break;
		case 2:
			bShow = true;
			imageView.setImageResource(R.drawable.ic_backupapps_sdcard);
			textView.setText(R.string.backup_app);
			break;
		case 3:
			bShow = true;
			imageView.setImageResource(R.drawable.ic_restoredata_sdcard);
			textView.setText(R.string.restore_data);
			break;
		case 4:
			bShow = true;
			imageView.setImageResource(R.drawable.ic_restoreapps_sdcard);
			textView.setText(R.string.restore_app);
			break;
		case 5:
			bShow = false;
			headerTextView.setText(R.string.clound);
			headerView.setEnabled(false);
			break;
		case 6:
			bShow = true;
			imageView.setImageResource(R.drawable.ic_backupdata_cloud);
			textView.setText(R.string.backup_data);
			break;
		case 7:
			bShow = true;
			imageView.setImageResource(R.drawable.ic_backupapps_cloud);
			textView.setText(R.string.backup_app);
			break;
		case 8:
			bShow = true;
			imageView.setImageResource(R.drawable.ic_restoredata_sdcard);
			textView.setText(R.string.restore_data);
			break;
		case 9:
			bShow = true;
			imageView.setImageResource(R.drawable.ic_restoreapps_sdcard);
			textView.setText(R.string.restore_app);
			break;
		default:
			break;
		}
		showContent(itemView, headerView, bShow);
		return view;
	}

	private void showContent(View itemView, View headerView, boolean show) {
		itemView.setVisibility(show ? View.VISIBLE : View.GONE);
		headerView.setVisibility(!show ? View.VISIBLE : View.GONE);
	}

}
