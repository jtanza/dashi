package com.tanza.kudu;

import lombok.NonNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.CharBuffer;

import static com.tanza.kudu.Constants.StatusCode.PAYLOAD_TOO_LARGE;

/**
 * @author jtanza
 */
public class SocketBuffer {
    private static final int MAX_READ = 8190;

    private final Socket client;
    private final CharBuffer buffer;
    private final BufferedReader in;
    private final PrintWriter out;

    public SocketBuffer(@NonNull Socket client) {
        this.client = client;
        this.buffer = CharBuffer.allocate(MAX_READ);
        try {
            this.in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            this.out = new PrintWriter(client.getOutputStream(), true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String read() {
        StringBuilder stringBuffer = new StringBuilder();
        try {
            int totalRead = 0;
            while (in.ready()) {
                int read = in.read(buffer);
                if (read == -1) {
                    break;
                }
                if ((totalRead += read) >= MAX_READ) {
                    throw new RequestException(PAYLOAD_TOO_LARGE);
                }
                buffer.flip();
                stringBuffer.append(buffer.toString());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return stringBuffer.toString();
    }

    public void write(String message) {
        out.println(message);
    }

    public void close() {
        try {
            in.close();
            out.close();
        } catch (IOException e) {
            //log
        }
    }
}
