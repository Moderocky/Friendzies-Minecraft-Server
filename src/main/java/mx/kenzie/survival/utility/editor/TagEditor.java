package mx.kenzie.survival.utility.editor;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponentMap;

import java.io.Closeable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TagEditor<Type> implements Closeable {

    private static final Field contentsField;

    static {
        try {
            contentsField = HolderSet.Named.class.getDeclaredField("contents");
            contentsField.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    protected final HolderSet.Named<Type> handle;
    protected DataComponentMap.Builder builder;
    protected List<Holder<Type>> contents;


    public TagEditor(Optional<HolderSet.Named<Type>> optional) {
        this(optional.get());
    }


    public TagEditor(HolderSet.Named<Type> handle) {
        this.handle = handle;
        this.accessContents();
    }

    protected void accessContents() {
        if (handle == null) return;
        try {
            this.contents = new ArrayList<>((List<Holder<Type>>) contentsField.get(handle));
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public void add(Holder<Type> holder) {
        if (handle == null) return;
        this.contents.add(holder);
    }

    public void remove(Holder<Type> holder) {
        if (handle == null) return;
        this.contents.remove(holder);
    }

    protected void overwrite() {
        if (handle == null) return;
        try {
            contentsField.set(handle, contents);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() {
        this.overwrite();
    }

    public void clear() {
        this.contents.clear();
    }
}
