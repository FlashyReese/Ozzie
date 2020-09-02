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

import me.wilsonhu.ozzie.Ozzie;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Locale;

public class TranslatableText {
    private String key;
    private MessageReceivedEvent event;
    private String lang;
    private TranslationType type;
    private long id;

    public TranslatableText(String key) {
        this.key = key;
        this.type = TranslationType.DEFAULT;
    }

    public TranslatableText(String key, MessageReceivedEvent event) {
        this.key = key;
        this.event = event;
    }

    public TranslatableText(String key, String lang) {
        this.key = key;
        this.lang = lang;
    }

    public TranslatableText(String key, TranslationType type, long id) {
        this.key = key;
        this.type = type;
        this.id = id;
    }

    public String toString() {
        String lang = Locale.getDefault().toString();
        if (type != null && id != 0L) {
            if (type == TranslationType.DEFAULT) {
                return Ozzie.getOzzie().getI18nManager().translate(key, lang);
            } else {
                return Ozzie.getOzzie().getI18nManager().translate(key, type, id);
            }
        }
        if (event != null) {
            return Ozzie.getOzzie().getI18nManager().translate(key, event);
        }
        if (this.lang != null) {
            return Ozzie.getOzzie().getI18nManager().translate(key, this.lang);
        }
        return Ozzie.getOzzie().getI18nManager().translate(key, lang);
    }

    enum TranslationType {
        DEFAULT, SERVER, USER;
    }
}
