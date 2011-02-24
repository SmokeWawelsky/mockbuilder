package mockbuilder;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.fest.assertions.Assertions;
import org.junit.Test;

/**
 * @author Tomasz Kisiel
 */
public class ParserTest {

	@Test
	public void testSimpleParse() throws Exception {
		// When
		Element elem = Parser.parse(A.class, new String[] {
			"b.c.double = 1.5"
		});

		// Then
		Assertions.assertThat(elem.name).isEqualTo("A");
		Assertions.assertThat(getValues(elem.children, "key"))
			.containsExactly("A.b");
		Assertions.assertThat(getValues(elem.children.get(0).children, "key"))
			.containsExactly("A.b.c");
		Assertions.assertThat(getValues(elem.children.get(0).children.get(0).children, "key"))
			.containsExactly("A.b.c.double");
		Assertions.assertThat(getValues(elem.children.get(0).children.get(0).children, "value"))
			.containsExactly("1.5");
	}

	@Test
	public void testParse() throws Exception {
		// When
		Element elem = Parser.parse(A.class, new String[] {
			"ats[0]<ArrayType>.validContext.mandantId<Integer> = 123"
		});

		// Then
		Assertions.assertThat(elem.name).isEqualTo("A");
		Assertions.assertThat(getValues(elem.children, "key"))
			.containsExactly("A.ats");
		Assertions.assertThat(getValues(getGrandChildren(elem, 0), "key"))
			.containsExactly("A.ats[0]<ArrayType>");
		Assertions.assertThat(getValues(getGrandChildren(elem, 0, 0), "key"))
			.containsExactly("A.ats[0]<ArrayType>.validContext");
		Assertions.assertThat(getValues(getGrandChildren(elem, 0, 0, 0), "key"))
			.containsExactly("A.ats[0]<ArrayType>.validContext.mandantId<Integer>");
		Assertions.assertThat(getValues(getGrandChildren(elem, 0, 0, 0), "value"))
			.containsExactly("123");
	}

	@Test
	public void testParseMap() throws Exception {
		// When
		Element elem = Parser.parse(A.class, new String[] {
			"map[KEY]<C>.mandantId<Integer> = 123"
		});

		// Then
		Assertions.assertThat(elem.name).isEqualTo("A");
		Assertions.assertThat(getValues(elem.children, "key"))
			.containsExactly("A.map");
		Assertions.assertThat(getValues(getGrandChildren(elem, 0), "key"))
			.containsExactly("A.map[KEY]<C>");
		Assertions.assertThat(getValues(getGrandChildren(elem, 0, 0), "key"))
			.containsExactly("A.map[KEY]<C>.mandantId<Integer>");
		Assertions.assertThat(getValues(getGrandChildren(elem, 0, 0), "value"))
			.containsExactly("123");
	}

	@Test
	public void testParseMapKeyHint() throws Exception {
		// When
		Element elem = Parser.parse(A.class, new String[] {
			"map[6<Long>]<C>.mandantId<Integer> = 123"
		});

		// Then
		Assertions.assertThat(elem.name).isEqualTo("A");
		Assertions.assertThat(getValues(elem.children, "key"))
			.containsExactly("A.map");
		Assertions.assertThat(getValues(getGrandChildren(elem, 0), "key"))
			.containsExactly("A.map[6<Long>]<C>");
		Assertions.assertThat(getValues(getGrandChildren(elem, 0, 0), "key"))
			.containsExactly("A.map[6<Long>]<C>.mandantId<Integer>");
		Assertions.assertThat(getValues(getGrandChildren(elem, 0, 0), "value"))
			.containsExactly("123");
	}

	@Test
	public void testParseArray() throws Exception {
		// When
		Element elem = Parser.parse(A.class, new String[] {
			"arr[0].int = 123",
			"arr[0].int = 222",
			"arr[1].int = 321",
		});

		// Then
		Assertions.assertThat(elem.name).isEqualTo("A");
		Assertions.assertThat(getValues(elem.children, "key"))
			.containsExactly("A.arr");
		Assertions.assertThat(getValues(getGrandChildren(elem, 0), "key"))
			.containsExactly("A.arr[0]", "A.arr[1]");
		Assertions.assertThat(getValues(getGrandChildren(elem, 0), "index"))
			.containsExactly("0", "1");
		Assertions.assertThat(getValues(getGrandChildren(elem, 0, 0), "key"))
			.containsExactly("A.arr[0].int", "A.arr[0].int");
		Assertions.assertThat(getValues(getGrandChildren(elem, 0, 0), "value"))
			.containsExactly("123", "222");
		Assertions.assertThat(getValues(getGrandChildren(elem, 0, 1), "key"))
			.containsExactly("A.arr[1].int");
		Assertions.assertThat(getValues(getGrandChildren(elem, 0, 1), "value"))
			.containsExactly("321");
	}

	@Test
	public void testParseArrayDeep() throws Exception {
		// When
		Element elem = Parser.parse(A.class, new String[] {
			"ats[0].componentType.kind = INT",
			"ats[1].componentType.kind = LONG",
			"ats[1].componentType.kind = DOUBLE",
			"ats[2].componentType.kind = BYTE",
			"ats[2].componentType = *",
			"ats[2] = *",
		});

		// Then
		Assertions.assertThat(elem.name).isEqualTo("A");
		Assertions.assertThat(getValues(elem.children, "key"))
			.containsExactly("A.ats");
		Assertions.assertThat(getValues(getGrandChildren(elem, 0), "key"))
			.containsExactly("A.ats[0]", "A.ats[1]", "A.ats[2]", "A.ats[2]");
		Assertions.assertThat(getValues(getGrandChildren(elem, 0), "index"))
			.containsExactly("0", "1", "2", "2");
		Assertions.assertThat(getValues(getGrandChildren(elem, 0, 0), "key"))
			.containsExactly("A.ats[0].componentType");
		Assertions.assertThat(getValues(getGrandChildren(elem, 0, 0, 0), "key"))
			.containsExactly("A.ats[0].componentType.kind");
		Assertions.assertThat(getValues(getGrandChildren(elem, 0, 0, 0), "value"))
			.containsExactly("INT");
		Assertions.assertThat(getValues(getGrandChildren(elem, 0, 1), "key"))
			.containsExactly("A.ats[1].componentType");
		Assertions.assertThat(getValues(getGrandChildren(elem, 0, 1, 0), "key"))
			.containsExactly("A.ats[1].componentType.kind", "A.ats[1].componentType.kind");
		Assertions.assertThat(getValues(getGrandChildren(elem, 0, 1, 0), "value"))
			.containsExactly("LONG", "DOUBLE");
		Assertions.assertThat(getValues(getGrandChildren(elem, 0, 2), "key"))
			.containsExactly("A.ats[2].componentType", "A.ats[2].componentType");
		Assertions.assertThat(getValues(getGrandChildren(elem, 0, 2, 0), "key"))
			.containsExactly("A.ats[2].componentType.kind");
		Assertions.assertThat(getValues(getGrandChildren(elem, 0, 2, 0), "value"))
			.containsExactly("BYTE");
		Assertions.assertThat(getGrandChildren(elem, 0, 2, 1)).isEmpty();
		Assertions.assertThat(getGrandChildren(elem, 0, 3)).isEmpty();
	}

	@Test
	public void testParseMultiplePaths() throws Exception {
		// Given
		Element elem = Parser.parse(A.class, new String[] {
			"b.c.byte = 11",
			"b.c.byteO = 12",
			"b.c.short = 111",
			"b.c.shortO = 112",
			"b.c.int = 1111",
			"b.c.intO = 1112",
			"b.c.long = 11111",
			"b.c.longO",
			"b.c.float = 2.2",
			"b.c.floatO",
			"b.c.double = 22.22",
			"b.c.doubleO",
			"b.c.char = X",
			"b.c.charO = Y",
			"b.c.string = yo",
			"b.cs[0] = *",
		});

		// Then
		Assertions.assertThat(elem.name).isEqualTo("A");
		Assertions.assertThat(getValues(elem.children, "key"))
			.containsExactly("A.b");
		Assertions.assertThat(getValues(getGrandChildren(elem, 0), "key"))
			.containsExactly("A.b.c", "A.b.cs");
		Assertions.assertThat(getValues(getGrandChildren(elem, 0, 0), "key"))
			.containsExactly("A.b.c.byte", "A.b.c.byteO", "A.b.c.short", "A.b.c.shortO",
					"A.b.c.int", "A.b.c.intO", "A.b.c.long", "A.b.c.longO",
					"A.b.c.float", "A.b.c.floatO", "A.b.c.double", "A.b.c.doubleO",
					"A.b.c.char", "A.b.c.charO", "A.b.c.string");
		Assertions.assertThat(getValues(getGrandChildren(elem, 0, 0), "value"))
			.containsExactly("11", "12", "111", "112",
					"1111", "1112", "11111", null,
					"2.2", null, "22.22", null,
					"X", "Y", "yo");
		Assertions.assertThat(getValues(getGrandChildren(elem, 0, 1), "key"))
			.containsExactly("A.b.cs[0]");
	}

	@Test
	public void testParseSimpleMultiplePathsWithReset() throws Exception {
		// Given
		Element elem = Parser.parse(A.class, new String[] {
			"b.c.int = 1",
			"b.c.int = 2",
			"b.c.int = 3",
		});

		// Then
		Assert.assertEquals("A", elem.name);
		Assertions.assertThat(getValues(elem.children, "key")).containsExactly("A.b");
		Assertions.assertThat(getValues(elem.children.get(0).children, "key"))
			.containsExactly("A.b.c");

		Element c1 = elem.children.get(0).children.get(0);
		Assertions.assertThat(getValues(c1.children, "key"))
			.containsExactly("A.b.c.int", "A.b.c.int", "A.b.c.int");
		Assertions.assertThat(getValues(c1.children, "value"))
			.containsExactly("1", "2", "3");
		Assertions.assertThat(c1.children.get(0).children).isEmpty();
		Assertions.assertThat(c1.children.get(1).children).isEmpty();
		Assertions.assertThat(c1.children.get(2).children).isEmpty();
	}

	@Test
	public void testParseMultiplePathsWithReset() throws Exception {
		// Given
		Element elem = Parser.parse(A.class, new String[] {
			"b.c = *",
			"b.c.int = 1",
			"b.c.int = 2",
			"b.c = *",
			"b.c.int = 3",
			"b.c.int = 3",
			"b.c.int = 4",
			"b.c = *",
			"b.c.int = 5",
			"b.c = *",
		});

		// Then
		Assert.assertEquals("A", elem.name);
		Assertions.assertThat(getValues(elem.children, "key")).containsExactly("A.b");
		Assertions.assertThat(getValues(elem.children.get(0).children, "key"))
			.containsExactly("A.b.c", "A.b.c", "A.b.c", "A.b.c");

		Element c1 = elem.children.get(0).children.get(0);
		Assertions.assertThat(getValues(c1.children, "key"))
			.containsExactly("A.b.c.int", "A.b.c.int");
		Assertions.assertThat(c1.children.get(0).children).isEmpty();
		Assertions.assertThat(c1.children.get(1).children).isEmpty();

		Element c2 = elem.children.get(0).children.get(1);
		Assertions.assertThat(getValues(c2.children, "key"))
			.containsExactly("A.b.c.int", "A.b.c.int", "A.b.c.int");
		Assertions.assertThat(c2.children.get(0).children).isEmpty();
		Assertions.assertThat(c2.children.get(1).children).isEmpty();
		Assertions.assertThat(c2.children.get(2).children).isEmpty();

		Element c3 = elem.children.get(0).children.get(2);
		Assertions.assertThat(getValues(c3.children, "key"))
			.containsExactly("A.b.c.int");
		Assertions.assertThat(c3.children.get(0).children).isEmpty();

		Element c4 = elem.children.get(0).children.get(3);
		Assertions.assertThat(c4.children).isEmpty();
	}

	List<String> getValues(final List<Element> elements, final String property) {
		List<String> values = new ArrayList<String>();
		for (Element elem : elements) {
			try {
				values.add((String) Element.class.getDeclaredField(property).get(elem));
			} catch (Exception e) {
				throw new IllegalArgumentException(property);
			}

		}
		return values;
	}

	List<Element> getGrandChildren(Element root, final int... levels) {
		List<Element> children = root.children.get(0).children;
		for (int i : levels) {
			root = root.children.get(i);
			children = root.children;
		}
		return children;
	}

}
