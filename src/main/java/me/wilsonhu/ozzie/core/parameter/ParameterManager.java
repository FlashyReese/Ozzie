package me.wilsonhu.ozzie.core.parameter;

import me.wilsonhu.ozzie.Ozzie;
import me.wilsonhu.ozzie.parameters.DisablePlugins;
import me.wilsonhu.ozzie.parameters.DiscordAPI;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;

public class ParameterManager {

    private static final Logger log = LogManager.getLogger(ParameterManager.class);

    private ArrayList<Parameter> parameters = new ArrayList<Parameter>();

    public ParameterManager(){
        log.info("Building Parameter Manager...");
        loadParameters();
        log.info("Parameter Manager built!");
    }

    private void loadParameters(){
        getParameters().add(new DiscordAPI());
        getParameters().add(new DisablePlugins());
    }

    public void runParameters(String[] args, Ozzie ozzie) throws Exception {
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
            for(Parameter parameter : getParameters()){
                if(full.toLowerCase().startsWith(parameter.getCommand().toLowerCase())){
                    parameter.onRun(full, split, ozzie);
                }
            }
        }
    }

    public Parameter getParameter(Class<?extends Parameter> leCommandClass){
        for (Parameter c: getParameters()){
            if (c.getClass() == leCommandClass){
                return c;
            }
        }
        return null;
    }

    public ArrayList<Parameter> getParameters(){
        return parameters;
    }

}
