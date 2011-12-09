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
package de.fips.util.tinybinding.weaklistener;

import static org.fest.reflect.core.Reflection.method;

import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import lombok.Rethrow;
import lombok.core.util.Cast;

import org.fest.reflect.exception.ReflectionError;

/**
 *
 * @param <LISTENER_TYPE>
 * @param <TYPE>
 * @author Philipp Eichhorn
 */
class WeakListenerHandler<LISTENER_TYPE, TYPE extends LISTENER_TYPE> implements InvocationHandler {
	private static final Method OBJECT_EQUALS = getObjectMethod("equals", Object.class);

	private final WeakReference<LISTENER_TYPE> weakListener;
	private final Class<LISTENER_TYPE> listenerType;
	private final Object target;
	private final String propertyName;
	private final boolean throwException;

	private WeakListenerHandler(final Object target, final Class<LISTENER_TYPE> listenerType, final TYPE listener, final boolean throwException, final String propertyName) {
		this.listenerType = listenerType;
		this.target = target;
		this.throwException = throwException;
		this.propertyName = propertyName;
		weakListener = new WeakReference<LISTENER_TYPE>(listener);
	}

	private final void addListener(final Object proxy) {
		try {
			if (propertyName != null) {
				method("add" + listenerType.getSimpleName()).withParameterTypes(String.class, listenerType).in(target).invoke(propertyName, proxy);
			} else {
				method("add" + listenerType.getSimpleName()).withParameterTypes(listenerType).in(target).invoke(proxy);
			}
		} catch (ReflectionError e) {
			if (throwException) throw new IllegalStateException(String.format("Unable to add weak '%s' to object of type '%s'.", listenerType, target.getClass()), e);
		}
	}

	private final void removeListener(final Object proxy) {
		try {
			if (propertyName != null) {
				method("remove" + listenerType.getSimpleName()).withParameterTypes(String.class, listenerType).in(target).invoke(propertyName, proxy);
			} else {
				method("remove" + listenerType.getSimpleName()).withParameterTypes(listenerType).in(target).invoke(proxy);
			}
		} catch (ReflectionError e) {
			if (throwException) throw new IllegalStateException(String.format("Unable to remove weak '%s' from object of type '%s'.", listenerType, target.getClass()), e);
		}
	}

	@Override
	public final Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
		final LISTENER_TYPE listener = weakListener.get();
		if (OBJECT_EQUALS.equals(method)) {
			final Object other = args[0];
			if (other == null) return false;
			if (!Proxy.isProxyClass(other.getClass())) return false;
			final InvocationHandler handler = Proxy.getInvocationHandler(other);
			return (handler instanceof WeakListenerHandler<?, ?>) && (((WeakListenerHandler<?, ?>) handler).weakListener.get() == listener);
		}
		if (listener == null) {
			removeListener(proxy);
			return null;
		} else {
			try {
				return method.invoke(listener, args);
			} catch (InvocationTargetException e) {
				throw e.getTargetException();
			}
		}
	}

	@Rethrow(value = NoSuchMethodException.class, as = IllegalStateException.class)
	private static Method getObjectMethod(String name, Class<?>... types) {
		return Object.class.getMethod(name, types);
	}

	static <LISTENER_TYPE, TYPE extends LISTENER_TYPE> LISTENER_TYPE addWeakListener(final Object target, final Class<LISTENER_TYPE> listenerType, final TYPE listener, final boolean throwException, final String propertyName) {
		final WeakListenerHandler<LISTENER_TYPE, TYPE> handler = new WeakListenerHandler<LISTENER_TYPE, TYPE>(target, listenerType, listener, throwException, propertyName);
		final LISTENER_TYPE proxy = Cast.uncheckedCast(Proxy.newProxyInstance(WeakListenerHandler.class.getClassLoader(), new Class[] { listenerType }, handler));
		handler.addListener(proxy);
		return proxy;
	}
}
