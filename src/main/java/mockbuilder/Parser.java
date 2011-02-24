package mockbuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * MockBuilder Parser.
 *
 * @author Tomasz Kisiel
 */
class Parser {

	private static Logger LOG = Logger.getLogger(Parser.class.getPackage().getName());

	private final Map<String, List<Element>> elements = new HashMap<String, List<Element>>();


	/**
	 * @param type root object type.
	 * @param settings settings.
	 * @param extraInterfaces additional interfaces for root object.
	 * @throws Exception .
	 */
	Parser(final Class<?> type, final String[] settings, final Class<?>... extraInterfaces) throws Exception {
		for (String path : settings) {
			parse(type, path, extraInterfaces);
		}
	}

	/**
	 * @param type root object type.
	 * @param path path.
	 * @param extraInterfaces additional interfaces for root object.
	 * @throws Exception .
	 */
	private void parse(final Class<?> type, final String path, final Class<?>... extraInterfaces)
	throws Exception {
		String rootName = Utils.strip$$(type.getSimpleName());
		Element root;
		if (!this.elements.containsKey(rootName)) {
			root = new Element(rootName, "");
			root.type = type;
			root.extraInterfaces = extraInterfaces;
			this.elements.put(rootName, Arrays.asList(root));
		}
		root = this.elements.get(rootName).get(0);

		String[] pathAndValue = path.split(" ?= ?");
		String p = cleanPath(pathAndValue[0]);

		Element current = root;
		for (Iterator<String> it = Arrays.asList(p.split("-")).iterator(); it.hasNext();) {
			String token = it.next();
			String key = Utils.join(current.key, token);
			if (!it.hasNext()) {
				String value = pathAndValue.length > 1 ? pathAndValue[1] : null;
				if ("*".equals(value)) {
					if (findElement(key) != null) {
						invalidateElements(key);
					}
					Element elem = newElementTree(token, current, true);
					elem.value = value;
					current = elem;
				} else {
					Element valueElem = new Element(token, current.key);
					valueElem.value = value;
					current.children.add(valueElem);
				}
			} else {
				current = newElementTree(token, current);
			}
		}
	}

	private Element newElementTree(final String token, final Element current) {
		return newElementTree(token, current, false);
	}

	private Element newElementTree(String token, Element current, final boolean isLast) {
		String key = Utils.join(current.key, token);
		String[] nameAndIndex = token.split("[\\[\\]]");
		Element elem = findElement(key);
		LOG.fine("Looking for " + key);
		if (elem != null && (!isLast || nameAndIndex.length == 1)) {
			LOG.fine("Exists " + key + "; " + elem);
			return elem;
		}
		LOG.fine("New " + key);
		elem = new Element(token, current.key);
		if (nameAndIndex.length > 1) {
			String akey = Utils.join(current.key, nameAndIndex[0]);
			Element aelem = findElement(akey);
			if (aelem == null) {
				LOG.fine("New array " + Utils.join(current.key, nameAndIndex[0]));
				aelem = new Element(nameAndIndex[0], current.key);
				aelem.isArray = true;
				current.children.add(aelem);
				putElement(akey, aelem);
			}
			elem.index = nameAndIndex[1];
			current = aelem;
			if (findElement(key) != null && isLast) {
				invalidateElements(key);
			}
		}
		token = token.replaceAll("\\[([^\\]]+)<([^>]+)>\\]", "[$1|$2|]");
		String[] nameAndHint = token.split("[<>]");
		if (nameAndHint.length > 1) {
			elem.hint = nameAndHint[1];
		}
		current.children.add(elem);
		putElement(key, elem);
		return elem;
	}

	private Element findElement(final String key) {
		List<Element> elems = this.elements.get(key);
		return elems == null ? null : elems.get(0);
	}

	private void putElement(final String key, final Element elem) {
		List<Element> elems = Utils.getList(key, this.elements);
		if (elems.size() > 0 && elems.get(0) == null) {
			elems.set(0, elem);
		} else {
			elems.add(0, elem);
		}
	}

	private void invalidateElements(final String key) {
		Pattern p = Pattern.compile("^" + key.replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]"));
		for (Entry<String, List<Element>> e : this.elements.entrySet()) {
			if (p.matcher(e.getKey()).find()) {
				LOG.fine("Invalidating " + e.getKey());
				e.getValue().add(0, null);
			}
		}
	}

	/**
	 * @param path path.
	 * @return path with '.' in elements' delimiters replaced with '-'.
	 * 		<br/>'.' in hints remain untouched.
	 */
	private String cleanPath(final String path) {
		int prev = 0;
		String cleaned = "";
		Matcher mm = Pattern.compile("<[^>]+>").matcher(path);
		while (mm.find()) {
			cleaned += replace(path.substring(prev, mm.start()));
			cleaned += path.substring(mm.start(), mm.end());
			prev = mm.end();
		}
		return prev == 0 ? replace(path) : cleaned + replace(path.substring(prev));
	}

	/**
	 * @param s string.
	 * @return string with all '.' replaced with '-'.
	 */
	private String replace(final String s) {
		return s.replace('.', '-');
	}

	/**
	 * @return root element.
	 */
	Element root() {
		List<String> keys = new ArrayList<String>(this.elements.keySet());
		Collections.sort(keys, new Comparator<String>() {
			@Override
			public int compare(final String o1, final String o2) {
				return o1.length() - o2.length();
			}
		});
		return this.elements.get(keys.get(0)).get(0);
	}

	/**
	 * @param type root object type.
	 * @param settings settings.
	 * @param extraInterfaces additional interfaces for root object.
	 * @return root element.
	 * @throws Exception .
	 */
	static Element parse(final Class<?> type, final String[] settings, final Class<?>... extraInterfaces)
	throws Exception {
		return new Parser(type, settings, extraInterfaces).root();
	}

}
