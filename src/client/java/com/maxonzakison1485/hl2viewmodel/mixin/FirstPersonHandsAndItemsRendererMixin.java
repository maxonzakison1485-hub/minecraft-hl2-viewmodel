package com.maxonzakison1485.hl2viewmodel.mixin;

import com.maxonzakison1485.hl2viewmodel.HL2ViewmodelClient;
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
    private static float hl2vm$lastYaw;
    private static float hl2vm$lastPitch;
    private static boolean hl2vm$hasPrevious;

    @Inject(method = "submitHandsWithItems", at = @At("HEAD"))
    private void hl2vm$updateMotion(float partialTicks, PoseStack poseStack,
            SubmitNodeCollector collector, PlayerRenderState playerState,
            FirstPersonHandsAndItemsRenderState state, CallbackInfo ci) {
        float yaw = state.viewYRot;
        float pitch = state.viewXRot;
        float deltaYaw = hl2vm$hasPrevious ? wrapDegrees(yaw - hl2vm$lastYaw) : 0.0f;
        float deltaPitch = hl2vm$hasPrevious ? pitch - hl2vm$lastPitch : 0.0f;
        hl2vm$lastYaw = yaw;
        hl2vm$lastPitch = pitch;
        hl2vm$hasPrevious = true;

        // Render-state extraction is frame based; a stable nominal step avoids
        // coupling the spring to the number of hands rendered.
        hl2vm$motion.tick(deltaYaw, deltaPitch, 0.0f, 0.0f,
                false, 1.0f / 60.0f, 1.0f, 0.88f);
    }

    @Inject(method = "submitArmWithItem", at = @At("HEAD"))
    private void hl2vm$applyMotion(PlayerRenderState playerState,
            FirstPersonHandsAndItemsRenderState state, float partialTicks,
            float xRot, InteractionHand hand, float attack, ItemStack itemStack,
            float inverseArmHeight, PoseStack poseStack,
            SubmitNodeCollector collector, int lightCoords, CallbackInfo ci) {
        poseStack.pushPose();
        poseStack.translate(hl2vm$motion.getX(), hl2vm$motion.getY(), hl2vm$motion.getZ());
        poseStack.mulPose(new Matrix4f()
                .rotateX((float) Math.toRadians(hl2vm$motion.getPitch()))
                .rotateY((float) Math.toRadians(hl2vm$motion.getYaw()))
                .rotateZ((float) Math.toRadians(hl2vm$motion.getRoll())));
    }

    @Inject(method = "submitArmWithItem", at = @At("RETURN"))
    private void hl2vm$restorePose(PlayerRenderState playerState,
            FirstPersonHandsAndItemsRenderState state, float partialTicks,
            float xRot, InteractionHand hand, float attack, ItemStack itemStack,
            float inverseArmHeight, PoseStack poseStack,
            SubmitNodeCollector collector, int lightCoords, CallbackInfo ci) {
        poseStack.popPose();
    }

    private static float wrapDegrees(float value) {
        value %= 360.0f;
        if (value >= 180.0f) value -= 360.0f;
        if (value < -180.0f) value += 360.0f;
        return value;
    }
}
