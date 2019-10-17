package com.tanza.kudu;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import static com.tanza.kudu.Constants.StatusCode.INTERNAL_SERVER_ERROR;

/**
 * @author jtanza
 */
public class Utils {

    private Utils() {
        throw new AssertionError();
    }

    /**
     * N.B. {@param path} must represent fully qualified path of resource
     * on disk
     *
     * @param path fully qualified path of resource on disk
     * @return {@link String} representation of resource located at {@param path}
     */
    public static String getResource(String path) {
        StringBuilder builder = new StringBuilder();
        try(BufferedReader reader = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(System.lineSeparator());
                builder.append(line);
            }
        } catch (Exception e) {
            throw new RequestException(INTERNAL_SERVER_ERROR);
        }
        return builder.toString();
    }

    public static void closeConnection(SelectionKey key) {
        SocketChannel channel = (SocketChannel) key.channel();
        key.cancel();
        try {
            channel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
