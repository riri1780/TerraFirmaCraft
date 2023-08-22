/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.common.items;

import java.util.function.Consumer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.items.ItemHandlerHelper;

import net.dries007.tfc.common.capabilities.glass.GlassOperation;
import net.dries007.tfc.common.capabilities.glass.GlassWorkData;
import net.dries007.tfc.common.recipes.TFCRecipeTypes;
import net.dries007.tfc.common.recipes.inventory.ItemStackInventory;

public class BlowpipeItem extends Item
{
    public static void stopUsing(LivingEntity entity, ItemStack stack)
    {
        if (entity instanceof Player player)
        {
            final ItemStack otherHand = getOtherHandItem(player);
            final GlassOperation op = GlassOperation.get(otherHand);
            if (op != null && stack.getItem() instanceof BlowpipeItem pipe && pipe.usable)
            {
                GlassWorkData.apply(stack, op);

                final Level level = entity.level();
                level.getRecipeManager().getRecipeFor(TFCRecipeTypes.GLASSWORKING.get(), new ItemStackInventory(stack), level).ifPresent(recipe -> {
                    entity.setItemInHand(player.getUsedItemHand(), TFCItems.BLOWPIPE.get().getDefaultInstance());
                    ItemHandlerHelper.giveItemToPlayer(player, recipe.getResultItem(level.registryAccess()));
                    level.playSound(null, entity.blockPosition(), SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS);
                });
            }
            player.awardStat(Stats.ITEM_USED.get(stack.getItem()));
            player.getCooldowns().addCooldown(TFCItems.BLOWPIPE.get(), 80);
            player.getCooldowns().addCooldown(TFCItems.BLOWPIPE_WITH_GLASS.get(), 80);
        }
    }

    private static ItemStack getOtherHandItem(Player player)
    {
        return player.getItemInHand(player.getUsedItemHand() == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND);
    }

    private static ItemStack getOtherHandItem(Player player, InteractionHand hand)
    {
        return player.getItemInHand(hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND);
    }

    private final boolean usable;

    public BlowpipeItem(Properties properties, boolean usable)
    {
        super(properties);
        this.usable = usable;
    }

    @Override
    public int getUseDuration(ItemStack stack)
    {
        return 80;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack)
    {
        return UseAnim.SPYGLASS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand)
    {
        final ItemStack held = player.getItemInHand(hand);
        final ItemStack otherItem = getOtherHandItem(player, hand);
        final GlassOperation op = GlassOperation.get(otherItem);
        if (op != null && op.hasRequiredTemperature(held))
        {
            player.startUsingItem(hand);
            return InteractionResultHolder.consume(held);
        }
        return InteractionResultHolder.pass(held);
    }


    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int ticksLeft)
    {
        super.onUseTick(level, entity, stack, ticksLeft);
        if (ticksLeft % 20 == 0 && entity instanceof Player player)
        {
            final GlassOperation op = GlassOperation.get(getOtherHandItem(player));
            if (op != null)
            {
                level.playSound(null, entity.blockPosition(), op.getSound(), SoundSource.PLAYERS, 1f, 0.8f + (float) (player.getLookAngle().y / 2f));
            }
        }
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity)
    {
        if (entity instanceof Player player)
        {
            stopUsing(player, stack);
        }
        return super.finishUsingItem(stack, level, entity);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer)
    {
        consumer.accept(new IClientItemExtensions() {
            @Override
            public boolean applyForgeHandTransform(PoseStack poseStack, LocalPlayer player, HumanoidArm arm, ItemStack itemInHand, float partialTick, float equipProcess, float swingProcess)
            {
            if (player.isUsingItem() && player.getUseItemRemainingTicks() > 0)
            {
                poseStack.translate(0f, -0.125f, 0f);
                return true;
            }
            return false;
            }
        });
    }
}
