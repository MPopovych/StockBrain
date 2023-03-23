package utils.math;

import brain.activation.FastTanhFunction;
import utils.frames.ScaleMeta;

import java.util.Arrays;

public class TanhNorm implements ScaleImpl {

	private FastTanhFunction activation = new FastTanhFunction();

	public float[] performScale(ScaleMeta owner, float[] data) {
		float[] r = new float[data.length];

		float std = owner.getStd();
		float median = owner.getMedian();

		if (std == 0f) {
			// don't allow for zero deviation
			throw new IllegalStateException();
		}

		for (int i = 0; i < data.length; i++) {
			float inner = (0.1f * ((data[i] - median) / std));
			r[i] = (activation.apply(inner));
		}
		return r;
	}

	@Override
	public float performScale(ScaleMeta owner, float data) {
		float std = owner.getStd();
		float median = owner.getMedian();
		if (std == 0f) return 0f;

		float inner = (0.1f * ((data - median) / std));
		return (activation.apply(inner));
	}
}
