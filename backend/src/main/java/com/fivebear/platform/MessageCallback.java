package com.fivebear.platform;
/**
 * Interface for handling messages from external sites.
 */
public interface MessageCallback {
    /**
     * Called when a message is received from the external site.
     * 
     * @param message     The message content
     * @param messageType The type of the message
     */
    void onMessageReceived(String message, String messageType);

    /**
     * Called when an error occurs while processing a message.
     * 
     * @param error     The error message
     * @param exception The exception that occurred (if any)
     */
    void onError(String error, Exception exception);
}