package activation;
// for tests
public class TanhFunction implements ActivationFunction {
    @Override
    public float apply(float value) {
        return (float) Math.tanh(value);
    }
}
