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
package me.wilsonhu.ozzie.runtimeoptions;

import me.wilsonhu.ozzie.Ozzie;
import me.wilsonhu.ozzie.core.runtimeoption.RuntimeOption;

public class DiscordAPI extends RuntimeOption {

    public DiscordAPI() {
        super("discordapi", "discordapi <token>");
    }

    @Override
    public void onRun(String full, String split, Ozzie ozzie) throws Exception {
        ozzie.getTokenManager().addToken("discord", split);
    }
}
