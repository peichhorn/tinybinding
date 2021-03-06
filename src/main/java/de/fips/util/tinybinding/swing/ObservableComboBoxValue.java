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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JComboBox;

import de.fips.util.tinybinding.util.Cast;

/**
 * {@link de.fips.util.tinybinding.impl.ObservableValue ObservableValue} that can wrap the value selection of a {@link JComboBox}.
 * <p>
 * <b>Note:</b> All used listeners are added as a {@link java.lang.ref.WeakReference WeakReferences}, so they gets
 * garbage collected when the time comes.
 *
 * @see ActionListener
 * @see PropertyChangeListener
 * @param <TYPE> Type of the observed combobox value.
 * @author Philipp Eichhorn
 */
class ObservableComboBoxValue<TYPE> extends ObservableComponentValue<TYPE, JComboBox> implements ActionListener, PropertyChangeListener {

	public ObservableComboBoxValue(final JComboBox component) {
		super(component);
		addWeak(ActionListener.class, this).toTarget(getComponent());
		addWeak(PropertyChangeListener.class, this).withPropertyName("model").toTarget(getComponent());
		guardedUpdateValue();
	}

	@Override
	public void actionPerformed(final ActionEvent event) {
		guardedUpdateValue();
	}

	@Override
	public void propertyChange(final PropertyChangeEvent event) {
		guardedUpdateValue();
	}

	@Override
	protected void guardedDoSet(final TYPE value) {
		getComponent().setSelectedItem(value);
	}

	@Override
	public TYPE getComponentValue() {
		return Cast.<TYPE>uncheckedCast(getComponent().getSelectedItem());
	}
}