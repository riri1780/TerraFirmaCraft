/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.compat.jei.transfer;

import java.util.Optional;
import java.util.stream.Collectors;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import mezz.jei.api.recipe.transfer.IRecipeTransferInfo;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import org.jetbrains.annotations.Nullable;

import net.dries007.tfc.common.blockentities.PotBlockEntity;
import net.dries007.tfc.common.container.PotContainer;
import net.dries007.tfc.common.container.TFCContainerTypes;
import net.dries007.tfc.common.recipes.PotRecipe;
import net.dries007.tfc.util.Helpers;

import static net.dries007.tfc.common.blockentities.GrillBlockEntity.*;

/**
 * Custom transfer info for the pot, this is to override {@link #canHandle(PotContainer, PotRecipe)} to respect
 * {@link PotBlockEntity#hasRecipeStarted()} as the slots get locked
 */
public class PotTransferInfo
    extends BaseTransferInfo<PotContainer, PotRecipe>
    implements IRecipeTransferInfo<PotContainer, PotRecipe>
{
    private final IRecipeTransferHandlerHelper transferHelper;

    public PotTransferInfo(IRecipeTransferHandlerHelper transferHelper, RecipeType<PotRecipe> recipeType)
    {
        super(PotContainer.class, Optional.of(TFCContainerTypes.POT.get()), recipeType, 9, SLOT_EXTRA_INPUT_START, 5, 6, 7, SLOT_EXTRA_INPUT_END);
        this.transferHelper = transferHelper;
    }

    @Override
    public boolean canHandle(PotContainer container, PotRecipe recipe)
    {
        if (container.getBlockEntity().hasRecipeStarted())
        {
            return false;
        }

        final var inventory = container.getBlockEntity().getInventory();
        return recipe.getFluidIngredient().test(inventory.getFluidInTank(0));
    }

    @Nullable
    @Override
    public IRecipeTransferError getHandlingError(PotContainer container, PotRecipe recipe)
    {
        final var inventory = container.getBlockEntity().getInventory();
        final SizedFluidIngredient fluidIngredient = recipe.getFluidIngredient();
        final FluidStack fluidInTank = inventory.getFluidInTank(0);

        if (!fluidIngredient.ingredient().test(fluidInTank))
        {
            return transferHelper.createUserErrorWithTooltip(Component.translatable("tfc.jei.transfer.error.pot_wrong_fluid", fluidInTank.getHoverName().getString()));
        }

        if (fluidInTank.getAmount() < fluidIngredient.amount())
        {
            final int missingFluid = fluidIngredient.amount() - fluidInTank.getAmount();
            return transferHelper.createUserErrorWithTooltip(Component.translatable("tfc.jei.transfer.error.pot_not_enough_fluid", missingFluid, fluidInTank.getHoverName().getString()));
        }

        return transferHelper.createUserErrorWithTooltip(Component.translatable("tfc.jei.transfer.error.pot_started"));
    }
}