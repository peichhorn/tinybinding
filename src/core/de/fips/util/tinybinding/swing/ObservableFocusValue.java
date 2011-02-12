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

import static de.fips.util.tinybinding.WeakReferences.weakListener;

import java.awt.Container;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import de.fips.util.tinybinding.ObservableValue;

/**
 * {@link ObservableValue} that can wrap the focus state of a Swing Component.
 * <p>
 * <b>Note:</b> All used listeners are added as a {@link java.lang.ref.WeakReference WeakReferences}, so they gets
 * garbage collected when the time comes.
 *
 * @see FocusListener
 * @author Philipp Eichhorn
 */
class ObservableFocusValue extends ObservableComponentValue<Boolean, Container> implements FocusListener {

	public ObservableFocusValue(final Container component) {
		super(component);
		getComponent().addFocusListener(weakListener(FocusListener.class, this).withTarget(getComponent()).get());
	}

	@Override
	public void focusGained(final FocusEvent e) {
		guardedUpdateValue();
	}

	@Override
	public void focusLost(final FocusEvent e) {
		guardedUpdateValue();
	}

	@Override
	protected void guardedDoSet(final Boolean value) {
		if ((value != null) && value.booleanValue()) getComponent().requestFocusInWindow();
	}

	@Override
	public Boolean getComponentValue() {
		return getComponent().hasFocus();
	}
}