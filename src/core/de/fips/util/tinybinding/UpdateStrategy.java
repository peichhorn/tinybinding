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

import lombok.FluentSetter;
import lombok.NoArgsConstructor;

/**
 *
 * @author Philipp Eichhorn
 */
@NoArgsConstructor
@FluentSetter
public class UpdateStrategy<SOURCE_TYPE, TARGET_TYPE> implements IUpdateStrategy<SOURCE_TYPE, TARGET_TYPE> {
	private IValidator<SOURCE_TYPE> afterGetValidator;
	private IValidator<TARGET_TYPE> beforeSetValidator;
	private IConverter<SOURCE_TYPE, TARGET_TYPE> converter = new DefaultConverter<SOURCE_TYPE, TARGET_TYPE>();

	public UpdateStrategy(final IValidator<SOURCE_TYPE> afterGetValidator, final IValidator<TARGET_TYPE> beforeSetValidator) {
		this.afterGetValidator = afterGetValidator;
		this.beforeSetValidator = beforeSetValidator;
	}

	@Override
	public TARGET_TYPE convert(final SOURCE_TYPE source) {
		return converter.convert(source);
	}

	@Override
	public boolean doSet(final IObservableValue<TARGET_TYPE> value, final TARGET_TYPE object) {
		value.set(object);
		return true;
	}

	@Override
	public boolean validateAfterGet(final SOURCE_TYPE source) {
		return (afterGetValidator == null) || afterGetValidator.validate(source);
	}

	@Override
	public boolean validateBeforeSet(final TARGET_TYPE target) {
		return (beforeSetValidator == null) || beforeSetValidator.validate(target);
	}
}
