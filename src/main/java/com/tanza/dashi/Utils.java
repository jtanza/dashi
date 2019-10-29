package com.tanza.dashi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

/**
 * @author jtanza
 */
class Utils {

    private Utils() {
        throw new AssertionError();
    }

    /**
     * @param streamReader an un-buffered {@link Reader}.
     * @return {@link String} representation of the character stream associated with the {@param streamReader}.
     */
    static String getResource(Reader streamReader) {
        StringBuilder builder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(streamReader)) {
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(System.lineSeparator());
                builder.append(line);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return builder.toString();
    }

    static void closeConnection(SelectionKey key) {
        SocketChannel channel = (SocketChannel) key.channel();
        key.cancel();
        try {
            channel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
