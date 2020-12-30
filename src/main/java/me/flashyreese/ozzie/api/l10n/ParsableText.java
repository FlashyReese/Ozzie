package me.flashyreese.ozzie.api.l10n;

import org.jetbrains.annotations.NotNull;

public class ParsableText implements CharSequence{

    private final String parsableText;
    private final String[] args;

    public ParsableText(String parsableText, String... args) {
        this.parsableText = parsableText;
        this.args = args;
    }

    public ParsableText(TranslatableText translatableText, String... args) {
        this.parsableText = translatableText.toString();
        this.args = args;
    }

    @Override
    public int length() {
        return this.toString().length();
    }

    @Override
    public char charAt(int index) {
        return this.toString().charAt(index);
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        return this.toString().subSequence(start, end);
    }

    @Override
    public @NotNull String toString() {
        return this.format(this.parsableText, this.args);
    }

    private String format(String text, String... args) {
        for (int i = 0; i < args.length; i++) {
            if (!text.contains("{" + (i + 1) + "}")) {
                //System.out.println("Missing notation to parse: " + args[i]);
                continue;
            }
            /*if (text.contains("{" + (i + 2) + "}") && i + 1 == args.length) {//this is technically useless xd only checks for 1 after args.length
                //System.out.println("Missing object to parse: {" + (i + 2) + "}");
            }*/
            text = text.replace("{" + (i + 1) + "}", args[i]);
        }
        return text;
    }

}