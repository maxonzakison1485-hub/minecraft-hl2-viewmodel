package com.maxonzakison1485.hl2viewmodel.mixin;

import com.maxonzakison1485.hl2viewmodel.ViewmodelMotion;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Adds a small viewmodel-only transform without replacing vanilla or shader rendering. */
@Mixin(ItemInHandRenderer.class)
public abstract class ItemInHandRendererMixin {
    @Unique private static final ViewmodelMotion hl2Viewmodel$motion = new ViewmodelMotion();
    @Unique private static float hl2Viewmodel$lastYaw;
    @Unique private static float hl2Viewmodel$lastPitch;
    @Unique private static boolean hl2Viewmodel$hasPrevious;

    @Inject(method = "submitHandsWithItems", at = @At("HEAD"))
    private void hl2Viewmodel$applyMotion(float partialTick, PoseStack poseStack,
            SubmitNodeCollector collector, LocalPlayer player, int combinedLight, CallbackInfo ci) {
        if (player == null) {
            hl2Viewmodel$motion.reset();
            hl2Viewmodel$hasPrevious = false;
            return;
        }

        float yaw = player.getYRot();
        float pitch = player.getXRot();
        float deltaYaw = hl2Viewmodel$hasPrevious ? wrapDegrees(yaw - hl2Viewmodel$lastYaw) : 0.0f;
        float deltaPitch = hl2Viewmodel$hasPrevious ? pitch - hl2Viewmodel$lastPitch : 0.0f;
        hl2Viewmodel$lastYaw = yaw;
        hl2Viewmodel$lastPitch = pitch;
        hl2Viewmodel$hasPrevious = true;

        // One frame's camera delta feeds a damped spring; clamp spikes after focus changes.
        hl2Viewmodel$motion.tick(deltaYaw * 0.0008f, deltaPitch * 0.0008f,
                1.0f / 60.0f, 1.0f, 8.0f);

        poseStack.pushPose();
        poseStack.translate(hl2Viewmodel$motion.getX(), -hl2Viewmodel$motion.getY(), 0.0);
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(-hl2Viewmodel$motion.getX() * 90.0f));
        poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(hl2Viewmodel$motion.getY() * 90.0f));
    }

    @Inject(method = "submitHandsWithItems", at = @At("RETURN"))
    private void hl2Viewmodel$restorePose(float partialTick, PoseStack poseStack,
            SubmitNodeCollector collector, LocalPlayer player, int combinedLight, CallbackInfo ci) {
        if (player != null) poseStack.popPose();
    }

    @Unique
    private static float wrapDegrees(float degrees) {
        degrees %= 360.0f;
        if (degrees >= 180.0f) degrees -= 360.0f;
        if (degrees < -180.0f) degrees += 360.0f;
        return degrees;
    }
}
