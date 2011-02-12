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
package de.fips.util.tinybinding.swing;

import static de.fips.util.tinybinding.util.Cast.uncheckedCast;
import static de.fips.util.tinybinding.Observables.observe;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Rectangle;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.SpinnerNumberModel;
import javax.swing.text.PlainDocument;
import javax.swing.text.StringContent;

import junit.extensions.abbot.ComponentTestFixture;

import org.junit.Before;
import org.junit.Test;

import de.fips.util.tinybinding.IObservableValue;
import de.fips.util.tinybinding.IValueObserver;

/**
 * Tests the {@link SwingObservables}
 *
 * @author Philipp Eichhorn
 */
public class SwingObservableTest extends ComponentTestFixture {
	protected JFrame frame;
	protected JButton button1;
	protected JButton button2;
	protected JPanel mainPanel;
	protected JTextArea textArea;
	protected JComboBox combobox;
	protected JSpinner spinner;
	protected JList list;

	@Before
	@Override
	public void setUp() throws Exception {
		super.setUp();
		textArea = new JTextArea();
		button1 = new JButton("Unfocused Button");
		button2 = new JButton("Focused Button");
		list = new JList();
		DefaultListModel listModel = new DefaultListModel();
		listModel.addElement("Item 1");
		listModel.addElement("Item 2");
		listModel.addElement("Item 3");
		list.setModel(listModel);
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BorderLayout());
		buttonPanel.add(button1, BorderLayout.WEST);
		buttonPanel.add(list, BorderLayout.CENTER);
		buttonPanel.add(button2, BorderLayout.EAST);
		combobox = new JComboBox();
		DefaultComboBoxModel model = new DefaultComboBoxModel();
		model.addElement("Value 1");
		model.addElement("Value 2");
		model.addElement("Value 3");
		combobox.setModel(model);
		spinner = new JSpinner();
		spinner.setModel(new SpinnerNumberModel(Long.valueOf(0), Long.valueOf(0), Long.valueOf(100), Long.valueOf(1)));
		JPanel buttonPanel2 = new JPanel();
		buttonPanel2.setLayout(new BorderLayout());
		buttonPanel2.add(combobox, BorderLayout.WEST);
		buttonPanel2.add(spinner, BorderLayout.EAST);
		mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		mainPanel.add(textArea, BorderLayout.NORTH);
		mainPanel.add(buttonPanel, BorderLayout.CENTER);
		mainPanel.add(buttonPanel2, BorderLayout.SOUTH);
		frame = new JFrame();
		frame.setLayout(new BorderLayout());
		frame.getContentPane().add(mainPanel, BorderLayout.CENTER);
		frame.pack();
		frame.setVisible(true);
		frame.setAlwaysOnTop(true);
		button2.requestFocusInWindow();
		getRobot().waitForIdle();
	}

	@Test
	public void test_observeBackground() {
		IObservableValue<Color> background = observe(textArea).background();
		IValueObserver<Color> observer = uncheckedCast(mock(IValueObserver.class));
		background.addObserver(observer);
		verify(observer, times(1)).valueChanged(any(Color.class), eq((Color) null));
		textArea.setBackground(Color.red);
		getRobot().waitForIdle();
		verify(observer, times(1)).valueChanged(eq(Color.red), any(Color.class));
	}

	@Test
	public void test_observeBounds() {
		IObservableValue<Rectangle> bounds = observe(mainPanel).bounds();
		IValueObserver<Rectangle> observer = uncheckedCast(mock(IValueObserver.class));
		bounds.addObserver(observer);
		verify(observer, times(1)).valueChanged(any(Rectangle.class), eq((Rectangle) null));
		mainPanel.setBounds(new Rectangle(0, 0, 40, 40));
		getRobot().waitForIdle();
		verify(observer, times(1)).valueChanged(eq(new Rectangle(0, 0, 40, 40)), any(Rectangle.class));
		mainPanel.setBounds(new Rectangle(20, 20, 40, 40));
		getRobot().waitForIdle();
		verify(observer, times(1)).valueChanged(eq(new Rectangle(20, 20, 40, 40)), eq(new Rectangle(0, 0, 40, 40)));
	}

	@Test
	public void test_observeEditable() {
		IObservableValue<Boolean> editable = observe(textArea).editable();
		IValueObserver<Boolean> observer = uncheckedCast(mock(IValueObserver.class));
		editable.addObserver(observer);
		verify(observer, times(1)).valueChanged(any(Boolean.class), eq((Boolean) null));
		textArea.setEditable(false);
		getRobot().waitForIdle();
		verify(observer, times(1)).valueChanged(eq(Boolean.FALSE), any(Boolean.class));
	}

	@Test
	public void test_observeEnabled() {
		IObservableValue<Boolean> enabled = observe(textArea).enabled();
		IValueObserver<Boolean> observer = uncheckedCast(mock(IValueObserver.class));
		enabled.addObserver(observer);
		verify(observer, times(1)).valueChanged(any(Boolean.class), eq((Boolean) null));
		textArea.setEnabled(false);
		getRobot().waitForIdle();
		verify(observer, times(1)).valueChanged(eq(Boolean.FALSE), any(Boolean.class));
	}

	@Test
	public void test_observeSelected() {
		IObservableValue<Boolean> selected = observe(button1).selected();
		IValueObserver<Boolean> observer = uncheckedCast(mock(IValueObserver.class));
		selected.addObserver(observer);
		verify(observer, times(1)).valueChanged(any(Boolean.class), eq((Boolean) null));
		button1.setSelected(true);
		getRobot().waitForIdle();
		verify(observer, times(1)).valueChanged(eq(Boolean.TRUE), any(Boolean.class));
	}

	@Test
	public void test_observeFocus() throws Exception {
		IObservableValue<Boolean> focus = observe(button1).focus();
		IValueObserver<Boolean> observer = uncheckedCast(mock(IValueObserver.class));
		focus.addObserver(observer);
		verify(observer, times(1)).valueChanged(any(Boolean.class), eq((Boolean) null));
		button1.requestFocusInWindow();
		getRobot().waitForIdle();
		verify(observer, times(1)).valueChanged(eq(Boolean.TRUE), any(Boolean.class));
	}

	@Test
	public void test_observeForeground() {
		IObservableValue<Color> foreground = observe(button1).foreground();
		IValueObserver<Color> observer = uncheckedCast(mock(IValueObserver.class));
		foreground.addObserver(observer);
		verify(observer, times(1)).valueChanged(any(Color.class), eq((Color) null));
		button1.setForeground(Color.red);
		getRobot().waitForIdle();
		verify(observer, times(1)).valueChanged(eq(Color.red), any(Color.class));
	}

	@Test
	public void test_observeTitle() {
		IObservableValue<String> title = observe(frame).title();
		IValueObserver<String> observer = uncheckedCast(mock(IValueObserver.class));
		title.addObserver(observer);
		verify(observer, times(1)).valueChanged(any(String.class), eq((String) null));
		frame.setTitle("New Title");
		getRobot().waitForIdle();
		verify(observer, times(1)).valueChanged(eq("New Title"), any(String.class));
	}

	@Test
	public void test_observeText_Button() {
		IObservableValue<String> text = observe(button1).text();
		IValueObserver<String> observer = uncheckedCast(mock(IValueObserver.class));
		text.addObserver(observer);
		verify(observer, times(1)).valueChanged(any(String.class), eq((String) null));
		button1.setText("Button Text");
		getRobot().waitForIdle();
		verify(observer, times(1)).valueChanged(eq("Button Text"), any(String.class));
	}

	@Test
	public void test_observeText_TextComponent() {
		IObservableValue<String> text = observe(textArea).text();
		IValueObserver<String> observer = uncheckedCast(mock(IValueObserver.class));
		text.addObserver(observer);
		verify(observer, times(1)).valueChanged(any(String.class), eq((String) null));
		textArea.setText("TextComponent Text");
		getRobot().waitForIdle();
		verify(observer, times(1)).valueChanged(eq("TextComponent Text"), any(String.class));
		textArea.replaceRange("", 1, 5);
		getRobot().waitForIdle();
		verify(observer, times(1)).valueChanged(eq("Tomponent Text"), eq("TextComponent Text"));
	}

	@Test
	public void test_observeText_TextComponent_replaceDocument() throws Exception {
		IObservableValue<String> text = observe(textArea).text();
		IValueObserver<String> observer = uncheckedCast(mock(IValueObserver.class));
		text.addObserver(observer);
		verify(observer, times(1)).valueChanged(any(String.class), eq((String) null));
		textArea.setText("TextComponent Text");
		getRobot().waitForIdle();
		verify(observer, times(1)).valueChanged(eq("TextComponent Text"), any(String.class));
		StringContent content = new StringContent();
		content.insertString(0, "TextComponent new Document");
		textArea.setDocument(new PlainDocument(content));
		getRobot().waitForIdle();
		verify(observer, times(1)).valueChanged(eq("TextComponent new Document"), any(String.class));
	}

	@Test
	public void test_observeTooltip() {
		IObservableValue<String> tooltip = observe(button1).tooltip();
		IValueObserver<String> observer = uncheckedCast(mock(IValueObserver.class));
		tooltip.addObserver(observer);
		verify(observer, times(1)).valueChanged(any(String.class), eq((String) null));
		button1.setToolTipText("Tooltip Text");
		getRobot().waitForIdle();
		verify(observer, times(1)).valueChanged(eq("Tooltip Text"), any(String.class));
	}

	@Test
	public void test_observeValue_ComboBox() {
		IObservableValue<String> value = observe(combobox).value();
		IValueObserver<String> observer = uncheckedCast(mock(IValueObserver.class));
		value.addObserver(observer);
		verify(observer, times(1)).valueChanged(any(String.class), eq((String) null));
		combobox.setSelectedIndex(1);
		getRobot().waitForIdle();
		verify(observer, times(1)).valueChanged(eq("Value 2"), any(String.class));
		DefaultComboBoxModel model = new DefaultComboBoxModel();
		model.addElement("Value a");
		model.addElement("Value b");
		model.addElement("Value c");
		combobox.setModel(model);
		getRobot().waitForIdle();
		verify(observer, times(1)).valueChanged(eq("Value a"), eq("Value 2"));
		value.set("Value c");
		assertEquals("Value c", combobox.getSelectedItem());
	}

	@Test
	public void test_observeValue_Spinner() {
		IObservableValue<Long> value = observe(spinner).value();
		IValueObserver<Long> observer = uncheckedCast(mock(IValueObserver.class));
		value.addObserver(observer);
		verify(observer, times(1)).valueChanged(any(Long.class), eq((Long) null));
		spinner.setValue(Long.valueOf(10));
		getRobot().waitForIdle();
		verify(observer, times(1)).valueChanged(eq(Long.valueOf(10)), any(Long.class));
	}
	
	@Test
	public void test_observeValue_List() {
		IObservableValue<String> value = observe(list).value();
		IValueObserver<String> observer = uncheckedCast(mock(IValueObserver.class));
		value.addObserver(observer);
		verify(observer, times(1)).valueChanged(any(String.class), eq((String) null));
		list.setSelectedValue("Item 2", true);
		getRobot().waitForIdle();
		verify(observer, times(1)).valueChanged(eq("Item 2"), any(String.class));
	}

	@Test
	public void test_observeVisible() {
		IObservableValue<Boolean> visible = observe(mainPanel).visible();
		visible.set(Boolean.TRUE);
		IValueObserver<Boolean> observer = uncheckedCast(mock(IValueObserver.class));
		visible.addObserver(observer);
		verify(observer, times(1)).valueChanged(any(Boolean.class), eq((Boolean) null));
		mainPanel.setVisible(false);
		getRobot().waitForIdle();
		verify(observer, times(1)).valueChanged(eq(Boolean.FALSE), eq(Boolean.TRUE));
		mainPanel.setVisible(true);
		getRobot().waitForIdle();
		verify(observer, times(1)).valueChanged(eq(Boolean.TRUE), eq(Boolean.FALSE));
	}
}
