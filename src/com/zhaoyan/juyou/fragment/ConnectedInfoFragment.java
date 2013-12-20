package com.zhaoyan.juyou.fragment;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import com.zhaoyan.common.net.NetWorkUtil;
import com.zhaoyan.common.util.Log;
import com.zhaoyan.communication.ProtocolCommunication;
import com.zhaoyan.communication.SocketCommunicationManager;
import com.zhaoyan.communication.connect.ServerCreator;
import com.zhaoyan.communication.search.SearchUtil;
import com.zhaoyan.juyou.R;
import com.zhaoyan.juyou.adapter.ConnectedUserAdapter;
import com.zhaoyan.juyou.provider.JuyouData;

public class ConnectedInfoFragment extends ListFragment implements
		OnClickListener, LoaderCallbacks<Cursor> {
	private static final String TAG = "ConnectedInfoFragment";
	private Button mDisconnectButton;
	private Context mContext;
	private ServerCreator mServerCreator;

	private ListView mListView;
	private ConnectedUserAdapter mAdapter;

	protected static final String[] PROJECTION = { JuyouData.User._ID,
			JuyouData.User.USER_NAME, JuyouData.User.USER_ID,
			JuyouData.User.HEAD_ID, JuyouData.User.HEAD_DATA,
			JuyouData.User.IP_ADDR, JuyouData.User.STATUS, JuyouData.User.TYPE,
			JuyouData.User.SSID };

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.d(TAG, "onCreateView");
		mContext = getActivity();
		mServerCreator = ServerCreator.getInstance(mContext);

		View rootView = inflater.inflate(R.layout.connected_info, container,
				false);
		initView(rootView);
		return rootView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mListView = getListView();
		mAdapter = new ConnectedUserAdapter(mContext, null, true);
		mListView.setAdapter(mAdapter);
		getLoaderManager().initLoader(0, null, this);
	}

	@Override
	public void onDestroyView() {
		Log.d(TAG, "onDestroyView");
		super.onDestroyView();
		mAdapter.swapCursor(null);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
		String selection = JuyouData.User.STATUS + "="
				+ JuyouData.User.STATUS_SERVER_CREATED + " or "
				+ JuyouData.User.STATUS + "=" + JuyouData.User.STATUS_CONNECTED;
		return new CursorLoader(mContext, JuyouData.User.CONTENT_URI,
				PROJECTION, selection, null, JuyouData.User.SORT_ORDER_DEFAULT);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		mAdapter.swapCursor(cursor);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		mAdapter.swapCursor(null);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_ci_disconnect:
			disconnect();
			break;
		default:
			break;
		}
	}

	private void initView(View rootView) {
		mDisconnectButton = (Button) rootView
				.findViewById(R.id.btn_ci_disconnect);
		mDisconnectButton.setOnClickListener(this);
	}

	private void disconnect() {
		ProtocolCommunication protocolCommunication = ProtocolCommunication
				.getInstance();
		protocolCommunication.logout();

		// Stop search if this is server.
		// Stop all communication if this is client.
		if (mServerCreator.isCreated()) {
			mServerCreator.stopServer();
		} else {
			SocketCommunicationManager manager = SocketCommunicationManager
					.getInstance();
			manager.closeAllCommunication();
		}

		// close WiFi AP if WiFi AP is enabled.
		if (NetWorkUtil.isWifiApEnabled(mContext)) {
			NetWorkUtil.setWifiAPEnabled(mContext, null, false);
		}
		// disconnect current network if connected to the WiFi AP created by
		// our application.
		SearchUtil.clearWifiConnectHistory(mContext);
	}
}
