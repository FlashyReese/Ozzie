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
package me.wilsonhu.ozzie.core;

import me.wilsonhu.ozzie.Ozzie;
import org.aperlambda.lambdacommon.utils.Nameable;

public abstract class AbstractManager implements Nameable {

    private final Ozzie ozzie;

    protected AbstractManager(Ozzie ozzie) {
        this.ozzie = ozzie;
    }

    protected Ozzie getOzzie() {
        return this.ozzie;
    }

    protected void info(String message) {
        this.getOzzie().getLogger().info(String.format("%s - %s", getName(), message));
    }

    protected void warn(String message) {
        this.getOzzie().getLogger().warn(String.format("%s - %s", getName(), message));
    }

    protected void error(String message) {
        this.getOzzie().getLogger().error(String.format("%s - %s", getName(), message));
    }
}
