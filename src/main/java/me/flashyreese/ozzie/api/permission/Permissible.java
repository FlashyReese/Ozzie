package me.flashyreese.ozzie.api.permission;

import java.util.Map;

public interface Permissible {
    Map<String, Boolean> permissions();
}
