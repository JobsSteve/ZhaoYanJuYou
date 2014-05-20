package com.zhaoyan.juyou.backuprestore;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.zhaoyan.common.util.Log;
import com.zhaoyan.juyou.R;
import com.zhaoyan.juyou.backuprestore.Constants.State;
import com.zhaoyan.juyou.backuprestore.RestoreService.RestoreProgress;
import com.zhaoyan.juyou.backuprestore.ResultDialog.ResultEntity;

public class AppRestoreActivity extends AbstractRestoreActivity {
    private String TAG = "AppRestoreActivity";
    private List<AppSnippet> mData;
    private AppRestoreAdapter mAdapter;
    private InitDataTask mInitDataTask;
    private boolean mIsDataInitialed = false;
    private File mFile;
    private AppRestoreStatusListener mRestoreStoreStatusListener;
    private boolean mIsCheckedRestoreStatus = false;
    
    private ArrayList<String> mApkFileList = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG,  "onCreate");
        if (SDCardUtils.isSdCardAvailable(getApplicationContext())) {
        	 String appBackupPath = SDCardUtils.getAppsBackupPath(getApplicationContext());
             mFile = new File(appBackupPath);
             Log.d(TAG,  "onCreate: file is " + mFile);
             showButtonBar(true);
             updateTitle();
		} else {
			setTitle(getString(R.string.app));
		}
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mFile != null && mFile.exists()) {
            mInitDataTask = new InitDataTask();
            mInitDataTask.execute();
        } else {
//            Toast.makeText(this, R.string.file_no_exist_and_update, Toast.LENGTH_SHORT).show();
//            finish();
        }
    }

    @Override
    public BaseAdapter initAdapter() {
        mAdapter = new AppRestoreAdapter(this, null, R.layout.br_data_item);
        return mAdapter;
    }

    private ArrayList<String> getSelectedApkNameList() {
        ArrayList<String> list = new ArrayList<String>();
        int count = getListAdapter().getCount();
        for (int position = 0; position < count; position++) {
            AppSnippet item = (AppSnippet) getItemByPosition(position);
            if (isItemCheckedByPosition(position)) {
                list.add(item.getFileName());
            }
        }
        return list;
    }

    @Override
    public void onCheckedCountChanged() {
        super.onCheckedCountChanged();
        updateTitle();
    }

    @Override
    protected void notifyListItemCheckedChanged() {
        super.notifyListItemCheckedChanged();
        updateTitle();
    }

    protected void updateTitle() {
        StringBuilder sb = new StringBuilder();
        sb.append(getString(R.string.backup_app));
        int totalNum = getCount();
        int selectNum = getCheckedCount();
        sb.append("(" + selectNum + "/" + totalNum + ")");
       setTitle(sb.toString());
    }

    private void showRestoreResult(ArrayList<ResultEntity> list) {
        dismissProgressDialog();
        Bundle args = new Bundle();
        args.putParcelableArrayList(Constants.RESULT_KEY, list);
        ListAdapter adapter = ResultDialog.createAppResultAdapter(mData, this, args,
                ResultDialog.RESULT_TYPE_RESTRORE);
        AlertDialog dialog = new AlertDialog.Builder(this).setCancelable(false)
                .setTitle(R.string.restore_result)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int which) {
                        if (mRestoreService != null) {
                            mRestoreService.reset();
                        }
                        stopService();
                        NotifyManager.getInstance(AppRestoreActivity.this).clearNotification();
                    }
                }).setAdapter(adapter, null).create();
        dialog.show();
    }

    private AppSnippet getAppSnippetByApkName(String apkName) {

        AppSnippet result = null;
        for (AppSnippet item : mData) {
            if (item.getFileName().equalsIgnoreCase(apkName)) {
                result = item;
                break;
            }
        }
        return result;
    }

    private String formatProgressDialogMsg(AppSnippet item) {
        StringBuilder builder = new StringBuilder(getString(R.string.restoring));
        if (item != null) {
            builder.append("(").append(item.getName()).append(")");
        }
        return builder.toString();
    }

    private class AppRestoreAdapter extends BaseAdapter {

        private List<AppSnippet> mList;
        private int mLayoutId;
        private LayoutInflater mInflater;

        public AppRestoreAdapter(Context context, List<AppSnippet> list, int resource) {
            mList = list;
            mLayoutId = resource;
            mInflater = LayoutInflater.from(context);
        }

        public void changeData(List<AppSnippet> list) {
            mList = list;
        }

        public int getCount() {
            if (mList == null) {
                return 0;
            }
            return mList.size();
        }

        public Object getItem(int position) {
            if (mList == null) {
                return null;
            }
            return mList.get(position);
        }

        public long getItemId(int position) {
            if (mList == null) {
                return 0;
            }
            return mList.get(position).getPackageName().hashCode();
        }

        public View getView(final int position, View convertView, ViewGroup parent) {
            if (mList == null) {
                return null;
            }
            View view = convertView;
            if (view == null) {
                view = mInflater.inflate(mLayoutId, parent, false);
            }
            final AppSnippet item = mList.get(position);
            
            String info = item.getFormatFileSize();
            
            StringBuilder sb = new StringBuilder();
            sb.append(info);
            
            boolean isInstalled = item.isInstalled();
            if (isInstalled) {
            	sb.append(" " + getString(R.string.installed));
			} 
            
            ImageView imgView = (ImageView) view.findViewById(R.id.iv_item_icon);
            TextView titleView = (TextView) view.findViewById(R.id.tv_item_title);
            TextView infoView = (TextView) view.findViewById(R.id.tv_item_info);
            CheckBox checkbox = (CheckBox) view.findViewById(R.id.cb_item_check);
            imgView.setBackgroundDrawable(item.getIcon());
            titleView.setText(item.getName());
            infoView.setText(sb.toString());
            checkbox.setChecked(isItemCheckedByPosition(position));
            return view;
        }
    }

    private class InitDataTask extends AsyncTask<Void, Void, Long> {

        ArrayList<AppSnippet> appDatas;

        @Override
        protected void onPostExecute(Long arg0) {
            super.onPostExecute(arg0);
            mData = appDatas;
            mAdapter.changeData(appDatas);
            syncUnCheckedItems();
            setButtonsEnable(true);
            notifyListItemCheckedChanged();
            mIsDataInitialed = true;
            showLoadingContent(false);
            if (mRestoreStoreStatusListener == null) {
                mRestoreStoreStatusListener = new AppRestoreStatusListener();
            }
            setOnRestoreStatusListener(mRestoreStoreStatusListener);
            checkRestoreState();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            setButtonsEnable(false);
            showLoadingContent(true);
            setTitle(R.string.backup_app);

        }

        @Override
        protected Long doInBackground(Void... arg0) {
            // try {
            // Thread.sleep(2000);
            // } catch (Exception e) {
            // // TODO: handle exception
            // }
            ArrayList<File> apkFileList = FileUtils.getAllApkFileInFolder(mFile);
            appDatas = new ArrayList<AppSnippet>();
            if (apkFileList != null) {
                for (File file : apkFileList) {
                    AppSnippet item = FileUtils.getAppSnippet(AppRestoreActivity.this,
                            file.getAbsolutePath());
                    if (item != null) {
                        appDatas.add(item);
                    }
                }
            }

            // sort
            Collections.sort(appDatas, new Comparator<AppSnippet>() {
                public int compare(AppSnippet object1, AppSnippet object2) {
                    String left = new StringBuilder(object1.getName()).toString();
                    String right = new StringBuilder(object2.getName()).toString();
                    if (left != null && right != null) {
                        return left.compareTo(right);
                    }
                    return 0;
                }
            });
            return null;
        }
    }

    /**
     * after service connected and data initialed, to check restore state to
     * restore UI. only to check once after onCreate, always used for activity
     * has been killed in background.
     */
    private void checkRestoreState() {
        if (mIsCheckedRestoreStatus) {
            Log.d(TAG,  "can not checkRestoreState, as it has been checked");
            return;
        }
        if (!mIsDataInitialed) {
            Log.d(TAG,  "can not checkRestoreState, wait data to initialed");
            return;
        }
        Log.d(TAG,  "all ready. to checkRestoreState");
        mIsCheckedRestoreStatus = true;
        if (mRestoreService != null) {
            int state = mRestoreService.getState();
            Log.d(TAG,  "checkRestoreState: state = " + state);
            switch (state) {
            case State.RUNNING:
            case State.PAUSE:
                ArrayList<String> params = mRestoreService.getRestoreItemParam(ModuleType.TYPE_APP);
                RestoreProgress p = mRestoreService.getCurRestoreProgress();
                Log.e(TAG, "checkRestoreState: Max = " + p.mMax + " curprogress = "
                        + p.mCurNum);

                if (state == State.RUNNING) {
                    showProgressDialog();
                }
                if (p.mCurNum < p.mMax) {
                    String apkName = params.get(p.mCurNum);
                    String msg = formatProgressDialogMsg(getAppSnippetByApkName(apkName));
                    setProgressDialogMessage(msg);
                }
                setProgressDialogMax(p.mMax);
                setProgressDialogProgress(p.mCurNum);
                break;
            case State.FINISH:
                showRestoreResult(mRestoreService.getAppRestoreResult());
                break;

            case State.ERR_HAPPEN:
                errChecked();
                break;

            default:
                break;
            }
        }
    }

    @Override
    protected void afterServiceConnected() {
        Log.d(TAG,  "afterServiceConnected, to checkRestorestate");
        checkRestoreState();
    }

    private int mIndex = 0;
    @Override
    protected void startRestore() {
        Log.d(TAG,  "startRestore");

        //yuri
        if (mApkFileList == null) {
			mApkFileList = new ArrayList<String>();
		}
        
        mApkFileList = getSelectedApkNameList();
        for (int i = 0; i < mApkFileList.size(); i++) {
			String apkFileName = mApkFileList.get(i);
			File file = new File(apkFileName);
			if (file != null && file.exists()) {
				installApp(file);
			}
		}
    }

    private class AppRestoreStatusListener extends NormalRestoreStatusListener {

        @Override
        public void onRestoreEnd(boolean bSuccess, final ArrayList<ResultEntity> resultRecord) {
            Log.d(TAG,  "onRestoreEnd");
            if (mHandler != null) {
                mHandler.post(new Runnable() {
                    public void run() {

                        Log.d(TAG,  " Restore show Result Dialog");
                        showRestoreResult(mRestoreService.getAppRestoreResult());
                    }
                });
            }
        }

        @Override
        public void onProgressChanged(final Composer composer, final int progress) {
            Log.i(TAG, "onProgressChange, p = " + progress);
            if (mHandler != null) {
                mHandler.post(new Runnable() {
                    public void run() {
                        setProgressDialogProgress(progress);
                        if (progress < composer.getCount()) {
                            ArrayList<String> params = mRestoreService
                                    .getRestoreItemParam(ModuleType.TYPE_APP);
                            String apkName = params.get(progress);
                            Log.d(TAG,  "onProgressChanged: the " + progress
                                    + "  apkName is " + apkName);
                            String msg = formatProgressDialogMsg(getAppSnippetByApkName(apkName));
                            setProgressDialogMessage(msg);
                        }
                    }
                });
            }
        }
    }
    
    private void installApp(File file) {
		Intent intent = new Intent();
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setAction(android.content.Intent.ACTION_VIEW);
		intent.setDataAndType(Uri.fromFile(file),
				"application/vnd.android.package-archive");
		try {
			startActivityForResult(intent, 123);
		} catch (Exception e) {
			Toast.makeText(this, "Open fail", Toast.LENGTH_SHORT)
					.show();
		}
	}
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	super.onActivityResult(requestCode, resultCode, data);
    	if (requestCode == 123) {
		}
    }
}
