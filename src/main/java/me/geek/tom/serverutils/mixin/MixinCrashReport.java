package me.geek.tom.serverutils.mixin;

import me.geek.tom.serverutils.TomsServerUtils;
import net.minecraft.util.crash.CrashReport;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.File;

@Mixin(CrashReport.class)
public class MixinCrashReport {
    @Inject(method = "writeToFile", at = @At("RETURN"))
    private void hookReportSaving(File file, CallbackInfoReturnable<Boolean> cir) {
        TomsServerUtils.crashed((CrashReport) (Object) this, cir.getReturnValueZ(), file);
    }
}
