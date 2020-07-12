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
import me.wilsonhu.ozzie.runtimeoptions.DisablePlugins;
import me.wilsonhu.ozzie.runtimeoptions.DiscordAPI;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;

public class RuntimeOptionManager {

    private static final Logger log = LogManager.getLogger(RuntimeOptionManager.class);

    private ArrayList<RuntimeOption> runtimeOptions = new ArrayList<RuntimeOption>();

    public RuntimeOptionManager(){
        log.info("Building Runtime Option Manager...");
        loadRuntimeOptions();
        log.info("Runtime Option Manager built!");
    }

    private void loadRuntimeOptions(){
        getRuntimeOptions().add(new DiscordAPI());
        getRuntimeOptions().add(new DisablePlugins());
    }

    public void initRuntimeOptions(String[] args, Ozzie ozzie) throws Exception {
        ArrayList<String> formattedCommandList = new ArrayList<String>();
        StringBuilder temp = new StringBuilder();
        for(String arg: args){
            if(arg.startsWith("-")){
                if(temp.length() > 0){
                    formattedCommandList.add(temp.toString());
                }
                temp = new StringBuilder();
                temp.append(arg).append(" ");
            }else{
                temp.append(arg).append(" ");
            }
        }
        if(temp.length() > 0)formattedCommandList.add(temp.toString());
        for(String cmd: formattedCommandList){
            cmd = cmd.trim().substring(1);
            String full = cmd;
            String split = cmd;
            if(cmd.contains(" ")){
                split = cmd.substring(cmd.indexOf(" ")).trim();
            }
            for(RuntimeOption runtimeOption : getRuntimeOptions()){
                if(full.toLowerCase().startsWith(runtimeOption.getCommand().toLowerCase())){
                    runtimeOption.onRun(full, split, ozzie);
                }
            }
        }
    }

    public RuntimeOption getRuntimeOption(Class<?extends RuntimeOption> leCommandClass){
        for (RuntimeOption c: getRuntimeOptions()){
            if (c.getClass() == leCommandClass){
                return c;
            }
        }
        return null;
    }

    public ArrayList<RuntimeOption> getRuntimeOptions(){
        return runtimeOptions;
    }

}
