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
package de.fips.util.tinybinding.impl;

import static de.fips.util.tinybinding.ValidationResultCondition.*;
import static de.fips.util.tinybinding.util.Cast.uncheckedCast;
import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import de.fips.util.tinybinding.ExpectedException;
import de.fips.util.tinybinding.IConverter;
import de.fips.util.tinybinding.IObservableValue;
import de.fips.util.tinybinding.IValidator;
import de.fips.util.tinybinding.ValidationResults;

/**
 * Tests the {@link UpdateStrategy}.
 *
 * @author Philipp Eichhorn
 */
public class UpdateStrategyTest {
	@Rule
	public final ExpectedException thrown = ExpectedException.none();

	private UpdateStrategy<Integer, String> updateStrategy;
	private IValidator<Integer> afterGetValidator;
	private IValidator<String> beforeSetValidator;
	private IConverter<Integer, String> converter;

	@Before
	public void createMocks() {
		afterGetValidator = uncheckedCast(mock(IValidator.class));
		beforeSetValidator = uncheckedCast(mock(IValidator.class));
		converter = uncheckedCast(mock(IConverter.class));
		updateStrategy = new UpdateStrategy<Integer, String>(afterGetValidator, beforeSetValidator).converter(converter);
	}

	@Test
	public void test_convertOnlyCallsConverter() {
		doReturn("A String").when(converter).convert(eq(Integer.valueOf(10)));
		String string = updateStrategy.convert(Integer.valueOf(10));
		assertThat(string).isEqualTo("A String");
		verify(converter, times(1)).convert(eq(Integer.valueOf(10)));
		verifyZeroInteractions(beforeSetValidator, afterGetValidator);
	}

	@Test
	public void test_convertDoesNotCatchExceptionComingFromConverter() {
		doThrow(new ClassCastException()).when(converter).convert(any(Integer.class));
		thrown.expect(ClassCastException.class);
		updateStrategy.convert(Integer.valueOf(10));
	}

	@Test
	public void test_doSetOnlyCallsSetOnValue() {
		IObservableValue<String> value = uncheckedCast(mock(IObservableValue.class));
		updateStrategy.doSet(value, "A String");
		verify(value, times(1)).set(eq("A String"));
		verifyZeroInteractions(beforeSetValidator, afterGetValidator, converter);
	}

	@Test
	public void test_validateAfterGetOnlyCallsAfterGetValidator() {
		doReturn(ValidationResults.ok()).when(afterGetValidator).validate(eq(Integer.valueOf(10)));
		doReturn(ValidationResults.warning("< 20")).when(afterGetValidator).validate(eq(Integer.valueOf(20)));
		assertThat(updateStrategy.validateAfterGet(Integer.valueOf(10))).is(ok());
		assertThat(updateStrategy.validateAfterGet(Integer.valueOf(20))).is(warning());
		verify(afterGetValidator, times(2)).validate(any(Integer.class));
		verifyZeroInteractions(beforeSetValidator, converter);
	}

	@Test
	public void test_validateBeforeSetOnlyCallsBeforeSetValidator() {
		doReturn(ValidationResults.ok()).when(beforeSetValidator).validate(eq("Hello"));
		doReturn(ValidationResults.error("not 'You'")).when(beforeSetValidator).validate(eq("You"));
		assertThat(updateStrategy.validateBeforeSet("Hello")).is(ok());
		assertThat(updateStrategy.validateBeforeSet("You")).is(error());
		verify(beforeSetValidator, times(2)).validate(any(String.class));
		verifyZeroInteractions(afterGetValidator, converter);
	}
}
