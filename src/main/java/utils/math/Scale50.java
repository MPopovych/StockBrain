package utils.math;

import utils.frames.ScaleMeta;

public class Scale50 implements ScaleImpl {

	public float[] performScale(ScaleMeta owner, float[] data) {
		float[] r = new float[data.length];
		for (int i = 0; i < data.length; i++) {
			r[i] = data[i] * 50;
		}
		return r;
	}

	@Override
	public float performScale(ScaleMeta owner, float data) {
		return data * 50;
	}


}
