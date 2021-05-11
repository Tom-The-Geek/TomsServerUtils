package me.geek.tom.serverutils.mixin;

import me.geek.tom.serverutils.ducks.IPlayerAccessor;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import static me.geek.tom.serverutils.ServerUtils2ElectricBoogalooKt.HAT_DISPLAY_MASK;

@SuppressWarnings("unused")
@Mixin(PlayerEntity.class)
public abstract class MixinPlayerEntity extends LivingEntity implements IPlayerAccessor {
    @Shadow @Final protected static TrackedData<Byte> PLAYER_MODEL_PARTS;

    protected MixinPlayerEntity(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    public boolean serverutils_showHat() {
        return (this.getDataTracker().get(PLAYER_MODEL_PARTS) & HAT_DISPLAY_MASK) != 0;
    }
}
