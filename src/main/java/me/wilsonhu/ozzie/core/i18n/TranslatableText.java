package me.wilsonhu.ozzie.core.i18n;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

/*
Create by @author flashyreese
Created @since 1/2/20
Created @version 1.0 
*/
public class TranslatableText {
    private String key;
    private MessageReceivedEvent event;
    private String lang;
    private TranslationType type;
    private long id;

    public TranslatableText(String key){
        this.key = key;
        this.type = TranslationType.DEFAULT;
    }

    public TranslatableText(String key, MessageReceivedEvent event){
        this.key = key;
        this.event = event;
    }

    public TranslatableText(String key, String lang){
        this.key = key;
        this.lang = lang;
    }

    public TranslatableText(String key, TranslationType type, long id){
        this.key = key;
        this.type = type;
        this.id = id;
    }

    public String toString(){
        String lang = "en_US";
        if(type != null && id != 0L){
            if(type == TranslationType.DEFAULT){
                return I18nManager.translate(key, lang);
            }else {
                return I18nManager.translate(key, type, id);
            }
        }
        if(event != null){
            return I18nManager.translate(key, event);
        }
        if(this.lang != null){
            return I18nManager.translate(key, this.lang);
        }
        return I18nManager.translate(key, lang);
    }

    enum TranslationType{
        DEFAULT, SERVER, USER;
    }
}
