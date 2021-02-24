package me.geek.tom.serverutils.mixin;

import me.geek.tom.serverutils.TomsServerUtils;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementFrame;
import net.minecraft.advancement.PlayerAdvancementTracker;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerAdvancementTracker.class)
public class MixinPlayerAdvancementTracker {
    @Shadow private ServerPlayerEntity owner;

    @Inject(method = "grantCriterion", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/PlayerManager;broadcastChatMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/MessageType;Ljava/util/UUID;)V"))
    private void hook_grantCriterion(Advancement advancement, String criterionName, CallbackInfoReturnable<Boolean> cir) {
        AdvancementFrame frame = advancement.getDisplay().getFrame();
        Text message = new TranslatableText("chat.type.advancement." + frame.getId(), this.owner.getDisplayName(), advancement.toHoverableText());
        TomsServerUtils.onPlayerAnnouncement(this.owner, message, frame == AdvancementFrame.CHALLENGE ? 0xAA00AA : 0x55FF55);
    }
}
