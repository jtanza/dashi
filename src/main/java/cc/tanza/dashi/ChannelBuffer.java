package cc.tanza.dashi;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Optional;

/**
 * Wrapper around a {@link ByteBuffer} used when reading from
 * client {@link SocketChannel}s.
 *
 * Note that the underlying {@link #buffer} is stored off-heap via
 * {@link ByteBuffer#allocateDirect(int)}
 *
 * @author jtanza
 */
class ChannelBuffer {
    private final int maxRead;
    private final ByteBuffer buffer;

    public ChannelBuffer(int maxRead) {
        this.maxRead = maxRead;
        // add some padding so we can detect and provide proper
        // error messaging if requests are > than MAX_READ
        this.buffer = ByteBuffer.allocateDirect(maxRead + 512);
    }

    Optional<byte[]> readFromChannel(SelectionKey key) throws RequestException {
        buffer.clear();

        int read;
        try {
            if (!key.isValid()) {
                return Optional.empty();
            }
            read = ((SocketChannel) key.channel()).read(buffer);
        } catch (IOException e) {
            e.printStackTrace();
            Utils.closeConnection(key);
            return Optional.empty();
        }

        if (read == -1) {
            Utils.closeConnection(key);
            return Optional.empty();
        }

        if (read > maxRead) {
            throw RequestException.from(HttpConstants.StatusCode.PAYLOAD_TOO_LARGE);
        }

        // flip the buffer's state for a read
        buffer.flip();
        byte[] res = new byte[read];
        buffer.get(res);

        return Optional.of(res);
    }
}
