package me.wilsonhu.ozzie.core.parameter;

import me.wilsonhu.ozzie.Ozzie;

public abstract class Parameter {
    private String command;
    private String syntax;

    public Parameter(String command, String syntax){
        this.command = command;
        this.syntax = syntax;
    }

    public abstract void onRun(String full, String split, Ozzie ozzie) throws Exception;

    public String getCommand(){
        return command;
    }

    public String getSyntax(){
        return syntax;
    }
}
