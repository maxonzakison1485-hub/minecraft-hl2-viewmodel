package com.maxonzakison1485.hl2viewmodel.mixin;

import com.maxonzakison1485.hl2viewmodel.ViewmodelMotion;
import com.mojang.blaze3d.vertex.PoseStack;
import org.joml.Matrix4f;
import net.minecraft.client.renderer.FirstPersonHandsAndItemsRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.FirstPersonHandsAndItemsRenderState;
import net.minecraft.client.renderer.state.level.PlayerRenderState;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FirstPersonHandsAndItemsRenderer.class)
public abstract class FirstPersonHandsAndItemsRendererMixin {
    private static final ViewmodelMotion hl2vm$motion = new ViewmodelMotion();
    private static float hl2vm$lastYaw, hl2vm$lastPitch;
    private static long hl2vm$lastNanos;
    private static boolean hl2vm$hasPrevious;

    @Inject(method = "submitHandsWithItems", at = @At("HEAD"))
    private void hl2vm$updateMotion(float partialTicks, PoseStack poseStack,
            SubmitNodeCollector collector, PlayerRenderState playerState,
            FirstPersonHandsAndItemsRenderState state, CallbackInfo ci) {
        long now = System.nanoTime();
        float dt = hl2vm$lastNanos == 0L ? 1f / 60f
                : Math.min((now - hl2vm$lastNanos) / 1_000_000_000f, 0.1f);
        hl2vm$lastNanos = now;

        float yaw = state.viewYRot, pitch = state.viewXRot;
        float dy = hl2vm$hasPrevious ? wrapDegrees(yaw - hl2vm$lastYaw) : 0f;
        float dp = hl2vm$hasPrevious ? pitch - hl2vm$lastPitch : 0f;
        hl2vm$lastYaw = yaw; hl2vm$lastPitch = pitch; hl2vm$hasPrevious = true;

        float speed = Math.min(1f, Math.abs(playerState.walkAnimationSpeed));
        float phase = playerState.walkAnimationPos;
        boolean grounded = !playerState.isFallFlying && !playerState.isVisuallySwimming;
        hl2vm$motion.tick(dy, dp, speed, phase, grounded, dt);
    }

    @Inject(method = "submitArmWithItem", at = @At("HEAD"))
    private void hl2vm$applyMotion(PlayerRenderState playerState,
            FirstPersonHandsAndItemsRenderState state, float partialTicks,
            float xRot, InteractionHand hand, float attack, ItemStack itemStack,
            float inverseArmHeight, PoseStack poseStack,
            SubmitNodeCollector collector, int lightCoords, CallbackInfo ci) {
        poseStack.pushPose();
        // Cancel the renderer's supplied camera-bob offsets for the hands/items only.
        poseStack.translate(-state.xBob, -state.yBob, 0.0f);
        poseStack.translate(hl2vm$motion.getX(), hl2vm$motion.getY(), hl2vm$motion.getZ());
        poseStack.mulPose(new Matrix4f()
                .rotateX((float)Math.toRadians(hl2vm$motion.getPitch()))
                .rotateY((float)Math.toRadians(hl2vm$motion.getYaw()))
                .rotateZ((float)Math.toRadians(hl2vm$motion.getRoll())));
    }

    @Inject(method = "submitArmWithItem", at = @At("RETURN"))
    private void hl2vm$restorePose(PlayerRenderState playerState,
            FirstPersonHandsAndItemsRenderState state, float partialTicks,
            float xRot, InteractionHand hand, float attack, ItemStack itemStack,
            float inverseArmHeight, PoseStack poseStack,
            SubmitNodeCollector collector, int lightCoords, CallbackInfo ci) {
        poseStack.popPose();
    }

    private static float wrapDegrees(float v) {
        v %= 360f;
        if (v >= 180f) v -= 360f;
        if (v < -180f) v += 360f;
        return v;
    }
}