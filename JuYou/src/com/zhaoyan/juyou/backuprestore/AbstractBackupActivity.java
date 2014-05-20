
package com.zhaoyan.juyou.backuprestore; 

import java.io.IOException;
import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zhaoyan.common.util.Log;
import com.zhaoyan.juyou.R;
import com.zhaoyan.juyou.backuprestore.BackupEngine.BackupResultType;
import com.zhaoyan.juyou.backuprestore.BackupService.BackupBinder;
import com.zhaoyan.juyou.backuprestore.BackupService.OnBackupStatusListener;
import com.zhaoyan.juyou.backuprestore.CheckedListActivity.OnCheckedCountChangedListener;
import com.zhaoyan.juyou.backuprestore.Constants.DialogID;
import com.zhaoyan.juyou.backuprestore.Constants.MessageID;
import com.zhaoyan.juyou.backuprestore.Constants.State;
import com.zhaoyan.juyou.backuprestore.ResultDialog.ResultEntity;
import com.zhaoyan.juyou.common.ZyStorageManager;

public abstract class AbstractBackupActivity extends CheckedListActivity implements
        OnCheckedCountChangedListener {
    private String TAG = "AbstractBackupActivity";
    protected BaseAdapter mAdapter;
    private LinearLayout mBackupRestoreButtonBar;
    private Button mButtonBackup;
    private Button mSelectAllBtn;
    protected ProgressDialog mProgressDialog;
    protected ProgressDialog mCancelDlg;
    protected Handler mHandler;
    protected BackupBinder mBackupService;
    private OnBackupStatusListener mBackupListener;
    protected TextView mTitleView;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.br_data_main);
        init();
        Log.i(TAG, "onCreate");
        if (savedInstanceState != null) {
            updateButtonState();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume");
        mAdapter = initBackupAdapter();
        setListAdapter(mAdapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy");
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
        if (mBackupService != null && mBackupService.getState() == State.INIT) {
            stopService();
        }
        if (mBackupService != null) {
            mBackupService.setOnBackupChangedListner(null);
        }
        unBindService();
        mHandler = null;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    private void init() {
    	Log.d(TAG, "init");
        this.bindService();
        registerOnCheckedCountChangedListener(this);
        
        View titleView = findViewById(R.id.title);
		View backView = titleView.findViewById(R.id.ll_title);
		mTitleView = (TextView) titleView.findViewById(R.id.tv_custom_title);
		backView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finishWithAnimation();
			}
		});
        initButton();
        initHandler();
        initLoadingView();
        createProgressDlg();
        // mAdapter = initBackupAdapter();
        // setListAdapter(mAdapter);
    }
    
    LinearLayout loadingContent = null;
    private void initLoadingView() {
    	loadingContent =  (LinearLayout) findViewById(R.id.ll_loading_content);
	}
    
    protected void setTitle(String title){
    	mTitleView.setText(title);
    }
    
    protected void showButtonBar(boolean show) {
		mBackupRestoreButtonBar.setVisibility(show ?  View.VISIBLE : View.GONE);
	}
		
	protected void showLoadingContent(boolean show){
		loadingContent.setVisibility(show?View.VISIBLE:View.GONE);
		getListView().setVisibility(!show?View.VISIBLE:View.GONE);
    }

	@Override
    public void onCheckedCountChanged() {
        mAdapter.notifyDataSetChanged();
        updateButtonState();
    }

    public void setOnBackupStatusListener(OnBackupStatusListener listener) {
        mBackupListener = listener;
        if (mBackupListener != null&&mBackupService != null) {
            mBackupService.setOnBackupChangedListner(mBackupListener);
        }
    }

    private ProgressDialog createProgressDlg() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            mProgressDialog.setMessage(getString(R.string.backuping));
            mProgressDialog.setCancelable(true);
            mProgressDialog.setCanceledOnTouchOutside(false);
            mProgressDialog.setCancelMessage(mHandler.obtainMessage(MessageID.PRESS_BACK));
        }
        return mProgressDialog;
    }

    private ProgressDialog createCancelDlg() {
        if (mCancelDlg == null) {
            mCancelDlg = new ProgressDialog(this);
            mCancelDlg.setMessage(getString(R.string.cancelling));
            mCancelDlg.setCancelable(false);
        }
        return mCancelDlg;
    }

    protected void showProgress() {
        if (mProgressDialog == null) {
            mProgressDialog = createProgressDlg();
        }
        mProgressDialog.show();
    }

    protected boolean errChecked() {
    	Log.d(TAG, "errChecked");
        boolean ret = false;
        String path = SDCardUtils.getStoragePath(getApplicationContext());
        if (path == null) {
            // no sdcard
            Log.d(TAG, "SDCard is removed");
            ret = true;
            if (mHandler != null) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        AbstractBackupActivity.this.showDialog(DialogID.DLG_SDCARD_REMOVED);
                    }
                });
            }
        } else if (ZyStorageManager.getAvailableBlockSize(path) <= SDCardUtils.MINIMUM_SIZE) {
            // no space
            Log.d(TAG, "SDCard is full");
            ret = true;
            if (mHandler != null) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        AbstractBackupActivity.this.showDialog(DialogID.DLG_SDCARD_FULL);
                    }
                });
            }
        } else {
            Log.e(TAG, "unkown error");
        }
        return ret;
    }

    private void initButton() {
    	mBackupRestoreButtonBar = (LinearLayout) findViewById(R.id.ll_backup);
        mButtonBackup = (Button) findViewById(R.id.btn_backup);
        mButtonBackup.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				 if (mBackupService == null || mBackupService.getState() != State.INIT) {
	                    Log.e(TAG,
	                            "Can not to start. BackupService not ready or BackupService is ruuning");
	                    return;
	                }

	                if (isAllChecked(false)) {
	                    Log.e(TAG, "to Backup List is null or empty");
	                    return;
	                }
	                String path = SDCardUtils.getStoragePath(getApplicationContext());
	                if (path != null) {
	                	if(Utils.getWorkingInfo()<0){
	                    	startBackup();
	                    }else{
	                    	showDialog(DialogID.DLG_RUNNING);
	                    }
	                } else {
	                    // scard not available
	                    showDialog(DialogID.DLG_NO_SDCARD);
	                }
			}
		});
        
        //select all
        mSelectAllBtn = (Button) findViewById(R.id.btn_selectall);
        mSelectAllBtn.setText(R.string.unselect_all);
        mSelectAllBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (isAllChecked(true)) {
					setAllChecked(false);
					mSelectAllBtn.setText(R.string.select_all);
				} else {
					setAllChecked(true);
					mSelectAllBtn.setText(R.string.unselect_all);
				}
			}
		});
    }

    protected void setButtonsEnable(boolean enable) {
    	Log.d(TAG, "setButtonsEnable - " +enable);
        if (mButtonBackup != null) {
        	mButtonBackup.setEnabled(enable);
        }
        if (mSelectAllBtn != null) {
        	mSelectAllBtn.setEnabled(enable);
        }
    }

    protected void updateButtonState() {
        if (isAllChecked(false)) {
            mButtonBackup.setEnabled(false);
            mSelectAllBtn.setText(R.string.select_all);
        } else {
            mButtonBackup.setEnabled(true);
            mSelectAllBtn.setText(R.string.unselect_all);
        }
    }

    protected final void initHandler() {
        mHandler = new Handler() {
            @Override
            public void handleMessage(final Message msg) {
                switch (msg.what) {
                case MessageID.PRESS_BACK:
                    if (mBackupService != null && mBackupService.getState() != State.INIT
                            && mBackupService.getState() != State.FINISH) {
                        mBackupService.pauseBackup();
                        AbstractBackupActivity.this.showDialog(DialogID.DLG_CANCEL_CONFIRM);
                    }
                    break;
                default:
                    break;
                }
            }
        };
    }

    @Override
    protected Dialog onCreateDialog(final int id, final Bundle args) {
        Dialog dialog = null;
        switch (id) {
        case DialogID.DLG_CANCEL_CONFIRM:
            dialog = new AlertDialog.Builder(AbstractBackupActivity.this)
                    .setTitle(R.string.warning).setMessage(R.string.cancel_backup_confirm)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(final DialogInterface arg0, final int arg1) {
                            if (mBackupService != null && mBackupService.getState() != State.INIT
                                    && mBackupService.getState() != State.FINISH) {
                                if (mCancelDlg == null) {
                                    mCancelDlg = createCancelDlg();
                                }
                                mCancelDlg.show();
                                mBackupService.cancelBackup();
                                NotifyManager.getInstance(AbstractBackupActivity.this).clearNotification();
                            }
                        }
                    }).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(final DialogInterface arg0, final int arg1) {

                            if (mBackupService != null && mBackupService.getState() == State.PAUSE) {
                                mBackupService.continueBackup();
                            }
                            if (mProgressDialog != null) {
                                mProgressDialog.show();
                            }
                        }
                    }).setCancelable(false).create();
            break;
        case DialogID.DLG_SDCARD_REMOVED:
            dialog = new AlertDialog.Builder(AbstractBackupActivity.this)
                    .setTitle(R.string.warning).setMessage(R.string.sdcard_removed)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(final DialogInterface dialog, final int which) {
                            if (mBackupService != null && mBackupService.getState() == State.PAUSE) {
                                mBackupService.cancelBackup();
                            }
                        }

                    }).setCancelable(false).create();
            break;
        case DialogID.DLG_SDCARD_FULL:
        	Log.d(TAG, "DLG_SDCARD_FULL");
            dialog = new AlertDialog.Builder(AbstractBackupActivity.this)
                    .setTitle(R.string.warning).setMessage(R.string.sdcard_is_full)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(final DialogInterface dialog, final int which) {
                            if (mBackupService != null && mBackupService.getState() == State.PAUSE) {
                                mBackupService.cancelBackup();
                            }
                        }
                    }).setCancelable(false).create();
            break;
        case DialogID.DLG_RUNNING:
            dialog = new AlertDialog.Builder(AbstractBackupActivity.this)
                    .setTitle(R.string.warning)
                    .setMessage(R.string.state_running)
                    .setPositiveButton(android.R.string.ok, null).create();
            break;
            
        case DialogID.DLG_NO_SDCARD:
            dialog = new AlertDialog.Builder(AbstractBackupActivity.this)
                    .setTitle(R.string.notice)
                    .setMessage(SDCardUtils.getSDStatueMessage(this))
                    .setPositiveButton(android.R.string.ok, null).create();
            break;

        case DialogID.DLG_CREATE_FOLDER_FAILED:
            String name = args.getString("name");
            String msg = String.format(getString(R.string.create_folder_fail), name);
            dialog = new AlertDialog.Builder(AbstractBackupActivity.this)
                   	.setTitle(R.string.notice)
                    .setMessage(msg).setPositiveButton(android.R.string.ok, null).create();
            break;

        default:
            break;
        }
        return dialog;
    }

    /**
     * when backup button click, if can start backup, will call startBackup
     */
    public abstract void startBackup();

    /**
     * init Backup Adapter, when activity will can this function. after call
     * this, activity can't change the adapter.
     * 
     * @return
     */
    public abstract BaseAdapter initBackupAdapter();

    /**
     * after service connected, will can the function, the son activity can do
     * anything that need service connected
     */
    protected abstract void afterServiceConnected();

    @Override
    public void onConfigurationChanged(final Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.e(TAG, "onConfigurationChanged");
    }

    /**
     * after service connected and data initialed, to check restore state to
     * restore UI. only to check once after onCreate, always used for activity
     * has been killed in background.
     */
    protected void checkBackupState() {
        if (mBackupService != null) {
            int state = mBackupService.getState();
            switch (state) {
            case State.ERR_HAPPEN:
                errChecked();
                break;
            default:
                break;
            }
        }
    }

    private void bindService() {
        this.getApplicationContext().bindService(new Intent(this, BackupService.class),
                mServiceCon, Service.BIND_AUTO_CREATE);
    }

    private void unBindService() {
        if (mBackupService != null) {
            mBackupService.setOnBackupChangedListner(null);
        }
        this.getApplicationContext().unbindService(mServiceCon);
    }

    protected void startService() {
        this.startService(new Intent(this, BackupService.class));
    }

    protected void stopService() {
        if (mBackupService != null) {
            mBackupService.reset();
        }
        this.stopService(new Intent(this, BackupService.class));
    }

    private ServiceConnection mServiceCon = new ServiceConnection() {
        @Override
        public void onServiceConnected(final ComponentName name, final IBinder service) {
            mBackupService = (BackupBinder) service;
            if (mBackupService != null) {
                if (mBackupListener != null) {
                    mBackupService.setOnBackupChangedListner(mBackupListener);
                }
            }
            // checkBackupState();
            afterServiceConnected();
            Log.i(TAG, "onServiceConnected.mBackupService:" + mBackupService);
        }

        @Override
        public void onServiceDisconnected(final ComponentName name) {
            mBackupService = null;
            Log.i(TAG, "onServiceDisconnected");
        }
    };

    public class NomalBackupStatusListener implements OnBackupStatusListener {
        @Override
        public void onBackupEnd(final BackupResultType resultCode,
                final ArrayList<ResultEntity> resultRecord,
                final ArrayList<ResultEntity> appResultRecord) {
            // do nothing
        }

        @Override
        public void onBackupErr(final IOException e) {
            if (errChecked()) {
                if (mBackupService != null && mBackupService.getState() != State.INIT
                        && mBackupService.getState() != State.FINISH) {
                    mBackupService.pauseBackup();
                }
            }
        }

        @Override
        public void onComposerChanged(final Composer composer) {
        	
        }

        @Override
        public void onProgressChanged(final Composer composer, final int progress) {
            if (mHandler != null) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (mProgressDialog != null) {
                            mProgressDialog.setProgress(progress);
                        }
                    }
                });
            }
        }
    }
    
    protected void finishWithAnimation() {
		finish();
		overridePendingTransition(0, R.anim.activity_right_out);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
				finish();
				overridePendingTransition(0, R.anim.activity_right_out);
				return true;
		}
		return super.onKeyDown(keyCode, event);
	}

}
