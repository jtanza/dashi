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
     * over a network.
     */
    void serve();
}
