/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.common.entities.livestock;

import java.util.function.Supplier;
import com.mojang.serialization.Dynamic;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import net.dries007.tfc.client.TFCSounds;
import net.dries007.tfc.common.TFCTags;
import net.dries007.tfc.common.entities.Temptable;
import net.dries007.tfc.common.entities.ai.TFCGroundPathNavigation;
import net.dries007.tfc.common.entities.ai.livestock.LivestockAi;
import net.dries007.tfc.common.entities.ai.prey.PreyAi;
import net.dries007.tfc.config.animals.AnimalConfig;
import net.dries007.tfc.util.Helpers;

public abstract class TFCAnimal extends Animal implements TFCAnimalProperties, Temptable
{
    private static final CommonAnimalData ANIMAL_DATA = CommonAnimalData.create(TFCAnimal.class);

    private final Supplier<? extends SoundEvent> ambient;
    private final Supplier<? extends SoundEvent> hurt;
    private final Supplier<? extends SoundEvent> death;
    private final Supplier<? extends SoundEvent> step;
    private final AnimalConfig config;

    public TFCAnimal(EntityType<? extends Animal> type, Level level, TFCSounds.EntityId sounds, AnimalConfig config)
    {
        super(type, level);
        getNavigation().setCanFloat(true);
        this.ambient = sounds.ambient();
        this.hurt = sounds.hurt();
        this.death = sounds.death();
        this.step = sounds.step();
        this.config = config;
    }

    // Next four overrides are the entire package needed to make Brain work

    @Override
    protected Brain.Provider<? extends TFCAnimal> brainProvider()
    {
        return Brain.provider(LivestockAi.MEMORY_TYPES, LivestockAi.SENSOR_TYPES);
    }

    @Override
    protected Brain<?> makeBrain(Dynamic<?> dynamic)
    {
        return LivestockAi.makeBrain(brainProvider().makeBrain(dynamic));
    }

    @Override
    public boolean hurt(DamageSource src, float amount)
    {
        final boolean hurt = super.hurt(src, amount);
        if (this.level().isClientSide) return hurt;
        if (hurt && src.getEntity() instanceof LivingEntity living)
        {
            PreyAi.wasHurtBy(this, living);
        }
        return hurt;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Brain<? extends TFCAnimal> getBrain()
    {
        return (Brain<TFCAnimal>) super.getBrain();
    }

    @Override
    protected void customServerAiStep()
    {
        super.customServerAiStep();
        tickBrain();
    }

    @SuppressWarnings("unchecked")
    public void tickBrain()
    {
        ((Brain<TFCAnimal>) getBrain()).tick((ServerLevel) level(), this);
        LivestockAi.updateActivity(this);
    }

    @Override
    public CommonAnimalData animalData()
    {
        return ANIMAL_DATA;
    }

    @Override
    public AnimalConfig animalConfig()
    {
        return config;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder)
    {
        super.defineSynchedData(builder);
        animalData().define(builder);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag nbt)
    {
        super.addAdditionalSaveData(nbt);
        saveCommonAnimalData(nbt);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag nbt)
    {
        super.readAdditionalSaveData(nbt);
        readCommonAnimalData(nbt);
    }

    @Override
    public boolean isBaby()
    {
        return getAgeType() == Age.CHILD;
    }

    @Override
    public void setAge(int age)
    {
        super.setAge(0); // no-op vanilla aging
    }

    @Override
    public int getAge()
    {
        return isBaby() ? -24000 : 0;
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob other)
    {
        return TFCAnimalProperties.super.getBreedOffspring(level, other);
    }


    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType, @Nullable SpawnGroupData spawnData)
    {
        if (spawnType != MobSpawnType.BREEDING)
        {
            initCommonAnimalData(level, difficulty, spawnType);
        }
        return super.finalizeSpawn(level, difficulty, spawnType, spawnData);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> data)
    {
        super.onSyncedDataUpdated(data);
        if (ANIMAL_DATA.birthTick().equals(data))
        {
            refreshDimensions();
        }
    }

    @Override
    public void tick()
    {
        super.tick();
        if (level().getGameTime() % 20 == 0)
        {
            tickAnimalData();
        }
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand)
    {
        InteractionResult result = TFCAnimalProperties.super.mobInteract(player, hand);
        return result == InteractionResult.PASS ? super.mobInteract(player, hand) : result;
    }

    @Override
    public boolean canMate(Animal otherAnimal)
    {
        if (otherAnimal.getClass() != this.getClass()) return false;
        TFCAnimal other = (TFCAnimal) otherAnimal;
        return this.getGender() != other.getGender() && this.isReadyToMate() && other.isReadyToMate();
    }

    @Override
    public boolean isFood(ItemStack stack)
    {
        return TFCAnimalProperties.super.isFood(stack);
    }

    @Override
    public Component getTypeName()
    {
        return getGenderedTypeName();
    }

    @Override
    protected SoundEvent getAmbientSound()
    {
        return ambient.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource src)
    {
        return hurt.get();
    }

    @Override
    protected SoundEvent getDeathSound()
    {
        return death.get();
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState block)
    {
        this.playSound(step.get(), 0.15F, 1.0F);
    }

    @Override
    public float getWalkTargetValue(BlockPos pos, LevelReader level)
    {
        return Helpers.isBlock(level.getBlockState(pos.below()), TFCTags.Blocks.BUSH_PLANTABLE_ON) ? 10.0F : level.getPathfindingCostFromLightLevels(pos);
    }

    @Override
    public PathNavigation createNavigation(Level level)
    {
        return new TFCGroundPathNavigation(this, level);
    }

    @Override
    public boolean isInWall()
    {
        return !level().isClientSide && super.isInWall();
    }

    @Override
    protected void pushEntities()
    {
        if (!level().isClientSide) super.pushEntities();
    }
}
