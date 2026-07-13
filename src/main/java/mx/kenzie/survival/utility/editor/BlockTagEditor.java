package mx.kenzie.survival.utility.editor;

import net.minecraft.world.level.block.Block;
import org.bukkit.Tag;
import org.bukkit.craftbukkit.tag.CraftBlockTag;

public class BlockTagEditor extends TagEditor<Block> {

    protected final Tag<?> tag;
    protected final CraftBlockTag craft;


    public BlockTagEditor(Tag<?> tag) {
        super(((CraftBlockTag) tag).getHandle());
        this.tag = tag;
        this.craft = ((CraftBlockTag) tag);
    }

    public void add(Block block) {
        this.add(block.builtInRegistryHolder());
    }

    public void remove(Block block) {
        this.remove(block.builtInRegistryHolder());
    }
}
