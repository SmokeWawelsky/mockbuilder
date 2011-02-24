package mockbuilder;

import java.math.BigDecimal;

import junit.framework.Assert;

import org.junit.Test;

/**
 * @author Tomasz Kisiel
 */
public class UtilsTest {

	@Test(expected = IllegalArgumentException.class)
	public void shouldThrowExceptionWhenTypeForCreateValueIsNull() throws Exception {
		Utils.createValue(null, null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldThrowExceptionWhenNullValueIsGivenForPrimitiveType() throws Exception {
		Utils.createValue(null, int.class);
	}

	@Test
	public void testCreateNullValue() throws Exception {
		Assert.assertNull(Utils.createValue(null, Object.class));
		Assert.assertNull(Utils.createValue("null", Object.class));
	}

	@Test
	public void testCreateBigDecimalValue() throws Exception {
		Assert.assertEquals(new BigDecimal(1234), Utils.createValue("1234<BigDecimal>", Object.class));
	}

}
