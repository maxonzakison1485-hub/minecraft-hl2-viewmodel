package com.maxonzakison1485.hl2viewmodel;

/**
 * Small frame-rate-independent spring model for viewmodel sway and inertia.
 * Feed it camera movement each frame, then use the returned offsets when rendering
 * the held item. This class deliberately has no Minecraft renderer dependencies.
 */
public final class ViewmodelMotion {
    private float positionX;
    private float positionY;
    private float velocityX;
    private float velocityY;

    /** Update the spring. deltaX/deltaY are camera movement since the prior frame. */
    public void tick(float deltaX, float deltaY, float deltaSeconds, float strength, float damping) {
        float dt = Math.max(0.0f, Math.min(deltaSeconds, 0.05f));
        float targetX = clamp(-deltaX * strength, -0.12f, 0.12f);
        float targetY = clamp(-deltaY * strength, -0.12f, 0.12f);
        float spring = 24.0f;
        velocityX += (targetX - positionX) * spring * dt;
        velocityY += (targetY - positionY) * spring * dt;
        float drag = (float) Math.exp(-Math.max(0.0f, damping) * dt);
        velocityX *= drag;
        velocityY *= drag;
        positionX += velocityX * dt;
        positionY += velocityY * dt;
    }

    public float getX() { return positionX; }
    public float getY() { return positionY; }

    public void reset() {
        positionX = positionY = velocityX = velocityY = 0.0f;
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
}
