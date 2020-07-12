package me.wilsonhu.ozzie.core.configuration;

import java.util.Map;

public class ClientConfiguration {
    private Map<String, String> databaseConfig;

    public Map<String, String> getDatabaseConfig() {
        return databaseConfig;
    }

    public void setDatabaseConfig(Map<String, String> databaseConfig) {
        this.databaseConfig = databaseConfig;
    }
}
