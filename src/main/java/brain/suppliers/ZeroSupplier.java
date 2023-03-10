package brain.suppliers;

public class ZeroSupplier extends ConstSupplier {

	public static final ZeroSupplier INSTANCE = new ZeroSupplier();

	public ZeroSupplier() {
		super(0);
	}

}
