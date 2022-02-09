package io.github.bloepiloepi.bite.runtime.object;

import java.util.List;

public class ListItem extends BiteObject<Object> implements Assignable {
	private final List<BiteObject<?>> list;
	private final int index;
	
	public ListItem(List<BiteObject<?>> list, int index) {
		super(list.get(index).getValue());
		this.list = list;
		this.index = index;
	}
	
	@Override
	public void storeValue(BiteObject<?> value) {
		list.set(index, value);
	}
}
