package me.geek.tom.serverutils.mixin;

import net.minecraft.server.filter.TextStream;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static me.geek.tom.serverutils.ServerUtils2ElectricBoogalooKt.chat;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class MixinServerPlayNetHandler {
    @Inject(method = "handleMessage", cancellable = true, at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/PlayerManager;broadcast(Lnet/minecraft/text/Text;Ljava/util/function/Function;Lnet/minecraft/network/MessageType;Ljava/util/UUID;)V"))
    private void onChat(TextStream.Message message, CallbackInfo ci) {
        if (!chat((ServerPlayNetworkHandler) (Object) this, message)) {
            ci.cancel();
        }
    }
}
