/*
 * Copyright © 2010-2011 Philipp Eichhorn.
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package de.fips.util.tinybinding.impl;

import static de.fips.util.tinybinding.ValidationResults.ok;

import de.fips.util.tinybinding.IConverter;
import de.fips.util.tinybinding.IObservableValue;
import de.fips.util.tinybinding.IUpdateStrategy;
import de.fips.util.tinybinding.IValidationResult;
import de.fips.util.tinybinding.IValidator;

import lombok.FluentSetter;
import lombok.NoArgsConstructor;

/**
 *
 * @author Philipp Eichhorn
 */
@NoArgsConstructor
@FluentSetter
public class UpdateStrategy<SOURCE, TARGET> implements IUpdateStrategy<SOURCE, TARGET> {
	private IValidator<? super SOURCE> afterGetValidator;
	private IValidator<? super TARGET> beforeSetValidator;
	private IConverter<SOURCE, TARGET> converter = new Converter<SOURCE, TARGET>();

	public UpdateStrategy(final IValidator<? super SOURCE> afterGetValidator, final IValidator<? super TARGET> beforeSetValidator) {
		this.afterGetValidator = afterGetValidator;
		this.beforeSetValidator = beforeSetValidator;
	}

	@Override
	public TARGET convert(final SOURCE source) {
		return converter.convert(source);
	}

	@Override
	public void doSet(final IObservableValue<TARGET> value, final TARGET object) {
		value.set(object);
	}

	@Override
	public IValidationResult validateAfterGet(final SOURCE source) {
		return (afterGetValidator == null) ? ok() : afterGetValidator.validate(source);
	}

	@Override
	public IValidationResult validateBeforeSet(final TARGET target) {
		return (beforeSetValidator == null) ? ok() : beforeSetValidator.validate(target);
	}
}
