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
package de.fips.util.tinybinding.binding;

import de.fips.util.tinybinding.BindingContexts;
import de.fips.util.tinybinding.IBindingContext;
import de.fips.util.tinybinding.IConverter;
import de.fips.util.tinybinding.IObservableValue;
import de.fips.util.tinybinding.IValidator;
import de.fips.util.tinybinding.impl.UpdateStrategy;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

/**
 * 
 * @param <TYPE>
 * @author Philipp Eichhorn
 */
@RequiredArgsConstructor
public class OngoingBinding<TYPE> {
	private final IObservableValue<TYPE> source;

	public <TARGET> WithTarget<TYPE, TARGET> to(final IObservableValue<TARGET> target) {
		return new WithTarget<TYPE, TARGET>(source, target);
	}

	@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
	public static class WithTarget<SOURCE, TARGET> {
		private final IObservableValue<SOURCE> source;
		private final IObservableValue<TARGET> target;
		private UpdateStrategy<SOURCE, TARGET> updateTarget;
		private UpdateStrategy<TARGET, SOURCE> updateSource;

		public WithTarget<SOURCE, TARGET> sourceConverter(final IConverter<SOURCE, TARGET> sourceToTarget) {
			getUpdateTarget().converter(sourceToTarget);
			return this;
		}

		public WithTarget<SOURCE, TARGET> targetConverter(final IConverter<TARGET, SOURCE> targetToSource) {
			getUpdateSource().converter(targetToSource);
			return this;
		}

		public WithUpdateStrategy<SOURCE, TARGET, SOURCE, TARGET> updateTarget() {
			return new WithUpdateStrategy<SOURCE, TARGET, SOURCE, TARGET>(this, getUpdateTarget());
		}

		public WithTarget<SOURCE, TARGET> updateTarget(UpdateStrategy<SOURCE, TARGET> updateTarget) {
			this.updateTarget = updateTarget;
			return this;
		}

		public WithUpdateStrategy<TARGET, SOURCE, SOURCE, TARGET> updateSource() {
			return new WithUpdateStrategy<TARGET, SOURCE, SOURCE, TARGET>(this, getUpdateSource());
		}

		public WithTarget<SOURCE, TARGET> updateSource(UpdateStrategy<TARGET, SOURCE> updateSource) {
			this.updateSource = updateSource;
			return this;
		}

		public IBindingContext go() {
			return in(BindingContexts.defaultContext());
		}

		public IBindingContext in(final IBindingContext context) {
			if (definesUpdateStrategies()) {
				context.bind(source, target, updateTarget, updateSource);
			} else {
				context.bind(source, target, getUpdateTarget(), getUpdateSource());
			}
			return context;
		}

		private UpdateStrategy<SOURCE, TARGET> getUpdateTarget() {
			if (updateTarget == null) {
				updateTarget = new UpdateStrategy<SOURCE, TARGET>();
			}
			return updateTarget;
		}

		private UpdateStrategy<TARGET, SOURCE> getUpdateSource() {
			if (updateSource == null) {
				updateSource = new UpdateStrategy<TARGET, SOURCE>();
			}
			return updateSource;
		}

		private boolean definesUpdateStrategies() {
			return (updateTarget != null) || (updateSource != null);
		}
	}

	@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
	public static class WithUpdateStrategy<SOURCE, TARGET, SOURCE_, TARGET_> {
		private final WithTarget<SOURCE_, TARGET_> withTarget;
		private final UpdateStrategy<SOURCE, TARGET> updateStrategy;

		public WithUpdateStrategy<SOURCE, TARGET, SOURCE_, TARGET_> convert(final IConverter<SOURCE, TARGET> converter) {
			updateStrategy.converter(converter);
			return this;
		}

		public WithUpdateStrategy<SOURCE, TARGET, SOURCE_, TARGET_> validateAfterGet(final IValidator<? super SOURCE> afterGetValidator) {
			updateStrategy.afterGetValidator(afterGetValidator);
			return this;
		}

		public WithUpdateStrategy<SOURCE, TARGET, SOURCE_, TARGET_> validateBeforeSet(final IValidator<? super TARGET> beforeSetValidator) {
			updateStrategy.beforeSetValidator(beforeSetValidator);
			return this;
		}

		public WithTarget<SOURCE_, TARGET_> and() {
			return withTarget;
		}

		public IBindingContext go() {
			return and().go();
		}

		public IBindingContext in(final IBindingContext context) {
			return and().in(context);
		}
	}
}
