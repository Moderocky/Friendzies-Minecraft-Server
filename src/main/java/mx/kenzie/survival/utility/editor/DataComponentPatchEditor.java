package mx.kenzie.survival.utility.editor;

import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.PatchedDataComponentMap;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.Optional;

public class DataComponentPatchEditor implements AutoCloseable {

    private static final Field patchField;

    static {
        try {
            patchField = PatchedDataComponentMap.class.getDeclaredField("patch");
            patchField.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    protected final DataComponentMap map;
    protected PatchedDataComponentMap patched;
    protected Reference2ObjectMap<DataComponentType<?>, Optional<?>> patch;

    public DataComponentPatchEditor(DataComponentMap map) {
        this.map = map;
        if (map instanceof PatchedDataComponentMap alt) {
            this.patched = alt;
            try {
                this.patch = new Reference2ObjectArrayMap<>((Reference2ObjectMap<DataComponentType<?>, Optional<?>>) patchField.get(patched));
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public <Type> void patch(DataComponentType<Type> type, @Nullable Type value) {
        if (patch == null) return;
        try {
            if (value == null) patch.put(type, Optional.empty());
            else patch.put(type, Optional.of(value));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() {
        if (patched == null) return;
        try {
            patchField.set(patched, patch);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }

    }
}
