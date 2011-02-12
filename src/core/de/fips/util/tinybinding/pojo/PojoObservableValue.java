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
package de.fips.util.tinybinding.pojo;

import static org.fest.reflect.core.Reflection.method;
import static org.fest.reflect.core.Reflection.property;
import static de.fips.util.tinybinding.WeakReferences.weakListener;
import static de.fips.util.tinybinding.util.Cast.uncheckedCast;
import static de.fips.util.tinybinding.util.Reflection.getPrimitive;
import static de.fips.util.tinybinding.util.Reflection.hasPrimitive;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.fest.reflect.beanproperty.Invoker;
import org.fest.reflect.exception.ReflectionError;

import de.fips.util.tinybinding.ObservableValue;
import de.fips.util.tinybinding.WeakReferences;
import de.fips.util.tinybinding.util.Cast;

/**
 * {@link ObservableValue} implementation for POJOs.
 * <p>
 * Per default the {@link ObservableValue} is only able to receive value-changes but not to submit them.
 * <br>
 * If a {@link PropertyChangeListener} can be registered to the object the {@link ObservableValue} can
 * both receive and sumbit value-changes.
 * <p>
 * <b>Note:</b> The {@link PropertyChangeListener} is added as a {@link java.lang.ref.WeakReference WeakReference},
 * so it gets garbage collected when the time comes.
 *
 * @param <T> Type of the observed POJO field
 * @see WeakReferences
 * @author Philipp Eichhorn
 */
class PojoObservableValue<T> extends ObservableValue<T> implements PropertyChangeListener {
	private final Object pojo;
	private final String propertyName;
	private final Class<T> propertyType;
	private volatile boolean propertyChange;

	PojoObservableValue(final Object pojo, final String propertyName, final Class<T> propertyType) {
		this.pojo = pojo;
		this.propertyName = propertyName;
		this.propertyType = propertyType;
		try {
			method("addPropertyChangeListener").withParameterTypes(String.class, PropertyChangeListener.class).in(pojo) //
				.invoke(propertyName, weakListener(PropertyChangeListener.class, this).withTarget(pojo).get());
		} catch (ReflectionError ignore) {
			// ignore
		}
		guardedSetValue(getPojoValue());
	}

	@Override
	public void propertyChange(final PropertyChangeEvent event) {
		guardedSetValue(Cast.<T>uncheckedCast(event.getNewValue()));
	}

	protected void guardedSetValue(final T value) {
		propertyChange = true;
		set(value);
		propertyChange = false;
	}

	protected T getPojoValue() {
		try {
			return getInvoker().get();
		} catch (ReflectionError ignore) {
			return null;
		}
	}

	@Override
	protected void doSet(final T value) {
		if (!propertyChange) {
			try {
				getInvoker().set(value);
			} catch (ReflectionError ignore) {
				// ignore
			}
		}
	}

	private Invoker<T> getInvoker() {
		try {
			return property(propertyName).ofType(propertyType).in(pojo);
		} catch (ReflectionError e) {
			if (hasPrimitive(propertyType)) {
				final Invoker<T> invoker = uncheckedCast(property(propertyName).ofType(getPrimitive(propertyType)).in(pojo));
				return invoker;
			} else throw e;
		}
	}
}