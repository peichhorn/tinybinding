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

import static de.fips.util.tinybinding.util.Cast.uncheckedCast;
import static de.fips.util.tinybinding.Observables.observe;
import static org.fest.reflect.util.Accessibles.setAccessible;
import static org.fest.reflect.util.Accessibles.setAccessibleIgnoringExceptions;

import java.awt.Container;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;

import de.fips.util.tinybinding.DataBindingContext;
import de.fips.util.tinybinding.IObservableValue;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Object that is capable of automatically binding {@link Form @Form}- and {@link Model @Model}-annotated objects.
 * <p>
 * For example:
 * <pre>
 * &#64;Form
 * class TestForm {
 *   public JTextArea     a = new JTextArea();
 *   public JToggleButton b = new JToggleButton();
 *   public JSpinner      c = new JSpinner();
 * }
 *
 * &#64;Model
 * class TestModel {
 *   public IObservableValue&lt;String&gt;  a = ObservableValue.nil();
 *   public IObservableValue&lt;Boolean&gt; b = ObservableValue.nil();
 *   public IObservableValue&lt;Double&gt;  c = ObservableValue.nil();
 * }
 * </pre>
 * <p>
 * Follow this checklist, if you want it to work:
 * <ol>
 * <li>annotate model class with {@code @Model},</li>
 * <li>annotate form class with {@code @Form},</li>
 * <li>fields you want expose the {@link AutoBinder} should be {@code public} and</li>
 * <li>have the same name in model and form.</li>
 * </ol>
 *
 * @author Philipp Eichhorn
 */
@NoArgsConstructor(access=AccessLevel.PRIVATE)
public final class AutoBinder {
	/**
	 * Binds the model and form objects.
	 *
	 * @param modelObject
	 * @param formObject
	 * @return The {@link DataBindingContext} used for the auto-bind.
	 * @throws IllegalArgumentException
	 * @throws NoSuchFieldException
	 */
	public static DataBindingContext bind(final Object modelObject, final Object formObject) throws NoSuchFieldException {
		final Class<?> modelClass = getAnnotatedClass(modelObject, "modelObject", Model.class);
		final Class<?> formClass = getAnnotatedClass(formObject, "formObject", Form.class);

		final DataBindingContext context = new DataBindingContext();
		for (final Field modelField : modelClass.getFields()) {
			final Class<?> observedModelClass = getObservedClass(modelField);
			if (observedModelClass == null) continue; // as we care for nothing else

			final Field formField = formClass.getField(modelField.getName());
			final boolean accessible = formField.isAccessible();
			try {
				setAccessible(formField, true);
				final Object formElement = formField.get(formObject);
				if (formElement instanceof Container) {
					final Object modelValue = modelField.get(modelObject);
					doBind(context, observedModelClass, (Container)formElement, (IObservableValue<?>)modelValue);
				}
			} catch (IllegalArgumentException ignore) {
				// ignore
			} catch (IllegalAccessException ignore) {
				// ignore
			} finally {
				setAccessibleIgnoringExceptions(formField, accessible);
			}
		}
		return context;
	}
	private static void doBind(final DataBindingContext context, final Class<?> observedModelClass, final Container element, final IObservableValue<?> value) {
		if (String.class == observedModelClass) {
			final IObservableValue<String> stringValue = uncheckedCast(value);
			context.bind(stringValue, observe(element).text());
		} else if (Boolean.class == observedModelClass) {
			final IObservableValue<Boolean> booleanValue = uncheckedCast(value);
			context.bind(booleanValue, observe(element).selected());
		} else {
			context.bind(value, observe(element).value());
		}
	}

	private static Class<?> getObservedClass(final Field field) {
		if (field.getGenericType() instanceof ParameterizedType) {
			final ParameterizedType type = (ParameterizedType) field.getGenericType();
			if (IObservableValue.class.isAssignableFrom((Class<?>) type.getRawType())) {
				return (Class<?>) type.getActualTypeArguments()[0];
			}
		}
		return null;
	}

	private static <ANNOTATION_TYPE extends Annotation> Class<?> getAnnotatedClass(final Object object, final String objectName, final Class<ANNOTATION_TYPE> annotationClazz) {
		verifyNotNull(object, objectName);
		Class<?> clazz = object.getClass();
		verifyClassIsAnnotatedWith(clazz, annotationClazz);
		return clazz;
	}

	private static void verifyNotNull(final Object object, final String objectName) {
		if (object == null)
			throw invalid("'%s' may not be null.", objectName);
	}

	private static <ANNOTATION_TYPE extends Annotation> void verifyClassIsAnnotatedWith(final Class<?> clazz, final Class<ANNOTATION_TYPE> annotationClazz) {
		if (clazz.getAnnotation(annotationClazz) == null)
			throw invalid("'%s' needs to be annotated with '@%s'.", clazz.getName(), annotationClazz.getName());
	}

	private static IllegalArgumentException invalid(final String message, final Object... args) {
		return new IllegalArgumentException(String.format(message, args));
	}
}
