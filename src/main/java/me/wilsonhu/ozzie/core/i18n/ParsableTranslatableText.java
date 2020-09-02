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
package me.wilsonhu.ozzie.core.i18n;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Locale;

public class ParsableTranslatableText {

    private String key;
    private MessageReceivedEvent event;
    private String lang;
    private TranslatableText.TranslationType type;
    private long id;
    private String[] args;

    public ParsableTranslatableText(String key, String... args) {
        this.key = key;
        this.args = args;
    }

    public ParsableTranslatableText(MessageReceivedEvent event, String key, String... args) {
        this.event = event;
        this.key = key;
        this.args = args;
    }

    public ParsableTranslatableText(String lang, String key, String... args) {
        this.lang = lang;
        this.key = key;
        this.args = args;
    }

    public ParsableTranslatableText(TranslatableText.TranslationType type, long id, String key, String... args) {
        this.type = type;
        this.id = id;
        this.key = key;
        this.args = args;
    }

    public ParsableTranslatableText(TranslatableText.TranslationType type, String key, String... args) {
        this.type = type;
        this.key = key;
        this.args = args;
    }

    public String toString() {
        String lang = Locale.getDefault().toString();
        if (type != null && id != 0L) {
            if (type == TranslatableText.TranslationType.DEFAULT) {
                return new ParsableText(new TranslatableText(lang, key), args).toString();
            } else {
                return new ParsableText(new TranslatableText(key, type, id), args).toString();
            }
        }
        if (event != null) {
            return new ParsableText(new TranslatableText(key, event), args).toString();
        }
        if (this.lang != null) {
            return new ParsableText(new TranslatableText(key, this.lang), args).toString();
        }
        return new ParsableText(new TranslatableText(key), args).toString();
    }
}
