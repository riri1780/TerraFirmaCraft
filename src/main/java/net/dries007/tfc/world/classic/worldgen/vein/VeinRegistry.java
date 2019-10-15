/*
 * Work under Copyright. Licensed under the EUPL.
 * See the project README.md and LICENSE.txt for more information.
 */

package net.dries007.tfc.world.classic.worldgen.vein;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.io.FileUtils;

import net.dries007.tfc.TerraFirmaCraft;
import net.dries007.tfc.util.collections.WeightedCollection;

import static net.dries007.tfc.Constants.GSON;
import static net.dries007.tfc.api.util.TFCConstants.MOD_ID;

public enum VeinRegistry
{
    INSTANCE;

    private static final String DEFAULT_ORE_SPAWN_LOCATION = "assets/tfc/config/ore_spawn_data.json";

    private final WeightedCollection<VeinType> weightedVeinTypes = new WeightedCollection<>();
    private final Map<String, VeinType> veinTypeRegistry = new HashMap<>();
    private List<File> oreFiles;

    @Nonnull
    public WeightedCollection<VeinType> getVeins()
    {
        return weightedVeinTypes;
    }

    @Nonnull
    public Set<String> keySet()
    {
        return veinTypeRegistry.keySet();
    }

    @Nullable
    public VeinType getVein(String name)
    {
        return veinTypeRegistry.get(name);
    }

    public void preInit(File dir)
    {
        File tfcDir = new File(dir, MOD_ID);
        if (!tfcDir.exists() && !tfcDir.mkdir())
        {
            throw new Error("Problem creating TFC extra config directory.");
        }
        try
        {
            oreFiles = Files.list(Paths.get(tfcDir.toURI()))
                .filter(Files::isRegularFile)
                .map(Path::toFile)
                .collect(Collectors.toList());
        }
        catch (IOException e)
        {
            throw new Error("Problem reading ore vein config files.", e);
        }
        if (oreFiles.isEmpty())
        {
            // Create default
            File defaultFile = new File(tfcDir, "ore_spawn_data.json");
            try
            {
                if (defaultFile.createNewFile())
                {
                    FileUtils.copyInputStreamToFile(Objects.requireNonNull(VeinRegistry.class.getClassLoader().getResourceAsStream(DEFAULT_ORE_SPAWN_LOCATION)), defaultFile);
                }
                oreFiles.add(defaultFile);
            }
            catch (IOException e)
            {
                throw new Error("Problem creating default ore vein config file.", e);
            }
        }
    }

    public void postInit()
    {
        List<String> worldGenData = new ArrayList<>();
        oreFiles.forEach(file -> {
            try
            {
                worldGenData.add(FileUtils.readFileToString(file, Charset.defaultCharset()));
            }
            catch (IOException e)
            {
                throw new Error("Error reading " + file, e);
            }
        });

        if (!worldGenData.isEmpty())
        {
            try
            {
                weightedVeinTypes.clear();
                veinTypeRegistry.clear();
                worldGenData.forEach(data -> {
                    Map<String, VeinType> values = GSON.fromJson(data, new TypeToken<Map<String, VeinType>>() {}.getType());
                    values.forEach((name, veinType) -> {
                        veinType.setRegistryName(name);
                        veinTypeRegistry.put(name, veinType);
                        weightedVeinTypes.add(veinType.weight, veinType);
                    });
                });
            }
            catch (JsonParseException e)
            {
                TerraFirmaCraft.getLog().warn("There was a serious issue parsing the ore generation files!! TFC will not generate any ores!", e);
            }
        }
        if (veinTypeRegistry.isEmpty())
        {
            TerraFirmaCraft.getLog().warn("The ore vein registry is empty!! TFC will not generate any ores!");
        }
    }
}
