package mx.kenzie.survival.utility.sanitiser;

import net.minecraft.nbt.*;
import org.jetbrains.annotations.NotNull;

public abstract class CompoundModifier implements TagVisitor {
    public abstract void modify(final CompoundTag tag);

    @Override
    public void visitList(final ListTag listTag) {
        for (final Tag tag : listTag) {
            tag.accept(this);
        }
    }

    @Override
    public void visitCompound(final @NotNull CompoundTag compoundTag) {
        modify(compoundTag);

        for (final String key : compoundTag.keySet()) {
            final Tag tag = compoundTag.get(key);
            if (tag instanceof final CompoundTag inner) {
                inner.accept(this);
            } else if (tag instanceof final ListTag list) {
                for (final Tag innerTag : list) {
                    innerTag.accept(this);
                }
            }
        }
    }

    @Override
    public void visitString(final @NotNull StringTag stringTag) {
    }

    @Override
    public void visitByte(final @NotNull ByteTag byteTag) {
    }

    @Override
    public void visitShort(final @NotNull ShortTag shortTag) {
    }

    @Override
    public void visitInt(final @NotNull IntTag intTag) {
    }

    @Override
    public void visitLong(final @NotNull LongTag longTag) {
    }

    @Override
    public void visitFloat(final @NotNull FloatTag floatTag) {
    }

    @Override
    public void visitDouble(final @NotNull DoubleTag doubleTag) {
    }

    @Override
    public void visitByteArray(final @NotNull ByteArrayTag byteArrayTag) {
    }

    @Override
    public void visitIntArray(final @NotNull IntArrayTag intArrayTag) {
    }

    @Override
    public void visitLongArray(final @NotNull LongArrayTag longArrayTag) {
    }

    @Override
    public void visitEnd(@NotNull final EndTag endTag) {
    }
}
