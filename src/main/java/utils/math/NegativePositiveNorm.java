package utils.math;

import utils.frames.ScaleMeta;

import java.util.Arrays;

public class NegativePositiveNorm implements ScaleImpl {

	public float[] performScale(ScaleMeta owner, float[] data) {
		float[] r = new float[data.length];

		float min = owner.getMin();
		float spread = owner.getMax() - min;

		if (spread == 0f) {
			Arrays.fill(r, 0f);
			return r;
		}

		for (int i = 0; i < data.length; i++) {
			r[i] = ((data[i] - min) * 2 / spread) - 1f;
		}
		return r;
	}

	@Override
	public float performScale(ScaleMeta owner, float data) {
		float min = owner.getMin();
		float spread = owner.getMax() - min;

		if (spread == 0f) {
			return 0f;
		}
		return ((data - min) * 2 / spread) - 1f;
	}

}
