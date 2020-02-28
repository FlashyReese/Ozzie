package me.wilsonhu.ozzie.schemas;

import java.util.ArrayList;

public class ServerUserPermissionSchema {

    private long serverId;
    private long userId;
    private ArrayList<String> permissions;

    public ServerUserPermissionSchema(long serverId, long userId){
        setServerId(serverId);
        setUserId(userId);
        setPermissions(new ArrayList<String>());
        getPermissions().add("*.default");
    }

    public long getServerId() {
        return serverId;
    }

    public void setServerId(long serverId) {
        this.serverId = serverId;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public ArrayList<String> getPermissions() {
        return permissions;
    }

    public void setPermissions(ArrayList<String> permissions) {
        this.permissions = permissions;
    }
}
