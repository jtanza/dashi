package com.tanza.kudu;

import lombok.NonNull;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Optional;

import static com.tanza.kudu.Constants.StatusCode.PAYLOAD_TOO_LARGE;

/**
 * @author jtanza
 */
public class SocketBuffer {
    private static final int MAX_READ = 8192;

    private final SelectionKey key;
    private final ByteBuffer buffer;

    public SocketBuffer(@NonNull SelectionKey key) {
        this.key = key;
        this.buffer = ByteBuffer.allocate(MAX_READ);
    }

    public Optional<byte[]> readFromChannel() {
        int read;
        try {
            read = ((SocketChannel) key.channel()).read(buffer);
        } catch (IOException e) {
            //TODO rethrow as runtime?
            e.printStackTrace();
            Utils.closeConnection(key);
            return Optional.empty();
        }

        if (read == -1) {
            Utils.closeConnection(key);
            return Optional.empty();
        }
        if (read >= MAX_READ) {
            throw new RequestException(PAYLOAD_TOO_LARGE);
        }

        buffer.flip();
        byte[] res = new byte[read];
        buffer.get(res, 0, read);

        return Optional.of(res);
    }
}
