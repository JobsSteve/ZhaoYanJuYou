package com.zhaoyan.juyou.adapter;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.zhaoyan.common.util.BitmapUtilities;
import com.zhaoyan.communication.UserHelper;
import com.zhaoyan.communication.UserInfo;
import com.zhaoyan.juyou.R;
import com.zhaoyan.juyou.provider.JuyouData;

/**
 * Adapter for network fragment user list view.
 * 
 */
public class ConnectedUserAdapter extends CursorAdapter {
	private LayoutInflater mLayoutInflater;

	public ConnectedUserAdapter(Context context, Cursor c, boolean autoRequery) {
		super(context, c, autoRequery);
		mLayoutInflater = LayoutInflater.from(context);
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		ViewHolder holder = (ViewHolder) view.getTag();
		if (holder != null) {
			// name
			String name = cursor.getString(cursor
					.getColumnIndex(JuyouData.User.USER_NAME));
			holder.userName.setText(name);
			// head
			int headId = cursor.getInt(cursor
					.getColumnIndex(JuyouData.User.HEAD_ID));
			if (headId == UserInfo.HEAD_ID_NOT_PRE_INSTALL) {
				byte[] headData = cursor.getBlob(cursor
						.getColumnIndex(JuyouData.User.HEAD_DATA));
				if (headData.length == 0) {
					holder.userIcon.setImageResource(R.drawable.head_unkown);
				} else {
					Bitmap headBitmap = BitmapUtilities
							.byteArrayToBitmap(headData);
					holder.userIcon.setImageBitmap(headBitmap);
				}
			} else {
				holder.userIcon.setImageResource(UserHelper
						.getHeadImageResource(headId));
			}

			// status
			int status = cursor.getInt(cursor.getColumnIndex(JuyouData.User.STATUS));
			if (status == JuyouData.User.STATUS_SERVER_CREATED) {
				holder.userStatus.setText(R.string.network_creator);
			} else if (status == JuyouData.User.STATUS_CONNECTED) {
				holder.userStatus.setText(R.string.connected);
			}
		}
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
		View view = mLayoutInflater.inflate(R.layout.connected_info_item, null);
		ViewHolder holder = new ViewHolder();

		holder.userIcon = (ImageView) view.findViewById(R.id.iv_cii_user_icon);
		holder.userName = (TextView) view.findViewById(R.id.tv_cii_user_name);
		holder.userStatus = (TextView) view.findViewById(R.id.tv_cii_user_status);
		view.setTag(holder);
		return view;
	}

	private class ViewHolder {
		ImageView userIcon;
		TextView userName;
		TextView userStatus;
	}
}
