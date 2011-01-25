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
package de.fips.util.tinybinding.swing;

import java.awt.Container;
import java.lang.ref.WeakReference;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import de.fips.util.tinybinding.ObservableValue;

/**
 * Abstract class that offers its subclasses the basic functionality 
 * to synchronize themselves with Swing Component values or states.
 * <p>
 * <b>Note:</b> All used listeners are added as a {@link WeakReference WeakReferences}, so they gets
 * garbage collected when the time comes.
 *
 * @param <T> Type of the observed value.
 * @param <C> Type of the observed Swing Component.
 * @author Philipp Eichhorn
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
abstract class ObservableComponentValue<T, C extends Container> extends ObservableValue<T> {
	@Getter(AccessLevel.PROTECTED)
	private final C component;
	private volatile boolean propertyChange;

	protected final void guardedSetValue(final T value) {
		propertyChange = true;
		set(value);
		propertyChange = false;
	}

	protected void guardedUpdateValue() {
		guardedSetValue(getComponentValue());
	}

	protected abstract T getComponentValue();

	@Override
	protected void doSet(final T value) throws VetoException {
		super.doSet(value);
		if (propertyChange) {
			throw new VetoException();
		}
	}
}