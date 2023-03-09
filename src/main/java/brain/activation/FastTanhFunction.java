package brain.activation;

public class FastTanhFunction implements ActivationFunction  {
	/** cred to <a href="https://stackoverflow.com/questions/3535593/lookup-table-fast-sigmoidal-function">finnw</a> */
	private static final int TANH_FRAC_EXP = 6; // LUT precision == 2 ** -6 == 1/64
	private static final int TANH_LUT_SIZE = (1 << TANH_FRAC_EXP) * 8 + 1;
	private static final float TANH_FRAC_BIAS =
			Float.intBitsToFloat((0x96 - TANH_FRAC_EXP) << 23);
	private static final float[] TANH_TAB = new float[TANH_LUT_SIZE];
	static {
		for (int i = 0; i < TANH_LUT_SIZE; ++ i) {
			TANH_TAB[i] = (float) Math.tanh(i / 64.0);
		}
	}

	@Override
	public float apply(float value) {
		return fastTanH(value);
	}

	private static float fastTanH(float x) {
		if (x<0) return -fastTanH(-x);
		if (x>8) return 1f;
		float xp = TANH_FRAC_BIAS + x;
		short ind = (short) Float.floatToRawIntBits(xp);
		float tanha = TANH_TAB[ind];
		float b = xp - TANH_FRAC_BIAS;
		x -= b;
		return tanha + x * (1f - tanha*tanha);
	}
}
