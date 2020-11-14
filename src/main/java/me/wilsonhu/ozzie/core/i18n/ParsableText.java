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

public class ParsableText {

    private String parsableText;
    private String[] args;

    public ParsableText(String parsableText, String... args) {
        this.parsableText = parsableText;
        this.args = args;
    }

    public ParsableText(TranslatableText translatableText, String... args) {
        this.parsableText = translatableText.toString();
        this.args = args;
    }

    @Override
    public String toString() {
        return format(parsableText, args);
    }

    private String format(String text, String... args) { //Fixme:  This can be improved to String.format level, Update: Also throw exception for easier diagnostic(wasn't hard for me but probably going to be hard for others)
        for (int i = 0; i < args.length; i++) {//Todo: Add custom expression support xd #toLower, #toTitle, #.2f
            if (!text.contains("{" + (i + 1) + "}")) {
                System.out.println("Missing notation to parse: " + args[i]);
                continue;
            }
            if (text.contains("{" + (i + 2) + "}") && i + 1 == args.length) {//this is technically useless xd only checks for 1 after args.length
                System.out.println("Missing object to parse: {" + (i + 2) + "}");
            }
            text = text.replaceAll("\\{" + (i + 1) + "}", args[i]);
        }
        return text;
    }

}
