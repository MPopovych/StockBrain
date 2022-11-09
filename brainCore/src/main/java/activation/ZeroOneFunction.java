package activation;

public class ZeroOneFunction implements ActivationFunction {
    @Override
    public float apply(float value) {
        return value > 0.5 ? 1 : 0;
    }
}
