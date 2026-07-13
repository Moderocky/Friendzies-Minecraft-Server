package mx.kenzie.survival.utility.pack;

import mx.kenzie.argo.Json;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class DataPackMaker {


    protected final File file;
    private transient ZipOutputStream stream;

    public DataPackMaker(File file) {
        this.file = file;
    }

    public URI create(Consumer<DataPackMaker> consumer) {
        if (file.getParentFile() != null) file.getParentFile().mkdirs();
        if (file.exists()) file.delete();
        try {
            this.file.createNewFile();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        try (ZipOutputStream stream = (this.stream) = new ZipOutputStream(new FileOutputStream(file))) {
            consumer.accept(this);
            stream.flush();
        } catch (IOException | URISyntaxException ex) {
            ex.printStackTrace();
        }
        return file.toURI();
    }

    public void add(String path, Object data) throws IOException {
        final ZipEntry entry = new ZipEntry(path);
        stream.putNextEntry(entry);
        new Json(stream).write(data);
        stream.closeEntry();
    }

    @FunctionalInterface
    public interface Consumer<DataPackMaker> {
        void accept(DataPackMaker maker) throws IOException, URISyntaxException;
    }

}
