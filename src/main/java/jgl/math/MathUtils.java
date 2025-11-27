package jgl.math;

import java.util.Arrays;
import java.util.Random;

/**
 * Math utilities that should have been in java.lang.Math from day one.
 * All methods are static and the class is not instantiable.
 */
public final class MathUtils {

    private MathUtils() {
        throw new AssertionError("No MathUtils instances for you!");
    }

    private static final Random RANDOM = new Random();

    // ========================================================================
    // Constants
    // ========================================================================
    public static final double E = Math.E;
    public static final double PI = Math.PI;
    public static final double TAU = Math.PI * 2.0;        // 2π – very useful in graphics/audio
    public static final double DEG_TO_RAD = Math.PI / 180.0;
    public static final double RAD_TO_DEG = 180.0 / Math.PI;

    // ========================================================================
    // Clamping & Bounding
    // ========================================================================
    public static int clamp(int value, int min, int max) {
        return value < min ? min : (value > max ? max : value);
    }

    public static long clamp(long value, long min, long max) {
        return value < min ? min : (value > max ? max : value);
    }

    public static float clamp(float value, float min, float max) {
        return value < min ? min : (value > max ? max : value);
    }

    public static double clamp(double value, double min, double max) {
        return value < min ? min : (value > max ? max : value);
    }

    // ========================================================================
    // Linear Interpolation & Mapping
    // ========================================================================
    public static float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }

    public static double lerp(double a, double b, double t) {
        return a + (b - a) * t;
    }

    public static float map(float value, float inMin, float inMax, float outMin, float outMax) {
        return lerp(outMin, outMax, norm(value, inMin, inMax));
    }

    public static double map(double value, double inMin, double inMax, double outMin, double outMax) {
        return lerp(outMin, outMax, norm(value, inMin, inMax));
    }

    public static float norm(float value, float min, float max) {
        return (value - min) / (max - min);
    }

    public static double norm(double value, double min, double max) {
        return (value - min) / (max - min);
    }

    // ========================================================================
    // Rounding & Snapping
    // ========================================================================
    public static int round(double value) {
        return (int) Math.round(value);
    }

    public static int floor(double value) {
        return (int) Math.floor(value);
    }

    public static int ceil(double value) {
        return (int) Math.ceil(value);
    }

    public static long roundToLong(double value) {
        return Math.round(value);
    }

    public static int nearestMultiple(int value, int multiple) {
        return multiple * Math.round((float) value / multiple);
    }

    public static long nearestMultiple(long value, long multiple) {
        return multiple * Math.round((double) value / multiple);
    }

    // ========================================================================
    // Trigonometry helpers
    // ========================================================================
    public static double sinDeg(double degrees) {
        return Math.sin(degrees * DEG_TO_RAD);
    }

    public static double cosDeg(double degrees) {
        return Math.cos(degrees * DEG_TO_RAD);
    }

    public static double tanDeg(double degrees) {
        return Math.tan(degrees * DEG_TO_RAD);
    }

    public static double asinDeg(double sin) {
        return Math.asin(sin) * RAD_TO_DEG;
    }

    public static double acosDeg(double cos) {
        return Math.acos(cos) * RAD_TO_DEG;
    }

    public static double atanDeg(double tan) {
        return Math.atan(tan) * RAD_TO_DEG;
    }

    public static double atan2Deg(double y, double x) {
        return Math.atan2(y, x) * RAD_TO_DEG;
    }

    // ========================================================================
    // Fast approximations (good enough for games/graphics)
    // ========================================================================
    public static float fastInvSqrt(float x) {
        float xhalf = 0.5f * x;
        int i = Float.floatToIntBits(x);
        i = 0x5f3759df - (i >> 1);
        x = Float.intBitsToFloat(i);
        x = x * (1.5f - xhalf * x * x); // Newton step (repeat for more accuracy)
        return x;
    }

    // ========================================================================
    // Random utilities
    // ========================================================================
    public static int randomInt(int min, int max) {
        return RANDOM.nextInt(max - min + 1) + min;
    }

    public static float randomFloat(float min, float max) {
        return min + RANDOM.nextFloat() * (max - min);
    }

    public static double randomDouble(double min, double max) {
        return min + RANDOM.nextDouble() * (max - min);
    }

    public static boolean randomBoolean() {
        return RANDOM.nextBoolean();
    }

    public static float randomGaussian(float mean, float deviation) {
        return mean + (float) RANDOM.nextGaussian() * deviation;
    }

    // ========================================================================
    // Angle utilities
    // ========================================================================
    public static double angleDiff(double a, double b) {
        double diff = b - a;
        while (diff <= -Math.PI) diff += TAU;
        while (diff > Math.PI) diff -= TAU;
        return diff;
    }

    public static double lerpAngle(double a, double b, double t) {
        return a + angleDiff(a, b) * t;
    }

    public static double lerpAngleDeg(double a, double b, double t) {
        double diff = ((b - a) + 180) % 360 - 180;
        return a + diff * t;
    }

    // ========================================================================
    // Miscellaneous gems
    // ========================================================================
    public static boolean isPowerOfTwo(int n) {
        return n > 0 && (n & (n - 1)) == 0;
    }

    public static int nextPowerOfTwo(int n) {
        if (n <= 0) return 1;
        n--;
        n |= n >> 1;
        n |= n >> 2;
        n |= n >> 4;
        n |= n >> 8;
        n |= n >> 16;
        return n + 1;
    }

    public static double sigmoid(double x) {
        return 1.0 / (1.0 + Math.exp(-x));
    }

    public static double smoothStep(double edge0, double edge1, double x) {
        x = clamp((x - edge0) / (edge1 - edge0), 0.0, 1.0);
        return x * x * (3 - 2 * x);
    }

    public static double smootherStep(double edge0, double edge1, double x) {
        x = clamp((x - edge0) / (edge1 - edge0), 0.0, 1.0);
        return x * x * x * (x * (x * 6 - 15) + 10);
    }

    // Safe versions that never throw ArithmeticException
    public static int safeDiv(int a, int b) {
        return b == 0 ? 0 : a / b;
    }

    public static long safeDiv(long a, long b) {
        return b == 0 ? 0 : a / b;
    }

    // --------------------- MAX ---------------------
    public static int max(int a, int b, int... values) {
        int m = Math.max(a, b);
        for (int v : values) m = Math.max(m, v);
        return m;
    }

    public static long max(long a, long b, long... values) {
        long m = Math.max(a, b);
        for (long v : values) m = Math.max(m, v);
        return m;
    }

    public static float max(float a, float b, float... values) {
        float m = Math.max(a, b);
        for (float v : values) m = Math.max(m, v);
        return m;
    }

    public static double max(double a, double b, double... values) {
        double m = Math.max(a, b);
        for (double v : values) m = Math.max(m, v);
        return m;
    }

    // --------------------- MIN ---------------------
    public static int min(int a, int b, int... values) {
        int m = Math.min(a, b);
        for (int v : values) m = Math.min(m, v);
        return m;
    }

    public static long min(long a, long b, long... values) {
        long m = Math.min(a, b);
        for (long v : values) m = Math.min(m, v);
        return m;
    }

    public static float min(float a, float b, float... values) {
        float m = Math.min(a, b);
        for (float v : values) m = Math.min(m, v);
        return m;
    }

    public static double min(double a, double b, double... values) {
        double m = Math.min(a, b);
        for (double v : values) m = Math.min(m, v);
        return m;
    }

    // --------------------- AVERAGE (mean) ---------------------
    public static double average(byte... values) {
        if (values.length == 0) return 0.0;
        long sum = 0;
        for (byte v : values) sum += v;
        return sum / (double) values.length;
    }

    public static double average(short... values) {
        if (values.length == 0) return 0.0;
        long sum = 0;
        for (short v : values) sum += v;
        return sum / (double) values.length;
    }

    public static double average(int... values) {
        if (values.length == 0) return 0.0;
        long sum = 0;
        for (int v : values) sum += v;
        return sum / (double) values.length;
    }

    public static double average(long... values) {
        if (values.length == 0) return 0.0;
        long sum = 0;
        for (long v : values) sum += v;
        return sum / (double) values.length;
    }

    public static double average(float... values) {
        if (values.length == 0) return 0.0f;
        double sum = 0.0;
        for (float v : values) sum += v;
        return sum / values.length;
    }

    public static double average(double... values) {
        if (values.length == 0) return 0.0;
        double sum = 0.0;
        for (double v : values) sum += v;
        return sum / values.length;
    }

    // Bonus: median (sorted middle value) – very handy!
    public static double median(int... values) {
        if (values.length == 0) return 0.0;
        int[] copy = values.clone();
        Arrays.sort(copy);
        int mid = copy.length >> 1;
        return (copy.length & 1) == 0 ? (copy[mid - 1] + copy[mid]) / 2.0 : copy[mid];
    }

    public static double median(long... values) {
        if (values.length == 0) return 0.0;
        long[] copy = values.clone();
        Arrays.sort(copy);
        int mid = copy.length >> 1;
        return (copy.length & 1) == 0 ? (copy[mid - 1] + copy[mid]) / 2.0 : copy[mid];
    }

    public static double median(float... values) {
        if (values.length == 0) return 0.0f;
        float[] copy = values.clone();
        Arrays.sort(copy);
        int mid = copy.length >> 1;
        return (copy.length & 1) == 0 ? (copy[mid - 1] + copy[mid]) / 2.0f : copy[mid];
    }

    public static double median(double... values) {
        if (values.length == 0) return 0.0;
        double[] copy = values.clone();
        Arrays.sort(copy);
        int mid = copy.length >> 1;
        return (copy.length & 1) == 0 ? (copy[mid - 1] + copy[mid]) / 2.0 : copy[mid];
    }

    // ========================================================================
    // Extra goodies that save your life daily
    // ========================================================================

    /**
     * Returns true if value is within [min, max] (inclusive)
     */
    public static boolean between(int value, int min, int max) {
        return value >= min && value <= max;
    }

    public static boolean between(long value, long min, long max) {
        return value >= min && value <= max;
    }

    public static boolean between(float value, float min, float max) {
        return value >= min && value <= max;
    }

    public static boolean between(double value, double min, double max) {
        return value >= min && value <= max;
    }

    /**
     * Returns the sign: -1, 0, or 1
     */
    public static int sign(int x) {
        return Integer.compare(x, 0);
    }

    public static int sign(long x) {
        return Long.compare(x, 0);
    }

    public static int sign(float x) {
        return Float.compare(x, 0.0f);
    }

    public static int sign(double x) {
        return Double.compare(x, 0.0);
    }

    /**
     * Absolute value without branching (faster on some CPUs)
     */
    public static int abs(int x) {
        return Math.abs(x);
    }

    public static long abs(long x) {
        return Math.abs(x);
    }

    public static float abs(float x) {
        return Math.abs(x);
    }

    public static double abs(double x) {
        return Math.abs(x);
    }

    /**
     * Fast floor for positive numbers only (used a lot in games)
     */
    public static int fastFloor(float x) {
        int xi = (int) x;
        return x < xi ? xi - 1 : xi;
    }

    public static int fastFloor(double x) {
        int xi = (int) x;
        return x < xi ? xi - 1 : xi;
    }

    // --------------------- Distance & Length ---------------------
    public static double distance(double x1, double y1, double x2, double y2) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        return Math.sqrt(dx * dx + dy * dy);
    }

    public static float distance(float x1, float y1, float x2, float y2) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    public static double distanceSq(double x1, double y1, double x2, double y2) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        return dx * dx + dy * dy;
    }

    public static float distanceSq(float x1, float y1, float x2, float y2) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        return dx * dx + dy * dy;
    }

    // --------------------- Fast approximations (great for games) ---------------------
    public static float fastSqrt(float x) {
        return 1.0f / fastInvSqrt(x);
    }

    public static float fastDistance(float x1, float y1, float x2, float y2) {
        return 1.0f / fastInvSqrt(distanceSq(x1, y1, x2, y2));
    }

    // --------------------- Easing / Animation curves ---------------------
    public static double easeInQuad(double t) {
        return t * t;
    }

    public static double easeOutQuad(double t) {
        return t * (2 - t);
    }

    public static double easeInOutQuad(double t) {
        return t < 0.5 ? 2 * t * t : -1 + (4 - 2 * t) * t;
    }

    public static double easeInCubic(double t) {
        return t * t * t;
    }

    public static double easeOutCubic(double t) {
        return (--t) * t * t + 1;
    }

    public static double easeInOutCubic(double t) {
        return t < 0.5 ? 4 * t * t * t : (t - 1) * (2 * t - 2) * (2 * t - 2) + 1;
    }

    public static double easeOutElastic(double t) {
        double p = 0.3;
        return Math.pow(2, -10 * t) * Math.sin((t - p / 4) * (2 * Math.PI) / p) + 1;
    }

    public static double easeOutBounce(double t) {
        if (t < 1 / 2.75) return 7.5625 * t * t;
        if (t < 2 / 2.75) {
            t -= 1.5 / 2.75;
            return 7.5625 * t * t + 0.75;
        }
        if (t < 2.5 / 2.75) {
            t -= 2.25 / 2.75;
            return 7.5625 * t * t + 0.9375;
        }
        t -= 2.625 / 2.75;
        return 7.5625 * t * t + 0.984375;
    }

    // --------------------- Number theory & bit tricks ---------------------
    public static int gcd(int a, int b) {
        while (b != 0) {
            int t = b;
            b = a % b;
            a = t;
        }
        return a < 0 ? -a : a;
    }

    public static long gcd(long a, long b) {
        while (b != 0) {
            long t = b;
            b = a % b;
            a = t;
        }
        return a < 0 ? -a : a;
    }

    public static int lcm(int a, int b) {
        return a / gcd(a, b) * b; // careful with overflow!
    }

    public static boolean isEven(int n) {
        return (n & 1) == 0;
    }

    public static boolean isOdd(int n) {
        return (n & 1) != 0;
    }

    // Reverse bits (fun and sometimes useful)
    public static int reverseBits(int n) {
        n = ((n >>> 1) & 0x55555555) | ((n & 0x55555555) << 1);
        n = ((n >>> 2) & 0x33333333) | ((n & 0x33333333) << 2);
        n = ((n >>> 4) & 0x0F0F0F0F) | ((n & 0x0F0F0F0F) << 4);
        n = ((n >>> 8) & 0x00FF00FF) | ((n & 0x00FF00FF) << 8);
        return (n >>> 16) | (n << 16);
    }

    // --------------------- Floating-point helpers ---------------------
    public static boolean equalsApprox(double a, double b) {
        return Math.abs(a - b) <= 1e-9;
    }

    public static boolean equalsApprox(float a, float b) {
        return Math.abs(a - b) <= 1e-6f;
    }

    public static boolean isZero(double x) {
        return Math.abs(x) < 1e-12;
    }

    public static boolean isZero(float x) {
        return Math.abs(x) < 1e-6f;
    }

    // --------------------- Random with seed control (when you need reproducibility) ---------------------
    private static final ThreadLocal<Random> threadRandom = ThreadLocal.withInitial(Random::new);

    public static void setSeed(long seed) {
        threadRandom.get().setSeed(seed);
    }

    public static int randomInt(int bound) {
        return threadRandom.get().nextInt(bound);
    }

    // Weighted coin flip
    public static boolean chance(double probability) {
        return threadRandom.get().nextDouble() < probability;
    }

    // --------------------- Logarithms (base 2, 10, natural already exist) ---------------------
    public static double log2(double x) {
        return Math.log(x) / Math.log(2);
    }

    public static double log10(double x) {
        return Math.log10(x);
    }

    // --------------------- Wrap / repeat (like modulo but works correctly for negative) ---------------------
    public static int wrap(int value, int min, int max) {
        int range = max - min + 1;
        return ((value - min) % range + range) % range + min;
    }

    public static double wrap(double value, double min, double max) {
        double range = max - min;
        return value - (range * Math.floor((value - min) / range));
    }

    // --------------------- Approach / move toward (great for smooth movement) ---------------------
    public static float approach(float current, float target, float delta) {
        return current < target ? Math.min(current + delta, target)
                : Math.max(current - delta, target);
    }

    public static double approach(double current, double target, double delta) {
        return current < target ? Math.min(current + delta, target)
                : Math.max(current - delta, target);
    }

    // --------------------- Deadzone (for gamepads / joysticks) ---------------------
    public static float applyDeadzone(float value, float deadzone) {
        return Math.abs(value) < deadzone ? 0.0f : value > 0 ? value - deadzone : value + deadzone;
    }
}