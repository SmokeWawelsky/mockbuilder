package mockbuilder;

import javax.lang.model.type.ArrayType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import junit.framework.Assert;

import org.junit.Test;

/**
 * @author Tomasz Kisiel
 */
public class MockBuilderTest {

	@Test
	public void testBuild() throws Exception {
		// When
		A mock = MockBuilder.<A>build(A.class, new String[] {
			"ats[0]<ArrayType>.componentType.kind = INT"
		}, new String[] {
			"javax.lang.model.type"
		});

		// Then
		Assert.assertEquals(TypeKind.INT, mock.getAts()[0].getComponentType().getKind());
	}

	@Test
	public void testBasicNesting() throws Exception {
		A a = MockBuilder.<A>build(A.class, new String[] {
			"b.c.int = 456",
			"b.c.string = 654",
			"b.cmap[KEY]<mockbuilder.C>.int = 555",
		});

		Assert.assertEquals(456, a.getB().getC().getInt());
		Assert.assertEquals("654", a.getB().getC().getString());
		Assert.assertEquals(555, a.getB().getCmap().get("KEY").getInt());
	}

	@Test
	public void testBasicTypes() throws Exception {
		A a = MockBuilder.<A>build(A.class, new String[] {
			"b.c.byte = 11",
			"b.c.byteO = 12",
			"b.c.short = 111",
			"b.c.shortO = ",
			"b.c.int = 1111",
			"b.c.intO = 1112",
			"b.c.long = 11111",
			"b.c.longO = null",
			"b.c.float = 2.2",
			"b.c.floatO = 2.3",
			"b.c.double = 22.22",
			"b.c.doubleO",
			"b.c.char = X",
			"b.c.charO = Y",
			"b.c.string = yo",
		});

		C c = a.getB().getC();
		Assert.assertEquals(11, c.getByte());
		Assert.assertEquals(12, c.getByteO().intValue());
		Assert.assertEquals(111, c.getShort());
		Assert.assertEquals(null, c.getShortO());
		Assert.assertEquals(1111, c.getInt());
		Assert.assertEquals(1112, c.getIntO().intValue());
		Assert.assertEquals(11111, c.getLong());
		Assert.assertEquals(null, c.getLongO());
		Assert.assertEquals(2.2f, c.getFloat());
		Assert.assertEquals(2.3f, c.getFloatO().floatValue());
		Assert.assertEquals(22.22, c.getDouble());
		Assert.assertEquals(null, c.getDoubleO());
		Assert.assertEquals('X', c.getChar());
		Assert.assertEquals('Y', c.getCharO().charValue());
		Assert.assertEquals("yo", c.getString());
	}

	@Test
	public void testValueHint() throws Exception {
		A a = MockBuilder.<A>build(A.class, new String[] {
			"b.c.o = <mockbuilder.A>",
		});

		Object o = a.getB().getC().getO();
		Assert.assertNotNull(o);
		Assert.assertTrue(A.class.isAssignableFrom(o.getClass()));
	}

	@Test
	public void testArray() throws Exception {
		A a = MockBuilder.<A>build(A.class, new String[] {
			"b.ca[0].int = 100",
			"b.ca[1].int = 101",
			"b.ca[1].int = 102",
		});

		C[] ca = a.getB().getCa();
		Assert.assertEquals(100, ca[0].getInt());
		Assert.assertEquals(101, ca[1].getInt());
		Assert.assertEquals(102, ca[1].getInt());
	}

	@Test
	public void testArray2() throws Exception {
		A a = MockBuilder.<A>build(A.class, new String[] {
			"ats[0].componentType.kind = INT",
			"ats[1].componentType.kind = LONG",
			"ats[1].componentType = *",
			"ats[1].componentType.kind = BYTE",
		});

		Assert.assertEquals(TypeKind.INT, a.getAts()[0].getComponentType().getKind());
		ArrayType ats1 = a.getAts()[1];
		TypeMirror ct1 = ats1.getComponentType();
		TypeMirror ct2 = ats1.getComponentType();
		Assert.assertEquals(TypeKind.LONG, ct1.getKind());
		Assert.assertEquals(TypeKind.LONG, ct1.getKind());
		Assert.assertEquals(TypeKind.BYTE, ct2.getKind());
		Assert.assertEquals(TypeKind.BYTE, ct2.getKind());
	}

	@Test
	public void testEnum() throws Exception {
		A a = MockBuilder.<A>build(A.class, new String[] {
			"b.e = EV1",
		});

		Assert.assertEquals(E.EV1, a.getB().getE());
	}

	@Test
	public void testList() throws Exception {
		A a = MockBuilder.<A>build(A.class, new String[] {
			"b.cl[0]<mockbuilder.C>.byte = 6",
			"b.cl[1]<mockbuilder.C>.byte = 7",
		});

		Assert.assertEquals(6, a.getB().getCl().get(0).getByte());
		Assert.assertEquals(7, a.getB().getCl().get(1).getByte());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testListNoHint() throws Exception {
		MockBuilder.<A>build(A.class, new String[] {
			"b.cl[0].int = 654",
		});
	}

	@Test
	public void testMap() throws Exception {
		A a = MockBuilder.<A>build(A.class, new String[] {
			"b.cmapLong[666<Long>]<mockbuilder.C>.byte = 6",
		});
		Assert.assertEquals(6, a.getB().getCmapLong().get(666L).getByte());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testMapNoHint() throws Exception {
		MockBuilder.<A>build(A.class, new String[] {
			"b.cmap[KEY].int = 654",
		});
	}

	@Test(expected = ClassCastException.class)
	public void testMapNoKeyHint() throws Exception {
		A a = MockBuilder.<A>build(A.class, new String[] {
			"b.cmapLong[666]<mockbuilder.C>.byte = 6",
		});
		a.getB().getCmapLong().keySet().iterator().next().getClass();
	}

	@Test
	public void testChar() throws Exception {
		A a = MockBuilder.<A>build(A.class, new String[] {
			"b.c.char = ",
			"b.c.charO = ",
		});

		Assert.assertEquals(' ', a.getB().getC().getChar());
		Assert.assertEquals(null, a.getB().getC().getCharO());
	}

	@Test
	public void testSubsequentInvocations() throws Exception {
		A a = MockBuilder.<A>build(A.class, new String[] {
			"b.c.char = A",
			"b.c.char = B",
			"b.c.char = B",
			"b.c.char = C",
		});

		Assert.assertEquals('A', a.getB().getC().getChar());
		Assert.assertEquals('B', a.getB().getC().getChar());
		Assert.assertEquals('B', a.getB().getC().getChar());
		Assert.assertEquals('C', a.getB().getC().getChar());
	}

	@Test
	public void testSubsequentInvocationsWithResets() throws Exception {
		A a = MockBuilder.<A>build(A.class, new String[] {
			"b.c.char = A",
			"b.c.char = B",
			"b.c = *",
			"b.c.char = C",
			"b.c.char = D",
		});

		C c1 = a.getB().getC();
		C c2 = a.getB().getC();
		Assert.assertEquals('A', c1.getChar());
		Assert.assertEquals('B', c1.getChar());
		Assert.assertEquals('B', c1.getChar());
		Assert.assertEquals('C', c2.getChar());
		Assert.assertEquals('D', c2.getChar());
		Assert.assertEquals('D', c2.getChar());
		Assert.assertEquals('D', c2.getChar());
	}

}
