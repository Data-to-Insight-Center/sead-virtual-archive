package edu.iu.dpn.messaging;

/**
 * This contains all the entries that will be part of the message header and body
 */

public enum DPNMsgConstants {
    // Message Header entries
    FROM("from"),
    FROM_VALUE("IU"),
    REPLY_KEY("reply_key"),
    REPLY_KEY_VALUE("dpn.iu"),
    SEQUENCE("sequence"),
    REP_INT_QUERY_SEQUENCE("0"),
    REP_AVAIL_REPLY_SEQUENCE("1"),
    REP_LOC_REPLY_SEQUENCE("2"),
    REP_LOC_CANCEL_SEQUENCE("3"),
    REP_TRANSFER_REPLY_SEQUENCE("4"),
    REP_VERIFY_REPLY_SEQUENCE("5"),
    CORRELATION_ID("correlation_id"),
    DATE("date"),
    TTL("ttl"),
    TTL_DURATION("3600"),

    // Message Body entries
    MESSAGE_NAME("message_name"),
    REPLICATION_SIZE("replication_size"),
    PROTOCOL("protocol"),
    PROTOCOL_TYPE("rsync"),
    DPN_OBJECT_ID("dpn_object_id"),
    LOCATION("location"),
    MESSAGE_ATT("message_att"),
    ;

    private final String text;

    DPNMsgConstants(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return this.text;
    }
}
