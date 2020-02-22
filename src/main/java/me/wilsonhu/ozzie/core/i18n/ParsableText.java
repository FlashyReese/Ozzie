package me.wilsonhu.ozzie.core.i18n;

public class ParsableText {

    private String parsableText;
    private String[] args;

    public ParsableText(String parsableText, String... args){
        this.parsableText = parsableText;
        this.args = args;
    }

    public ParsableText(TranslatableText translatableText, String... args){
        this.parsableText = translatableText.toString();
        this.args = args;
    }

    @Override
    public String toString() {
        return format(parsableText, args);
    }

    private String format(String text, String... args) { //Todo:  This can be improved to String.format level
        for(int i = 0; i < args.length; i++) {
            if(!text.contains("{" + (i+1) + "}")) {
                System.out.println("Missing notation to parse: " + args[i]);
                continue;
            }
            if(text.contains("{" + (i+2) + "}") && i+1 == args.length){//this is technically useless xd only checks for 1 after args.length
                System.out.println("Missing object to parse: {" + (i+2) + "}");
            }
            text = text.replaceAll("\\{" + (i+1) + "}", args[i]);
        }
        return text;
    }

}
