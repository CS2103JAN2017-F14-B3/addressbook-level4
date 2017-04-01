package seedu.address.commons.util;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Helper functions for handling strings.
 */
public class StringUtil {

    /**
     * Returns true if the {@code sentence} contains the {@code word}.
     *   Ignores case, but a full word match is required.
     *   <br>examples:<pre>
     *       containsWordIgnoreCase("ABc def", "abc") == true
     *       containsWordIgnoreCase("ABc def", "DEF") == true
     *       containsWordIgnoreCase("ABc def", "AB") == false //not a full word match
     *       </pre>
     * @param sentence cannot be null
     * @param word cannot be null, cannot be empty, must be a single word
     */
    public static boolean containsWordIgnoreCase(String sentence, String word) {
        assert word != null : "Word parameter cannot be null";
        assert sentence != null : "Sentence parameter cannot be null";

        String preppedWord = word.trim();
        assert !preppedWord.isEmpty() : "Word parameter cannot be empty";
        assert preppedWord.split("\\s+").length == 1 : "Word parameter should be a single word";

        String preppedSentence = sentence;
        String[] wordsInPreppedSentence = preppedSentence.split("\\s+");

        for (String wordInSentence: wordsInPreppedSentence) {
            if (wordInSentence.equalsIgnoreCase(preppedWord)) return true;
        }
        return false;
    }

    /**
     * Returns a detailed message of the t, including the stack trace.
     */
    public static String getDetails(Throwable t) {
        assert t != null;
        StringWriter sw = new StringWriter();
        t.printStackTrace(new PrintWriter(sw));
        return t.getMessage() + "\n" + sw.toString();
    }

    /**
     * Returns true if s represents an unsigned integer e.g. 1, 2, 3, ... <br>
     * Will return false if the string is:
     * null, empty string, "-1", "0", "+1", and " 2 " (untrimmed) "3 0" (contains whitespace).
     * @param s Should be trimmed.
     */
    public static boolean isUnsignedInteger(String s) {
        return s != null && s.matches("^0*[1-9]\\d*$");
    }

    //@@author A0140023E
    /**
     * Replaces the substring specified by {@code startIndex} and {@code endIndex} of {@code s} with
     * the String {@code replacement}. The substring begins at the specified {@code startIndex} and
     * extends to the character at {@code endIndex} - 1 or to the end of the sequence if the endIndex is larger.
     * exists. First the characters in the substring are removed and then the specified String is
     * inserted at start. (The String will be lengthened to accommodate the specified String if
     * necessary.)
     *
     * @param s
     * @param startIndex
     * @param endIndex
     * @param replacement
     * @return
     * @see StringBuilder#replace(int, int, String)
     */
    public static String replace(String s, int startIndex, int endIndex, String replacement) {
        assert s != null && replacement != null;

        StringBuilder sb = new StringBuilder(s);
        sb.replace(startIndex, endIndex, replacement);
        return sb.toString();
    }
}
