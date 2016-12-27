package de.codehat.mcwrapper.i18n;

/**
 * Thrown if the selected language is not available.
 */
public class UnknownLanguageException extends Exception {

    /**
     * Selected language is not available.
     *
     * @param message Message shown in stacktrace.
     */
    public UnknownLanguageException(String message) {
        super(message);
    }

}
