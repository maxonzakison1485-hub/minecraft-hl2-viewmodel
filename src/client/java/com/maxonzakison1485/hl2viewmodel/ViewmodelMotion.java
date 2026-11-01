package com.maxonzakison1485.hl2viewmodel;

/**
 * Frame-rate-independent Half-Life 2-style first-person viewmodel motion.
 *
 * The class is renderer-agnostic: call tick() from the client tick/render loop
 * and apply the returned translation and rotation to the held-item pose stack.
 *
 * Camera deltas are measured in degrees since the previous update.
 * Movement inputs are normalized to [-1, 1].
 */
public final class ViewmodelMotion {
    private static final float SPRING_FREQUENCY = 18.0f;

    private final Spring x = new Spring();
    private final Spring y = new Spring();
    private final Spring z = new Spring();

    private final Spring pitch = new Spring();
    private final Spring yaw = new Spring();
    private final Spring roll = new Spring();

    private float bobPhase;

    /**
     * Updates the viewmodel motion.
     *
     * @param cameraDeltaYaw   camera yaw change in degrees since the last update
     * @param cameraDeltaPitch camera pitch change in degrees since the last update
     * @param moveStrafe       horizontal movement input, normalized to [-1, 1]
     * @param moveForward      forward movement input, normalized to [-1, 1]
     * @param onGround         whether the player is on the ground
     * @param deltaSeconds     elapsed time since the previous update, in seconds
     * @param strength         overall motion multiplier; 1.0 is a good starting point
     * @param damping          spring damping ratio; 0.9 gives a little overshoot
     */
    public void tick(
            float cameraDeltaYaw,
            float cameraDeltaPitch,
            float moveStrafe,
            float moveForward,
            boolean onGround,
            float deltaSeconds,
            float strength,
            float damping
    ) {
        float dt = clamp(deltaSeconds, 0.0f, 0.1f);
        if (dt <= 0.0f) {
            return;
        }

        float power = clamp(strength, 0.0f, 3.0f);
        float dampingRatio = clamp(damping, 0.15f, 2.0f);

        moveStrafe = clamp(moveStrafe, -1.0f, 1.0f);
        moveForward = clamp(moveForward, -1.0f, 1.0f);

        // Convert frame deltas to angular velocity so sensitivity is stable across FPS.
        float yawSpeed = clamp(cameraDeltaYaw / dt, -720.0f, 720.0f);
        float pitchSpeed = clamp(cameraDeltaPitch / dt, -720.0f, 720.0f);

        float movement = Math.min(
                1.0f,
                (float) Math.sqrt(
                        moveStrafe * moveStrafe + moveForward * moveForward
                )
        );

        if (onGround && movement > 0.01f) {
            bobPhase += dt * (8.0f + movement * 4.0f);
        }

        float bobY = 0.0f;
        float bobZ = 0.0f;

        if (onGround) {
            bobY = (float) Math.sin(bobPhase * 2.0f) * 0.018f * movement * power;
            bobZ = (float) Math.cos(bobPhase) * 0.012f * movement * power;
        }

        // Translation: slight lag opposite the camera turn plus movement bob.
        float targetX = (
                -yawSpeed * 0.00020f - moveStrafe * 0.018f
        ) * power;

        float targetY = pitchSpeed * 0.00016f * power + bobY;

        float targetZ = (
                -moveForward * 0.018f + bobZ
        ) * power;

        // Rotation: the weapon follows the camera with a small, damped delay.
        float targetPitch = (
                -pitchSpeed * 0.015f - moveForward * 1.5f
        ) * power;

        float targetYaw = (
                -yawSpeed * 0.012f + moveStrafe * 1.0f
        ) * power;

        float targetRoll = (
                -yawSpeed * 0.020f - moveStrafe * 3.0f
        ) * power;

        x.update(targetX, dt, SPRING_FREQUENCY, dampingRatio);
        y.update(targetY, dt, SPRING_FREQUENCY, dampingRatio);
        z.update(targetZ, dt, SPRING_FREQUENCY, dampingRatio);

        pitch.update(clamp(targetPitch, -10.0f, 10.0f), dt,
                SPRING_FREQUENCY, dampingRatio);
        yaw.update(clamp(targetYaw, -10.0f, 10.0f), dt,
                SPRING_FREQUENCY, dampingRatio);
        roll.update(clamp(targetRoll, -12.0f, 12.0f), dt,
                SPRING_FREQUENCY, dampingRatio);
    }

    /**
     * Backwards-compatible update method for callers using the original API.
     * deltaX and deltaY are camera yaw and pitch deltas in degrees.
     */
    public void tick(
            float deltaX,
            float deltaY,
            float deltaSeconds,
            float strength,
            float damping
    ) {
        tick(
                deltaX,
                deltaY,
                0.0f,
                0.0f,
                false,
                deltaSeconds,
                strength,
                damping
        );
    }

    /**
     * Adds a weapon recoil impulse. Call once when a shot is fired.
     *
     * @param recoilPitch pitch impulse in degrees
     * @param pushBack    backward impulse in model-space units
     */
    public void addRecoil(float recoilPitch, float pushBack) {
        if (!Float.isFinite(recoilPitch) || !Float.isFinite(pushBack)) {
            return;
        }

        pitch.addVelocity(recoilPitch * 12.0f);
        z.addVelocity(-Math.abs(pushBack) * 12.0f);
    }

    public float getX() {
        return x.position;
    }

    public float getY() {
        return y.position;
    }

    public float getZ() {
        return z.position;
    }

    public float getPitch() {
        return pitch.position;
    }

    public float getYaw() {
        return yaw.position;
    }

    public float getRoll() {
        return roll.position;
    }

    public void reset() {
        x.reset();
        y.reset();
        z.reset();

        pitch.reset();
        yaw.reset();
        roll.reset();

        bobPhase = 0.0f;
    }

    private static float clamp(float value, float min, float max) {
        if (!Float.isFinite(value)) {
            return 0.0f;
        }
        return Math.max(min, Math.min(max, value));
    }

    /**
     * Exact damped spring integration for a constant target during one update.
     * This avoids the frame-rate-dependent behavior of explicit Euler integration.
     */
    private static final class Spring {
        private float position;
        private float velocity;

        void update(float target, float dt, float frequency, float damping) {
            if (dt <= 0.0f) {
                return;
            }

            float omega = frequency;
            float displacement = position - target;

            if (Math.abs(damping - 1.0f) < 0.001f) {
                // Critically damped.
                float j = velocity + omega * displacement;
                float decay = (float) Math.exp(-omega * dt);

                position = target + (displacement + j * dt) * decay;
                velocity = (velocity - omega * j * dt) * decay;
            } else if (damping < 1.0f) {
                // Underdamped.
                float dampedFrequency = omega * (float) Math.sqrt(
                        1.0f - damping * damping
                );

                float a = displacement;
                float b = (velocity + damping * omega * displacement)
                        / dampedFrequency;

                float sin = (float) Math.sin(dampedFrequency * dt);
                float cos = (float) Math.cos(dampedFrequency * dt);
                float decay = (float) Math.exp(-damping * omega * dt);
                float oscillation = a * cos + b * sin;

                position = target + decay * oscillation;
                velocity = decay * (
                        -damping * omega * oscillation
                                - a * dampedFrequency * sin
                                + b * dampedFrequency * cos
                );
            } else {
                // Overdamped.
                float root = (float) Math.sqrt(damping * damping - 1.0f);
                float r1 = -omega * (damping - root);
                float r2 = -omega * (damping + root);

                float c1 = (velocity - r2 * displacement) / (r1 - r2);
                float c2 = (r1 * displacement - velocity) / (r1 - r2);

                float e1 = (float) Math.exp(r1 * dt);
                float e2 = (float) Math.exp(r2 * dt);

                position = target + c1 * e1 + c2 * e2;
                velocity = r1 * c1 * e1 + r2 * c2 * e2;
            }
        }

        void addVelocity(float amount) {
            velocity += amount;
        }

        void reset() {
            position = 0.0f;
            velocity = 0.0f;
        }
    }
}
