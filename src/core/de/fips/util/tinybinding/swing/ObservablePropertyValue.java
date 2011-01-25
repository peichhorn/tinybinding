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

import static org.fest.reflect.core.Reflection.*;
import static de.fips.util.tinybinding.util.Reflection.*;
import static de.fips.util.tinybinding.util.WeakReferences.weakListener;

import java.awt.Container;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.fest.reflect.beanproperty.Invoker;
import org.fest.reflect.exception.ReflectionError;

import de.fips.util.tinybinding.ObservableValue;
import de.fips.util.tinybinding.util.Cast;

/**
 * {@link ObservableValue} that can wrap any named property of a Swing Component.
 * <p>
 * <b>Note:</b> All used listeners are added as a {@link WeakReference WeakReferences}, so they gets
 * garbage collected when the time comes.
 *
 * @param <T> Type of the observed property.
 * @see PropertyChangeListener
 * @see ChangeListener
 * @author Philipp Eichhorn
 */
class ObservablePropertyValue<T> extends ObservableComponentValue<T, Container> implements PropertyChangeListener, ChangeListener {
	private final String propertyName;
	private final Class<T> propertyType;
	
	public ObservablePropertyValue(final String propertyName, final Class<T> propertyType, final Container component) {
		super(component);
		this.propertyName = propertyName;
		this.propertyType = propertyType;
		getComponent().addPropertyChangeListener(propertyName, weakListener(PropertyChangeListener.class, this, getComponent()));
		try {
			method("addChangeListener") //
					.withParameterTypes(ChangeListener.class) //
					.in(getComponent()) //
					.invoke(weakListener(ChangeListener.class, this, getComponent()));
		} catch (ReflectionError ignore) {
			// ignore
		}
		guardedUpdateValue();
	}

	@Override
	public void stateChanged(final ChangeEvent event) {
		guardedUpdateValue();
	}

	public void propertyChange(final PropertyChangeEvent event) {
		guardedSetValue(Cast.<T>uncheckedCast(event.getNewValue()));
	}

	@Override
	protected void doSet(final T value) throws VetoException {
		super.doSet(value);
		if (value != null) {
			try {
				getInvoker().set(value);
			} catch (ReflectionError ignore) {
				// ignore
			}
		}
	}

	@Override
	public T getComponentValue() {
		T value = null;
		try {
			value = getInvoker().get();
		} catch (ReflectionError ignore) {
			// ignore
		}
		return value;
	}
	
	private Invoker<T> getInvoker() {
		try {
			return property(propertyName) //
				.ofType(propertyType) //
				.in(getComponent());
		} catch (ReflectionError e) {
			if (hasPrimitive(propertyType)) {
				return Cast.<Invoker<T>>uncheckedCast(property(propertyName) //
					.ofType(getPrimitive(propertyType)) //
					.in(getComponent()));
			} else throw e;
		}
	}
}