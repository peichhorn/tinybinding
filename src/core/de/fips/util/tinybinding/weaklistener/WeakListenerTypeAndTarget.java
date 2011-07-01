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
package de.fips.util.tinybinding.weaklistener;

import static org.fest.reflect.core.Reflection.method;

import java.awt.event.ActionListener;
import java.awt.event.ComponentListener;
import java.awt.event.FocusListener;
import java.beans.PropertyChangeListener;

import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionListener;

import org.fest.reflect.exception.ReflectionError;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class WeakListenerTypeAndTarget<S, T extends S> {
	private final Class<S> listenerType;
	private final T listener;
	final Object target;

	public S get() {
		final Object weakListener;
		if (ActionListener.class.isAssignableFrom(listenerType)) {
			weakListener = new WeakActionListener(target, (ActionListener)listener, listenerType);
		} else if (ChangeListener.class.isAssignableFrom(listenerType)) {
			weakListener = new WeakChangeListener(target, (ChangeListener)listener, listenerType);
		} else if (ComponentListener.class.isAssignableFrom(listenerType)) {
			weakListener = new WeakComponentListener(target, (ComponentListener)listener, listenerType);
		} else if (DocumentListener.class.isAssignableFrom(listenerType)) {
			weakListener = new WeakDocumentListener(target, (DocumentListener)listener, listenerType);
		} else if (FocusListener.class.isAssignableFrom(listenerType)) {
			weakListener = new WeakFocusListener(target, (FocusListener)listener, listenerType);
		} else if (ListSelectionListener.class.isAssignableFrom(listenerType)) {
			weakListener = new WeakListSelectionListener(target, (ListSelectionListener)listener, listenerType);
		} else if (PropertyChangeListener.class.isAssignableFrom(listenerType)) {
			weakListener = new WeakPropertyChangeListener(target, (PropertyChangeListener)listener, listenerType);
		} else {
			throw new IllegalArgumentException("Unknown listener type '" + listenerType.getName() + "'.");
		}
		return listenerType.cast(weakListener);
	}

	public S add() {
		final S listener = get();
		try {
			method("add" + listenerType.getSimpleName()).withParameterTypes(listenerType).in(target).invoke(listener);
		} catch (ReflectionError ignore) {
			// ignore
		}
		return listener;
	}
}
