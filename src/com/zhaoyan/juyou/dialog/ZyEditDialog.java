package com.zhaoyan.juyou.dialog;

import com.zhaoyan.common.util.ZYUtils;
import com.zhaoyan.juyou.R;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class ZyEditDialog extends ZyAlertDialog {

	private TextView mInfoView;
	private EditText mEditText;
	private TextView mTipView;
	
	private String message;
	private String editStr;
	
	private boolean isSelectAll = false;
	private boolean showIME = false;
	
	public ZyEditDialog(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		View view = getLayoutInflater().inflate(R.layout.dialog_edit, null);
		
		mInfoView = (TextView) view.findViewById(R.id.tv_editdialog_info);
		mTipView = (TextView) view.findViewById(R.id.tv_editdialog_tip);
		mEditText = (EditText) view.findViewById(R.id.et_dialog);
		mEditText.addTextChangedListener(watcher);
		
		if (!"".equals(message) && null != message) {
			mInfoView.setVisibility(View.VISIBLE);
			mInfoView.setText(message);
		}
		
		mEditText.setText(editStr);
		if (isSelectAll) {
			mEditText.selectAll();
		}
		
		ZYUtils.onFocusChange(mEditText, showIME);
		
		setCustomView(view);
		super.onCreate(savedInstanceState);
	}
	
	public void setEditDialogMsg(String msg){
		message = msg;
	}
	
	public void setEditStr(String str){
		editStr = str;
	}
	
	public void selectAll(){
		isSelectAll = true;
	}
	
	public void showIME(boolean show){
		showIME = show;
	}
	
	public String getEditTextStr(){
		return mEditText.getText().toString().trim();
	}
	
	public void showTipMessage(boolean show, String message){
		mTipView.setVisibility(show ? View.VISIBLE : View.GONE);
		mTipView.setText(message);
	}
	
	public void refreshUI(){
		mEditText.setText(editStr);
		if (isSelectAll) {
			mEditText.selectAll();
		}
	}

	private TextWatcher watcher = new TextWatcher(){
	    @Override
	    public void afterTextChanged(Editable s) {
	    	mTipView.setVisibility(View.GONE);
	    }
	 
	    @Override
	    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
	    }
	 
	    @Override
	    public void onTextChanged(CharSequence s, int start, int before, int count) {
	        Log.d("TAG","[TextWatcher][onTextChanged]"+s);
	    }
	     
	};

}
