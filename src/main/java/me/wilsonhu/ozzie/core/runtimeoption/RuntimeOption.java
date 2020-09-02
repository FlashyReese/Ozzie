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

public abstract class RuntimeOption {
    private String command;
    private String syntax;

    public RuntimeOption(String command, String syntax) {
        this.command = command;
        this.syntax = syntax;
    }

    public abstract void onRun(String full, String split, Ozzie ozzie) throws Exception;

    public String getCommand() {
        return this.command;
    }

    public String getSyntax() {
        return this.syntax;
    }
}
