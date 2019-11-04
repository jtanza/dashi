package cc.tanza.dashi;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * {@link RequestException}s are thrown when a
 * client {@link Request} results in some exceptional
 * situation that <em>should</em> be relayed back to the
 * requesting client.
 *
 * As an example a {@link RequestException} is thrown when a
 * client request exceeds the user configured max HTTP form size.
 *
 * @author jtanza
 */
@Getter
@RequiredArgsConstructor
class RequestException extends Exception {
    static final long serialVersionUID = 1;

    private final HttpConstants.StatusCode statusCode;
    private final String body;

    static RequestException from(HttpConstants.StatusCode statusCode) {
        return new RequestException(statusCode, null);
    }
}
