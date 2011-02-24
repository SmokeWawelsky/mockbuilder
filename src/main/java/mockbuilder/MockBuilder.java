package mockbuilder;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.mockito.Mockito;
import org.mockito.stubbing.OngoingStubbing;

/**
 * MockBuilder.
 *
 * @author Tomasz Kisiel
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class MockBuilder {

	private final String[] commonPackages;

	private Object root;


	private MockBuilder(final Element rootElement, final String[] commonPackages) throws Exception {
		this.commonPackages = Utils.join(Utils.COMMON_PACKAGES, commonPackages);
		this.root = buildTree(rootElement);
	}

	private ElementBuilder builder(final Class<?> type) {
		if (type == null) throw new IllegalArgumentException();
		return type.isArray() ? new ArrayBuilder()
			: List.class.isAssignableFrom(type) ? new ListBuilder()
			: Map.class.isAssignableFrom(type) ? new MapBuilder()
			: new PlainBuilder();
	}

	private Object buildTree(final Element element) throws Exception {
		return builder(element.type).build(element);
	}

	/**
	 * ElementBuilder.
	 */
	interface ElementBuilder {
		Object build(Element element) throws Exception;
	}

	/**
	 * AbstractElementBuiler.
	 */
	private abstract class AbstractElementBuiler implements ElementBuilder {
		Object find(final Element element) throws Exception {
			return element.children.isEmpty()
				? Utils.createValue(element.value, element.type, MockBuilder.this.commonPackages, element.extraInterfaces)
				: Utils.create(element.type, element.extraInterfaces);
		}
	}

	/**
	 * PlainBuilder.
	 */
	private class PlainBuilder extends AbstractElementBuiler {
		@Override
		public Object build(final Element element) throws Exception {
			Object obj = find(element);
			List<List<Element>> calls = sortCalls(element.children);
			for (List<Element> elems : calls) {
				Iterator<Element> it = elems.iterator();
				Element child = it.next();
				Method getter = Utils.getter(element.type, child.name);
				Class<?> getterType = getter.getReturnType();
				Object childObj = getObject(child, getterType);
				OngoingStubbing<Object> stubb =
					Mockito.when(getter.invoke(obj)).thenReturn(childObj);
				while (it.hasNext()) {
					stubb.thenReturn(getObject(it.next(), getterType));
				}
			}
			return obj;
		}
		private Object getObject(final Element elem, final Class<?> getterType)
		throws Exception {
			elem.type = elem.hint == null ? getterType
				: Utils.asType(elem.hint, MockBuilder.this.commonPackages);
			return buildTree(elem);
		}
		private List<List<Element>> sortCalls(final List<Element> elements) {
			Map<String, List<Element>> map = new LinkedHashMap<String, List<Element>>();
			for (Element elem : elements) {
				Utils.getList(elem.name, map).add(elem);
			}
			return new ArrayList<List<Element>>(map.values());
		}
	}

	/**
	 * ArrayBuilder.
	 */
	private class ArrayBuilder extends AbstractElementBuiler {
		@Override
		public Object build(final Element element) throws Exception {
			Object[] array = (Object[]) find(element);
			for (Element child : element.children) {
				child.type = child.hint != null
					? Utils.asType(child.hint, MockBuilder.this.commonPackages)
					: element.type.getComponentType();
				Object childObj = buildTree(child);
				array[Integer.parseInt(child.index)] = childObj;
			}
			return array;
		}
	}

	/**
	 * ListBuilder.
	 */
	private class ListBuilder extends AbstractElementBuiler {
		@Override
		public Object build(final Element element) throws Exception {
			List list = (List) find(element);
			for (Element child : element.children) {
				if (child.hint == null || child.hint.isEmpty()) {
					throw new IllegalArgumentException("List elements should have hint.");
				}
				child.type = Utils.asType(child.hint, MockBuilder.this.commonPackages);
				Object childObj = buildTree(child);
				set(list, Integer.parseInt(child.index), childObj);
			}
			return list;
		}
		private void set(final List list, final int index, final Object obj) {
			int diff = 1 + index - list.size();
			if (diff > 0) {
				for (int i = 0; i < diff; i++) {
					list.add(null);
				}
			}
			list.set(index, obj);
		}
	}

	/**
	 * MapBuilder.
	 */
	private class MapBuilder extends AbstractElementBuiler {
		@Override
		public Object build(final Element element) throws Exception {
			Map map = (Map) find(element);
			for (Element child : element.children) {
				if (child.hint == null || child.hint.isEmpty()) {
					throw new IllegalArgumentException("Map elements should have hint.");
				}
				child.type = Utils.asType(child.hint, MockBuilder.this.commonPackages);
				Object childObj = buildTree(child);
				Object key;
				String[] tokens = child.index.split("[<>]");
				if (tokens.length > 1) {
					Class<?> keyType = Utils.asType(tokens[1], MockBuilder.this.commonPackages);
					key = Utils.createValue(tokens[0], keyType);
				} else {
					key = tokens[0];
				}
				map.put(key, childObj);
			}
			return map;
		}
	}

	/**
	 * @param <T>
	 * @param type
	 * @param settings
	 * @param commonPackages
	 * @param extraInterfaces
	 * @return
	 * @throws Exception
	 */
	public static <T> T build(final Class<T> type, final String[] settings,
			final String[] commonPackages, final Class<?>... extraInterfaces) throws Exception {
		return (T) new MockBuilder(Parser.parse(type, settings, extraInterfaces), commonPackages).root;
	}

	/**
	 * @param <T>
	 * @param type
	 * @param settings
	 * @param extraInterfaces
	 * @return
	 * @throws Exception
	 */
	public static <T> T build(final Class<T> type, final String[] settings, final Class<?>... extraInterfaces)
	throws Exception {
		return (T) new MockBuilder(Parser.parse(type, settings, extraInterfaces), new String[0]).root;
	}

}
