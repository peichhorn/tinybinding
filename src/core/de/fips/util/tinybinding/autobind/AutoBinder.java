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
package de.fips.util.tinybinding.autobind;

import static java.util.Arrays.asList;
import static org.fest.reflect.core.Reflection.field;
import static org.fest.reflect.util.Accessibles.*;

import java.awt.Container;
import java.beans.BeanInfo;
import java.beans.FeatureDescriptor;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.ref.Reference;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.fips.util.tinybinding.BindingContexts;
import de.fips.util.tinybinding.Bindings;
import de.fips.util.tinybinding.IBindingContext;
import de.fips.util.tinybinding.IObservableValue;
import de.fips.util.tinybinding.Observables;
import de.fips.util.tinybinding.swing.SwingObservable;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 * Object that is capable of automatically binding {@link Bindable @Bindable}- and
 * {@link SwingBindable @SwingBindable}-annotated objects.
 * <p>
 * For example:
 * 
 * <pre>
 * class TestForm {
 *   &#064;SwingBindable
 *   public JTextArea a = new JTextArea();
 *   &#064;SwingBindable
 *   public JToggleButton b = new JToggleButton();
 *   &#064;SwingBindable
 *   public JSpinner c = new JSpinner();
 * }
 * 
 * &#064;Bindable
 * class TestModel {
 *   public IObservableValue&lt;String&gt; a = ObservableValue.nil();
 *   public IObservableValue&lt;Boolean&gt; b = ObservableValue.nil();
 *   public IObservableValue&lt;Double&gt; c = ObservableValue.nil();
 * }
 * </pre>
 * 
 * @author Philipp Eichhorn
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AutoBinder {

	public static IBindingContext bind(final Object pojoA, final Object pojoB) throws NoSuchFieldException {
		return new AutoBinder().bindPojo(pojoA, pojoB);
	}

	private IBindingContext bindPojo(final Object pojoA, final Object pojoB) throws NoSuchFieldException {
		verifyNotNull(pojoA, "pojoA");
		verifyNotNull(pojoB, "pojoB");
		final Map<String, BindingData> bindingsA = bindableFieldsOf(pojoA);
		final Map<String, BindingData> bindingsB = bindableFieldsOf(pojoB);
		final IBindingContext context = BindingContexts.defaultContext();
		for (final BindingData bindingA : bindingsA.values()) {
			final BindingData bindingB = bindingsB.get(bindingA.getName());
			if (bindingB == null) continue;

			final IObservableValue<?> observableValueA = observableValueFor(pojoA, bindingA);
			final IObservableValue<?> observableValueB = observableValueFor(pojoB, bindingB);

			Bindings.bind(observableValueA).to(observableValueB).in(context);
			bindingA.setComplete(true);
			bindingB.setComplete(true);
		}
		validate(pojoA, pojoB, bindingsA.values(), bindingsB.values());
		return context;
	}

	private IObservableValue<?> observableValueFor(final Object pojo, final BindingData binding) throws NoSuchFieldException {
		final Field field = binding.getField();
		final boolean accessible = field.isAccessible();
		try {
			setAccessible(field, true);
			final Class<?> type = field.getType();
			final IObservableValue<?> observableValue;
			if (IObservableValue.class.isAssignableFrom(type)) {
				observableValue = (IObservableValue<?>) field.get(pojo);
			} else if (Container.class.isAssignableFrom(type)) {
				observableValue = (IObservableValue<?>) SwingObservable.class.getMethod(binding.hint).invoke(Observables.observe((Container) field.get(pojo)));
			} else {
				observableValue = Observables.observe(pojo).property(field.getName(), type);
			}
			return observableValue;
		} catch (Exception ignore) {
			throw new NoSuchFieldException();
		} finally {
			setAccessibleIgnoringExceptions(field, accessible);
		}
	}

	private Map<String, BindingData> bindableFieldsOf(final Object pojo) {
		final Map<String, BindingData> bindableFields = new HashMap<String, BindingData>();
		final Class<?> type = pojo.getClass();
		BeanInfo beanInfo = null;
		try {
			beanInfo = Introspector.getBeanInfo(type, Object.class);
		} catch (IntrospectionException e) {
			throw new IllegalStateException(e);
		}
		final PropertyDescriptor[] descriptors = beanInfo.getPropertyDescriptors();
		for (PropertyDescriptor descriptor : descriptors) {
			final Class<?> declaringType = getDeclaringClass(descriptor);
			if (declaringType == null) continue;
			final boolean bindAllFields = declaringType.isAnnotationPresent(Bindable.class);
			final Field field = getDeclaredField(declaringType, descriptor.getName());
			if (field == null) continue;
			BindingData data = null;
			if (Container.class.isAssignableFrom(field.getType())) {
				data = swingBindableFieldFor(field, bindAllFields);
			} else {
				data = bindableFieldFor(field, bindAllFields);
			}
			if (data == null) continue;
			if (bindableFields.put(data.getName(), data) != null) {
				throw invalid("The field name '%s' is used more than once.", data.getName());
			}
		}
		final boolean bindAllFields = type.isAnnotationPresent(Bindable.class);
		for (Field field : type.getDeclaredFields()) {
			if (field.getName().startsWith("$")) continue;
			if (field.getName().equals("serialVersionUID")) continue;
			BindingData data = null;
			if (Container.class.isAssignableFrom(field.getType())) {
				data = swingBindableFieldFor(field, bindAllFields);
			} else {
				data = bindableFieldFor(field, bindAllFields);
			}
			if (data == null) continue;
			if (bindableFields.containsKey(data.getName())) continue;
			bindableFields.put(data.getName(), data);
		}
		
		return bindableFields;
	}
	
	private Class<?> getDeclaringClass(final PropertyDescriptor descriptor) {
		final Field refClassField = getDeclaredField(FeatureDescriptor.class, "classRef");
		if (refClassField == null) return null;
		final boolean accessible = refClassField.isAccessible();
		try {
			setAccessible(refClassField, true);
			final Reference<?> refClass = field("classRef").ofType(Reference.class).in(descriptor).get();
			return (refClass == null) ? null : (Class<?>)refClass.get();
		} finally {
			setAccessibleIgnoringExceptions(refClassField, accessible);
		}
	}
	
	private Field getDeclaredField(final Class<?> type, final String fieldName) {
		try {
			return type.getDeclaredField(fieldName);
		} catch (NoSuchFieldException e) {
			return null;
		}
	}

	private BindingData bindableFieldFor(final Field field, final boolean bindAllFields) {
		final Bindable bindable = field.getAnnotation(Bindable.class);
		if ((bindable == null) && !bindAllFields) return null;
		final String name = ((bindable == null) || bindable.name().isEmpty()) ? field.getName() : bindable.name();
		return new BindingData(field, name, null);
	}
	
	private BindingData swingBindableFieldFor(final Field field, final boolean bindAllFields) {
		final SwingBindable swingBindable = field.getAnnotation(SwingBindable.class);
		if (swingBindable == null) {
			if (bindAllFields || field.isAnnotationPresent(Bindable.class)) {
				throw invalid("Field '%s' should be annotated with @%s.", field, SwingBindable.class.getSimpleName());
			}
			return null;
		}
		final String name = (swingBindable.name().isEmpty()) ? field.getName() : swingBindable.name();
		final String hint = swingBindable.hint();
		sanatizeHint(field, hint);
		return new BindingData(field, name, hint);
	}
	
	private void sanatizeHint(final Field field, final String hint) {
		final List<String> validHints = asList("background", "bounds", "editable", "enabled", "selected", "focus", "foreground", "title", "text", "tooltip", "value");
		if (!validHints.contains(hint)) {
			throw invalid("Invalid hint '%s' used for field '%s'.\nOnly the following hints are allowed:\n\t%s", hint, field, validHints);
		}
	}

	private void validate(final Object pojoA, final Object pojoB, final Collection<BindingData> bindingsA, final Collection<BindingData> bindingsB) throws NoSuchFieldException {

		final StringBuilder builder = new StringBuilder();
		for (final BindingData binding : bindingsA) {
			if (binding.isComplete()) continue;
			builder.append("\t").append(binding.getField().getName()).append(" - ???\n");
		}
		for (final BindingData binding : bindingsB) {
			if (binding.isComplete()) continue;
			builder.append("\t??? - ").append(binding.getField().getName()).append("\n");
		}
		if (builder.length() > 0) {
			builder.insert(0, "\n").insert(0,pojoB.getClass().getName()).insert(0, " - ").insert(0,pojoA.getClass().getName());
			builder.insert(0,"unresolved bingings:\n\n\t");
			throw new NoSuchFieldException(builder.toString());
		}
	}

	private void verifyNotNull(final Object object, final String objectName) {
		if (object == null)
			throw invalid("'%s' may not be null.", objectName);
	}

	private IllegalArgumentException invalid(final String message, final Object... args) {
		return new IllegalArgumentException(String.format(message, args));
	}

	@Getter
	@RequiredArgsConstructor
	private static class BindingData {
		private final Field field;
		private final String name;
		private final String hint;
		@Setter
		private boolean complete;
	}
}
