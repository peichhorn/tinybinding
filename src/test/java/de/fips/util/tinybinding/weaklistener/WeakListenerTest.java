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

import static de.fips.util.tinybinding.WeakListeners.addWeak;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Action;
import javax.swing.JButton;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import de.fips.util.tinybinding.WeakListeners;

/**
 * Tests {@link WeakListeners}.
 */
@RunWith(JUnit4.class)
public class WeakListenerTest {

	@Test
	public void test_weakActionListener() {
		ActionListener listener = mock(ActionListener.class);
		JButton button = mock(JButton.class);
		ActionListener weakListener = addWeak(ActionListener.class, listener).toTarget(button);
		verify(button, times(1)).addActionListener(eq(weakListener));
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
		Action action = mock(Action.class);
		PropertyChangeListener weakListener = addWeak(PropertyChangeListener.class, listener).toTarget(action);
		verify(action, times(1)).addPropertyChangeListener(eq(weakListener));
		weakListener.propertyChange(mock(PropertyChangeEvent.class));
		verify(listener, times(1)).propertyChange(any(PropertyChangeEvent.class));
		listener = null;
		System.gc();
		weakListener.propertyChange(mock(PropertyChangeEvent.class));
		verify(action, times(1)).removePropertyChangeListener(eq(weakListener));
	}

	@Test
	public void test_weakPropertyChangeListener_withPropertyName() {
		PropertyChangeListener listener = mock(PropertyChangeListener.class);
		JButton button = mock(JButton.class);
		PropertyChangeListener weakListener = addWeak(PropertyChangeListener.class, listener).withPropertyName("text").toTarget(button);
		verify(button, times(1)).addPropertyChangeListener(eq("text"), eq(weakListener));
		weakListener.propertyChange(mock(PropertyChangeEvent.class));
		verify(listener, times(1)).propertyChange(any(PropertyChangeEvent.class));
		listener = null;
		System.gc();
		weakListener.propertyChange(mock(PropertyChangeEvent.class));
		verify(button, times(1)).removePropertyChangeListener(eq("text"), eq(weakListener));
	}
}
