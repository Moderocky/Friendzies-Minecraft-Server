package mx.kenzie.survival.utility.pack;

import com.google.common.hash.Hashing;
import mx.kenzie.argo.Json;
import mx.kenzie.survival.Survival;
import mx.kenzie.survival.potion.Ingredient;
import org.bukkit.Bukkit;

import java.io.*;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ResourcePackMaker {


    protected final File file;

    public ResourcePackMaker(File file) {
        this.file = file;
    }

    public static void generateResourcePack() {
        final ResourcePackMaker maker = new ResourcePackMaker(new File("resource-pack.zip"));
        maker.create();
    }

    public void create() {
        if (file.getParentFile() != null) file.getParentFile().mkdirs();
        if (file.exists()) file.delete();
        try {
            this.file.createNewFile();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        try (ZipOutputStream stream = new ZipOutputStream(new FileOutputStream(file))) {
            this.transfer(stream, "pack.mcmeta");
            this.transfer(stream, "pack.png");
            Ingredient.makeFiles(this, stream);
            this.transferPack(stream);
            stream.flush();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        if (Bukkit.getServer() == null) System.out.println("Pack built.");
        try {
            Resources.hash = this.createSha1(file);
            if (Bukkit.getServer() == null) System.out.println("Hash: " + Resources.hash);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void create(Object data, String path, ZipOutputStream stream) throws IOException {
        final ZipEntry entry = new ZipEntry(path);
        stream.putNextEntry(entry);
        new Json(stream).write(data);
        stream.closeEntry();
    }

    private void transferPack(ZipOutputStream stream) throws IOException {
        final CodeSource src = Survival.class.getProtectionDomain().getCodeSource();
        final List<String> names = new ArrayList<>();
        try (JarFile jarFile = new JarFile(src.getLocation().getFile())) {
            final Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                final JarEntry entry = entries.nextElement();
                final String name = entry.getName();
                final int index = name.indexOf("assets/server/");
                if (index > -1) names.add(name.substring(index));
            }
        }
        for (final String name : names) this.transfer(stream, name);
    }

    public void transfer(ZipOutputStream stream, String path) throws IOException {
        if (path.endsWith("/")) return;
        try (InputStream input = Survival.class.getClassLoader().getResourceAsStream("pack/" + path)) {
            assert input != null;
            final ZipEntry entry = new ZipEntry(path);
            stream.putNextEntry(entry);
            input.transferTo(stream);
            stream.closeEntry();
        }
    }

    public String createSha1(File file) throws Exception {
        try (InputStream stream = new FileInputStream(file)) {
            return Hashing.sha1().hashBytes(stream.readAllBytes()).toString();
        }
    }

}
