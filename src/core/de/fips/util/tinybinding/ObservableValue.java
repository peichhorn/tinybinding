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
public class ObservableValue<T> implements IObservableValue<T> {
	private T value;
	private final List<IValueObserver<T>> registeredObservers = new CopyOnWriteArrayList<IValueObserver<T>>();

	protected ObservableValue(final T value) {
		this.value = value;
	}
	
	public T get() {
		return value;
	}
	
	public boolean set(final T value) {
		T oldValue = get();
		boolean valueIsNull = value == null;
		boolean oldValueIsNull = oldValue == null;
		boolean valueChanged = (oldValueIsNull && !valueIsNull) || (valueIsNull && !oldValueIsNull)
				|| ((value == oldValue) ? false : !value.equals(oldValue));
		if (valueChanged) {
			try {
				doSet(value);
			} catch (VetoException ignore) {
			}
			notifyObserver(value, oldValue);
		}
		return valueChanged;
	}
	
	protected void doSet(final T value) throws VetoException {
		this.value = value;
	}
	
	protected void notifyObserver(final T newValue, final T oldValue) {
		for (IValueObserver<T> observer : registeredObservers) {
			observer.valueChanged(newValue, oldValue);
		}
	}

	public void addObserver(final IValueObserver<T> observer) {
		addObserver(observer, true);
	}

	public void addObserver(final IValueObserver<T> observer, final boolean emitValueChanged) {
		if (!registeredObservers.contains(observer)) {
			registeredObservers.add(observer);
		}
		if (emitValueChanged) {
			observer.valueChanged(get(), null);
		}
	}

	public void removeObserver(final IValueObserver<T> observer) {
		registeredObservers.remove(observer);
	}

	public static class VetoException extends RuntimeException {
		private static final long serialVersionUID = -3160482313228995937L;
	}
	
	public static <T> ObservableValue<T> of(final T value) {
		return new ObservableValue<T>(value);
	}
	
	public static <T> ObservableValue<T> nil() {
		return new ObservableValue<T>();
	}
}
