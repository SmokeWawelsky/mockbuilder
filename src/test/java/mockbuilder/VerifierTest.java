package mockbuilder;

import junit.framework.AssertionFailedError;

import org.junit.Test;
import org.mockito.Mockito;

import mockbuilder.Verifier.Verification;

/**
 * @author Tomasz Kisiel
 */
public class VerifierTest {

	@Test
	public void testVerifyGetters() throws Exception {
		// Given
		C c = Mockito.mock(C.class);
		Mockito.when(c.getInt()).thenReturn(6);
		Mockito.when(c.getLong()).thenReturn(666L);
		Mockito.when(c.getChar()).thenReturn('Z');
		Mockito.when(c.getString()).thenReturn("string");

		B b = Mockito.mock(B.class);
		Mockito.when(b.getC()).thenReturn(c);

		A a = Mockito.mock(A.class);
		Mockito.when(a.getB()).thenReturn(b);

		// Then
		Verifier.verify(Verification.GETTERS, a, new String[] {
				"b.c.int = 6",
				"b.c.long = 666",
				"b.c.char = Z",
				"b.c.string = string",
		});
	}

	@Test(expected = AssertionFailedError.class)
	public void testVerifyGettersFailing() throws Exception {
		// Given
		C c = Mockito.mock(C.class);
		Mockito.when(c.getInt()).thenReturn(6);
		Mockito.when(c.getLong()).thenReturn(666L);
		Mockito.when(c.getChar()).thenReturn('Z');
		Mockito.when(c.getString()).thenReturn("string");

		B b = Mockito.mock(B.class);
		Mockito.when(b.getC()).thenReturn(c);

		A a = Mockito.mock(A.class);
		Mockito.when(a.getB()).thenReturn(b);

		// Then
		Verifier.verify(Verification.GETTERS, a, new String[] {
				"b.c.long = 777",
		});
	}

	@Test
	public void testVerifySetters() throws Exception {
		// Given
		C c = Mockito.mock(C.class);

		B b = Mockito.mock(B.class);
		Mockito.when(b.getC()).thenReturn(c);

		A a = Mockito.mock(A.class);
		Mockito.when(a.getB()).thenReturn(b);

		// Then
		C cc = a.getB().getC();
		cc.setInt(666);
		cc.setLong(666L);
		cc.setChar('6');
		cc.setString(null);

		Verifier.verify(Verification.SETTERS, a, new String[] {
				"b.c.int = 666",
				"b.c.long = 666",
				"b.c.char = 6",
				"b.c.string = null",
		});
	}

	@Test(expected = AssertionFailedError.class)
	public void testVerifySettersFailing() throws Exception {
		// Given
		C c = Mockito.mock(C.class);

		B b = Mockito.mock(B.class);
		Mockito.when(b.getC()).thenReturn(c);

		A a = Mockito.mock(A.class);
		Mockito.when(a.getB()).thenReturn(b);

		// Then
		a.getB().getC().setInt(666);
		a.getB().getC().setLong(666L);
		a.getB().getC().setChar('6');
		a.getB().getC().setString(null);

		Verifier.verify(Verification.SETTERS, a, new String[] {
				"b.c.string = 777",
		});
	}

	@Test
	public void testVerifyGettersWithMockBuilder() throws Exception {
		String[] settings = new String[] {
			"b.c.byte = 11",
			"b.c.byteO = 12",
			"b.c.short = 111",
			"b.c.shortO = 112",
			"b.c.int = 1111",
			"b.c.intO = 1112",
			"b.c.long = 11111",
			"b.c.longO = 11112",
			"b.c.float = 2.2",
			"b.c.floatO = 2.3",
			"b.c.double = 22.22",
			"b.c.doubleO = 22.33",
			"b.c.char = X",
			"b.c.charO = Y",
			"b.c.string = yo",
			"b.ca[0].int = 100",
			"b.ca[1].int = 101",
			"b.cl[0]<mockbuilder.C>.byte = 6",
			"b.cl[1]<mockbuilder.C>.byte = 7",
			"b.e = EV1",
		};
		A a = MockBuilder.<A>build(A.class, settings);
		Verifier.verify(Verification.GETTERS, a, settings);
	}

	@Test(expected = AssertionFailedError.class)
	public void testVerifyGettersWithMockBuilderNegative() throws Exception {
		String[] settings = new String[] {
			"b.c.byte = 11",
			"b.c.short = 111",
			"b.c.int = 1111",
			"b.c.long = 11111",
			"b.c.float = 2.2",
			"b.c.double = 22.22",
			"b.c.char = X",
			"b.c.string = yo",
		};
		A a = MockBuilder.<A>build(A.class, settings);
		settings[7] = settings[7] + "x";
		Verifier.verify(Verification.GETTERS, a, settings);
	}

}
