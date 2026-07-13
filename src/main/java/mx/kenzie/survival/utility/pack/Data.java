package mx.kenzie.survival.utility.pack;

import mx.kenzie.argo.Json;

import java.io.OutputStream;

public class Data {
    public void write(OutputStream stream) {
        new Json(stream).write(this);
    }
}
