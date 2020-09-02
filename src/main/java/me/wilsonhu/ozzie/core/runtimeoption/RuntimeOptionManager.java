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
package me.wilsonhu.ozzie.core.runtimeoption;

import me.wilsonhu.ozzie.Ozzie;
import me.wilsonhu.ozzie.core.AbstractManager;
import me.wilsonhu.ozzie.runtimeoptions.DisablePlugins;
import me.wilsonhu.ozzie.runtimeoptions.DiscordAPI;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class RuntimeOptionManager extends AbstractManager {

    private ArrayList<RuntimeOption> runtimeOptions = new ArrayList<RuntimeOption>();

    public RuntimeOptionManager(Ozzie ozzie) {
        super(ozzie);
        this.info("Building Runtime Option Manager...");
        this.loadRuntimeOptions();
        this.info("Runtime Option Manager built!");
    }

    private void loadRuntimeOptions() {
        this.getRuntimeOptions().add(new DiscordAPI());
        this.getRuntimeOptions().add(new DisablePlugins());
    }

    public void initRuntimeOptions(String[] args) throws Exception {
        List<String> formattedCommandList = new ArrayList<>();
        StringBuilder temp = new StringBuilder();
        for (String arg : args) {
            if (arg.startsWith("-")) {
                if (temp.length() > 0) {
                    formattedCommandList.add(temp.toString());
                }
                temp = new StringBuilder();
                temp.append(arg).append(" ");
            } else {
                temp.append(arg).append(" ");
            }
        }
        if (temp.length() > 0) formattedCommandList.add(temp.toString());
        for (String cmd : formattedCommandList) {
            cmd = cmd.trim().substring(1);
            String full = cmd;
            String split = cmd;
            if (cmd.contains(" ")) {
                split = cmd.substring(cmd.indexOf(" ")).trim();
            }
            for (RuntimeOption runtimeOption : this.getRuntimeOptions()) {
                if (full.toLowerCase().startsWith(runtimeOption.getCommand().toLowerCase())) {
                    runtimeOption.onRun(full, split, getOzzie());
                }
            }
        }
    }

    public RuntimeOption getRuntimeOption(Class<? extends RuntimeOption> leCommandClass) {
        for (RuntimeOption c : this.getRuntimeOptions()) {
            if (c.getClass() == leCommandClass) {
                return c;
            }
        }
        return null;
    }

    public ArrayList<RuntimeOption> getRuntimeOptions() {
        return this.runtimeOptions;
    }

    @Override
    public @NotNull String getName() {
        return "Runtime Option Manager";
    }
}
