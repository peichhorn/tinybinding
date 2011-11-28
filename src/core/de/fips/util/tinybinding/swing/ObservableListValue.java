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
package de.fips.util.tinybinding.swing;

import static de.fips.util.tinybinding.WeakListeners.addWeak;

import javax.swing.JList;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import de.fips.util.tinybinding.util.Cast;

/**
 * {@link de.fips.util.tinybinding.impl.ObservableValue ObservableValue} that can wrap the item selection of a {@link JList}.
 * <p>
 * <b>Note:</b> All used listeners are added as a {@link java.lang.ref.WeakReference WeakReferences}, so they gets
 * garbage collected when the time comes.
 *
 * @param <TYPE> Type of the observed list value.
 * @see ListSelectionListener
 * @author Philipp Eichhorn
 */
class ObservableListValue<TYPE> extends ObservableComponentValue<TYPE, JList> implements ListSelectionListener {

	public ObservableListValue(final JList component) {
		super(component);
		addWeak(ListSelectionListener.class, this).toTarget(getComponent());
		guardedUpdateValue();
	}

	@Override
	public void valueChanged(final ListSelectionEvent e) {
		guardedUpdateValue();
	}

	@Override
	protected void guardedDoSet(final TYPE value) {
		getComponent().setSelectedValue(value, true);
	}

	@Override
	public TYPE getComponentValue() {
		return Cast.<TYPE>uncheckedCast(getComponent().getSelectedValue());
	}
}