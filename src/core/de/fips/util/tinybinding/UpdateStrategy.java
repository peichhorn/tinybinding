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
public class UpdateStrategy<S, T> implements IUpdateStrategy<S, T> {
	private IValidator<S> afterGetValidator;
	private IValidator<T> beforeSetValidator;
	private IConverter<S, T> converter = new DefaultConverter<S, T>();

	public UpdateStrategy(final IValidator<S> afterGetValidator, final IValidator<T> beforeSetValidator) {
		this();
		this.afterGetValidator = afterGetValidator;
		this.beforeSetValidator = beforeSetValidator;
	}

	public T convert(final S source) {
		return converter.convert(source);
	}

	public boolean doSet(final IObservableValue<T> value, final T object) {
		value.set(object);
		return true;
	}

	public boolean validateAfterGet(final S source) {
		if (afterGetValidator == null) {
			return true;
		}
		return afterGetValidator.validate(source);
	}

	public boolean validateBeforeSet(final T target) {
		if (beforeSetValidator == null) {
			return true;
		}
		return beforeSetValidator.validate(target);
	}
}
