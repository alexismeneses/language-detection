package com.cybozu.labs.langdetect;

public class NoFeatureInTextException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public NoFeatureInTextException() {
        super();
    }

    public NoFeatureInTextException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoFeatureInTextException(String message) {
        super(message);
    }

    public NoFeatureInTextException(Throwable cause) {
        super(cause);
    }

}
