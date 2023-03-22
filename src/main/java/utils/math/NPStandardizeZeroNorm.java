package utils.math;

import utils.frames.ScaleMeta;

import java.util.Arrays;

public class NPStandardizeZeroNorm implements ScaleImpl {

	public float[] performScale(ScaleMeta owner, float[] data) {
		float[] r = new float[data.length];

		float std = owner.getStd();

		if (std == 0f) {
			Arrays.fill(r, 0f);
			return r;
		}

		for (int i = 0; i < data.length; i++) {
			r[i] = data[i] / std;
		}
		return r;
	}

	@Override
	public float performScale(ScaleMeta owner, float data) {
		float std = owner.getStd();
		if (std == 0f) {
			return 0f;
		}

		return data / std;
	}

}
