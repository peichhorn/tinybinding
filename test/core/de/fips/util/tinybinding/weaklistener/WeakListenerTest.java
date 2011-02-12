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

import static de.fips.util.tinybinding.WeakReferences.weakListener;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import javax.swing.JButton;

import lombok.Getter;
import lombok.NoArgsConstructor;

import org.junit.Test;

import de.fips.util.tinybinding.WeakReferences;

/**
 * Tests the {@link WeakReferences}.
 *
 * @author Philipp Eichhorn
 */
public class WeakListenerTest {
	
	@Test
	public void test_weakActionListener() {
		ActionListener listener = mock(ActionListener.class);
		JButton button = mock(JButton.class);
		ActionListener weakListener = weakListener(ActionListener.class, listener).withTarget(button).get();
		weakListener.actionPerformed(mock(ActionEvent.class));
		verify(listener, times(1)).actionPerformed(any(ActionEvent.class));
		listener = null;
		System.gc();
		weakListener.actionPerformed(mock(ActionEvent.class));
		verify(button, times(1)).removeActionListener(eq(weakListener));
	}
	
	@Test
	public void test_weakPropertyChangeListener() {
		PropertyChangeListener listener = mock(PropertyChangeListener.class);
		SimpleBean bean = mock(SimpleBean.class);
		PropertyChangeListener weakListener = weakListener(PropertyChangeListener.class, listener).withTarget(bean).get();
		weakListener.propertyChange(mock(PropertyChangeEvent.class));
		verify(listener, times(1)).propertyChange(any(PropertyChangeEvent.class));
		listener = null;
		System.gc();
		weakListener.propertyChange(mock(PropertyChangeEvent.class));
		verify(bean, times(1)).removePropertyChangeListener(eq((String) null), eq(weakListener));
	}
	
	@NoArgsConstructor
	public static class SimpleBean {
		@Getter
		private String text;
		private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

		public void addPropertyChangeListener(final String propertyName,
				final PropertyChangeListener listener) {
			propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
		}
		
		public void removePropertyChangeListener(final String propertyName,
				final PropertyChangeListener listener) {
			propertyChangeSupport.removePropertyChangeListener(propertyName, listener);
		}

		public void setText(final String text) {
			propertyChangeSupport.firePropertyChange("text", this.text, this.text = text);
		}
	}
}
