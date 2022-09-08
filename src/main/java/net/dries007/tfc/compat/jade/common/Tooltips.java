/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.compat.jade.common;

import java.util.function.BiConsumer;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FireBlock;

import net.dries007.tfc.common.blocks.BloomBlock;
import net.dries007.tfc.common.blocks.TFCTorchBlock;
import net.dries007.tfc.common.blocks.TFCWallTorchBlock;
import net.dries007.tfc.common.blocks.crop.CropBlock;
import net.dries007.tfc.common.blocks.crop.DeadCropBlock;
import net.dries007.tfc.common.blocks.crop.DeadDoubleCropBlock;
import net.dries007.tfc.common.blocks.crop.DecayingBlock;
import net.dries007.tfc.common.blocks.devices.*;
import net.dries007.tfc.common.blocks.plant.fruit.*;
import net.dries007.tfc.common.blocks.soil.FarmlandBlock;
import net.dries007.tfc.common.blocks.wood.TFCLoomBlock;
import net.dries007.tfc.common.blocks.wood.TFCSaplingBlock;
import net.dries007.tfc.common.entities.TFCFishingHook;
import net.dries007.tfc.common.entities.WildAnimal;
import net.dries007.tfc.common.entities.aquatic.TFCSquid;
import net.dries007.tfc.common.entities.livestock.TFCAnimal;
import net.dries007.tfc.common.entities.livestock.horse.TFCChestedHorse;
import net.dries007.tfc.common.entities.livestock.horse.TFCHorse;
import net.dries007.tfc.common.entities.predator.Predator;
import net.dries007.tfc.common.entities.prey.TFCRabbit;

public class Tooltips
{
    public static void register(BiConsumer<BlockEntityTooltip, Class<? extends Block>> registerBlock, BiConsumer<EntityTooltip, Class<? extends Entity>> registerEntity)
    {
        registerBlock.accept(BlockEntityTooltips.BARREL, BarrelBlock.class);
        registerBlock.accept(BlockEntityTooltips.BELLOWS, BellowsBlock.class);
        registerBlock.accept(BlockEntityTooltips.SAPLING, TFCSaplingBlock.class);
        registerBlock.accept(BlockEntityTooltips.BLAST_FURNACE, BlastFurnaceBlock.class);
        registerBlock.accept(BlockEntityTooltips.BLOOMERY, BloomeryBlock.class);
        registerBlock.accept(BlockEntityTooltips.BLOOM, BloomBlock.class);
        registerBlock.accept(BlockEntityTooltips.CHARCOAL_FORGE, CharcoalForgeBlock.class);
        registerBlock.accept(BlockEntityTooltips.COMPOSTER, TFCComposterBlock.class);
        registerBlock.accept(BlockEntityTooltips.CROP, CropBlock.class);
        registerBlock.accept(BlockEntityTooltips.CRUCIBLE, CrucibleBlock.class);
        registerBlock.accept(BlockEntityTooltips.FIREPIT, FirepitBlock.class);
        registerBlock.accept(BlockEntityTooltips.FRUIT_TREE_SAPLING, FruitTreeSaplingBlock.class);
        registerBlock.accept(BlockEntityTooltips.HOE_OVERLAY, FarmlandBlock.class);
        registerBlock.accept(BlockEntityTooltips.HOE_OVERLAY, DeadCropBlock.class);
        registerBlock.accept(BlockEntityTooltips.HOE_OVERLAY, DeadDoubleCropBlock.class);
        registerBlock.accept(BlockEntityTooltips.HOE_OVERLAY, BananaPlantBlock.class);
        registerBlock.accept(BlockEntityTooltips.HOE_OVERLAY, FruitTreeBranchBlock.class);
        registerBlock.accept(BlockEntityTooltips.HOE_OVERLAY, FruitTreeLeavesBlock.class);
        registerBlock.accept(BlockEntityTooltips.HOE_OVERLAY, StationaryBerryBushBlock.class);
        registerBlock.accept(BlockEntityTooltips.HOE_OVERLAY, SpreadingBushBlock.class);
        registerBlock.accept(BlockEntityTooltips.LAMP, LampBlock.class);
        registerBlock.accept(BlockEntityTooltips.NEST_BOX, NestBoxBlock.class);
        registerBlock.accept(BlockEntityTooltips.PIT_KILN_INTERNAL, PitKilnBlock.class);
        registerBlock.accept(BlockEntityTooltips.PIT_KILN_ABOVE, FireBlock.class);
        registerBlock.accept(BlockEntityTooltips.POWDER_KEG, PowderkegBlock.class);
        registerBlock.accept(BlockEntityTooltips.TORCH, TFCTorchBlock.class);
        registerBlock.accept(BlockEntityTooltips.TORCH, TFCWallTorchBlock.class);
        registerBlock.accept(BlockEntityTooltips.JACK_O_LANTERN, JackOLanternBlock.class);
        registerBlock.accept(BlockEntityTooltips.MUD_BRICKS, DryingBricksBlock.class);
        registerBlock.accept(BlockEntityTooltips.DECAYING, DecayingBlock.class);
        registerBlock.accept(BlockEntityTooltips.LOOM, TFCLoomBlock.class);

        registerEntity.accept(EntityTooltips.ANIMAL, TFCAnimal.class);
        registerEntity.accept(EntityTooltips.ANIMAL, TFCHorse.class);
        registerEntity.accept(EntityTooltips.ANIMAL, TFCChestedHorse.class);
        registerEntity.accept(EntityTooltips.ANIMAL, WildAnimal.class);
        registerEntity.accept(EntityTooltips.SQUID, TFCSquid.class);
        registerEntity.accept(EntityTooltips.FISH, WaterAnimal.class);
        registerEntity.accept(EntityTooltips.PREDATOR, Predator.class);
        registerEntity.accept(EntityTooltips.RABBIT, Rabbit.class);
        registerEntity.accept(EntityTooltips.HOOK, TFCFishingHook.class);
    }
}
