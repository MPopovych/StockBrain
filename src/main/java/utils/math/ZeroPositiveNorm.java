package utils.math;

import utils.frames.ScaleMeta;

import java.util.Arrays;

public class ZeroPositiveNorm implements ScaleImpl {

	public float[] performScale(ScaleMeta owner, float[] data) {
		float[] r = new float[data.length];

		float min = owner.getMin();
		float spread = owner.getMax() - min;

		if (spread == 0f) {
			Arrays.fill(r, 1f);
			return r;
		}

		for (int i = 0; i < data.length; i++) {
			r[i] = (data[i] - min) / spread;
		}
		return r;
	}

	@Override
	public float performScale(ScaleMeta owner, float data) {
		float min = owner.getMin();
		float spread = owner.getMax() - min;

		if (spread == 0f) {
			return 1f;
		}
		return (data - owner.getMin()) / spread;
	}

}
