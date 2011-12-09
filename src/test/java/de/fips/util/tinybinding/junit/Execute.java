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
package de.fips.util.tinybinding.junit;

import static org.fest.swing.edt.GuiActionRunner.execute;

import java.lang.reflect.Method;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.core.util.Cast;

import org.fest.swing.edt.GuiQuery;
import org.mockito.cglib.proxy.MethodInterceptor;
import org.mockito.cglib.proxy.MethodProxy;
import org.mockito.internal.creation.jmock.ClassImposterizer;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class Execute {
	@RequiredArgsConstructor
	private static class MethodInterceptorImpl<TYPE> implements MethodInterceptor {
		private final TYPE object;

		@Override
		public Object intercept(final Object obj, final Method method, final Object[] args, final MethodProxy proxy) throws Throwable {
			return execute(new GuiQuery<Object>() {
				@Override
				@SneakyThrows
				protected Object executeInEDT() {
					return method.invoke(object, args);
				}
			});
		}
	}

	public static <TYPE> TYPE inEDT(final TYPE object) {
		final MethodInterceptor callback = new MethodInterceptorImpl<TYPE>(object);
		return Cast.uncheckedCast(ClassImposterizer.INSTANCE.imposterise(callback, object.getClass(), new Class<?>[0]));
	}
}
