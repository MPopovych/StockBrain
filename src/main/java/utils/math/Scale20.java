package utils.math;

import utils.frames.ScaleMeta;

import java.util.Arrays;

public class Scale20 implements ScaleImpl {

	public float[] performScale(ScaleMeta owner, float[] data) {
		float[] r = new float[data.length];
		for (int i = 0; i < data.length; i++) {
			r[i] = data[i] * 20;
		}
		return r;
	}

	@Override
	public float performScale(ScaleMeta owner, float data) {
		return data * 20;
	}

}
