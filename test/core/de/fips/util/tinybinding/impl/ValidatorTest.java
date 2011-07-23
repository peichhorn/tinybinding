/*
Copyright © 2011 Philipp Eichhorn.

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

import static de.fips.util.tinybinding.ValidationResultCondition.ok;
import static org.fest.assertions.Assertions.assertThat;

import org.junit.Test;

import de.fips.util.tinybinding.IValidationResult;
import de.fips.util.tinybinding.IValidator;

/**
 * Tests the {@link Validator}.
 *
 * @author Philipp Eichhorn
 */
public class ValidatorTest {

	@Test
	public void whenValueIsEmpty_validate_shouldReturnAnOkValidationResult() {
		// setup
		final IValidator<String> validator = new Validator<String>();
		// run 
		final IValidationResult result = validator.validate("");
		// assert
		assertThat(result).is(ok());
	}

	@Test
	public void whenValueIsNull_validate_shouldReturnAnOkValidationResult() {
		// setup
		final IValidator<String> validator = new Validator<String>();
		// run 
		final IValidationResult result = validator.validate(null);
		// assert
		assertThat(result).is(ok());
	}

	@Test
	public void whenValueIsFoo_validate_shouldReturnAnOkValidationResult() {
		// setup
		final IValidator<String> validator = new Validator<String>();
		// run 
		final IValidationResult result = validator.validate("foo");
		// assert
		assertThat(result).is(ok());
	}
}
