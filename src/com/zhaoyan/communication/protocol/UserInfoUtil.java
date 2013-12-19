package com.zhaoyan.communication.protocol;

import com.dreamlink.communication.aidl.User;
import com.google.protobuf.ByteString;
import com.zhaoyan.communication.UserInfo;
import com.zhaoyan.communication.protocol.pb.PBUserUserInfoProtos.PBUserInfo;

public class UserInfoUtil {

	public static PBUserInfo userInfo2PBUserInfo(UserInfo userInfo) {
		PBUserInfo.Builder userInfoBuilder = PBUserInfo.newBuilder();
		userInfoBuilder.setUserId(userInfo.getUser().getUserID());
		userInfoBuilder.setUserName(userInfo.getUser().getUserName());
		userInfoBuilder.setHeadImageId(userInfo.getHeadId());
		if (userInfo.getHeadBitmapData() != null) {
			userInfoBuilder.setHeadImageData(ByteString.copyFrom(userInfo
					.getHeadBitmapData()));
		}
		if (userInfo.getIpAddress() != null) {
			userInfoBuilder.setIpAddress(userInfo.getIpAddress());
		}
		userInfoBuilder.setType(userInfo.getType());
		if (userInfo.getSsid() != null) {
			userInfoBuilder.setSsid(userInfo.getSsid());
		}
		userInfoBuilder.setStatus(userInfo.getStatus());
		userInfoBuilder.setNetworkType(userInfo.getNetworkType());
		return userInfoBuilder.build();
	}

	public static UserInfo pbUserInfo2UserInfo(PBUserInfo pbUserInfo) {
		UserInfo userInfo = new UserInfo();
		User user = new User();
		user.setUserID(pbUserInfo.getUserId());
		user.setUserName(pbUserInfo.getUserName());
		userInfo.setUser(user);
		userInfo.setHeadId(pbUserInfo.getHeadImageId());
		userInfo.setHeadBitmapData(pbUserInfo.getHeadImageData().toByteArray());
		userInfo.setIpAddress(pbUserInfo.getIpAddress());
		userInfo.setType(userInfo.getType());
		userInfo.setSsid(pbUserInfo.getSsid());
		userInfo.setStatus(pbUserInfo.getStatus());
		userInfo.setNetworkType(pbUserInfo.getNetworkType());
		return userInfo;
	}
}
