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
package de.fips.util.tinybinding.swing;

import static de.fips.util.tinybinding.junit.Execute.inEDT;
import static de.fips.util.tinybinding.util.Cast.uncheckedCast;
import static de.fips.util.tinybinding.Observables.observe;

import static org.fest.swing.edt.GuiActionRunner.execute;
import static org.junit.Assert.assertEquals;
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
import javax.swing.text.JTextComponent;
import javax.swing.text.PlainDocument;
import javax.swing.text.StringContent;

import org.fest.swing.edt.GuiQuery;
import org.fest.swing.fixture.FrameFixture;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import de.fips.util.tinybinding.IObservableValue;
import de.fips.util.tinybinding.IValueObserver;
import de.fips.util.tinybinding.junit.FailOnThreadViolation;

/**
 * Tests the {@link SwingObservable}
 * 
 * @author Philipp Eichhorn
 */
@RunWith(JUnit4.class)
public class SwingObservableTest {
	@ClassRule
	public static final FailOnThreadViolation checkThreadViolation = new FailOnThreadViolation();

	private FrameFixture window;

	@Before
	public void setUp() throws Exception {
		window = new FrameFixture(execute(new GuiQuery<JFrame>() {
			@Override
			protected JFrame executeInEDT() {
				JTextArea textArea = new JTextArea();
				textArea.setName("textArea");
				JButton button1 = new JButton("Unfocused Button");
				button1.setName("button1");
				JButton button2 = new JButton("Focused Button");
				button2.requestFocusInWindow();
				JList list = new JList();
				list.setName("list");
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
				JComboBox combobox = new JComboBox();
				combobox.setName("combobox");
				DefaultComboBoxModel model = new DefaultComboBoxModel();
				model.addElement("Value 1");
				model.addElement("Value 2");
				model.addElement("Value 3");
				combobox.setModel(model);
				JSpinner spinner = new JSpinner();
				spinner.setName("spinner");
				spinner.setModel(new SpinnerNumberModel(Long.valueOf(0), Long.valueOf(0), Long.valueOf(100), Long.valueOf(1)));
				JPanel buttonPanel2 = new JPanel();
				buttonPanel2.setLayout(new BorderLayout());
				buttonPanel2.add(combobox, BorderLayout.WEST);
				buttonPanel2.add(spinner, BorderLayout.EAST);
				JPanel mainPanel = new JPanel();
				mainPanel.setName("mainPanel");
				mainPanel.setLayout(new BorderLayout());
				mainPanel.add(textArea, BorderLayout.NORTH);
				mainPanel.add(buttonPanel, BorderLayout.CENTER);
				mainPanel.add(buttonPanel2, BorderLayout.SOUTH);

				JFrame frame = new JFrame();
				frame.setLayout(new BorderLayout());
				frame.getContentPane().add(mainPanel, BorderLayout.CENTER);
				frame.pack();
				return frame;
			}
		}));
		window.show();
	}

	@After
	public void tearDown() throws Exception {
		window.cleanUp();
	}

	@Test
	public void test_observeBackground() {
		final JTextComponent textArea = window.textBox("textArea").component();
		IObservableValue<Color> background = observe(textArea).background();
		window.robot.waitForIdle();
		IValueObserver<Color> observer = uncheckedCast(mock(IValueObserver.class));
		background.addObserver(observer);
		verify(observer, times(1)).valueChanged(any(Color.class), eq((Color) null));
		inEDT(textArea).setBackground(Color.red);
		window.robot.waitForIdle();
		verify(observer, times(1)).valueChanged(eq(Color.red), any(Color.class));
	}

	@Test
	public void test_observeBounds() {
		final JPanel mainPanel = window.panel("mainPanel").component();
		IObservableValue<Rectangle> bounds = observe(mainPanel).bounds();
		window.robot.waitForIdle();
		IValueObserver<Rectangle> observer = uncheckedCast(mock(IValueObserver.class));
		bounds.addObserver(observer);
		verify(observer, times(1)).valueChanged(any(Rectangle.class), eq((Rectangle) null));
		inEDT(mainPanel).setBounds(new Rectangle(0, 0, 40, 40));
		window.robot.waitForIdle();
		verify(observer, times(1)).valueChanged(eq(new Rectangle(0, 0, 40, 40)), any(Rectangle.class));
		inEDT(mainPanel).setBounds(new Rectangle(20, 20, 40, 40));
		window.robot.waitForIdle();
		verify(observer, times(1)).valueChanged(eq(new Rectangle(20, 20, 40, 40)), eq(new Rectangle(0, 0, 40, 40)));
	}

	@Test
	public void test_observeEditable() {
		final JTextComponent textArea = window.textBox("textArea").component();
		IObservableValue<Boolean> editable = observe(textArea).editable();
		window.robot.waitForIdle();
		IValueObserver<Boolean> observer = uncheckedCast(mock(IValueObserver.class));
		editable.addObserver(observer);
		verify(observer, times(1)).valueChanged(any(Boolean.class), eq((Boolean) null));
		inEDT(textArea).setEditable(false);
		window.robot.waitForIdle();
		verify(observer, times(1)).valueChanged(eq(Boolean.FALSE), any(Boolean.class));
	}

	@Test
	public void test_observeEnabled() {
		final JTextComponent textArea = window.textBox("textArea").component();
		IObservableValue<Boolean> enabled = observe(textArea).enabled();
		window.robot.waitForIdle();
		IValueObserver<Boolean> observer = uncheckedCast(mock(IValueObserver.class));
		enabled.addObserver(observer);
		verify(observer, times(1)).valueChanged(any(Boolean.class), eq((Boolean) null));
		inEDT(textArea).setEnabled(false);
		window.robot.waitForIdle();
		verify(observer, times(1)).valueChanged(eq(Boolean.FALSE), any(Boolean.class));
	}

	@Test
	public void test_observeSelected() {
		final JButton button1 = window.button("button1").component();
		IObservableValue<Boolean> selected = observe(button1).selected();
		window.robot.waitForIdle();
		IValueObserver<Boolean> observer = uncheckedCast(mock(IValueObserver.class));
		selected.addObserver(observer);
		verify(observer, times(1)).valueChanged(any(Boolean.class), eq((Boolean) null));
		inEDT(button1).setSelected(true);
		window.robot.waitForIdle();
		verify(observer, times(1)).valueChanged(eq(Boolean.TRUE), any(Boolean.class));
	}

	@Test
	public void test_observeFocus() throws Exception {
		final JButton button1 = window.button("button1").component();
		IObservableValue<Boolean> focus = observe(button1).focus();
		window.robot.waitForIdle();
		IValueObserver<Boolean> observer = uncheckedCast(mock(IValueObserver.class));
		focus.addObserver(observer);
		verify(observer, times(1)).valueChanged(any(Boolean.class), eq((Boolean) null));
		inEDT(button1).requestFocusInWindow();
		window.robot.waitForIdle();
		verify(observer, times(1)).valueChanged(eq(Boolean.TRUE), any(Boolean.class));
	}

	@Test
	public void test_observeForeground() {
		final JButton button1 = window.button("button1").component();
		IObservableValue<Color> foreground = observe(button1).foreground();
		window.robot.waitForIdle();
		IValueObserver<Color> observer = uncheckedCast(mock(IValueObserver.class));
		foreground.addObserver(observer);
		verify(observer, times(1)).valueChanged(any(Color.class), eq((Color) null));
		inEDT(button1).setForeground(Color.red);
		window.robot.waitForIdle();
		verify(observer, times(1)).valueChanged(eq(Color.red), any(Color.class));
	}

	@Test
	public void test_observeTitle() {
		IObservableValue<String> title = observe(window.component()).title();
		window.robot.waitForIdle();
		IValueObserver<String> observer = uncheckedCast(mock(IValueObserver.class));
		title.addObserver(observer);
		verify(observer, times(1)).valueChanged(any(String.class), eq((String) null));
		inEDT(window.component()).setTitle("New Title");
		window.robot.waitForIdle();
		verify(observer, times(1)).valueChanged(eq("New Title"), any(String.class));
	}

	@Test
	public void test_observeText_Button() {
		final JButton button1 = window.button("button1").component();
		IObservableValue<String> text = observe(button1).text();
		window.robot.waitForIdle();
		IValueObserver<String> observer = uncheckedCast(mock(IValueObserver.class));
		text.addObserver(observer);
		verify(observer, times(1)).valueChanged(any(String.class), eq((String) null));
		inEDT(button1).setText("Button Text");
		window.robot.waitForIdle();
		verify(observer, times(1)).valueChanged(eq("Button Text"), any(String.class));
	}

	@Test
	public void test_observeText_TextComponent() {
		final JTextComponent textArea = window.textBox("textArea").component();
		IObservableValue<String> text = observe(textArea).text();
		window.robot.waitForIdle();
		IValueObserver<String> observer = uncheckedCast(mock(IValueObserver.class));
		text.addObserver(observer);
		verify(observer, times(1)).valueChanged(any(String.class), eq((String) null));
		inEDT(textArea).setText("TextComponent Text");
		window.robot.waitForIdle();
		verify(observer, times(1)).valueChanged(eq("TextComponent Text"), any(String.class));
		inEDT((JTextArea) textArea).replaceRange("", 1, 5);
		window.robot.waitForIdle();
		verify(observer, times(1)).valueChanged(eq("Tomponent Text"), eq("TextComponent Text"));
	}

	@Test
	public void test_observeText_TextComponent_replaceDocument() throws Exception {
		final JTextComponent textArea = window.textBox("textArea").component();
		IObservableValue<String> text = observe(textArea).text();
		window.robot.waitForIdle();
		IValueObserver<String> observer = uncheckedCast(mock(IValueObserver.class));
		text.addObserver(observer);
		verify(observer, times(1)).valueChanged(any(String.class), eq((String) null));
		inEDT(textArea).setText("TextComponent Text");
		window.robot.waitForIdle();
		verify(observer, times(1)).valueChanged(eq("TextComponent Text"), any(String.class));
		final StringContent content = new StringContent();
		content.insertString(0, "TextComponent new Document");
		inEDT(textArea).setDocument(new PlainDocument(content));
		window.robot.waitForIdle();
		verify(observer, times(1)).valueChanged(eq("TextComponent new Document"), any(String.class));
	}

	@Test
	public void test_observeTooltip() {
		final JButton button1 = window.button("button1").component();
		IObservableValue<String> tooltip = observe(button1).tooltip();
		window.robot.waitForIdle();
		IValueObserver<String> observer = uncheckedCast(mock(IValueObserver.class));
		tooltip.addObserver(observer);
		verify(observer, times(1)).valueChanged(any(String.class), eq((String) null));
		inEDT(button1).setToolTipText("Tooltip Text");
		window.robot.waitForIdle();
		verify(observer, times(1)).valueChanged(eq("Tooltip Text"), any(String.class));
	}

	@Test
	public void test_observeValue_ComboBox() {
		final JComboBox combobox = window.comboBox("combobox").component();
		IObservableValue<String> value = observe(combobox).value();
		window.robot.waitForIdle();
		IValueObserver<String> observer = uncheckedCast(mock(IValueObserver.class));
		value.addObserver(observer);
		verify(observer, times(1)).valueChanged(any(String.class), eq((String) null));
		inEDT(combobox).setSelectedIndex(1);
		window.robot.waitForIdle();
		verify(observer, times(1)).valueChanged(eq("Value 2"), any(String.class));
		final DefaultComboBoxModel model = new DefaultComboBoxModel();
		model.addElement("Value a");
		model.addElement("Value b");
		model.addElement("Value c");
		inEDT(combobox).setModel(model);
		window.robot.waitForIdle();
		verify(observer, times(1)).valueChanged(eq("Value a"), eq("Value 2"));
		value.set("Value c");
		assertEquals("Value c", inEDT(combobox).getSelectedItem());
	}

	@Test
	public void test_observeValue_Spinner() {
		final JSpinner spinner = window.spinner("spinner").component();
		IObservableValue<Long> value = observe(spinner).value();
		window.robot.waitForIdle();
		IValueObserver<Long> observer = uncheckedCast(mock(IValueObserver.class));
		value.addObserver(observer);
		verify(observer, times(1)).valueChanged(any(Long.class), eq((Long) null));
		inEDT(spinner).setValue(Long.valueOf(10));
		window.robot.waitForIdle();
		verify(observer, times(1)).valueChanged(eq(Long.valueOf(10)), any(Long.class));
	}

	@Test
	public void test_observeValue_List() {
		final JList list = window.list("list").component();
		IObservableValue<String> value = observe(list).value();
		window.robot.waitForIdle();
		IValueObserver<String> observer = uncheckedCast(mock(IValueObserver.class));
		value.addObserver(observer);
		verify(observer, times(1)).valueChanged(any(String.class), eq((String) null));
		inEDT(list).setSelectedValue("Item 2", true);
		window.robot.waitForIdle();
		verify(observer, times(1)).valueChanged(eq("Item 2"), any(String.class));
	}

	@Test
	public void test_observeVisible() {
		IObservableValue<Boolean> visible = observe(window.component()).visible();
		window.robot.waitForIdle();
		visible.set(Boolean.TRUE);
		IValueObserver<Boolean> observer = uncheckedCast(mock(IValueObserver.class));
		visible.addObserver(observer);
		verify(observer, times(1)).valueChanged(any(Boolean.class), eq((Boolean) null));
		window.close();
		verify(observer, times(1)).valueChanged(eq(Boolean.FALSE), eq(Boolean.TRUE));
		window.show();
		verify(observer, times(1)).valueChanged(eq(Boolean.TRUE), eq(Boolean.FALSE));
	}
}
