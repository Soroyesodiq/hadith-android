package org.sonna.www.sonna.services;

import android.support.annotation.NonNull;

public class TextUtils {

    private static String addVowels(String arabic) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < arabic.length(); i++) {
            result.append(arabic.charAt(i));
            result.append("[\u064B-\u065F]*"); //vowels
            //unicode vowels letters from url: http://unicode.org/charts/PDF/U0600.pdf
        }
        return result.toString();
    }

    @NonNull
    public static String highlight(String bodyString, String highlightWords) {
        final String spanStart = "<font color=\"red\">";
        final String spanEnd = "</font>";

        for (String word : highlightWords.split(" ")) {
            word = word.trim();
            if (word.length() > 0) {
                String processedWord = addVowels(word);
                bodyString = bodyString.replaceAll("(" + processedWord + ")", spanStart + "$1" + spanEnd);
            }
        }
        return bodyString;
    }


    @NonNull
    public static String decorate(@NonNull String searchWords, @NonNull String title, @NonNull String content) {

        content = content.trim();
        content = removeTrailingHashes(content);
        content = content.trim();

        final String htmlPagePrefix = "<html><body style='direction: rtl; text-align:justify; align-content: right;  text-align=right'><span align='right'>";
        final String htmlPagePostfix = "</span></body><html>";

        content = content.replaceAll("##", "<br><hr>");
        content = content.replaceAll("\n", "<br>");
        if(searchWords.trim().length() > 0) { //highlight search text
            content = TextUtils.highlight(content, searchWords);
        }

        //Add title
        content = "<font color=\"blue\">" + TextUtils.removeTrailingDot(title) + "</font><hr>" + content;
        return htmlPagePrefix + content + htmlPagePostfix;
    }

    @NonNull
    public static String removeTrailingHashes(@NonNull String content) {
        if(content.charAt(content.length()-1) == '#') {
            return content.substring(0, content.length()-2);
        }
        return content;
    }

    @NonNull
    public static String removeTrailingDot(@NonNull String content) {
        if(content.charAt(content.length()-1) == '.') {
            return content.substring(0, content.length()-1);
        }
        return content;
    }


}
