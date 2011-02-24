package mockbuilder;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mockito.MockSettings;
import org.mockito.Mockito;

/**
 * MockBuilder Utils.
 *
 * @author Tomasz Kisiel
 */
public class Utils {

	static final String[] COMMON_PACKAGES = new String[] { null, "java.lang", "java.util", "java.math" };

	static final int DEFAULT_ARRAY_SIZE = 10;


	/**
	 * @param type type.
	 * @param propertyName property name.
	 * @return getter.
	 * @throws Exception .
	 */
	static Method getter(final Class<?> type, final String propertyName) throws Exception {
		String name = propertyName.split("[<\\[]")[0];
		return type.getMethod("get" + name.substring(0, 1).toUpperCase() + name.substring(1));
	}

	/**
	 * @param type type.
	 * @param propertyName property name.
	 * @return setter.
	 * @throws Exception .
	 */
	static Method setter(final Class<?> type, final String propertyName) throws Exception {
		String name = propertyName.split("[<\\[]")[0];
		String setterName = "set" + name.substring(0, 1).toUpperCase() + name.substring(1);
		for (Method method : type.getMethods()) {
			if (setterName.equals(method.getName())) {
				return method;
			}
		}
		throw new NoSuchMethodException(setterName);
	}

	/**
	 * @param value value as string.
	 * @param type expected type.
	 * @return value converted to given type.
	 */
	static Object createPrimitiveValue(final String value, final Class<?> type) {
		if (value == null) {
			if (char.class.isAssignableFrom(type)) {
				return ' ';
			}
			throw new IllegalArgumentException("Primitive value cannot be null.");
		} else if (int.class.isAssignableFrom(type)) {
			return Integer.parseInt(value);
		} else if (char.class.isAssignableFrom(type)) {
			return value.charAt(0);
		} else if (long.class.isAssignableFrom(type)) {
			return Long.parseLong(value);
		} else if (short.class.isAssignableFrom(type)) {
			return Short.parseShort(value);
		} else if (byte.class.isAssignableFrom(type)) {
			return Byte.parseByte(value);
		} else if (double.class.isAssignableFrom(type)) {
			return Double.parseDouble(value);
		} else if (float.class.isAssignableFrom(type)) {
			return Float.parseFloat(value);
		} else {
			throw new UnsupportedOperationException("Unimplemented primitive creator for type " + type);
		}
	}

	/**
	 * @param value value as string.
	 * @param type expected type.
	 * @return value converted to given type.
	 * @throws Exception .
	 */
	static Object createValue(final String value, final Class<?> type, final Class<?>... extraInterfaces)
	throws Exception {
		return createValue(value, type, COMMON_PACKAGES, extraInterfaces);
	}

	/**
	 * @param value value as string.
	 * @param type expected type.
	 * @param commonPackages common packages.
	 * @return value converted to given type.
	 * @throws Exception .
	 */
	@SuppressWarnings({"rawtypes", "unchecked"})
	static Object createValue(final String value, final Class<?> type, final String[] commonPackages,
			final Class<?>... extraInterfaces) throws Exception {
		if (type == null) throw new IllegalArgumentException("Type cannot be null.");
		String theValue = "null".equals(value) ? null : value;
		Class<?> theType = type;
		String[] tokens = value == null ? new String[0] : value.split("[<>]");
		if (tokens.length > 1) {
			theType = asType(tokens[1], commonPackages);
			theValue = tokens[0];
		}
		// String
		if (String.class.isAssignableFrom(theType)) {
			return theValue;
		// primitives
		} else if (theType.isPrimitive()) {
			return createPrimitiveValue(theValue, theType);
		// null
		} else if (theValue == null) {
			return null;
		// numerics
		} else if (Integer.class.isAssignableFrom(theType)) {
			return Integer.parseInt(theValue);
		} else if (Character.class.isAssignableFrom(theType)) {
			return theValue.charAt(0);
		} else if (Long.class.isAssignableFrom(theType)) {
			return Long.parseLong(theValue);
		} else if (Double.class.isAssignableFrom(theType)) {
			return Double.parseDouble(theValue);
		} else if (Short.class.isAssignableFrom(theType)) {
			return Short.parseShort(theValue);
		} else if (Byte.class.isAssignableFrom(theType)) {
			return Byte.parseByte(theValue);
		} else if (Float.class.isAssignableFrom(theType)) {
			return Float.parseFloat(theValue);
		// enums
		} else if (theType.isEnum()) {
			return Enum.valueOf((Class<Enum>) theType, theValue);
		// Date
		} else if (Date.class.isAssignableFrom(theType)) {
			return new Date(Long.parseLong(theValue));
		} else {
			try {
				// constructor with String paramter
				return theType.getConstructor(String.class).newInstance(theValue);
			} catch (NoSuchMethodException e) {
				// collections or mock
				return create(theType, extraInterfaces);
			}
		}
	}

	@SuppressWarnings("rawtypes")
	static Object create(final Class<?> type, final Class<?>... extraInterfaces) throws Exception {
		if (type == null) throw new IllegalArgumentException("Type cannot be null.");
		if (type.isArray()) {
			return Array.newInstance(type.getComponentType(), DEFAULT_ARRAY_SIZE);
		} else if (List.class.isAssignableFrom(type)) {
			return new ArrayList();
		} else if (Map.class.isAssignableFrom(type)) {
			return new HashMap();
		} else {
			return mock(type, extraInterfaces);
		}
	}

	static Object mock(final Class<?> type, final Class<?>... extraInterfaces) {
		MockSettings settings = Mockito.withSettings().serializable();
		if (extraInterfaces != null && extraInterfaces.length > 0) {
			settings.extraInterfaces(extraInterfaces);
		}
		return Mockito.mock(type, settings);
	}

	/**
	 * @param str string.
	 * @param commonPackages common packages.
	 * @return type corresponding to given string.
	 * @throws Exception .
	 */
	static Class<?> asType(final String str, final String[] commonPackages) throws Exception {
		for (String pkg : commonPackages) {
			try {
				return findType(pkg, str);
			} catch (ClassNotFoundException e) {}
		}
		throw new IllegalArgumentException("Cannot find type " + str);
	}

	/**
	 * @param pkg package.
	 * @param str class name.
	 * @return class.
	 * @throws ClassNotFoundException .
	 */
	static Class<?> findType(final String pkg, final String str) throws ClassNotFoundException {
		return Class.forName(pkg == null ? str : join(pkg, str));
	}

	/**
	 * @param s string.
	 * @return .
	 */
	static String strip$$(final String s) {
		return s.replaceAll("\\$\\$.*$", "");
	}

	/**
	 * @param upstream
	 * @param name
	 * @return
	 */
	static String join(final String upstream, final String name) {
		return upstream.isEmpty() ? name : upstream + "." + name;
	}

	/**
	 * @param a1
	 * @param a2
	 * @return
	 */
	static String[] join(final String[] a1, final String[] a2) {
		int a1Size = a1.length;
		int a2Size = a2.length;
		String[] a = new String[a1Size + a2Size];
		System.arraycopy(a1, 0, a, 0, a1Size);
		System.arraycopy(a2, 0, a, a1Size, a2Size);
		return a;
	}

	static <K, V> List<V> getList(final K key, final Map<K, List<V>> map) {
		List<V> list = map.get(key);
		if (list == null) {
			list = new ArrayList<V>();
			map.put(key, list);
		}
		return list;
	}

}
