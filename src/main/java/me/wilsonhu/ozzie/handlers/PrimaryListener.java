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
package me.wilsonhu.ozzie.handlers;

import me.wilsonhu.ozzie.Ozzie;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public class PrimaryListener extends ListenerAdapter {

    private static final Logger log = LogManager.getLogger(PrimaryListener.class);
    private Ozzie ozzie;

    public PrimaryListener(Ozzie ozzie){
        this.ozzie = ozzie;
    }

    @Override
    public void onReady(@NotNull ReadyEvent event){
        log.info("Ready to go!");
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        getOzzie().getCommandManager().onMessageReceived(event, null);
    }

    public Ozzie getOzzie(){
        return ozzie;
    }
}
