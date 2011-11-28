package de.fips.util.tinybinding.junit;

import static org.fest.swing.edt.GuiActionRunner.execute;

import java.awt.Component;
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
	private static class MethodInterceptorImpl<TYPE extends Component> implements MethodInterceptor {
		private final TYPE component;

		@Override
		public Object intercept(final Object obj, final Method method, final Object[] args, final MethodProxy proxy) throws Throwable {
			return execute(new GuiQuery<Object>() {
				@Override
				@SneakyThrows
				protected Object executeInEDT() {
					return method.invoke(component, args);
				}
			});
		}
	}

	public static <TYPE extends Component> TYPE inEDT(final TYPE component) {
		final MethodInterceptor callback = new MethodInterceptorImpl<TYPE>(component);
		return Cast.uncheckedCast(ClassImposterizer.INSTANCE.imposterise(callback, component.getClass(), new Class<?>[0]));
	}
}
