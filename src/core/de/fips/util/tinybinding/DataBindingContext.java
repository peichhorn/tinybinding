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

import java.util.HashMap;
import java.util.Map;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 *
 * @author Philipp Eichhorn
 */
public final class DataBindingContext {
	private final Map<Pair<?, ?>, Binding<?, ?>> bindings = new HashMap<Pair<?, ?>, Binding<?, ?>>();

	public final <SOURCE_TYPE, TARGET_TYPE> DataBindingContext bind(final IObservableValue<SOURCE_TYPE> source, final IObservableValue<TARGET_TYPE> target) {
		return bind(source, target, new UpdateStrategy<SOURCE_TYPE, TARGET_TYPE>(), new UpdateStrategy<TARGET_TYPE, SOURCE_TYPE>());
	}

	public final <SOURCE_TYPE, TARGET_TYPE> DataBindingContext bind(final IObservableValue<SOURCE_TYPE> source, final IObservableValue<TARGET_TYPE> target,
			final IConverter<SOURCE_TYPE, TARGET_TYPE> sourceToTarget) {
		return bind(source, target, new UpdateStrategy<SOURCE_TYPE, TARGET_TYPE>().converter(sourceToTarget), null);
	}

	public final <SOURCE_TYPE, TARGET_TYPE> DataBindingContext bind(final IObservableValue<SOURCE_TYPE> source, final IObservableValue<TARGET_TYPE> target,
			final IConverter<SOURCE_TYPE, TARGET_TYPE> sourceToTarget, final IConverter<TARGET_TYPE, SOURCE_TYPE> targetToSource) {
		return bind(source, target, new UpdateStrategy<SOURCE_TYPE, TARGET_TYPE>().converter(sourceToTarget), new UpdateStrategy<TARGET_TYPE, SOURCE_TYPE>().converter(targetToSource));
	}

	public final <SOURCE_TYPE, TARGET_TYPE> DataBindingContext bind(final IObservableValue<SOURCE_TYPE> source, final IObservableValue<TARGET_TYPE> target,
			final IUpdateStrategy<SOURCE_TYPE, TARGET_TYPE> sourceToTarget, final IUpdateStrategy<TARGET_TYPE, SOURCE_TYPE> targetToSource) {
		Binding<SOURCE_TYPE, TARGET_TYPE> binder = new Binding<SOURCE_TYPE, TARGET_TYPE>(source, target, sourceToTarget, targetToSource);
		bindings.put(Pair.of(source, target), binder);
		binder.bind();
		return this;
	}

	public final <SOURCE_TYPE, TARGET_TYPE> DataBindingContext unbind(final IObservableValue<SOURCE_TYPE> source, final IObservableValue<TARGET_TYPE> target) {
		Binding<SOURCE_TYPE, TARGET_TYPE> binder = uncheckedCast(bindings.get(Pair.of(source, target)));
		if (binder != null) {
			bindings.put(Pair.of(source, target), null);
			binder.unbind();
		}
		return this;
	}

	public final DataBindingContext unbindAll() {
		for (Pair<?, ?> key : bindings.keySet()) {
			Binding<?, ?> binder = bindings.get(key);
			if (binder != null) {
				binder.unbind();
			}
		}
		bindings.clear();
		return this;
	}

	private static class Binding<SOURCE_TYPE, TARGET_TYPE> {
		private final IObservableValue<SOURCE_TYPE> source;
		private final IObservableValue<TARGET_TYPE> target;
		private final ValueObserver<SOURCE_TYPE, TARGET_TYPE> sourceObserver;
		private final ValueObserver<TARGET_TYPE, SOURCE_TYPE> targetObserver;

		public Binding(final IObservableValue<SOURCE_TYPE> source, final IObservableValue<TARGET_TYPE> target, final IUpdateStrategy<SOURCE_TYPE, TARGET_TYPE> sourceToTarget,
				final IUpdateStrategy<TARGET_TYPE, SOURCE_TYPE> targetToSource) {
			this.source = source;
			this.target = target;
			sourceObserver = new ValueObserver<SOURCE_TYPE, TARGET_TYPE>(source, target, sourceToTarget);
			targetObserver = new ValueObserver<TARGET_TYPE, SOURCE_TYPE>(target, source, targetToSource);
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
			target.removeObserver(targetObserver);
			S s = source.get();
			if (sourceToTarget != null) {
				if (sourceToTarget.validateAfterGet(s)) {
					T t = sourceToTarget.convert(s);
					if (sourceToTarget.validateBeforeSet(t)) {
						sourceToTarget.doSet(target, t);
					}
				}
			}
			target.addObserver(targetObserver, false);
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
