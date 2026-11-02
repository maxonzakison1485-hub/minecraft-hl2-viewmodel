package com.maxonzakison1485.hl2viewmodel;

/** Direct HL2-style viewmodel lag and walk bob. */
public final class ViewmodelMotion {
    private float x, y, z;
    private float phase;

    public void tick(float deltaYaw, float deltaPitch, float walkSpeed,
            float walkPhase, boolean grounded, float dt) {
        walkSpeed = clamp(walkSpeed, 0f, 1f);
        // Source CalcViewModelLag: angle delta directly offsets the viewmodel.
        x = clamp(-deltaYaw * 0.0018f, -0.09f, 0.09f);
        z = clamp(deltaPitch * 0.0018f, -0.07f, 0.07f);

        float speed = grounded ? walkSpeed : 0f;
        phase += speed * Math.max(0f, dt) * 12f;
        // HL2 bob: vertical sine and forward/back cosine.
        z += (float)Math.sin(phase * 2f) * 0.025f * speed;
        y = (float)Math.cos(phase) * 0.018f * speed;
    }

    public float getX() { return x; }
    public float getY() { return y; }
    public float getZ() { return z; }
    public float getPitch() { return 0f; }
    public float getYaw() { return 0f; }
    public float getRoll() { return 0f; }

    public void reset() {
        x = y = z = phase = 0f;
    }

    private static float clamp(float value, float min, float max) {
        return Float.isFinite(value) ? Math.max(min, Math.min(max, value)) : 0f;
    }
}
