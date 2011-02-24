package mockbuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Element.
 *
 * @author Tomasz Kisiel
 */
final class Element {

	final String name;

	final String key;

	final List<Element> children = new ArrayList<Element>();

	Class<?> type;

	String index;

	String hint;

	boolean isArray;

	String value;

	Class<?>[] extraInterfaces;


	/**
	 * @param name element name.
	 * @param upstreamKey key combining all parent elements.
	 */
	Element(final String name, final String upstreamKey) {
		this.name = Utils.strip$$(name);
		this.key = Utils.join(upstreamKey, name);
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || !(obj instanceof Element)) {
			return false;
		}
		return this.name.equals(((Element) obj).name);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "E["
			+ "name=" + this.name + ", "
			+ "key=" + this.key
			+ (this.type != null ? ", type=" + this.type : "")
			+ (this.index != null ? ", index=" + this.index : "")
			+ (this.hint != null ? ", hint=" + this.hint : "")
			+ "]";
	}

}
