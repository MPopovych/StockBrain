package utils.math;

import utils.frames.ScaleMeta;

public interface ScaleImpl {
	float[] performScale(ScaleMeta owner, float[] data);
	float performScale(ScaleMeta owner, float data);
}
