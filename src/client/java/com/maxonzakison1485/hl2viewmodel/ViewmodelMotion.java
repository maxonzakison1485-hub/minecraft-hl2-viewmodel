package com.maxonzakison1485.hl2viewmodel;

/** Spring-driven HL2-inspired inertia and independent first-person walk bob. */
public final class ViewmodelMotion {
    private static final float FREQUENCY = 15.5f;
    private final Spring x = new Spring(), y = new Spring(), z = new Spring();
    private final Spring pitch = new Spring(), yaw = new Spring(), roll = new Spring();
    private float phase;

    public void tick(float deltaYaw, float deltaPitch, float walkSpeed,
            float walkPhase, boolean grounded, float dt) {
        dt = clamp(dt, 0.001f, 0.1f);
        walkSpeed = clamp(walkSpeed, 0.0f, 1.0f);
        float yawRate = clamp(deltaYaw / dt, -650.0f, 650.0f);
        float pitchRate = clamp(deltaPitch / dt, -650.0f, 650.0f);

        float move = grounded ? walkSpeed : 0.0f;
        phase += move * dt * 12.0f;
        // Own subdued HL2-like walk cycle; it fades smoothly as movement stops.
        float bob = move * move;
        float bobY = (float)Math.sin(phase * 2.0f) * 0.024f * bob;
        float bobZ = (float)Math.cos(phase) * 0.014f * bob;
        float targetX = clamp(-yawRate * 0.00024f, -0.12f, 0.12f);
        float targetY = clamp(pitchRate * 0.00018f + bobY, -0.10f, 0.10f);
        float targetZ = -0.018f * move + bobZ;
        float targetPitch = clamp(-pitchRate * 0.012f - move * 1.1f, -8f, 8f);
        float targetYaw = clamp(-yawRate * 0.010f, -7f, 7f);
        float targetRoll = clamp(-yawRate * 0.018f, -10f, 10f);

        x.update(targetX, dt); y.update(targetY, dt); z.update(targetZ, dt);
        pitch.update(targetPitch, dt); yaw.update(targetYaw, dt); roll.update(targetRoll, dt);
    }

    public float getX(){ return x.position; }
    public float getY(){ return y.position; }
    public float getZ(){ return z.position; }
    public float getPitch(){ return pitch.position; }
    public float getYaw(){ return yaw.position; }
    public float getRoll(){ return roll.position; }

    public void reset() {
        x.reset(); y.reset(); z.reset(); pitch.reset(); yaw.reset(); roll.reset();
        phase = 0f;
    }

    private static float clamp(float v,float min,float max) {
        return Float.isFinite(v) ? Math.max(min,Math.min(max,v)) : 0f;
    }

    private static final class Spring {
        float position, velocity;
        void update(float target,float dt) {
            float omega=FREQUENCY, d=position-target;
            float j=velocity+omega*d, decay=(float)Math.exp(-omega*dt);
            position=target+(d+j*dt)*decay;
            velocity=(velocity-omega*j*dt)*decay;
        }
        void reset(){position=velocity=0f;}
    }
}