package com.tiestoettoet.create_train_parts.content.foundation.mixin;

import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllTags;
import com.simibubi.create.api.contraption.ContraptionType;
import com.simibubi.create.content.contraptions.Contraption;
import com.simibubi.create.content.contraptions.actors.trainControls.ControlsBlock;
import com.simibubi.create.content.decoration.slidingDoor.SlidingDoorBlock;
import com.simibubi.create.content.kinetics.steamEngine.PoweredShaftBlockEntity;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBlockEntity;
import com.simibubi.create.content.redstone.contact.RedstoneContactBlock;
import com.simibubi.create.foundation.utility.BlockHelper;
import com.tiestoettoet.create_train_parts.content.decoration.slidingWindow.SlidingWindowBlock;
import com.tiestoettoet.create_train_parts.content.decoration.trainSlide.TrainSlideBlock;
import com.tiestoettoet.create_train_parts.content.decoration.trainStep.TrainStepBlock;
import com.tiestoettoet.create_train_parts.content.trains.bellow.BellowBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.PressurePlateBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import javax.annotation.Nullable;

@Mixin(Contraption.class)
public abstract class ContraptionMixin {

    @Shadow
    public abstract ContraptionType getType();

    @Shadow
    @Nullable
    protected CompoundTag getBlockEntityNBT(Level world, BlockPos pos) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity == null)
            return null;
        CompoundTag nbt = blockEntity.saveWithFullMetadata(world.registryAccess());
        nbt.remove("x");
        nbt.remove("y");
        nbt.remove("z");

        return nbt;
    }

    @Overwrite
    protected Pair<StructureTemplate.StructureBlockInfo, BlockEntity> capture(Level world, BlockPos pos) {
        BlockState blockstate = world.getBlockState(pos);
        if (AllBlocks.REDSTONE_CONTACT.has(blockstate))
            blockstate = blockstate.setValue(RedstoneContactBlock.POWERED, true);
        if (AllBlocks.POWERED_SHAFT.has(blockstate))
            blockstate = BlockHelper.copyProperties(blockstate, AllBlocks.SHAFT.getDefaultState());
        if (blockstate.getBlock() instanceof ControlsBlock
                && AllTags.AllContraptionTypeTags.OPENS_CONTROLS.matches(this.getType()))
            blockstate = blockstate.setValue(ControlsBlock.OPEN, true);
        if (blockstate.hasProperty(SlidingDoorBlock.VISIBLE))
            blockstate = blockstate.setValue(SlidingDoorBlock.VISIBLE, false);
        if (blockstate.getBlock() instanceof ButtonBlock) {
            blockstate = blockstate.setValue(ButtonBlock.POWERED, false);
            world.scheduleTick(pos, blockstate.getBlock(), -1);
        }
        if (blockstate.getBlock() instanceof PressurePlateBlock) {
            blockstate = blockstate.setValue(PressurePlateBlock.POWERED, false);
            world.scheduleTick(pos, blockstate.getBlock(), -1);
        }
        if (blockstate.hasProperty(TrainStepBlock.VISIBLE))
            blockstate = blockstate.setValue(TrainStepBlock.VISIBLE, false);
        if (blockstate.hasProperty(TrainSlideBlock.VISIBLE))
            blockstate = blockstate.setValue(TrainSlideBlock.VISIBLE, false);
        if (blockstate.hasProperty(SlidingWindowBlock.VISIBLE))
            blockstate = blockstate.setValue(SlidingWindowBlock.VISIBLE, false);
        CompoundTag compoundnbt = getBlockEntityNBT(world, pos);
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof PoweredShaftBlockEntity)
            blockEntity = AllBlockEntityTypes.BRACKETED_KINETIC.create(pos, blockstate);
        if (blockEntity instanceof FactoryPanelBlockEntity fpbe)
            fpbe.writeSafe(compoundnbt, world.registryAccess());

        return Pair.of(new StructureTemplate.StructureBlockInfo(pos, blockstate, compoundnbt), blockEntity);
    }

}
