/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.common.blocks.wood;

import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import net.dries007.tfc.common.TFCTags;
import net.dries007.tfc.common.blockentities.TickCounterBlockEntity;
import net.dries007.tfc.common.blocks.EntityBlockExtension;
import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.dries007.tfc.common.blocks.IForgeBlockExtension;
import net.dries007.tfc.config.TFCConfig;
import net.dries007.tfc.util.Helpers;

public class TFCSaplingBlock extends SaplingBlock implements IForgeBlockExtension, EntityBlockExtension
{
    private final ExtendedProperties properties;
    private final Supplier<Integer> ticksToGrow;
    private final boolean sand;

    /**
     * @param ticksToGrow The minimum number of player ticks that must elapse before this sapling will grow
     * @param sand If {@code true}, this tree will grow on sand.
     */
    public TFCSaplingBlock(TreeGrower tree, ExtendedProperties properties, Supplier<Integer> ticksToGrow, boolean sand)
    {
        super(tree, properties.properties());
        this.properties = properties;
        this.ticksToGrow = ticksToGrow;
        this.sand = sand;
    }

    @Override
    public ExtendedProperties getExtendedProperties()
    {
        return properties;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random)
    {
        if (level.getMaxLocalRawBrightness(pos.above()) >= 9 && random.nextInt(7) == 0)
        {
            if (!level.isAreaLoaded(pos, 1))
            {
                return;
            }
            if (level.getBlockEntity(pos) instanceof TickCounterBlockEntity counter)
            {
                if (counter.getTicksSinceUpdate() > getTicksToGrow())
                {
                    this.advanceTree(level, pos, state.setValue(STAGE, 1), random);
                }
            }
        }
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack)
    {
        TickCounterBlockEntity.reset(level, pos);
        super.setPlacedBy(level, pos, state, placer, stack);
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos)
    {
        if (sand && Helpers.isBlock(state, BlockTags.SAND))
            return true;
        return super.mayPlaceOn(state, level, pos) || Helpers.isBlock(state.getBlock(), TFCTags.Blocks.BUSH_PLANTABLE_ON);
    }

    /**
     * @return The minimum number of player ticks before this sapling will grow. Affected by per-sapling and global configuration values
     */
    public final long getTicksToGrow()
    {
        return (long) (ticksToGrow.get() * TFCConfig.SERVER.saplingGrowthModifier.get());
    }
}