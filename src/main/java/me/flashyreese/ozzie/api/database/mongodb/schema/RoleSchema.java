package me.flashyreese.ozzie.api.database.mongodb.schema;

import me.flashyreese.common.permission.Permissible;

import java.util.HashMap;
import java.util.Map;

public class RoleSchema implements Permissible {

    private long roleIdentifier;
    private Map<String, Boolean> permissions = new HashMap<>();

    public RoleSchema() {

    }

    public RoleSchema(long roleIdentifier) {
        this.roleIdentifier = roleIdentifier;
    }

    public long getRoleIdentifier() {
        return roleIdentifier;
    }

    public void setRoleIdentifier(long roleIdentifier) {
        this.roleIdentifier = roleIdentifier;
    }

    public Map<String, Boolean> getPermissions() {
        return permissions;
    }

    public void setPermissions(Map<String, Boolean> permissions) {
        this.permissions = permissions;
    }

    @Override
    public Map<String, Boolean> permissions() {
        return permissions;
    }
}
