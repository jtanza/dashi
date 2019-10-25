package com.tanza.dashi;

import com.tanza.dashi.lib.LibConstants.StatusCode;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Optional;

/**
 * @author jtanza
 */
class ChannelBuffer {
    private static final int MAX_READ = 8192;
    // add some padding so we can detect and provide proper
    // error messaging if requests are > than MAX_READ
    private static final ByteBuffer BUFFER = ByteBuffer.allocateDirect(MAX_READ + 512);

    static Optional<byte[]> readFromChannel(SelectionKey key) throws RequestException {
        BUFFER.clear();

        int read;
        try {
            read = ((SocketChannel) key.channel()).read(BUFFER);
        } catch (IOException e) {
            e.printStackTrace();
            Utils.closeConnection(key);
            return Optional.empty();
        }

        if (read == -1) {
            Utils.closeConnection(key);
            return Optional.empty();
        }

        if (read > MAX_READ) {
            throw RequestException.from(StatusCode.PAYLOAD_TOO_LARGE);
        }

        // flip the buffer's state for a read
        BUFFER.flip();
        byte[] res = new byte[read];
        BUFFER.get(res);

        return Optional.of(res);
    }
}
