/*
 * Work under Copyright. Licensed under the EUPL.
 * See the project README.md and LICENSE.txt for more information.
 */

package net.dries007.tfc.objects.blocks.agriculture;

import java.util.Random;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import net.dries007.tfc.api.capability.player.CapabilityPlayerData;
import net.dries007.tfc.api.types.ICrop;
import net.dries007.tfc.objects.items.ItemSeedsTFC;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.util.skills.SimpleSkill;
import net.dries007.tfc.util.skills.SkillTier;
import net.dries007.tfc.util.skills.SkillType;

@ParametersAreNonnullByDefault
public abstract class BlockCropSimple extends BlockCropTFC
{
    private final boolean isPickable;

    protected BlockCropSimple(ICrop crop, boolean isPickable)
    {
        super(crop);
        this.isPickable = isPickable;

        setDefaultState(getBlockState().getBaseState().withProperty(getStageProperty(), 0).withProperty(WILD, false));
    }

    @Override
    public void grow(World worldIn, Random rand, BlockPos pos, IBlockState state)
    {
        if (!worldIn.isRemote)
        {
            if (state.getValue(getStageProperty()) < crop.getMaxStage())
            {
                worldIn.setBlockState(pos, state.withProperty(getStageProperty(), state.getValue(getStageProperty()) + 1), 2);
            }
        }
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
        if (isPickable)
        {
            ItemStack foodDrop = crop.getFoodDrop(state.getValue(getStageProperty()));
            if (!foodDrop.isEmpty())
            {
                if (!worldIn.isRemote)
                {
                    worldIn.setBlockState(pos, this.getDefaultState().withProperty(getStageProperty(), state.getValue(getStageProperty()) - 2));
                    Helpers.spawnItemStack(worldIn, pos, crop.getFoodDrop(crop.getMaxStage()));
                }
                return true;
            }
        }
        return false;
    }

    public static BlockCropSimple create(ICrop crop, boolean isPickable)
    {
        switch (crop.getMaxStage() + 1)
        {
            case 5:
                return new BlockCropSimple(crop, isPickable)
                {
                    @Override
                    public PropertyInteger getStageProperty()
                    {
                        return STAGE_5;
                    }
                };
            case 6:
                return new BlockCropSimple(crop, isPickable)
                {
                    @Override
                    public PropertyInteger getStageProperty()
                    {
                        return STAGE_6;
                    }
                };
            case 7:
                return new BlockCropSimple(crop, isPickable)
                {
                    @Override
                    public PropertyInteger getStageProperty()
                    {
                        return STAGE_7;
                    }
                };
            case 8:
                return new BlockCropSimple(crop, isPickable)
                {
                    @Override
                    public PropertyInteger getStageProperty()
                    {
                        return STAGE_8;
                    }
                };
        }
        throw new IllegalStateException("Invalid growthstage property " + (crop.getMaxStage() + 1) + " for crop");
    }
}
