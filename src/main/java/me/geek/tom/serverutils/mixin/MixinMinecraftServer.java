package me.geek.tom.serverutils.mixin;

import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static me.geek.tom.serverutils.ServerUtils2ElectricBoogalooKt.*;

@Mixin(MinecraftServer.class)
public class MixinMinecraftServer {
    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;setupServer()Z"), method = "runServer")
    private void serverutils_serverStarting(CallbackInfo info) {
        starting((MinecraftServer) (Object) this);
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;setFavicon(Lnet/minecraft/server/ServerMetadata;)V", ordinal = 0), method = "runServer")
    private void serverutils_serverStarted(CallbackInfo info) {
        started((MinecraftServer) (Object) this);
    }

    @Inject(at = @At("HEAD"), method = "shutdown")
    private void serverutils_serverStopping(CallbackInfo info) {
        stopping((MinecraftServer) (Object) this);
    }

    @Inject(at = @At("TAIL"), method = "shutdown")
    private void serverutils_serverStopped(CallbackInfo info) {
        stopped((MinecraftServer) (Object) this);
    }
}
