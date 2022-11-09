import activation.ActivationFunction;
import activation.Functions;
import suppliers.RandomRangeSupplier;
import suppliers.ValueSupplier;
import suppliers.ZeroSupplier;

import java.util.Random;

public class BrainBuilder {

	private final Random random = new Random(System.currentTimeMillis());

	private int[] structure;
	private ActivationFunction[] func_structure;
	private ValueSupplier[] init_structure;
	private ValueSupplier[] bias_structure;

	private ActivationFunction function = Functions.LeReLu;
	private ValueSupplier initialSupply = ZeroSupplier.INSTANCE;
	private ValueSupplier mutateSupply = RandomRangeSupplier.INSTANCE;

	private BrainBuilder() {
		structure = new int[0];
		func_structure = new ActivationFunction[0];
		init_structure = new ValueSupplier[0];
		bias_structure = new ValueSupplier[0];
	}

	public static BrainBuilder builder() {
		return new BrainBuilder();
	}

	public BrainBuilder addLayer(int count) {
		return addLayer(count, null, null, null);
	}

	public BrainBuilder addLayer(int count, ActivationFunction function) {
		return addLayer(count, function, null, null);
	}

	public BrainBuilder addLayerBias(int count, ValueSupplier biasSupplier) {
		return addLayer(count, null, null, biasSupplier);
	}

	public BrainBuilder addLayerWeight(int count, ValueSupplier supplier) {
		return addLayer(count, null, supplier, null);
	}

	public BrainBuilder addLayer(int count, ValueSupplier supplier, ValueSupplier biasSupplier) {
		return addLayer(count, null, supplier, biasSupplier);
	}

	/**
	 * @param count size of layer
	 * @param function activation function for that specific layer, may be null, will take general function
	 * @param supplier weight value source on creation, may be null, will take general supply
	 * @param biasSupplier bias source on creation, may be null, then the array wont initialise
	 * @return this builder
	 */
	public BrainBuilder addLayer(int count, ActivationFunction function, ValueSupplier supplier, ValueSupplier biasSupplier) {
		int newSize = structure.length + 1;

		if (count <= 0) {
			throw new IllegalStateException("Layer count cannot be zero or less");
		}

		// STRUCTURE
		int[] oldArray = structure;
		structure = new int[newSize];
		System.arraycopy(oldArray, 0, structure, 0, oldArray.length);
		structure[newSize - 1] = count;

		// FUNCTIONS
		ActivationFunction[] oldFuncArray = func_structure;
		func_structure = new ActivationFunction[newSize];
		System.arraycopy(oldFuncArray, 0, func_structure, 0, oldFuncArray.length);
		func_structure[newSize - 1] = function == null ? this.function : function;

		// INIT
		ValueSupplier[] oldInitArray = init_structure;
		init_structure = new ValueSupplier[newSize];
		System.arraycopy(oldInitArray, 0, init_structure, 0, oldInitArray.length);
		init_structure[newSize - 1] = supplier == null ? this.initialSupply : supplier;

		// BIASES
		ValueSupplier[] oldBiasArray = bias_structure;
		bias_structure = new ValueSupplier[newSize];
		System.arraycopy(oldBiasArray, 0, bias_structure, 0, oldBiasArray.length);
		bias_structure[newSize - 1] = biasSupplier;

		return this;
	}

	private BrainBuilder setStructure(int[] structure) {
		this.structure = structure;
		return this;
	}

	private BrainBuilder setFuncStructure(ActivationFunction[] structure) {
		this.func_structure = structure;
		if (func_structure.length != this.structure.length) {
			throw new IllegalStateException("Mismatch in func structure");
		}
		return this;
	}

	private BrainBuilder setInitStructure(ValueSupplier[] structure) {
		this.init_structure = structure;
		if (init_structure.length != this.structure.length) {
			throw new IllegalStateException("Mismatch in init structure");
		}
		return this;
	}

	private BrainBuilder setBiasStructure(ValueSupplier[] structure) {
		this.bias_structure = structure;
		if (bias_structure.length != this.structure.length) {
			throw new IllegalStateException("Mismatch in bias structure");
		}
		return this;
	}

	public BrainBuilder setFunction(ActivationFunction function) {
		this.function = function;
		return this;
	}

	/**
	 * @param initialSupply When appending the layer, if supply is not used - this one will be used
	 * @return this builder
	 */
	public BrainBuilder setInitialSupply(ValueSupplier initialSupply) {
		this.initialSupply = initialSupply;
		return this;
	}

	/**
	 * @param mutateSupply When mutation the brain values from this one source will be used
	 * @return this builder
	 */
	public BrainBuilder setMutationSupply(ValueSupplier mutateSupply) {
		this.mutateSupply = mutateSupply;
		return this;
	}

	public BrainBuilder branchDestination(Brain destination) {
		return new DestinationBuilder(destination)
				.setInitialSupply(this.initialSupply)
				.setMutationSupply(this.mutateSupply)
				.setFunction(this.function)
				.setStructure(this.structure)
				.setFuncStructure(this.func_structure)
				.setInitStructure(this.init_structure)
				.setBiasStructure(this.bias_structure);
	}

	public Brain build() {
		return build(ZeroSupplier.INSTANCE); // to avoid iterating on random sources
	}

	private Brain build(ValueSupplier init) {
		ValueSupplier initial = init == null ? this.initialSupply : init;
		Brain brain = new Brain(this.function, initial);
		for (int i = 0; i < structure.length; i++) {
			int count = structure[i];
			ValueSupplier initSupplier = init_structure[i];
			ValueSupplier biasSupplier = bias_structure[i];
			ActivationFunction function = func_structure[i];
			brain.append(count, function, initSupplier, biasSupplier);
		}

		return brain;
	}

	protected Brain createNewOrFromExtension() {
		return build();
	}

	public Brain produceChildUnsafe(Brain parent1, Brain parent2) {
		Brain child = createNewOrFromExtension();
		child.setFunction(parent2.getFunction());

		for (int i = 0; i < child.getWeightLayerCount(); i++) {
			BrainLayer layer = child.getWeightLayer(i);
			BrainLayer parentLayer1 = parent1.getWeightLayer(i);
			BrainLayer parentLayer2 = parent2.getWeightLayer(i);
			for (int x = 0; x < layer.getWidth(); x++) {
				for (int y = 0; y < layer.getHeight(); y++) {
					if (random.nextBoolean()) {
						layer.values[x][y] = parentLayer2.values[x][y];
					}
					layer.values[x][y] = parentLayer1.values[x][y];
				}
			}
		}

		for (int i = 0; i < child.getLayerCount(); i++) {
			BrainLayer layer = child.getLayer(i);
			BrainLayer parentLayer1 = parent1.getLayer(i);
			BrainLayer parentLayer2 = parent2.getLayer(i);

			if (parentLayer1.isBiased() && parentLayer2.isBiased()) {
				layer.createBiasZeroed();
				for (int x = 0; x < layer.getWidth(); x++) {
					for (int y = 0; y < layer.getHeight(); y++) {
						if (random.nextBoolean()) {
							layer.bias[x][y] = parentLayer2.bias[x][y];
						}
						layer.bias[x][y] = parentLayer1.bias[x][y];
					}
				}
			} else {
				BrainLayer oneBiased = parentLayer1.isBiased() ? parentLayer1 : (parentLayer2.isBiased() ? parentLayer2 : null);

				if (oneBiased == null) {
					continue;
				}
				for (int x = 0; x < layer.getWidth(); x++) {
					for (int y = 0; y < layer.getHeight(); y++) {
						layer.bias[x][y] = oneBiased.bias[x][y];
					}
				}
			}
		}

		return child;
	}

	public Brain produceChildUnsafe(Brain parent1, Brain parent2, int mutationPercent, int mutationDivider) {
		Brain child = produceChildUnsafe(parent1, parent2);
		return mutate(child, mutationPercent, mutationDivider);
	}

	public Brain produceChild(Brain parent1, Brain parent2) {
		//match signature
		int[] sign1 = parent1.getStructure();
		int[] sign2 = parent2.getStructure();

		boolean success = false;
		if (sign1.length == sign2.length) {
			success = true;
			for (int i = 0; i < sign1.length; i++) {
				success = success && (sign1[i] == sign2[i]);
			}
		}
		if (!success) {
			throw new IllegalStateException("MISMATCH OF BRAIN SIGNATURES");
		}

		boolean thisSignatureMatch = false;
		if (structure.length == sign2.length) {
			thisSignatureMatch = true;
			for (int i = 0; i < structure.length; i++) {
				thisSignatureMatch = thisSignatureMatch && (structure[i] == sign2[i]);
			}
		}
		if (!thisSignatureMatch) {
			structure = new int[sign2.length];
			System.arraycopy(sign2, 0, structure, 0, sign2.length);
		}

		Brain child = createNewOrFromExtension();
		child.setFunction(parent2.getFunction());

		for (int i = 0; i < child.getWeightLayerCount(); i++) {
			BrainLayer layer = child.getWeightLayer(i);
			BrainLayer parentLayer1 = parent1.getWeightLayer(i);
			BrainLayer parentLayer2 = parent2.getWeightLayer(i);
			for (int x = 0; x < layer.getWidth(); x++) {
				for (int y = 0; y < layer.getHeight(); y++) {
					if (random.nextBoolean()) {
						layer.values[x][y] = parentLayer2.values[x][y];
					}
					layer.values[x][y] = parentLayer1.values[x][y];
				}
			}
		}

		for (int i = 0; i < child.getLayerCount(); i++) {
			BrainLayer layer = child.getLayer(i);
			BrainLayer parentLayer1 = parent1.getLayer(i);
			BrainLayer parentLayer2 = parent2.getLayer(i);

			if (parentLayer1.isBiased() && parentLayer2.isBiased()) {
				layer.createBiasZeroed();
				for (int x = 0; x < layer.getWidth(); x++) {
					for (int y = 0; y < layer.getHeight(); y++) {
						if (random.nextBoolean()) {
							layer.bias[x][y] = parentLayer2.bias[x][y];
						}
						layer.bias[x][y] = parentLayer1.bias[x][y];
					}
				}
			} else {
				BrainLayer oneBiased = parentLayer1.isBiased() ? parentLayer1 : (parentLayer2.isBiased() ? parentLayer2 : null);

				if (oneBiased == null) {
					continue;
				}
				for (int x = 0; x < layer.getWidth(); x++) {
					for (int y = 0; y < layer.getHeight(); y++) {
						layer.bias[x][y] = oneBiased.bias[x][y];
					}
				}
			}
		}

		return child;
	}

	public Brain produceChild(Brain parent1, Brain parent2, int mutationPercent, int mutationDivider) {
		Brain child = produceChild(parent1, parent2);
		return mutate(child, mutationPercent, mutationDivider);
	}

	public Brain copy(Brain parent1) {
		int[] sign1 = parent1.getStructure();

		boolean thisSignatureMatch = false;
		if (structure.length == sign1.length) {
			thisSignatureMatch = true;
			for (int i = 0; i < structure.length; i++) {
				thisSignatureMatch = thisSignatureMatch && (structure[i] == sign1[i]);
			}
		}
		if (!thisSignatureMatch) {
			structure = new int[sign1.length];
			System.arraycopy(sign1, 0, structure, 0, sign1.length);
		}

		Brain child = createNewOrFromExtension();
		child.setFunction(parent1.getFunction());

		for (int i = 0; i < child.getWeightLayerCount(); i++) {
			BrainLayer layer = child.getWeightLayer(i);
			BrainLayer parentLayer1 = parent1.getWeightLayer(i);

			for (int x = 0; x < layer.getWidth(); x++) {
				for (int y = 0; y < layer.getHeight(); y++) {
					layer.values[x][y] = parentLayer1.values[x][y];
				}
			}
		}

		for (int i = 0; i < child.getLayerCount(); i++) {
			BrainLayer parentLayer1 = parent1.getLayer(i);
			if (!parentLayer1.isBiased()) {
				continue;
			}
			BrainLayer layer = child.getLayer(i);

			for (int x = 0; x < layer.getWidth(); x++) {
				for (int y = 0; y < layer.getHeight(); y++) {
					layer.bias[x][y] = parentLayer1.bias[x][y];
				}
			}
		}

		return child;
	}

	public Brain copy(Brain parent1, int mutationPercent, int mutationDivider) {
		Brain child = copy(parent1);
		mutate(child, mutationPercent, mutationDivider);
		return child;
	}

	public Brain mutate(Brain brain, int mutationPercent, int mutationDivider) {
		if (mutationPercent == 0) return brain;
		if (mutationDivider == 0) {
			mutationDivider = 1;
		}

		for (int i = 0; i < brain.getWeightLayerCount(); i++) {
			BrainLayer layer = brain.getWeightLayer(i);
			int count = (layer.getNodeCount() * mutationPercent / Math.max(mutationDivider, 1)) / 100;
			count = Math.max(count, 1);

			for (int j = 0; j < count; j++) {
				int x = random.nextInt(layer.getWidth());
				int y = random.nextInt(layer.getHeight());
				layer.values[x][y] = mutateSupply.supply(x, y);
			}
		}

		for (int i = 0; i < brain.getLayerCount(); i++) {
			BrainLayer layer = brain.getLayer(i);
			if (!layer.isBiased()) {
				continue;
			}

			int count = (layer.getNodeCount() * mutationPercent / mutationDivider) / 100;
			count = Math.max(count, 1);

			for (int j = 0; j < count; j++) {
				int x = random.nextInt(layer.getWidth());
				int y = random.nextInt(layer.getHeight());
				layer.bias[x][y] = mutateSupply.supply(x, y);
			}
		}
		return brain;
	}

	private static class DestinationBuilder extends BrainBuilder {
		private final Brain destination;

		DestinationBuilder(Brain destination) {
			this.destination = destination;
		}

		@Override
		protected Brain createNewOrFromExtension() {
			return destination;
		}
	}

}
