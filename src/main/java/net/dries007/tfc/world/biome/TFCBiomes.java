/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.world.biome;

import java.util.*;
import javax.annotation.Nullable;

import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.data.worldgen.biome.OverworldBiomes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.CommonLevelAccessor;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.util.collections.FiniteLinkedHashMap;
import net.dries007.tfc.world.TFCChunkGenerator;
import net.dries007.tfc.world.surface.builder.*;

import static net.dries007.tfc.TerraFirmaCraft.MOD_ID;
import static net.dries007.tfc.world.biome.BiomeBuilder.builder;

public final class TFCBiomes
{
    public static final DeferredRegister<Biome> BIOMES = DeferredRegister.create(ForgeRegistries.BIOMES, MOD_ID);

    public static final BiomeDictionary.Type SALT_WATER_TYPE = BiomeDictionary.Type.getType("SALT_WATER");
    public static final BiomeDictionary.Type FRESH_WATER_TYPE = BiomeDictionary.Type.getType("FRESH_WATER");
    public static final BiomeDictionary.Type VOLCANIC_TYPE = BiomeDictionary.Type.getType("VOLCANIC");

    private static final List<ResourceKey<Biome>> DEFAULT_BIOME_KEYS = new ArrayList<>(); // All possible biomes generated by the TFC BiomeProvider
    private static final List<BiomeVariants> VARIANTS = new ArrayList<>();
    private static final Map<ResourceKey<Biome>, BiomeExtension> EXTENSIONS = new IdentityHashMap<>(); // All extensions, indexed by registry key for quick access

    // Aquatic biomes
    public static final BiomeVariants OCEAN = register("ocean", builder().heightmap(seed -> BiomeNoise.ocean(seed, -26, -12)).surface(OceanSurfaceBuilder.INSTANCE).aquiferHeightOffset(-16).salty().group(BiomeVariants.Group.OCEAN).types(BiomeDictionary.Type.OCEAN)); // Ocean biome found near continents.
    public static final BiomeVariants OCEAN_REEF = register("ocean_reef", builder().heightmap(seed -> BiomeNoise.ocean(seed, -16, -8)).surface(OceanSurfaceBuilder.INSTANCE).aquiferHeightOffset(-16).salty().group(BiomeVariants.Group.OCEAN).types(BiomeDictionary.Type.OCEAN)); // Ocean biome with reefs depending on climate. Could be interpreted as either barrier, fringe, or platform reefs.
    public static final BiomeVariants DEEP_OCEAN = register("deep_ocean", builder().heightmap(seed -> BiomeNoise.ocean(seed, -30, -16)).surface(OceanSurfaceBuilder.INSTANCE).aquiferHeightOffset(-16).group(BiomeVariants.Group.OCEAN).salty().types(BiomeDictionary.Type.OCEAN)); // Deep ocean biome covering most all oceans.
    public static final BiomeVariants DEEP_OCEAN_TRENCH = register("deep_ocean_trench", builder().heightmap(seed -> BiomeNoise.oceanRidge(seed, -30, -16)).surface(OceanSurfaceBuilder.INSTANCE).aquiferHeightOffset(-16).group(BiomeVariants.Group.OCEAN).salty().types(BiomeDictionary.Type.OCEAN)); // Deeper ocean with sharp relief carving to create very deep trenches

    // Low biomes
    public static final BiomeVariants PLAINS = register("plains", builder().heightmap(seed -> BiomeNoise.hills(seed, 4, 10)).surface(NormalSurfaceBuilder.INSTANCE).spawnable().types(BiomeDictionary.Type.PLAINS)); // Very flat, slightly above sea level.
    public static final BiomeVariants HILLS = register("hills", builder().heightmap(seed -> BiomeNoise.hills(seed, -5, 16)).surface(NormalSurfaceBuilder.INSTANCE).spawnable().types(BiomeDictionary.Type.HILLS)); // Small hills, slightly above sea level.
    public static final BiomeVariants LOWLANDS = register("lowlands", builder().heightmap(BiomeNoise::lowlands).surface(NormalSurfaceBuilder.INSTANCE).aquiferHeightOffset(-16).spawnable().types(BiomeDictionary.Type.SWAMP)); // Flat, swamp-like, lots of shallow pools below sea level.
    public static final BiomeVariants LOW_CANYONS = register("low_canyons", builder().heightmap(seed -> BiomeNoise.canyons(seed, -8, 21)).surface(NormalSurfaceBuilder.INSTANCE).aquiferHeightOffset(-16).spawnable().types(BiomeDictionary.Type.HILLS, BiomeDictionary.Type.SWAMP)); // Sharp, small hills, with lots of water / snaking winding rivers.

    // Mid biomes
    public static final BiomeVariants ROLLING_HILLS = register("rolling_hills", builder().heightmap(seed -> BiomeNoise.hills(seed, -5, 28)).surface(NormalSurfaceBuilder.INSTANCE).spawnable().types(BiomeDictionary.Type.HILLS)); // Higher hills, above sea level. Some larger / steeper hills.
    public static final BiomeVariants BADLANDS = register("badlands", builder().heightmap(BiomeNoise::badlands).surface(BadlandsSurfaceBuilder.NORMAL).spawnable().types(BiomeDictionary.Type.HILLS, BiomeDictionary.Type.MESA)); // Very high flat area with steep relief carving, similar to vanilla mesas.
    public static final BiomeVariants INVERTED_BADLANDS = register("inverted_badlands", builder().heightmap(BiomeNoise::bryceCanyon).surface(BadlandsSurfaceBuilder.INVERTED).spawnable().types(BiomeDictionary.Type.HILLS, BiomeDictionary.Type.MESA)); // Inverted badlands: hills with additive ridges, similar to vanilla bryce canyon mesas.
    public static final BiomeVariants PLATEAU = register("plateau", builder().heightmap(seed -> BiomeNoise.hills(seed, 20, 30)).surface(MountainSurfaceBuilder.INSTANCE).spawnable().types(BiomeDictionary.Type.PLATEAU)); // Very high area, very flat top.
    public static final BiomeVariants CANYONS = register("canyons", builder().heightmap(seed -> BiomeNoise.canyons(seed, -2, 40)).surface(NormalSurfaceBuilder.INSTANCE).volcanoes(6, 14, 30, 28).spawnable().types(BiomeDictionary.Type.HILLS)); // Medium height with snake like ridges, minor volcanic activity

    // High biomes
    public static final BiomeVariants MOUNTAINS = register("mountains", builder().heightmap(seed -> BiomeNoise.mountains(seed, 10, 70)).surface(MountainSurfaceBuilder.INSTANCE).spawnable().types(BiomeDictionary.Type.MOUNTAIN)); // High, picturesque mountains. Pointed peaks, low valleys well above sea level.
    public static final BiomeVariants OLD_MOUNTAINS = register("old_mountains", builder().heightmap(seed -> BiomeNoise.mountains(seed, 16, 40)).surface(MountainSurfaceBuilder.INSTANCE).spawnable().types(BiomeDictionary.Type.PLATEAU, BiomeDictionary.Type.MOUNTAIN)); // Rounded top mountains, very large hills.
    public static final BiomeVariants OCEANIC_MOUNTAINS = register("oceanic_mountains", builder().heightmap(seed -> BiomeNoise.mountains(seed, -16, 60)).surface(MountainSurfaceBuilder.INSTANCE).aquiferHeightOffset(-8).salty().spawnable().types(BiomeDictionary.Type.OCEAN, BiomeDictionary.Type.MOUNTAIN)); // Mountains with high areas, and low, below sea level valleys. Water is salt water here.
    public static final BiomeVariants VOLCANIC_MOUNTAINS = register("volcanic_mountains", builder().heightmap(seed -> BiomeNoise.mountains(seed, 10, 60)).surface(MountainSurfaceBuilder.INSTANCE).volcanoes(5, 25, 50, 40).types(BiomeDictionary.Type.MOUNTAIN)); // Volcanic mountains - slightly smaller, but with plentiful tall volcanoes
    public static final BiomeVariants VOLCANIC_OCEANIC_MOUNTAINS = register("volcanic_oceanic_mountains", builder().heightmap(seed -> BiomeNoise.mountains(seed, -24, 50)).surface(MountainSurfaceBuilder.INSTANCE).aquiferHeightOffset(-8).salty().volcanoes(1, -12, 50, 20).types(BiomeDictionary.Type.OCEAN, BiomeDictionary.Type.MOUNTAIN)); // Volcanic oceanic islands. Slightly smaller and lower but with very plentiful volcanoes

    // Shores
    public static final BiomeVariants SHORE = register("shore", builder().heightmap(BiomeNoise::shore).surface(ShoreSurfaceBuilder.INSTANCE).aquiferHeightOffset(-16).group(BiomeVariants.Group.OCEAN).salty().types(BiomeDictionary.Type.BEACH)); // Standard shore / beach. Material will vary based on location

    // Water
    public static final BiomeVariants LAKE = register("lake", builder().heightmap(BiomeNoise::lake).surface(NormalSurfaceBuilder.INSTANCE).aquiferHeightOffset(-16).group(BiomeVariants.Group.LAKE).types(BiomeDictionary.Type.RIVER));
    public static final BiomeVariants RIVER = register("river", builder().noise(BiomeNoise::riverSampler).surface(NormalSurfaceBuilder.INSTANCE).aquiferHeight(h -> TFCChunkGenerator.SEA_LEVEL_Y - 16).group(BiomeVariants.Group.RIVER).types(BiomeDictionary.Type.RIVER));

    // Mountain Fresh water / carving biomes
    public static final BiomeVariants MOUNTAIN_RIVER = register("mountain_river", builder().heightmap(seed -> BiomeNoise.mountains(seed, 10, 70)).surface(MountainSurfaceBuilder.INSTANCE).carving(BiomeNoise::undergroundRivers).group(BiomeVariants.Group.RIVER).types(BiomeDictionary.Type.MOUNTAIN, BiomeDictionary.Type.RIVER));
    public static final BiomeVariants OLD_MOUNTAIN_RIVER = register("old_mountain_river", builder().heightmap(seed -> BiomeNoise.mountains(seed, 16, 40)).surface(MountainSurfaceBuilder.INSTANCE).carving(BiomeNoise::undergroundRivers).group(BiomeVariants.Group.RIVER).types(BiomeDictionary.Type.PLATEAU, BiomeDictionary.Type.MOUNTAIN, BiomeDictionary.Type.RIVER));
    public static final BiomeVariants OCEANIC_MOUNTAIN_RIVER = register("oceanic_mountain_river", builder().heightmap(seed -> BiomeNoise.mountains(seed, -16, 60)).surface(MountainSurfaceBuilder.INSTANCE).carving(BiomeNoise::undergroundRivers).salty().group(BiomeVariants.Group.RIVER).types(BiomeDictionary.Type.MOUNTAIN, BiomeDictionary.Type.OCEAN, BiomeDictionary.Type.RIVER));
    public static final BiomeVariants VOLCANIC_MOUNTAIN_RIVER = register("volcanic_mountain_river", builder().heightmap(seed -> BiomeNoise.mountains(seed, 10, 60)).surface(MountainSurfaceBuilder.INSTANCE).volcanoes(5, 25, 50, 40).carving(BiomeNoise::undergroundRivers).group(BiomeVariants.Group.RIVER).types(BiomeDictionary.Type.MOUNTAIN, BiomeDictionary.Type.RIVER));
    public static final BiomeVariants VOLCANIC_OCEANIC_MOUNTAIN_RIVER = register("volcanic_oceanic_mountain_river", builder().heightmap(seed -> BiomeNoise.mountains(seed, -24, 50)).surface(MountainSurfaceBuilder.INSTANCE).volcanoes(1, -12, 50, 20).carving(BiomeNoise::undergroundRivers).salty().group(BiomeVariants.Group.RIVER).types(BiomeDictionary.Type.MOUNTAIN, BiomeDictionary.Type.OCEAN, BiomeDictionary.Type.RIVER));

    public static final BiomeVariants MOUNTAIN_LAKE = register("mountain_lake", builder().heightmap(seed -> BiomeNoise.mountains(seed, 10, 70)).surface(MountainSurfaceBuilder.INSTANCE).carving(BiomeNoise::undergroundLakes).group(BiomeVariants.Group.LAKE).types(BiomeDictionary.Type.MOUNTAIN, BiomeDictionary.Type.RIVER));
    public static final BiomeVariants OLD_MOUNTAIN_LAKE = register("old_mountain_lake", builder().heightmap(seed -> BiomeNoise.mountains(seed, -16, 60)).surface(MountainSurfaceBuilder.INSTANCE).carving(BiomeNoise::undergroundLakes).group(BiomeVariants.Group.LAKE).types(BiomeDictionary.Type.PLATEAU, BiomeDictionary.Type.MOUNTAIN, BiomeDictionary.Type.RIVER));
    public static final BiomeVariants OCEANIC_MOUNTAIN_LAKE = register("oceanic_mountain_lake", builder().heightmap(seed -> BiomeNoise.mountains(seed, -16, 60)).surface(MountainSurfaceBuilder.INSTANCE).carving(BiomeNoise::undergroundLakes).salty().group(BiomeVariants.Group.LAKE).types(BiomeDictionary.Type.MOUNTAIN, BiomeDictionary.Type.OCEAN, BiomeDictionary.Type.RIVER));
    public static final BiomeVariants VOLCANIC_MOUNTAIN_LAKE = register("volcanic_mountain_lake", builder().heightmap(seed -> BiomeNoise.mountains(seed, 10, 60)).surface(MountainSurfaceBuilder.INSTANCE).volcanoes(5, 25, 50, 40).carving(BiomeNoise::undergroundLakes).group(BiomeVariants.Group.LAKE).types(BiomeDictionary.Type.MOUNTAIN, BiomeDictionary.Type.RIVER));
    public static final BiomeVariants VOLCANIC_OCEANIC_MOUNTAIN_LAKE = register("volcanic_oceanic_mountain_lake", builder().heightmap(seed -> BiomeNoise.mountains(seed, -24, 50)).surface(MountainSurfaceBuilder.INSTANCE).volcanoes(1, -12, 50, 20).carving(BiomeNoise::undergroundLakes).salty().group(BiomeVariants.Group.LAKE).types(BiomeDictionary.Type.MOUNTAIN, BiomeDictionary.Type.OCEAN, BiomeDictionary.Type.RIVER));

    public static final BiomeVariants PLATEAU_LAKE = register("plateau_lake", builder().heightmap(seed -> BiomeNoise.hills(seed, 20, 30)).surface(MountainSurfaceBuilder.INSTANCE).carving(BiomeNoise::undergroundLakes).group(BiomeVariants.Group.LAKE).types(BiomeDictionary.Type.PLATEAU, BiomeDictionary.Type.RIVER));


    private static final Map<Biome, BiomeExtension> CACHED_EXTENSIONS = new FiniteLinkedHashMap<>(16); // Faster route from biome -> extension

    public static BiomeExtension getExtensionOrThrow(LevelAccessor world, Biome biome)
    {
        BiomeExtension extension = getExtension(world, biome);
        if (extension == null)
        {
            throw new IllegalStateException("No extension found for biome: " + biome + " (" + biome.getRegistryName() + ")");
        }
        return extension;
    }

    @Nullable
    public static BiomeExtension getExtension(CommonLevelAccessor world, Biome biome)
    {
        return getExtension(world.registryAccess(), biome);
    }

    @Nullable
    public static BiomeExtension getExtension(RegistryAccess registries, Biome biome)
    {
        // First query the cache, it is fast
        BiomeExtension extension = CACHED_EXTENSIONS.get(biome);
        if (extension == BiomeExtension.EMPTY)
        {
            // No match for this biome - this exists as a cache miss marker
            return null;
        }
        else if (extension != null)
        {
            // Cache hit
            return extension;
        }
        else
        {
            // This lookup here is the comparatively slow operation - avoid it if possible as this is happening a lot.
            Registry<Biome> registry = registries.registryOrThrow(Registry.BIOME_REGISTRY);
            BiomeExtension lookupExtension = registry.getResourceKey(biome).map(EXTENSIONS::get).orElse(null);
            if (lookupExtension != null)
            {
                // Save the extension and biome to the cache
                CACHED_EXTENSIONS.put(biome, lookupExtension);
                return lookupExtension;
            }
            else
            {
                // Mark this as a cache miss with the empty extension
                CACHED_EXTENSIONS.put(biome, BiomeExtension.EMPTY);
                return null;
            }
        }
    }

    public static List<ResourceKey<Biome>> getAllKeys()
    {
        return DEFAULT_BIOME_KEYS;
    }

    public static List<BiomeVariants> getVariants()
    {
        return VARIANTS;
    }

    private static BiomeVariants register(String baseName, BiomeBuilder builder)
    {
        BiomeVariants variants = builder.build();
        VARIANTS.add(variants);
        for (BiomeTemperature temp : BiomeTemperature.values())
        {
            for (BiomeRainfall rain : BiomeRainfall.values())
            {
                final String name = (baseName + "_" + temp.name() + "_" + rain.name()).toLowerCase(Locale.ROOT);
                final ResourceLocation id = Helpers.identifier(name);
                final ResourceKey<Biome> key = ResourceKey.create(Registry.BIOME_REGISTRY, id);
                final BiomeExtension extension = variants.createBiomeExtension(key, temp, rain);

                EXTENSIONS.put(key, extension);
                DEFAULT_BIOME_KEYS.add(key);
                TFCBiomes.BIOMES.register(name, OverworldBiomes::theVoid);

                registerDefaultBiomeDictionaryTypes(key, temp, rain);
                builder.registerTypes(key);
                if (variants.isVolcanic())
                {
                    BiomeDictionary.addTypes(key, VOLCANIC_TYPE);
                }
                BiomeDictionary.addTypes(key, variants.isSalty() ? SALT_WATER_TYPE : FRESH_WATER_TYPE);
            }
        }
        return variants;
    }

    private static void registerDefaultBiomeDictionaryTypes(ResourceKey<Biome> key, BiomeTemperature temp, BiomeRainfall rain)
    {
        BiomeDictionary.addTypes(key, BiomeDictionary.Type.OVERWORLD);
        if (temp == BiomeTemperature.COLD)
        {
            BiomeDictionary.addTypes(key, BiomeDictionary.Type.COLD);
        }
        else if (temp == BiomeTemperature.WARM)
        {
            BiomeDictionary.addTypes(key, BiomeDictionary.Type.HOT);
        }

        if (rain == BiomeRainfall.WET)
        {
            BiomeDictionary.addTypes(key, BiomeDictionary.Type.WET);
        }
        else if (rain == BiomeRainfall.DRY)
        {
            BiomeDictionary.addTypes(key, BiomeDictionary.Type.DRY);
        }

        if (rain == BiomeRainfall.WET && temp == BiomeTemperature.WARM)
        {
            BiomeDictionary.addTypes(key, BiomeDictionary.Type.JUNGLE);
        }
        else if (rain == BiomeRainfall.DRY && temp == BiomeTemperature.WARM)
        {
            BiomeDictionary.addTypes(key, BiomeDictionary.Type.SAVANNA);
        }
        else if (temp == BiomeTemperature.COLD && rain != BiomeRainfall.DRY)
        {
            BiomeDictionary.addTypes(key, BiomeDictionary.Type.CONIFEROUS);
        }
    }
}