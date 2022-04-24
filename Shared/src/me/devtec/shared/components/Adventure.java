package me.devtec.shared.components;

import java.util.List;

@SuppressWarnings("unchecked")
public interface Adventure<T> {
	public default T fromString(String string) {
		return toBaseComponent(ComponentAPI.fromString(string));
	}
	
	public T toBaseComponent(Component component);

	public T toBaseComponent(List<Component> components);
	
	public default T[] toBaseComponents(Component component) {
		return (T[])new Object[] {toBaseComponent(component)};
	}
	
	public default T[] toBaseComponents(List<Component> components) {
		return (T[])new Object[] {toBaseComponent(components)};
	}
}
