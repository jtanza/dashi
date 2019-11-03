package com.tanza.dashi;

/**
 * Represents a {@link Server} designed to listen for
 * and respond to client requests as they are received
 * over a network.
 *
 * @author jtanza
 */
public interface Server {

    /**
     * Service client connections as they are received
     * over the network.
     */
    void serve();

    /**
     * Closes any open socket connections while releasing
     * any additional threads which may have been spawned
     * as part of this {@link Server}s execution context.
     */
    void stop();
}
