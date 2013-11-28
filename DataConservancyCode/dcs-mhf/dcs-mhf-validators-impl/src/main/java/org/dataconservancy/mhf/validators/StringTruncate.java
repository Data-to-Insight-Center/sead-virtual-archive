package org.dataconservancy.mhf.validators;

/**
 * Truncates long strings for readability.  It is a naive implementation which truncates long strings at a specified
 * length, and optionally appends trailing ellipses to indicate truncation has occurred.  It doesn't have more
 * advanced features such as truncating on word boundaries, truncating the beginning of a string instead of the
 * end, or truncating the middle of the string.
 */
class StringTruncate {

    private static final int DEFAULT_LENGTH = 8;

    private static final boolean DEFAULT_TRAILING_ELLIPSE = true;

    private static final String TRAILING_ELLIPSE = "[...]";

    private boolean trailingEllipse = DEFAULT_TRAILING_ELLIPSE;

    private int length = DEFAULT_LENGTH;

    String truncate(String toTruncate) {
        if (toTruncate == null) {
            return null;
        }

        if (toTruncate.length() < length || length < 1) {
            return toTruncate;
        }

        final StringBuilder truncated = new StringBuilder(toTruncate.substring(0, length));

        if (trailingEllipse) {
            truncated.append(TRAILING_ELLIPSE);
        }

        return truncated.toString();
    }

    int getLength() {
        return length;
    }

    void setLength(int length) {
        this.length = length;
    }

    boolean isTrailingEllipse() {
        return trailingEllipse;
    }

    void setTrailingEllipse(boolean trailingEllipse) {
        this.trailingEllipse = trailingEllipse;
    }
}
