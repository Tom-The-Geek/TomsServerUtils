package me.geek.tom.serverutils.mixin;

import me.geek.tom.serverutils.TomsServerUtils;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class MixinServerPlayNetHandler {
    @Inject(method = "onGameMessage", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/PlayerManager;broadcastChatMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/MessageType;Ljava/util/UUID;)V"))
    private void onChat(ChatMessageC2SPacket packet, CallbackInfo ci) {
        TomsServerUtils.chat((ServerPlayNetworkHandler) (Object) this, packet);
    }
}
