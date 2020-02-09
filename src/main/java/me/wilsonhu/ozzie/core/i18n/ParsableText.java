package me.wilsonhu.ozzie.core.i18n;

public class ParsableText {

    private TranslatableText translatableText;
    private String[] args;

    public ParsableText(TranslatableText translatableText, String... args){
        this.translatableText = translatableText;
        this.args = args;
    }

    @Override
    public String toString() {
        return format(translatableText.toString(), args);
    }

    private String format(String text, String... args) { // This can be improved to String.format level
        for(int i = 0; i < args.length; i++) {
            if(!text.contains("{" + (i+1) + "}")) {
                System.out.println("Missing notation to parse: " + args[i]);
                continue;
            }
            text = text.replaceAll("\\{" + (i+1) + "}", args[i]);
        }
        return text;
    }

}
