package cc.tanza.dashi;

/**
 * @author jtanza
 */
public class Example {
    public static void main(String[] args) {
        HttpServer.builder(new RequestDispatcher()
            .addHandler(new RequestHandler("/ping", r -> Response.ok("pong")))
            .addResourcePath("/web")
        ).build().serve();
    }
}
