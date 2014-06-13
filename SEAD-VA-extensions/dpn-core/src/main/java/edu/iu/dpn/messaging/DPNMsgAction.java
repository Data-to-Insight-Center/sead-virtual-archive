/**
 * Contains all valid message activities. This is used to denote what kind of
 * message is being sent.
 */

package edu.iu.dpn.messaging;

public enum DPNMsgAction {
    ACK("ack"),
    NACK("nack"),
    QUERY("query"),
    ;

    private final String text;

    DPNMsgAction(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return this.text;
    }
}
