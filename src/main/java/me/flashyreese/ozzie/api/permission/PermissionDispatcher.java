package me.flashyreese.ozzie.api.permission;

import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class PermissionDispatcher {

    public boolean hasPermission(String requiredPermission, Object entity){
        if (!(entity instanceof Permissible))
            return false;
        Permissible permissible = (Permissible) entity;
        return this.hasPermission(requiredPermission, permissible.permissions());
    }

    public boolean hasPermission(String requiredPermission, @NotNull Map<String, Boolean> userPermissionMap){
        requiredPermission = requiredPermission.toLowerCase().trim();
        //Todo: Add valid format check with regex
        if (requiredPermission.contains("*") || requiredPermission.endsWith(".") || requiredPermission.startsWith(".")) {
            return false;
        }

        if (userPermissionMap.containsKey("*") && userPermissionMap.get("*") || userPermissionMap.containsKey(requiredPermission) && userPermissionMap.get(requiredPermission)) {
            return true;
        }
        String[] tree = requiredPermission.split("\\.");
        StringBuilder node = new StringBuilder();
        for (int i = 0; i <= tree.length - 1; i++) {
            if (!node.toString().isEmpty()) {
                node.deleteCharAt(node.length() - 1);
            }
            node.append(tree[i]);
            node.append(".*");
            if (userPermissionMap.containsKey(node.toString()) && userPermissionMap.get(node.toString())) {
                return true;
            }
        }

        return false;
    }
}
