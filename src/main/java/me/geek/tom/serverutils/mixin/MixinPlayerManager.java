package me.geek.tom.serverutils.mixin;

import net.minecraft.network.ClientConnection;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static me.geek.tom.serverutils.ServerUtils2ElectricBoogalooKt.join;
import static me.geek.tom.serverutils.ServerUtils2ElectricBoogalooKt.leave;

@SuppressWarnings("unused")
@Mixin(PlayerManager.class)
public class MixinPlayerManager {
    @Inject(method = "onPlayerConnect", at = @At("RETURN"))
    private void playerConnected(ClientConnection connection, ServerPlayerEntity player, CallbackInfo ci) {
        join(player);
    }

    @Inject(method = "remove", at = @At("HEAD"))
    private void playerDisconnected(ServerPlayerEntity player, CallbackInfo ci) {
        leave(player);
    }
}
