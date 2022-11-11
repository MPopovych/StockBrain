package activation;

public class ReLuFunction implements ActivationFunction {
    @Override
    public float apply(float value) {
        return Math.max(value, 0);
    }
}
