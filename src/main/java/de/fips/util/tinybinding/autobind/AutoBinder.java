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
package de.fips.util.tinybinding.autobind;

import static java.util.Arrays.asList;
import static org.fest.reflect.util.Accessibles.*;

import java.awt.Container;
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
 *   &#064;SwingBindable(hint="text")
 *   public JTextArea a = new JTextArea();
 *   &#064;SwingBindable(hint="selected")
 *   public JToggleButton b = new JToggleButton();
 *   &#064;SwingBindable(hint="value")
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

	public static IBindingContext bind(final Object pojoA, final Object pojoB) throws UnresolvedBindingException {
		return new AutoBinder().bindPojo(pojoA, pojoB);
	}

	private IBindingContext bindPojo(final Object pojoA, final Object pojoB) throws UnresolvedBindingException {
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

	private IObservableValue<?> observableValueFor(final Object pojo, final BindingData binding) throws UnresolvedBindingException {
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
		} catch (Exception e) {
			throw new UnresolvedBindingException(e);
		} finally {
			setAccessibleIgnoringExceptions(field, accessible);
		}
	}

	private Map<String, BindingData> bindableFieldsOf(final Object pojo) {
		final Map<String, BindingData> bindableFields = new HashMap<String, BindingData>();
		fillBindableFields(pojo.getClass(), bindableFields);
		return bindableFields;
	}

	private void fillBindableFields(final Class<?> type, final Map<String, BindingData> bindableFields) {
		for (Field field : type.getDeclaredFields()) { 
			final BindingData data;
			if (field.isAnnotationPresent(SwingBindable.class)) {
				data = swingBindableFieldFor(field);
			} else if (field.isAnnotationPresent(Bindable.class)) {
				if (Container.class.isAssignableFrom(field.getType())) {
					throw invalid("Field '%s' should be annotated with @%s.", field, SwingBindable.class.getSimpleName());
				}
				data = bindableFieldFor(field);
			} else {
				continue;
			}
			bindableFields.put(data.getName(), data);
		}
		final Class<?> superType = type.getSuperclass();
		if (superType != null) {
			fillBindableFields(superType, bindableFields);
		}
	}

	private BindingData bindableFieldFor(final Field field) {
		final Bindable bindable = field.getAnnotation(Bindable.class);
		final String name = bindable.name().isEmpty() ? field.getName() : bindable.name();
		return new BindingData(field, name, null);
	}

	private BindingData swingBindableFieldFor(final Field field) {
		final SwingBindable swingBindable = field.getAnnotation(SwingBindable.class);
		final String name = swingBindable.name().isEmpty() ? field.getName() : swingBindable.name();
		final String hint = swingBindable.hint();
		return new BindingData(field, name, sanatizeHint(field, hint));
	}

	private String sanatizeHint(final Field field, final String hint) {
		final List<String> validHints = asList("background", "bounds", "editable", "enabled", "selected", "focus", "foreground", "title", "text", "tooltip", "value");
		if (validHints.contains(hint)) return hint;
		throw invalid("Invalid hint '%s' used for field '%s'.\nOnly the following hints are allowed:\n\t%s", hint, field, validHints);
	}

	private void validate(final Object pojoA, final Object pojoB, final Collection<BindingData> bindingsA, final Collection<BindingData> bindingsB) throws UnresolvedBindingException {
		final StringBuilder builder = new StringBuilder();
		for (final BindingData binding : bindingsA) {
			if (binding.isComplete()) continue;
			builder.append("\t").append(fieldSignature(binding.getField())).append(" - ???\n");
		}
		for (final BindingData binding : bindingsB) {
			if (binding.isComplete()) continue;
			builder.append("\t??? - ").append(fieldSignature(binding.getField())).append("\n");
		}
		if (builder.length() > 0) {
			builder.insert(0, "\n").insert(0,pojoB.getClass().getName()).insert(0, " - ").insert(0,pojoA.getClass().getName());
			builder.insert(0,"unresolved bingings:\n\n\t");
			throw new UnresolvedBindingException(builder.toString());
		}
	}

	private String fieldSignature(final Field field) {
		return field.getType().getName() + " " + field.getName();
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
