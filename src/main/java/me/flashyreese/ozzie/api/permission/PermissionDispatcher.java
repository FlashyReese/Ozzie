package me.flashyreese.ozzie.api.permission;

import me.flashyreese.common.permission.PermissionException;
import me.flashyreese.ozzie.api.OzzieApi;
import me.flashyreese.ozzie.api.database.mongodb.schema.RoleSchema;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class PermissionDispatcher extends me.flashyreese.common.permission.PermissionDispatcher {

    public boolean hasPermission(String requiredPermission, long roleIdentifier){
        try {
            RoleSchema roleSchema = OzzieApi.INSTANCE.getDatabaseHandler().retrieveRole(roleIdentifier);
            return this.hasPermission(requiredPermission, roleSchema);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean hasPermission(String requiredPermission, @NotNull Map<String, Boolean> userPermissionMap) throws PermissionException {
        return super.hasPermission(requiredPermission, userPermissionMap);
    }
}
