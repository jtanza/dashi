package com.tanza.kudu;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Optional;

import static com.tanza.kudu.Constants.StatusCode.PAYLOAD_TOO_LARGE;

/**
 * @author jtanza
 */
public class ChannelBuffer {
    private static final int MAX_READ = 8192;
    // add some padding so we can detect and provide proper
    // error messaging if requests are > than MAX_READ
    private static final ByteBuffer BUFFER = ByteBuffer.allocateDirect(MAX_READ + 8);

    public static Optional<byte[]> readFromChannel(SelectionKey key) {
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
            throw new RequestException(PAYLOAD_TOO_LARGE);
        }

        // flip the buffer's state for a read
        BUFFER.flip();
        byte[] res = new byte[read];
        BUFFER.get(res);

        return Optional.of(res);
    }
}
