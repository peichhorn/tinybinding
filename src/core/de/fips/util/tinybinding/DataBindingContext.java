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
public class DataBindingContext {
	private final Map<Pair<?, ?>, Binding<?, ?>> bindings = new HashMap<Pair<?, ?>, Binding<?, ?>>();

	public <S, T> DataBindingContext bind(final IObservableValue<S> source, final IObservableValue<T> target) {
		return bind(source, target, new UpdateStrategy<S, T>(), new UpdateStrategy<T, S>());
	}
	
	public <S, T> DataBindingContext bind(final IObservableValue<S> source, final IObservableValue<T> target, final IConverter<S, T> sourceToTarget) {
		return bind(source, target, new UpdateStrategy<S, T>().converter(sourceToTarget), null);
	}
	
	public <S, T> DataBindingContext bind(final IObservableValue<S> source, final IObservableValue<T> target, final IConverter<S, T> sourceToTarget,
			final IConverter<T, S> targetToSource) {
		return bind(source, target, new UpdateStrategy<S, T>().converter(sourceToTarget), new UpdateStrategy<T, S>().converter(targetToSource));
	}
	
	public <S, T> DataBindingContext bind(final IObservableValue<S> source, final IObservableValue<T> target, final IUpdateStrategy<S, T> sourceToTarget,
			final IUpdateStrategy<T, S> targetToSource) {
		Binding<S, T> binder = new Binding<S, T>(source, target, sourceToTarget, targetToSource);
		bindings.put(Pair.of(source, target), binder);
		binder.bind();
		return this;
	}

	public <S, T> DataBindingContext unbind(final IObservableValue<S> source, final IObservableValue<T> target) {
		Binding<S, T> binder = uncheckedCast(bindings.get(Pair.of(source, target)));
		if (binder != null) {
			bindings.put(Pair.of(source, target), null);
			binder.unbind();
		}
		return this;
	}

	public DataBindingContext unbindAll() {
		for (Pair<?, ?> key : bindings.keySet()) {
			Binding<?, ?> binder = bindings.get(key);
			if (binder != null) {
				binder.unbind();
			}
		}
		bindings.clear();
		return this;
	}

	private static class Binding<S, T> {
		private final IObservableValue<S> source;
		private final IObservableValue<T> target;
		private final ValueObserver<S, T> sourceObserver;
		private final ValueObserver<T, S> targetObserver;

		public Binding(final IObservableValue<S> source, final IObservableValue<T> target, final IUpdateStrategy<S, T> sourceToTarget,
				final IUpdateStrategy<T, S> targetToSource) {
			this.source = source;
			this.target = target;
			sourceObserver = new ValueObserver<S, T>(source, target, sourceToTarget);
			targetObserver = new ValueObserver<T, S>(target, source, targetToSource);
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
