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
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import de.fips.util.tinybinding.ObservableValue;

import lombok.AutoGenMethodStub;

/**
 * {@link ObservableValue} that can wrap the visibility state of a swing component.
 * <p>
 * <b>Note:</b> All used listeners are added as a {@link java.lang.ref.WeakReference WeakReferences}, so they gets
 * garbage collected when the time comes.
 *
 * @see ComponentListener
 * @author Philipp Eichhorn
 */
@AutoGenMethodStub
class ObservableVisibleValue extends ObservableComponentValue<Boolean, Container> implements ComponentListener {

	public ObservableVisibleValue(final Container component) {
		super(component);
		component.addComponentListener(weakListener(ComponentListener.class, this).createFor(getComponent()));
		guardedUpdateValue();
	}

	@Override
	public void componentShown(final ComponentEvent e) {
		guardedUpdateValue();
	}

	@Override
	public void componentHidden(final ComponentEvent e) {
		guardedUpdateValue();
	}

	@Override
	protected void guardedDoSet(final Boolean value) {
		if (value != null) getComponent().setVisible(value.booleanValue());
	}

	@Override
	public Boolean getComponentValue() {
		return getComponent().isVisible();
	}
}