/*
 * Copyright (C) 2019-2020 Yao Chung Hu / FlashyReese
 *
 * Ozzie is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * Ozzie is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Ozzie.  If not, see http://www.gnu.org/licenses/
 *
 */
package me.wilsonhu.ozzie.schemas;

import java.util.HashMap;

public class PluginSchema {
    private int schemaVersion;
    private String id;
    private String version;
    private String name;
    private String description;
    private HashMap<String, String> authors;
    private HashMap<String, String> contact;
    private String entrypoint;

    public int getSchemaVersion() {
        return schemaVersion;
    }

    public String getId() {
        return id;
    }

    public String getVersion() {
        return version;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public HashMap<String, String> getAuthors() {
        return authors;
    }

    public HashMap<String, String> getContact() {
        return contact;
    }

    public String getEntrypoint() {
        return entrypoint;
    }

}
