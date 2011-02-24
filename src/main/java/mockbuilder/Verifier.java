package mockbuilder;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;
import junit.framework.AssertionFailedError;

import org.mockito.Mockito;


/**
 * Verifier.
 *
 * @author Tomasz Kisiel
 */
@SuppressWarnings("rawtypes")
public class Verifier {

	public enum Verification {
		GETTERS, SETTERS;
	}

	private final Verification verification;


	private Verifier(final Verification verification) {
		this.verification = verification;
	}

	private void verify(final Object parentObj, final Element parentElem) throws Exception {
		for (Element childElem : parentElem.children) {
			String desc = parentElem.name + "." + childElem.name;
			Class<?> type = parentObj.getClass();
			Object childObj = resolver(type).resolve(parentObj, childElem);
			if (childElem.children.isEmpty()) {
				switch (this.verification) {
				case GETTERS:
					if (childObj == null) {
						throw new IllegalStateException(desc + " is null.");
					}
					Object getterValue = Utils.createValue(childElem.value, childObj.getClass());
					Assert.assertEquals(getterValue, childObj);
					break;
				case SETTERS:
					Method setter = Utils.setter(type, childElem.name);
					Object setterValue = Utils.createValue(childElem.value, setter.getParameterTypes()[0]);
					try {
						setter.invoke(Mockito.verify(parentObj), setterValue);
					} catch (InvocationTargetException e) {
						throw new AssertionFailedError(e.getCause().getMessage());
					}
					break;
				default:
					throw new IllegalArgumentException();
				}
			} else {
				verify(childObj, childElem);
			}
		}
	}

	private ChildResolver resolver(final Class<?> type) {
		if (type == null) throw new IllegalArgumentException();
		return type.isArray() ? new ArrayResolver()
			: List.class.isAssignableFrom(type) ? new ListResolver()
			: Map.class.isAssignableFrom(type) ? new MapResolver()
			: new PlainResolver();
	}

	/**
	 * ChildResolver.
	 */
	interface ChildResolver {
		Object resolve(Object rootObj, Element childElem) throws Exception;
	}

	/**
	 * ArrayResolver.
	 */
	private class ArrayResolver implements ChildResolver {
		@Override
		public Object resolve(final Object rootObj, final Element childElem) throws Exception {
			return ((Object[]) rootObj)[Integer.parseInt(childElem.index)];
		}
	}

	/**
	 * ListResolver.
	 */
	private class ListResolver implements ChildResolver {
		@Override
		public Object resolve(final Object rootObj, final Element childElem) throws Exception {
			return ((List) rootObj).get(Integer.parseInt(childElem.index));
		}
	}

	/**
	 * MapResolver.
	 */
	private class MapResolver implements ChildResolver {
		@Override
		public Object resolve(final Object rootObj, final Element childElem) throws Exception {
			return ((Map) rootObj).get(childElem.index);
		}
	}

	/**
	 * PlainResolver.
	 */
	private class PlainResolver implements ChildResolver {
		@Override
		public Object resolve(final Object root, final Element child) throws Exception {
			try {
				return Utils.getter(root.getClass(), child.name).invoke(root);
			} catch (NoSuchMethodException e) {
				throw new AssertionFailedError("No such method: " + child.key);
			}
		}
	}

	/**
	 * @param verification verification type.
	 * @param rootObj root object.
	 * @param expecteds expecteds.
	 * @throws Exception .
	 */
	public static void verify(final Verification verification, final Object rootObj, final String[] expecteds)
	throws Exception {
		new Verifier(verification).verify(rootObj, Parser.parse(rootObj.getClass(), expecteds));
	}

}
