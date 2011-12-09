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

import static de.fips.util.tinybinding.util.Cast.uncheckedCast;

import java.util.HashMap;
import java.util.Map;

import de.fips.util.tinybinding.IBindingContext;
import de.fips.util.tinybinding.IObservableValue;
import de.fips.util.tinybinding.IUpdateStrategy;
import de.fips.util.tinybinding.IValidationResult;
import de.fips.util.tinybinding.IValueObserver;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 *
 * @author Philipp Eichhorn
 */
public final class BindingContext implements IBindingContext {
	private final Map<Pair<?, ?>, Binding<?, ?>> bindings = new HashMap<Pair<?, ?>, Binding<?, ?>>();

	@Override
	public <SOURCE, TARGET> void bind(final IObservableValue<SOURCE> source, final IObservableValue<TARGET> target,
			final IUpdateStrategy<SOURCE, TARGET> sourceToTarget, final IUpdateStrategy<TARGET, SOURCE> targetToSource) {
		Binding<SOURCE, TARGET> binding = new Binding<SOURCE, TARGET>(source, target, sourceToTarget, targetToSource);
		bindings.put(Pair.of(source, target), binding);
		binding.bind();
	}

	@Override
	public <SOURCE, TARGET> void unbind(final IObservableValue<SOURCE> source, final IObservableValue<TARGET> target) {
		Binding<SOURCE, TARGET> binder = uncheckedCast(bindings.get(Pair.of(source, target)));
		if (binder != null) {
			bindings.put(Pair.of(source, target), null);
			binder.unbind();
		}
	}

	@Override
	public void unbindAll() {
		for (Pair<?, ?> key : bindings.keySet()) {
			Binding<?, ?> binder = bindings.get(key);
			if (binder != null) {
				binder.unbind();
			}
		}
		bindings.clear();
	}

	private static class Binding<SOURCE, TARGET> {
		private final IObservableValue<SOURCE> source;
		private final IObservableValue<TARGET> target;
		private final ValueObserver<SOURCE, TARGET> sourceObserver;
		private final ValueObserver<TARGET, SOURCE> targetObserver;

		public Binding(final IObservableValue<SOURCE> source, final IObservableValue<TARGET> target, final IUpdateStrategy<SOURCE, TARGET> sourceToTarget,
				final IUpdateStrategy<TARGET, SOURCE> targetToSource) {
			this.source = source;
			this.target = target;
			sourceObserver = new ValueObserver<SOURCE, TARGET>(source, target, sourceToTarget);
			targetObserver = new ValueObserver<TARGET, SOURCE>(target, source, targetToSource);
			sourceObserver.setTargetObserver(targetObserver);
			targetObserver.setTargetObserver(sourceObserver);
		}

		public void bind() {
			source.addObserver(sourceObserver);
			target.addObserver(targetObserver);
		}

		public void unbind() {
			source.removeObserver(sourceObserver);
			target.removeObserver(targetObserver);
		}
	}

	@RequiredArgsConstructor
	private static class ValueObserver<S, T> implements IValueObserver<S> {
		private final IObservableValue<S> source;
		private final IObservableValue<T> target;
		private final IUpdateStrategy<S, T> sourceToTarget;
		@Setter
		private IValueObserver<T> targetObserver;

		@Override
		public void valueChanged(final S value, final S oldValue) {
			final S s = source.get();
			if (sourceToTarget != null) {
				final IValidationResult resultAfterGet = sourceToTarget.validateAfterGet(s);
				if (isOk(resultAfterGet)) {
					final T t = sourceToTarget.convert(s);
					final IValidationResult resultBeforeSet = sourceToTarget.validateBeforeSet(t);
					if (isOk(resultBeforeSet)) {
						target.removeObserver(targetObserver);
						sourceToTarget.doSet(target, t);
						target.addObserver(targetObserver, false);
					}
				}
			}
		}

		private boolean isOk(final IValidationResult status) {
			return IValidationResult.Type.OK.equals(status.getType());
		}
	}

	@Data
	private static class Pair<A, B> {
		private final A first;
		private final B second;

		public static <A, B> Pair<A, B> of(final A first, final B second) {
			return new Pair<A, B>(first, second);
		}
	}
}
