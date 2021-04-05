package me.geek.tom.serverutils.mixin;

import net.minecraft.server.network.ServerPlayNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static me.geek.tom.serverutils.ServerUtils2ElectricBoogalooKt.chat;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class MixinServerPlayNetHandler {
    @Inject(method = "method_31286", cancellable = true, at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/PlayerManager;broadcastChatMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/MessageType;Ljava/util/UUID;)V"))
    private void onChat(String message, CallbackInfo ci) {
        if (!chat((ServerPlayNetworkHandler) (Object) this, message)) {
            ci.cancel();
        }
    }
}
