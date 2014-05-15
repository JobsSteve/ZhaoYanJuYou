package com.zhaoyan.juyou.backuprestore;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.zhaoyan.juyou.R;
import com.zhaoyan.juyou.activity.BaseActivity;

public class BackupResotreActivity extends BaseActivity implements OnClickListener, OnItemClickListener {
	private ListView mListView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.backup_restore);
		
		View titleView = findViewById(R.id.title);
		View backView = titleView.findViewById(R.id.ll_title);
		TextView titleTextView = (TextView) titleView.findViewById(R.id.tv_custom_title);
		titleTextView.setText(R.string.backup_restore);
		backView.setOnClickListener(this);
		mListView = (ListView) findViewById(R.id.lv_br_main);
		BRAdapter adapter = new BRAdapter(getApplicationContext());
		mListView.setAdapter(adapter);
		mListView.setOnItemClickListener(this);
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.ll_title:
			finishWithAnimation();
			break;

		default:
			break;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		switch (position) {
		case 1:
			//local data backup
			openActivity(DataBackupActivity.class);
			break;
		case 2:
			//local app backup
			openActivity(AppBackupActivity.class);
			break;
		case 3:
			//local data restore
			openActivity(DataRestoreActivity.class);
			break;
		case 4:
			//local app restore
			openActivity(AppRestoreActivity.class);
			break;
		default:
			break;
		}
	}
	
}
