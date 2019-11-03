package com.tanza.dashi;

import com.tanza.dashi.HttpConstants.Method;

import lombok.NonNull;
import lombok.Value;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * A {@link RequestDispatcher} allocates {@link RequestHandler}s to
 * incoming client {@link Request}s as they are received over the network.
 *
 * @author jtanza
 */
public class RequestDispatcher {
    private static final String JAR = "jar";

    private final Map<ResourceId, RequestHandler> handlers;
    private final List<RequestHandler> variableHandlers;

    public RequestDispatcher() {
        this.handlers = new HashMap<>();
        this.variableHandlers = new ArrayList<>();
    }

    /**
     * A convenience method instantiating a {@link RequestDispatcher} from a collection of
     * {@link RequestHandler}s
     *
     * @param handlers
     */
    public RequestDispatcher(Collection<RequestHandler> handlers) {
        this.handlers = handlers.stream().collect(Collectors.toMap(
            h -> new ResourceId(h.getMethod(), h.getPath()), Function.identity())
        );
        this.variableHandlers = new ArrayList<>();
    }

    /**
     * Adds an {@link RequestHandler} to the underlying {@link RequestDispatcher}.
     * This method is specified to return the dispatcher upon which it was invoked,
     * allowing for chained method invocation.
     *
     * @param handler
     * @return
     */
    public RequestDispatcher addHandler(@NonNull RequestHandler handler) {
        String path = handler.getPath();
        handlers.put(new ResourceId(handler.getMethod(), path), handler);
        if (isVarPath(path)) {
            variableHandlers.add(handler);
        }
        return this;
    }

    /**
     * Configures {@param resourcePath} as the directory from which to serve static files on client requests.
     *
     * More specifically, this method scans for files located in {@code /src/main/resources/{@param resourcePath}}
     * and generates {@link RequestHandler}s to respond to user requests for said static files.
     *
     * Note, {@param resourcePath} <em>must</em> be a subdirectory of {@code /src/main/resources/},
     * and it is required that only the basename is provided as an argument to this method.
     *
     * For example, with static files located under {@code /src/main/resources/web/} the {@param resourcePath}
     * passed to this method should be {@code /web}.
     *
     * @param resourcePath the directory under which static resources are located
     * @return
     */
    public RequestDispatcher addResourcePath(@NonNull String resourcePath) {
        return addResourceHandlers(makeResourceHandlers(scanForResources(resourcePath), resourcePath));
    }

    Optional<RequestHandler> getHandlerFor(Request request) {
        return request == null
            ? Optional.empty()
            : getHandlerFor(request.getMethod(), request.getUrl().getPath());
    }

    Optional<RequestHandler> getHandlerFor(Method requestMethod, String requestPath) {
        ResourceId id = new ResourceId(requestMethod, requestPath);
        return handlers.containsKey(id)
            ? Optional.of(handlers.get(id))
            : Optional.ofNullable(findVariableHandler(requestPath));
    }

    private RequestHandler findVariableHandler(String requestPath) {
        if (variableHandlers.isEmpty()) {
            return null;
        }

        RequestHandler ret = null;
        String[] requestPathSegments = requestPath.split("/");
        int maxIndexDiff = Integer.MIN_VALUE;
        for (RequestHandler handler : variableHandlers) {
            String varPath = handler.getPath();
            if (isCongruentPaths(varPath.split("/"), requestPathSegments)) {
                int index = StringUtils.indexOfDifference(varPath, requestPath);
                if (index > maxIndexDiff) {
                    maxIndexDiff = index;
                    ret = handler;
                }
            }
        }
        return ret;
    }

    private static boolean isCongruentPaths(String[] varPath, String[] requestPath) {
        for (int i = 0; i < varPath.length; i++) {
            String variablePathSegment = varPath[i];
            if (!isVarPath(variablePathSegment) && !variablePathSegment.equals(requestPath[i])) {
                return false;
            }
        }
        return true;
    }

    private static boolean isVarPath(String path) {
        return path.indexOf('{') >= 0;
    }

    private RequestDispatcher addResourceHandlers(List<RequestHandler> requestHandlers) {
        requestHandlers.forEach(rh -> handlers.put(new ResourceId(rh.getMethod(), rh.getPath()), rh));
        return this;
    }

    private List<RequestHandler> makeResourceHandlers(List<String> filePaths, String resourcesPath) {
        return filePaths.stream().map(filePath -> new RequestHandler(
            filePath,
            r -> Response.ok(Utils.getResource(new InputStreamReader(this.getClass().getResourceAsStream(resourcesPath + filePath)))))
        ).collect(Collectors.toList());
    }

    /**
     * Scans for resource file names. Handles scenarios where resources are
     * either part of a JAR file or within the regular FS.
     */
    private List<String> scanForResources(@NonNull String resourcePath) {
        try {
            List<String> files;
            URI resourceUri = this.getClass().getResource(resourcePath).toURI();
            if (resourceUri.getScheme().equals(JAR)) {
                FileSystem fs = FileSystems.newFileSystem(resourceUri, Collections.emptyMap());
                files = Files.list(Paths.get(resourceUri)).filter(Files::isRegularFile).map(formatFileName(resourcePath)).collect(Collectors.toList());
                fs.close();
            } else {
                files = Files.list(Paths.get(resourceUri)).filter(Files::isRegularFile).map(formatFileName()).collect(Collectors.toList());
            }
            return files;
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private static Function<Path, String> formatFileName(@NonNull String resourcePath) {
        return p -> StringUtils.difference(resourcePath, p.toString());
    }

    private static Function<Path, String> formatFileName() {
        return p -> File.separator + p.toFile().getName();
    }

    /**
     * POJO used as keys to differentiate {@link RequestHandler}s by their path + request method,
     * as {@link RequestHandler} do not contain a reference to a {@link Request}
     */
    @Value
    private static class ResourceId {
        private final Method method;
        private final String path;
    }
}
