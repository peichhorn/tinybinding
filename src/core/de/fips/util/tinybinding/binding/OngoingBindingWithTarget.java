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
package de.fips.util.tinybinding.binding;

import de.fips.util.tinybinding.BindingContexts;
import de.fips.util.tinybinding.IBindingContext;
import de.fips.util.tinybinding.IConverter;
import de.fips.util.tinybinding.IObservableValue;
import de.fips.util.tinybinding.IValidator;
import de.fips.util.tinybinding.impl.UpdateStrategy;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class OngoingBindingWithTarget<SOURCE_TYPE, TARGET_TYPE> {
	private final IObservableValue<SOURCE_TYPE> source;
	private final IObservableValue<TARGET_TYPE> target;
	private UpdateStrategy<SOURCE_TYPE, TARGET_TYPE> updateSourceToTarget;
	private UpdateStrategy<TARGET_TYPE, SOURCE_TYPE> updateTargetToSource;

	public OngoingBindingWithTarget<SOURCE_TYPE, TARGET_TYPE> sourceConverter(final IConverter<SOURCE_TYPE, TARGET_TYPE> sourceToTarget) {
		getUpdateSourceToTarget().converter(sourceToTarget);
		return this;
	}

	public OngoingBindingWithTarget<SOURCE_TYPE, TARGET_TYPE> targetConverter(final IConverter<TARGET_TYPE, SOURCE_TYPE> targetToSource) {
		getUpdateTargetToSource().converter(targetToSource);
		return this;
	}

	public OngoingUpdateStrategyDef<SOURCE_TYPE, TARGET_TYPE> updateTarget() {
		return new OngoingUpdateStrategyDef<SOURCE_TYPE, TARGET_TYPE>(getUpdateSourceToTarget());
	}

	public OngoingUpdateStrategyDef<TARGET_TYPE, SOURCE_TYPE> updateSource() {
		return new OngoingUpdateStrategyDef<TARGET_TYPE, SOURCE_TYPE>(getUpdateTargetToSource());
	}

	public IBindingContext go() {
		return in(BindingContexts.defaultContext());
	}

	public IBindingContext in(final IBindingContext context) {
		if (definesUpdateStrategies()) {
			context.bind(source, target, updateSourceToTarget, updateTargetToSource);
		} else {
			context.bind(source, target, getUpdateSourceToTarget(), getUpdateTargetToSource());
		}
		return context;
	}

	private UpdateStrategy<SOURCE_TYPE, TARGET_TYPE> getUpdateSourceToTarget() {
		if (updateSourceToTarget == null) {
			updateSourceToTarget = new UpdateStrategy<SOURCE_TYPE, TARGET_TYPE>();
		}
		return updateSourceToTarget;
	}

	private UpdateStrategy<TARGET_TYPE, SOURCE_TYPE> getUpdateTargetToSource() {
		if (updateTargetToSource == null) {
			updateTargetToSource = new UpdateStrategy<TARGET_TYPE, SOURCE_TYPE>();
		}
		return updateTargetToSource;
	}

	private boolean definesUpdateStrategies() {
		return (updateSourceToTarget != null) || (updateTargetToSource != null);
	}

	@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
	public class OngoingUpdateStrategyDef<SOURCE_TYPE_, TARGET_TYPE_> {
		private final UpdateStrategy<SOURCE_TYPE_, TARGET_TYPE_> updateStrategy;

		public OngoingUpdateStrategyDef<SOURCE_TYPE_, TARGET_TYPE_> convert(final IConverter<SOURCE_TYPE_, TARGET_TYPE_> converter) {
			updateStrategy.converter(converter);
			return this;
		}

		public OngoingUpdateStrategyDef<SOURCE_TYPE_, TARGET_TYPE_> validateAfterGet(final IValidator<SOURCE_TYPE_> afterGetValidator) {
			updateStrategy.afterGetValidator(afterGetValidator);
			return this;
		}

		public OngoingUpdateStrategyDef<SOURCE_TYPE_, TARGET_TYPE_> validateBeforeSet(final IValidator<TARGET_TYPE_> beforeSetValidator) {
			updateStrategy.beforeSetValidator(beforeSetValidator);
			return this;
		}

		public IBindingContext go() {
			return OngoingBindingWithTarget.this.go();
		}

		public IBindingContext in(final IBindingContext context) {
			return OngoingBindingWithTarget.this.in(context);
		}
	}
}
