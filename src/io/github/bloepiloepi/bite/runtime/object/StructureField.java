package io.github.bloepiloepi.bite.runtime.object;

public class StructureField extends BiteObject<Object> implements Assignable {
	private final BiteStructure structure;
	private final String name;
	
	public StructureField(BiteStructure structure, String name) {
		super(structure.get(name).getValue());
		this.structure = structure;
		this.name = name;
	}
	
	@Override
	public void storeValue(BiteObject<?> value) {
		structure.store(name, value);
	}
}
