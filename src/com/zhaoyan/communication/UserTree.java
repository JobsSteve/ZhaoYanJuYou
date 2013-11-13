package com.zhaoyan.communication;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.dreamlink.communication.aidl.User;
import com.zhaoyan.common.util.Log;

public class UserTree implements Serializable {
	private static final long serialVersionUID = 18121118330L;
	/** the head of the user tree, */
	public UserNode headNode;

	public class UserNode implements Serializable {
		private static final long serialVersionUID = 18121118331L;
		/** if the parentNode is null,it is the head node, main server */
		public UserNode parentNode;
		/** if the child is not null,mean it is Wifi-direct server */
		public List<UserNode> child = new ArrayList<UserTree.UserNode>();
		/** the node information */
		public User user;
	}

	private static UserTree userTree;

	private UserTree() {

	}

	/** the instance of {@link UserTree} */
	public static UserTree getInstance() {
		if (userTree == null) {
			userTree = new UserTree();
		}
		return userTree;
	}

	/**
	 * @param user
	 *            the user want to query
	 * @return if success,return the {@link UserNode} of the user;else null
	 */
	public UserNode queryUser(User user) {
		return queryByNode(headNode, user);
	}

	/**
	 * @param oldUser
	 *            the old user data,if it not in the tree,will be fail
	 * @param newUser
	 *            the new data
	 * @return true ,success;false,fail
	 */
	public boolean modifyUser(User oldUser, User newUser) {
		UserNode node = queryUser(oldUser);
		if (node != null) {
			node.user = newUser;
		}
		return false;
	}

	/** if the head node is not null ,return false ,mean fail */
	public boolean setHead(User user) {
		if (headNode == null) {
			headNode = new UserNode();
			headNode.user = user;
			headNode.parentNode = null;
			return true;
		}
		return false;
	}

	/**
	 * @param parentUser
	 *            if null ,and headNode is null ,mean childUser is head;else ,it
	 *            is the parent
	 * @param childUser
	 *            the user need to add
	 * @return true ,success; false ,fail
	 */
	public boolean addUser(User parentUser, User childUser) {
		if (parentUser == null && headNode == null) {
			headNode = new UserNode();
			headNode.user = childUser;
			headNode.parentNode = null;
			return true;
		}
		UserNode node = queryUser(parentUser);
		if (node != null) {
			UserNode userNode = new UserNode();
			userNode.user = childUser;
			userNode.parentNode = node;
			node.child.add(userNode);
			return true;
		}
		return false;
	}

	public boolean deleteUser(User user) {
		UserNode node = queryUser(user);
		if (node != null) {
			node.child.clear();
			node.user = null;
			node.parentNode.child.remove(node);
			node.parentNode = null;
			return true;
		}
		return false;

	}

	private UserNode queryByNode(UserNode node, User user) {
		if (node != null) {
			if (node.user.getUserID() == user.getUserID()) {
				return node;
			} else if (node.child != null && node.child.size() != 0) {
				for (UserNode childeNode : node.child) {
					UserNode result = queryByNode(childeNode, user);
					if (result != null) {
						return result;
					}
				}
			}
		}
		return null;
	}

	public void printTree() {
		printNode(headNode);
	}

	private void printNode(UserNode node) {
		if (node == null) {
			return;
		}
		Log.e("ArbiterLiu", node.user.toString());
		if (node.child != null && node.child.size() != 0) {
			for (UserNode n : node.child) {
				printNode(n);
			}
		}
	}
}
