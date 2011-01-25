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
import static org.fest.reflect.util.Accessibles.*;

import java.awt.Container;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;

import de.fips.util.tinybinding.DataBindingContext;
import de.fips.util.tinybinding.IObservableValue;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Object that is capable of automaically bind {@link Form}- and {@link Model}-annotated objects.
 * <br>
 * For example:
 * <pre>
 * &#64;Form
 * class TestForm {
 * 	public JTextArea     a = new JTextArea();
 * 	public JToggleButton b = new JToggleButton();
 * 	public JSpinner      c = new JSpinner();
 * }
 *
 * &#64;Model
 * class TestModel {
 * 	public IObservableValue&lt;String&gt;  a = ObservableValue.nil();
 * 	public IObservableValue&lt;Boolean&gt; b = ObservableValue.nil();
 * 	public IObservableValue&lt;Double&gt;  c = ObservableValue.nil();
 * }
 * </pre>
 * Follow this checklist, if you want it to work:
 * <ol>
 * <li>annotate model class with {@code @Model},</li>
 * <li>annotate form class with {@code @Form},</li>
 * <li>fields you want to auto-bind should be public and</li>
 * <li>have the same name in model and form.</li>
 * </ol>
 * @author Philipp Eichhorn
 */
@NoArgsConstructor(access=AccessLevel.PRIVATE)
public final class AutoBinder {
	/**
	 * Binds the model- and form-object.
	 * 
	 * @param modelObject
	 * @param formObject
	 * @return The {@link DataBindingContext} used for the auto-bind.
	 * @throws NullPointerException
	 * @throws IllegalArgumentException
	 * @throws NoSuchFieldException
	 */
	public static DataBindingContext bind(final Object modelObject, final Object formObject) throws NoSuchFieldException {
		verifyNotNull(modelObject, "modelObject");
		verifyNotNull(formObject, "formObject");
		Class<?> modelClazz = modelObject.getClass();
		Class<?> formClazz = formObject.getClass();
		verifyClassAnnotatedWith(modelClazz, Model.class);
		verifyClassAnnotatedWith(formClazz, Form.class);
		Field formField;
		ParameterizedType modelFieldType;
		Class<?> modelFieldInternalType;
		Object formElement;
		DataBindingContext context = new DataBindingContext();
		for (Field modelField : modelClazz.getFields()) {
			// we care for IObservableValues only
			if (!(modelField.getGenericType() instanceof ParameterizedType)) continue;
			modelFieldType = (ParameterizedType) modelField.getGenericType();
			if (!IObservableValue.class.isAssignableFrom((Class<?>) modelFieldType.getRawType())) continue;
			modelFieldInternalType = (Class<?>) modelFieldType.getActualTypeArguments()[0];
			// determine the form representative
			formField = formClazz.getField(modelField.getName());
			boolean accessible = formField.isAccessible();
			try {
				setAccessible(formField, true);
				formElement = formField.get(formObject);
				if (formElement instanceof Container) {
					try {
						Object modelValue = modelField.get(modelObject);
						if (String.class == modelFieldInternalType) {
							IObservableValue<String> value = uncheckedCast(modelValue);
							context.bind(value, observe((Container) formElement).text());
						} else if (Boolean.class == modelFieldInternalType) {
							IObservableValue<Boolean> value = uncheckedCast(modelValue);
							context.bind(value, observe((Container) formElement).selected());
						} else {
							IObservableValue<?> value = (IObservableValue<?>) modelValue;
							context.bind(value, observe((Container) formElement).value());
						}
					} catch(IllegalAccessException e) {
						// can't access model-fields.. no binding.. all good..
					}
				}
			} catch(Exception ignore) {
			} finally {
				setAccessibleIgnoringExceptions(formField, accessible);
			}
		}
		return context;
	}

	private static void verifyNotNull(Object object, String objectString) {
		if (object == null)
			throw new NullPointerException(objectString + " may be null.");
	}
	
	private static <T extends Annotation> void verifyClassAnnotatedWith(Class<?> clazz, Class<T> annotationClazz) {
		if (clazz.getAnnotation(annotationClazz) == null)
			throw new IllegalArgumentException(clazz + " need to be annotated with '@" + annotationClazz + "'.");
	}
}
