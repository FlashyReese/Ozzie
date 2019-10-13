package me.wilsonhu.ozzie.manager.json.configuration;

import java.util.ArrayList;
import java.util.HashMap;

import me.wilsonhu.ozzie.OzzieManager;

public class PermissionManager {
	private HashMap<Long, ArrayList<String>> userPermissionList = new HashMap<Long, ArrayList<String>>();
	private OzzieManager manager;
	
	public PermissionManager(OzzieManager manager) {
		setOzzieManager(manager);
		getOzzieManager().getLogger().info("Permission Manager started");
	}
	
	//First one returns if userExist, second one permission Exist?
	public Boolean[] addPermission(long userID, String permissions) {
		if(getUserPermissionList().containsKey(userID)) {
			ArrayList<String> userPerms = getUserPermissionList().get(userID);
			if(userPerms.contains(permissions)) {
				return new Boolean[]{true, true};//Already exist no changes;
			}else {
				userPerms.add(permissions);
				return new Boolean[]{true, false};//Didn't exist, but added
			}
		}else {
			ArrayList<String> newPerms = new ArrayList<String>();
			newPerms.add(permissions);
			getUserPermissionList().put(userID, newPerms);
		}
		return new Boolean[] {false, false};//user doesn't exist, and added
	}
	
	public Boolean[] removePermission(long userID, String permissions) {
		if(getUserPermissionList().containsKey(userID)) {
			ArrayList<String> userPerms = getUserPermissionList().get(userID);
			if(userPerms.contains(permissions)) {
				userPerms.remove(permissions);
				return new Boolean[]{true, true};//Already exist, removed
			}else {
				return new Boolean[]{true, false};//Didn't exist no changes
			}
		}
		return new Boolean[] {false, false};//user doesn't exist nothing happened
	}
	
	public boolean hasPermission(long userID, String permission) {
		if(getUserPermissionList().containsKey(userID)) {
			ArrayList<String> userPerms = getUserPermissionList().get(userID);
			if(userPerms.contains(permission)) {
				return true;
			}
			if(userPerms.contains("*")) {
				return true;
			}
			String parser = permission.substring(0, permission.lastIndexOf(".") + 1) + "*";
			if(userPerms.contains(parser)) {
				return true;
			}
		}
		return false;
	}
	
	public HashMap<Long, ArrayList<String>> getUserPermissionList() {
		return userPermissionList;
	}

	public void setUserPermissionList(HashMap<Long, ArrayList<String>> userPermissionList) {
		this.userPermissionList = userPermissionList;
	}

	private OzzieManager getOzzieManager() {
		return manager;
	}

	private void setOzzieManager(OzzieManager manager) {
		this.manager = manager;
	}
}
