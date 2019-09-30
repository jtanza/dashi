package com.tanza.kudu;

import java.io.BufferedReader;
import java.io.FileReader;

import static com.tanza.kudu.Constants.StatusCode.INTERNAL_SERVER_ERROR;

/**
 * @author jtanza
 */
public class Utils {

    private Utils() {
        throw new AssertionError();
    }

    public static boolean isEmpty(String value) {
        return value == null || value.isEmpty();
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
}
