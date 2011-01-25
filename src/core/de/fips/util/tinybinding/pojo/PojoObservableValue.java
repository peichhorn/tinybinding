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
import static de.fips.util.tinybinding.util.Reflection.getPrimitive;
import static de.fips.util.tinybinding.util.Reflection.hasPrimitive;
import static de.fips.util.tinybinding.util.WeakReferences.weakListener;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;

import org.fest.reflect.beanproperty.Invoker;
import org.fest.reflect.exception.ReflectionError;

import de.fips.util.tinybinding.ObservableValue;
import de.fips.util.tinybinding.util.Cast;
import de.fips.util.tinybinding.util.WeakReferences;

/**
 * {@link ObservableValue} implementation for POJOs.
 * <p>
 * Per default the {@link ObservableValue} is only able to receive value-changes but not to submit them.
 * <br>
 * If a {@link PropertyChangeListener} can be registered to the object the {@link ObservableValue} can
 * both receive and sumbit value-changes.
 * <p>
 * <b>Note:</b> The {@link PropertyChangeListener} is added as a {@link WeakReference}, so it gets
 * garbage collected when the time comes.
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
	
	public PojoObservableValue(final Object pojo, final String propertyName, final Class<T> propertyType) {
		this.pojo = pojo;
		this.propertyName = propertyName;
		this.propertyType = propertyType;
		try {
			method("addPropertyChangeListener") //
				.withParameterTypes(String.class, PropertyChangeListener.class) //
				.in(pojo) //
				.invoke(propertyName, weakListener(PropertyChangeListener.class, this, pojo));
		} catch (ReflectionError ignore) {
			// ignore
		}
		guardedSetValue(getPojoValue());
	}
	
	public void propertyChange(final PropertyChangeEvent event) {
		guardedSetValue(Cast.<T>uncheckedCast(event.getNewValue()));
	}
	
	protected void guardedSetValue(final T value) {
		propertyChange = true;
		set(value);
		propertyChange = false;
	}
	
	protected T getPojoValue() {
		T value = null;
		try {
			value = getInvoker().get();
		} catch (ReflectionError ignore) {
			// ignore
		}
		return value;
	}
	
	@Override
	protected void doSet(final T value) throws VetoException {
		super.doSet(value);
		if (propertyChange) {
			throw new VetoException();
		}	
		try {
			getInvoker().set(value);
		} catch (ReflectionError ignore) {
			// ignore
		}
	}
	
	private Invoker<T> getInvoker() {
		try {
			return property(propertyName) //
				.ofType(propertyType) //
				.in(pojo);
		} catch (ReflectionError e) {
			if (hasPrimitive(propertyType)) {
				return Cast.<Invoker<T>>uncheckedCast(property(propertyName) //
					.ofType(getPrimitive(propertyType)) //
					.in(pojo));
			} else throw e;
		}
	}
}