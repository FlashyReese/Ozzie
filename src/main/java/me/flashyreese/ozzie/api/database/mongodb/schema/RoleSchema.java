/*
 * Copyright © 2021 FlashyReese <reeszrbteam@gmail.com>
 *
 * This file is part of Ozzie.
 *
 * Licensed under the MIT license. For more information,
 * see the LICENSE file.
 */

package me.flashyreese.ozzie.api.database.mongodb.schema;

import me.flashyreese.common.permission.Permissible;

import java.util.HashMap;
import java.util.Map;

/**
 * Serialization/Deserialization template for Role
 *
 * @author FlashyReese
 * @version 0.9.0+build-20210105
 * @since 0.9.0+build-20210105
 */
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
