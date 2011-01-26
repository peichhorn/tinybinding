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
package de.fips.util.tinybinding.util;

import static org.fest.reflect.core.Reflection.method;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.fest.reflect.exception.ReflectionError;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Creates all kinds of useful {@link WeakReference WeakReferences}.
 * 
 * @author Philipp Eichhorn
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class WeakReferences {
	
	public static <T> T weakListener(final Class<T> listenerType, final T listener, final Object source) {
		Object weakListener = null;
		if (ActionListener.class.isAssignableFrom(listenerType)) {
			weakListener = new WeakActionListener(source, (ActionListener)listener, listenerType);
		} else if (ChangeListener.class.isAssignableFrom(listenerType)) {
			weakListener = new WeakChangeListener(source, (ChangeListener)listener, listenerType);
		} else if (ComponentListener.class.isAssignableFrom(listenerType)) {
			weakListener = new WeakComponentListener(source, (ComponentListener)listener, listenerType);
		} else if (DocumentListener.class.isAssignableFrom(listenerType)) {
			weakListener = new WeakDocumentListener(source, (DocumentListener)listener, listenerType);
		} else if (FocusListener.class.isAssignableFrom(listenerType)) {
			weakListener = new WeakFocusListener(source, (FocusListener)listener, listenerType);
		} else if (ListSelectionListener.class.isAssignableFrom(listenerType)) {
			weakListener = new WeakListSelectionListener(source, (ListSelectionListener)listener, listenerType);
		} else if (PropertyChangeListener.class.isAssignableFrom(listenerType)) {
			weakListener = new WeakPropertyChangeListener(source, (PropertyChangeListener)listener, listenerType);
		}
		if (weakListener == null) {
			throw new IllegalArgumentException("Unknown listener type '" + listenerType + "'.");
		}
		return listenerType.cast(weakListener);
	}

	private static class WeakActionListener extends AbstractWeakListener<ActionListener, ActionEvent> implements ActionListener {
		public WeakActionListener(final Object source, final ActionListener listener, final Class<?> listenerType) {
			super(source, listener, listenerType);
		}

		@Override
		public void actionPerformed(final ActionEvent event) {
			ActionListener listener = get(event);
			if (listener != null) {
				listener.actionPerformed(event);
			}
		}
	}
	
	private static class WeakChangeListener extends AbstractWeakListener<ChangeListener, ChangeEvent> implements ChangeListener {
		public WeakChangeListener(final Object source, final ChangeListener listener, final Class<?> listenerType) {
			super(source, listener, listenerType);
		}

		@Override
		public void stateChanged(final ChangeEvent event) {
			ChangeListener listener = get(event);
			if (listener != null) {
				listener.stateChanged(event);
			}
		}
	}
	
	private static class WeakComponentListener extends AbstractWeakListener<ComponentListener, ComponentEvent> implements ComponentListener {
		public WeakComponentListener(final Object source, final ComponentListener listener, final Class<?> listenerType) {
			super(source, listener, listenerType);
		}

		@Override
		public void componentResized(final ComponentEvent event) {
			ComponentListener listener = get(event);
			if (listener != null) {
				listener.componentResized(event);
			}
		}

		@Override
		public void componentMoved(final ComponentEvent event) {
			ComponentListener listener = get(event);
			if (listener != null) {
				listener.componentMoved(event);
			}
		}

		@Override
		public void componentShown(final ComponentEvent event) {
			ComponentListener listener = get(event);
			if (listener != null) {
				listener.componentShown(event);
			}
		}

		@Override
		public void componentHidden(final ComponentEvent event) {
			ComponentListener listener = get(event);
			if (listener != null) {
				listener.componentHidden(event);
			}
		}	
	}
	
	private static class WeakDocumentListener extends AbstractWeakListener<DocumentListener, DocumentEvent> implements DocumentListener {
		public WeakDocumentListener(final Object source, final DocumentListener listener, final Class<?> listenerType) {
			super(source, listener, listenerType);
		}

		@Override
		public void insertUpdate(final DocumentEvent event) {
			DocumentListener listener = get(event);
			if (listener != null) {
				listener.insertUpdate(event);
			}
		}

		@Override
		public void removeUpdate(final DocumentEvent event) {
			DocumentListener listener = get(event);
			if (listener != null) {
				listener.removeUpdate(event);
			}
		}

		@Override
		public void changedUpdate(final DocumentEvent event) {
			DocumentListener listener = get(event);
			if (listener != null) {
				listener.changedUpdate(event);
			}
		}
	}
	
	private static class WeakFocusListener extends AbstractWeakListener<FocusListener, FocusEvent> implements FocusListener {
		public WeakFocusListener(final Object source, final FocusListener listener, final Class<?> listenerType) {
			super(source, listener, listenerType);
		}

		@Override
		public void focusGained(final FocusEvent event) {
			FocusListener listener = get(event);
			if (listener != null) {
				listener.focusGained(event);
			}
		}

		@Override
		public void focusLost(final FocusEvent event) {
			FocusListener listener = get(event);
			if (listener != null) {
				listener.focusLost(event);
			}	
		}
	}
	
	private static class WeakListSelectionListener extends AbstractWeakListener<ListSelectionListener, ListSelectionEvent> implements ListSelectionListener {
		public WeakListSelectionListener(final Object source, final ListSelectionListener listener, final Class<?> listenerType) {
			super(source, listener, listenerType);
		}

		@Override
		public void valueChanged(final ListSelectionEvent event) {
			ListSelectionListener listener = get(event);
			if (listener != null) {
				listener.valueChanged(event);
			}
		}
	}
	
	private static class WeakPropertyChangeListener extends AbstractWeakListener<PropertyChangeListener, PropertyChangeEvent> implements PropertyChangeListener {
		public WeakPropertyChangeListener(final Object source, final PropertyChangeListener listener, final Class<?> listenerType) {
			super(source, listener, listenerType);
		}

		public void propertyChange(final PropertyChangeEvent event) {
			PropertyChangeListener listener = get(event);
			if (listener != null) {
				listener.propertyChange(event);
			}
		}

		@Override
		protected void removeListener(final PropertyChangeEvent event) {
			super.removeListener(event);
			try {
				method("removePropertyChangeListener") //
						.withParameterTypes(String.class, PropertyChangeListener.class) //
						.in(event.getSource()) //
						.invoke(event.getPropertyName(), this);
			} catch (ReflectionError ignore) {
				// ignore
			}
		}
	}
	
	private static abstract class AbstractWeakListener<T, E> {
		private final WeakReference<T> delegate;
		private final Class<?> listenerType;
		private final Object source;

		protected AbstractWeakListener(final Object source, final T listener, final Class<?> listenerType) {
			this.source = source;
			delegate = new WeakReference<T>(listener);
			this.listenerType = listenerType;
		}
		
		protected void removeListener(final E event) {
			try {
				method("remove" + listenerType.getSimpleName()) //
						.withParameterTypes(listenerType) //
						.in(source) //
						.invoke(this);
			} catch (ReflectionError ignore) {
				// ignore
			}
		}
		
		protected T get(final E event) {
			T listener = delegate.get();
			if (listener == null) {
				removeListener(event);
			}
			return listener;
		}
	}
}
