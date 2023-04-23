package utils.math;

import utils.frames.ScaleMeta;

public class SplitScale20 implements ScaleImpl {

	public float[] performScale(ScaleMeta owner, float[] data) {
		float[] r = new float[data.length];
		for (int i = 0; i < data.length; i++) {
			float v = data[i];
			if (v > 0) {
				r[i] = 1f + data[i] * 20;
			} else if (v < 0) {
				r[i] = -1f + data[i] * 20;
			} else {
				r[i] = 0f;
			}
		}
		return r;
	}

	@Override
	public float performScale(ScaleMeta owner, float data) {
		return data * 20;
	}

}
