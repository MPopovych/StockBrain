package suppliers;

public class ZeroSupplier extends ConstSupplier {

	public static final ZeroSupplier INSTANCE = new ZeroSupplier();

	public ZeroSupplier() {
		super(0);
	}

	@Override
	public float supply(int x, int y) {
		return 0f;
	}
}
