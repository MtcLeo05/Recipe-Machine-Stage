package net.sdm.recipemachinestage.utils;

import dev.latvian.mods.kubejs.integration.forge.gamestages.GameStagesWrapper;
import net.darkhax.gamestages.GameStageHelper;
import net.minecraft.nbt.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fml.ModList;
import net.sdm.recipemachinestage.RecipeMachineStage;
import org.apache.commons.io.FilenameUtils;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.nio.file.Path;
import java.util.*;

public class PlayerHelper {
    public static final String folderName = "RecipeMachineStages";
    public static final Map<UUID, RMSStagePlayerData> PLAYER_DATA = new HashMap<>();

    @Nullable
    public static RMSStagePlayerData getPlayerByGameProfile(MinecraftServer server, UUID id){
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if(Objects.equals(player.getGameProfile().getId(), id)) {
                RMSStagePlayerData stagePlayerData = new RMSStagePlayerData();

                if(ModList.get().isLoaded("gamestages")) {
                    stagePlayerData.addStage(GameStageHelper.getPlayerData(player).getStages());
                }
                if(ModList.get().isLoaded("kubejs")) {
                    stagePlayerData.addStage(GameStagesWrapper.get(player).getAll());
                }
                return stagePlayerData;
            }
        }

        return PLAYER_DATA.getOrDefault(id, null);
    }



    public static class RMSStagePlayerData implements INBTSerializable<CompoundTag> {
        public List<String> stages = new ArrayList<>();

        public void addStage(Collection<String> stages) {
            this.stages.addAll(stages);
        }

        public List<String> getStages() {
            return stages;
        }

        public boolean hasStage(String stage) {
            return getStages().contains(stage);
        }

        @Override
        public CompoundTag serializeNBT() {
            CompoundTag nbt = new CompoundTag();
            ListTag listTag = new ListTag();
            for (String stage : stages) {
                listTag.add(StringTag.valueOf(stage));
            }
            nbt.put("player_stages", listTag);
            return nbt;
        }

        @Override
        public void deserializeNBT(CompoundTag nbt) {
            ListTag player_stages = (ListTag) nbt.get("player_stages");
            stages.clear();
            for (Tag playerStage : player_stages) {
                stages.add(playerStage.getAsString());
            }
        }
    }

    public static void addPlayer(ServerPlayer player) {
        RMSStagePlayerData data = new RMSStagePlayerData();

        if(ModList.get().isLoaded("gamestages")) {
            data.addStage(GameStageHelper.getPlayerData(player).getStages());
        }
        if(ModList.get().isLoaded("kubejs")) {
            data.addStage(GameStagesWrapper.get(player).getAll());
        }
        PLAYER_DATA.put(player.getGameProfile().getId(), data);
        savePlayer(player.getGameProfile().getId(), player.server);
    }

    public static void onPlayerLeave(ServerPlayer player) {
        addPlayer(player);
    }

    public static void loadData(MinecraftServer server) {
        Path path = server.getWorldPath(LevelResource.ROOT).resolve(folderName);
        if(!path.toFile().exists()) {
            path.toFile().mkdir();
            return;
        }
        PLAYER_DATA.clear();

        for (File file : path.toFile().listFiles()) {
            try {
                UUID uuid = UUID.fromString(FilenameUtils.removeExtension(file.getName()));
                CompoundTag nbt = NbtIo.read(file);
                if (nbt == null) continue;
                RMSStagePlayerData data = new RMSStagePlayerData();
                data.deserializeNBT(nbt);
                PLAYER_DATA.put(uuid, data);
            } catch (Exception e){
                RecipeMachineStage.LOGGER.error("net.sdm.recipemachinestage.utils.PlayerHelper$loadData");
                RecipeMachineStage.LOGGER.error(e.getMessage());
            }
        }
    }

    public static void saveData(MinecraftServer server) {
        Path path = server.getWorldPath(LevelResource.ROOT).resolve(folderName);
        if(!path.toFile().exists()) {
            path.toFile().mkdir();
        }

        for (UUID uuid : PLAYER_DATA.keySet()) {
            savePlayer(uuid, path);
        }
    }

    public static void savePlayer(UUID player, MinecraftServer server) {
        Path path = server.getWorldPath(LevelResource.ROOT).resolve(folderName);
        if(!path.toFile().exists()) {
            path.toFile().mkdir();
        }

        savePlayer(player, path);
    }

    public static void savePlayer(UUID player, Path path) {
        try {
            if(PLAYER_DATA.isEmpty()) return;

            Path f1 = path.resolve(player.toString() + ".data");
            if(!f1.toFile().exists())
                f1.toFile().createNewFile();
            NbtIo.write(PLAYER_DATA.get(player).serializeNBT(), f1.toFile());
        } catch (Exception e) {
            RecipeMachineStage.LOGGER.error("net.sdm.recipemachinestage.utils.PlayerHelper$savePlayer(UUID player, Path path)");
            RecipeMachineStage.LOGGER.error(e.getMessage());
        }
    }
}
