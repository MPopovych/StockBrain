package suppliers;

public class ConstSupplier implements ValueSupplier {

	private final float value;

	public ConstSupplier(float value) {
		this.value = value;
	}

	@Override
	public float supply(int x, int y) {
		return value;
	}
}
