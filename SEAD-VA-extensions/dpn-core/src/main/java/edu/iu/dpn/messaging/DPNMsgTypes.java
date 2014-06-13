/**
 * Contains all valid message types or message names used by the First Node
 * Add an entry to this file, if a new message type or message name is being
 * introduced
 */

package edu.iu.dpn.messaging;

public enum DPNMsgTypes {
    REPLICATION_INIT_QUERY("replication-init-query"),
    REPLICATION_AVAILABLE_REPLY("replication-available-reply"),
    REPLICATION_LOCATION_REPLY("replication-location-reply"),
    REPLICATION_VERIFICATION_REPLY("replication-verification-reply"),
    REPLICATION_LOCATION_CANCEL("replication-location-cancel"),
    REPLICATION_TRANSFER_REPLY("replication-transfer-reply"),
    REPLICATION_VERIFY_REPLY("replication-verify-reply"),
    REGISTRY_ITEM_CREATE("registry-item-create"),
    REGISTRY_ENTRY_CREATED("registry-entry-created"),
    ;

    private final String text;

    DPNMsgTypes(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return this.text;
    }
}
