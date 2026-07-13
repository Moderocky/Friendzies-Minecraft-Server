package mx.kenzie.survival.utility.web;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import mx.kenzie.survival.Survival;
import org.bukkit.Bukkit;
import org.bukkit.structure.Structure;

import java.io.*;
import java.net.InetSocketAddress;
import java.util.concurrent.ThreadLocalRandom;

public class WebServer {
    public final int port;
    private HttpServer server;

    public WebServer(int port) {
        this.port = port;
    }

    public void accept() {
        try {
            this.server = HttpServer.create(new InetSocketAddress(port), 0);
            this.server.createContext("/pack", new ResourcePackHandler());
            this.server.setExecutor(null);
            this.server.start();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    public String download(Structure structure) {
        final String random = Integer.toHexString(ThreadLocalRandom.current().nextInt());
        final HttpContext context = this.server.createContext("/" + random, new StructureHandler(structure));
        Bukkit.getScheduler().scheduleSyncDelayedTask(Survival.plugin, () -> this.server.removeContext(context), 6000);
        return random;
    }

    public String download(File file) {
        final String random = Integer.toHexString(file.hashCode());
        try {
            final HttpContext context = this.server.createContext("/" + random, new FileDownloadHandler(file));
            Bukkit.getScheduler()
                    .scheduleSyncDelayedTask(Survival.plugin, () -> this.server.removeContext(context), 6000);
        } catch (IllegalArgumentException ignored) { // already asked for this file
        }
        return random;
    }

    public void close() {
        if (server != null) server.stop(1);
    }

    public static class ResourcePackHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            final File file = new File("resource-pack.zip");
            exchange.sendResponseHeaders(200, file.length());
            try (OutputStream stream = exchange.getResponseBody()) {
                try (InputStream input = new FileInputStream(file)) {
                    input.transferTo(stream);
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

    }

}
