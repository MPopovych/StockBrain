package utils.math;

import utils.frames.ScaleMeta;

public class Scale30 implements ScaleImpl {

	public float[] performScale(ScaleMeta owner, float[] data) {
		float[] r = new float[data.length];
		for (int i = 0; i < data.length; i++) {
			r[i] = data[i] * 30;
		}
		return r;
	}

	@Override
	public float performScale(ScaleMeta owner, float data) {
		return data * 30;
	}


}
