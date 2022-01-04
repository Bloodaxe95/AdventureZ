package net.adventurez.mixin;

import java.util.List;
import java.util.concurrent.Executor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.At;

import net.adventurez.entity.SummonerEntity;
import net.adventurez.init.ConfigInit;
import net.adventurez.init.EntityInit;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.SpawnRestriction;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldGenerationProgressListener;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.Heightmap;
import net.minecraft.world.SpawnHelper;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.Spawner;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.level.ServerWorldProperties;
import net.minecraft.world.level.storage.LevelStorage;

@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin extends World {

    public ServerWorldMixin(MinecraftServer server, Executor workerExecutor, LevelStorage.Session session, ServerWorldProperties properties, RegistryKey<World> registryKey,
            DimensionType dimensionType, WorldGenerationProgressListener worldGenerationProgressListener, ChunkGenerator chunkGenerator, boolean bl, long l, List<Spawner> list, boolean bl2) {
        super(properties, registryKey, dimensionType, server::getProfiler, false, bl, l);

    }

    @Inject(method = "tickChunk", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LightningEntity;setCosmetic(Z)V", shift = Shift.AFTER))
    public void tickChunk(WorldChunk chunk, int randomTickSpeed, CallbackInfo info) {
        int summonerSpawnChance = ConfigInit.CONFIG.summoner_thunder_spawn_chance;
        if (summonerSpawnChance != 0) {
            int spawnChanceInt = this.getRandom().nextInt(summonerSpawnChance) + 1;
            if (spawnChanceInt == 1) {
                ChunkPos chunkPos = chunk.getPos();
                int i = chunkPos.getStartX();
                int j = chunkPos.getStartZ();
                BlockPos blockPos = this.getTopPosition(Heightmap.Type.MOTION_BLOCKING, this.getRandomPosInChunk(i, 0, j, 15));
                if (SpawnHelper.canSpawn(SpawnRestriction.Location.ON_GROUND, chunk.getWorld(), blockPos, EntityInit.SUMMONER_ENTITY)) {
                    SummonerEntity summonerEntity = (SummonerEntity) EntityInit.SUMMONER_ENTITY.create(this);
                    summonerEntity.updatePosition((double) blockPos.getX(), (double) blockPos.getY(), (double) blockPos.getZ());
                    summonerEntity.initialize((ServerWorld) (Object) this, this.getLocalDifficulty(blockPos), SpawnReason.EVENT, null, null);
                    this.spawnEntity(summonerEntity);
                    summonerEntity.playSpawnEffects();
                }
            }
        }
    }

}