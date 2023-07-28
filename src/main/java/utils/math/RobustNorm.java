package utils.math;

import utils.frames.ScaleMeta;

import java.util.Arrays;

public class RobustNorm implements ScaleImpl {

	public float[] performScale(ScaleMeta owner, float[] data) {
		float[] r = new float[data.length];

		float iqr = owner.getIqr();
		float median = owner.getMedian();

		if (iqr == 0f) {
			Arrays.fill(r, 0f);
			return r;
		}

		for (int i = 0; i < data.length; i++) {
			r[i] = (data[i] - median) / iqr;
		}
		return r;
	}

	@Override
	public float performScale(ScaleMeta owner, float data) {
		float iqr = owner.getIqr();
		if (iqr == 0f) return 0f;
		return (data - owner.getMedian()) / iqr;
	}
}
