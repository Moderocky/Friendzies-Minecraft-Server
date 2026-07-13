package mx.kenzie.survival.utility.web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.*;

public class FileDownloadHandler implements HttpHandler {

    private final File file;

    public FileDownloadHandler(File file) {
        this.file = file;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().add("Content-Disposition", "attachment; filename=" + file.getName());
        exchange.sendResponseHeaders(200, file.length());
        try (OutputStream stream = exchange.getResponseBody();
             InputStream input = new FileInputStream(file)) {
            input.transferTo(stream);
        }
    }

}
