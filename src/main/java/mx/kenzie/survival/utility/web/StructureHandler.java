package mx.kenzie.survival.utility.web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.bukkit.Bukkit;
import org.bukkit.structure.Structure;

import java.io.IOException;
import java.io.OutputStream;

public class StructureHandler implements HttpHandler {

    private final Structure structure;

    public StructureHandler(Structure structure) {
        this.structure = structure;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().add("Content-Disposition", "attachment; filename=structure.nbt");
        exchange.sendResponseHeaders(200, 0);
        try (OutputStream stream = exchange.getResponseBody()) {
            Bukkit.getStructureManager().saveStructure(stream, structure);
        }
    }

}
