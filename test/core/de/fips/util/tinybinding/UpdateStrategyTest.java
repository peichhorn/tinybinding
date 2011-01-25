/*
Copyright © 2010-2011 Philipp Eichhorn.

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
*/
package de.fips.util.tinybinding;

import static de.fips.util.tinybinding.util.Cast.uncheckedCast;
import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests the {@link UpdateStrategy}.
 * 
 * @author Philipp Eichhorn
 */
public class UpdateStrategyTest {
	private UpdateStrategy<Integer, String> updateStrategy;
	private IValidator<Integer> afterGetValidator;
	private IValidator<String> beforeSetValidator;
	private IConverter<Integer, String> converter;

	@Before
	public void setUp() {
		afterGetValidator = uncheckedCast(mock(IValidator.class));
		beforeSetValidator = uncheckedCast(mock(IValidator.class));
		converter = uncheckedCast(mock(IConverter.class));
		updateStrategy = new UpdateStrategy<Integer, String>(afterGetValidator, beforeSetValidator);
	}

	@Test
	public void test_convert_calls_converter() {
		String string;
		try {
			string = updateStrategy.convert(Integer.valueOf(10));
			assertThat(false).describedAs("No Exception was thrown").isTrue();
		} catch (Exception e) {
			// expected
		}
		updateStrategy.converter(converter);
		doReturn("A String").when(converter).convert(eq(Integer.valueOf(10)));
		string = updateStrategy.convert(Integer.valueOf(10));
		verify(converter, times(1)).convert(eq(Integer.valueOf(10)));
		assertThat(string).isEqualTo("A String");
	}

	@Test
	public void test_validateAfterGet_calls_validator() {
		doReturn(true).when(afterGetValidator).validate(eq(Integer.valueOf(10)));
		assertThat(updateStrategy.validateAfterGet(Integer.valueOf(10))).isTrue();
		assertThat(updateStrategy.validateAfterGet(Integer.valueOf(20))).isFalse();
		afterGetValidator = uncheckedCast(mock(IValidator.class));
		doReturn(true).when(afterGetValidator).validate(eq(Integer.valueOf(20)));
		updateStrategy.afterGetValidator(afterGetValidator);
		assertThat(updateStrategy.validateAfterGet(Integer.valueOf(20))).isTrue();
		assertThat(updateStrategy.validateAfterGet(Integer.valueOf(10))).isFalse();
	}

	@Test
	public void test_validateBeforeSet_calls_validator() {
		doReturn(true).when(beforeSetValidator).validate(eq("Hello"));
		assertThat(updateStrategy.validateBeforeSet("Hello")).isTrue();
		assertThat(updateStrategy.validateBeforeSet("You")).isFalse();
		beforeSetValidator = uncheckedCast(mock(IValidator.class));
		doReturn(true).when(beforeSetValidator).validate(eq("You"));
		updateStrategy.beforeSetValidator(beforeSetValidator);
		assertThat(updateStrategy.validateBeforeSet("You")).isTrue();
		assertThat(updateStrategy.validateBeforeSet("Hello")).isFalse();
	}
}
