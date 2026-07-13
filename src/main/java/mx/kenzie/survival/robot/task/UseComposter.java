package mx.kenzie.survival.robot.task;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.animal.golem.CopperGolem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;
import java.util.function.Predicate;

class UseComposter extends UseWorkstation {
    private static final Predicate<ItemLike> IS_COMPOSTABLE = ComposterBlock.COMPOSTABLES::containsKey;

    public UseComposter() {
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, CopperGolem body) {
        if (!body.hasItemInSlot(EquipmentSlot.MAINHAND)) return false;
        ItemStack item = body.getItemBySlot(EquipmentSlot.MAINHAND);
        if (item.isEmpty() || !IS_COMPOSTABLE.test(item.getItem())) return false;
        return super.checkExtraStartConditions(level, body);
    }

    protected void useWorkstation(ServerLevel level, CopperGolem body) {
        Optional<GlobalPos> jobSiteMemory = body.getBrain().getMemory(MemoryModuleType.JOB_SITE);
        if (jobSiteMemory.isPresent()) {
            GlobalPos jobSitePos = jobSiteMemory.get();
            BlockState blockState = level.getBlockState(jobSitePos.pos());
            if (blockState.is(Blocks.COMPOSTER)) {
                this.compostItems(level, body, jobSitePos, blockState);
            }
        }

    }

    private void compostItems(ServerLevel level, CopperGolem body, GlobalPos jobSitePos, BlockState blockState) {
        BlockPos pos = jobSitePos.pos();
        if (blockState.getValue(ComposterBlock.LEVEL) == 8) {
            blockState = ComposterBlock.extractProduce(body, blockState, level, pos);
        }
        ItemStack item = body.getItemBySlot(EquipmentSlot.MAINHAND);
        if (item.isEmpty() || !IS_COMPOSTABLE.test(item.getItem())) return;
        BlockState tempState = blockState;
        tempState = ComposterBlock.insertItem(body, tempState, level, item, pos);
        tempState.getValue(ComposterBlock.LEVEL);
        this.spawnComposterFillEffects(level, blockState, pos, tempState);
    }

    private void spawnComposterFillEffects(ServerLevel level, BlockState blockState, BlockPos pos, BlockState newState) {
        level.levelEvent(1500, pos, newState != blockState ? 1 : 0);
    }
}