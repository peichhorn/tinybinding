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

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 *
 * @author Philipp Eichhorn
 */
@ToString(of="value")
@NoArgsConstructor(access=AccessLevel.PROTECTED)
public class ObservableValue<TYPE> implements IObservableValue<TYPE> {
	private TYPE value;
	private final List<IValueObserver<TYPE>> registeredObservers = new CopyOnWriteArrayList<IValueObserver<TYPE>>();

	protected ObservableValue(final TYPE value) {
		this.value = value;
	}

	@Override
	public final TYPE get() {
		return value;
	}

	@Override
	public final boolean set(final TYPE value) {
		TYPE oldValue = get();
		boolean valueIsNull = value == null;
		boolean oldValueIsNull = oldValue == null;
		boolean valueChanged = (oldValueIsNull && !valueIsNull) || (valueIsNull && !oldValueIsNull)
				|| ((value == oldValue) ? false : !value.equals(oldValue));
		if (valueChanged) {
			this.value = value;
			doSet(value);
			notifyObserver(value, oldValue);
		}
		return valueChanged;
	}

	/** Hook for subclasses */
	protected void doSet(final TYPE value) {
		// Subclasses may use to hook to call their own setter
	}

	protected final void notifyObserver(final TYPE newValue, final TYPE oldValue) {
		for (final IValueObserver<TYPE> observer : registeredObservers) {
			observer.valueChanged(newValue, oldValue);
		}
	}

	@Override
	public final void addObserver(final IValueObserver<TYPE> observer) {
		addObserver(observer, true);
	}

	@Override
	public final void addObserver(final IValueObserver<TYPE> observer, final boolean emitValueChanged) {
		if (!registeredObservers.contains(observer)) {
			registeredObservers.add(observer);
		}
		if (emitValueChanged) {
			observer.valueChanged(get(), null);
		}
	}

	@Override
	public final void removeObserver(final IValueObserver<TYPE> observer) {
		registeredObservers.remove(observer);
	}

	public static <TYPE> ObservableValue<TYPE> of(final TYPE value) {
		return new ObservableValue<TYPE>(value);
	}

	public static <TYPE> ObservableValue<TYPE> nil() {
		return new ObservableValue<TYPE>();
	}
}
