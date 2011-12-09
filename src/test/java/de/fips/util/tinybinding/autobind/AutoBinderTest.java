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

import static de.fips.util.tinybinding.Observables.observe;
import static org.fest.assertions.Assertions.assertThat;
import static org.fest.swing.edt.GuiActionRunner.execute;

import java.awt.GridLayout;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JToggleButton;
import javax.swing.SpinnerNumberModel;

import lombok.BoundPropertySupport;
import lombok.BoundSetter;
import lombok.Getter;
import lombok.Setter;

import org.fest.swing.edt.GuiQuery;
import org.fest.swing.fixture.FrameFixture;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import de.fips.util.tinybinding.IBindingContext;
import de.fips.util.tinybinding.IObservableValue;
import de.fips.util.tinybinding.autobind.AutoBinder;
import de.fips.util.tinybinding.junit.ExpectedException;
import de.fips.util.tinybinding.junit.FailOnThreadViolation;

/**
 * Tests {@link AutoBinder}.
 */
@RunWith(JUnit4.class)
public class AutoBinderTest {
	@ClassRule
	public static final FailOnThreadViolation checkThreadViolation = new FailOnThreadViolation();

	@Rule
	public final ExpectedException thrown = ExpectedException.none();

	private FrameFixture window;
	private TestForm1 form1;
	private TestForm2 form2;
	private TestForm3 form3;
	private TestForm4 form4;
	private TestModel1 model1;
	private TestModel2 model2;
	private TestModel3 model3;
	private IBindingContext context;

	@Before
	public void setUp() throws Exception {
		model1 = new TestModel1();
		model2 = new TestModel2();
		model3 = new TestModel3();
		window = new FrameFixture(execute(new GuiQuery<JFrame>() {
			protected JFrame executeInEDT() {
				form1 = new TestForm1();
				form2 = new TestForm2();
				form3 = new TestForm3();
				form4 = new TestForm4();

				final JFrame frame = new JFrame();
				frame.setLayout(new GridLayout(2, 2));
				frame.getContentPane().add(form1);
				frame.getContentPane().add(form2);
				frame.getContentPane().add(form3);
				frame.getContentPane().add(form4);
				frame.pack();
				return frame;
			}
		}));
		window.show();
	}

	@After
	public void tearDown() throws Exception {
		if (context != null) {
			context.unbindAll();
		}
		window.cleanUp();
	}

	@Test
	public void test_bind() throws Exception {
		context = AutoBinder.bind(model1, form1);
		window.panel("form1").textBox("text").setText("All new input");
		assertThat(model1.getText()).isEqualTo("All new input");
		model1.setStatus(Boolean.TRUE);
		window.panel("form1").toggleButton("status").requireSelected();
	}

	@Test
	public void test_bind_pojo() throws Exception {
		context = AutoBinder.bind(model3, form1);
		window.panel("form1").textBox("text").setText("All new input");
		window.panel("form1").toggleButton("status").check();
		assertThat(model3.getText()).isEqualTo("All new input");
		assertThat(model3.getStatus()).isTrue();
	}

	@Test
	public void test_bind_subclassing() throws Exception {
		context = AutoBinder.bind(model2, form2);
		window.panel("form2").textBox("text").setText("All new input");
		assertThat(model2.getText()).isEqualTo("All new input");
		model2.setStatus(Boolean.TRUE);
		window.panel("form2").toggleButton("status").requireSelected();
		model2.getValue().set(33.3);
		window.panel("form2").spinner("value").requireValue(33.3);
	}

	@Test
	public void test_bind_missingFormField() throws Exception {
		thrown.expectUnresolvedBindingException("unresolved bingings:\n" + //
				"\n" + //
				"\tde.fips.util.tinybinding.autobind.AutoBinderTest$TestModel2 - de.fips.util.tinybinding.autobind.AutoBinderTest$TestForm1\n" + //
				"\tde.fips.util.tinybinding.IObservableValue value - ???");
		context = AutoBinder.bind(model2, form1);
	}

	@Test
	public void test_bind_missingModelField() throws Exception {
		thrown.expectUnresolvedBindingException("unresolved bingings:\n" + //
				"\n" + //
				"\tde.fips.util.tinybinding.autobind.AutoBinderTest$TestModel1 - de.fips.util.tinybinding.autobind.AutoBinderTest$TestForm2\n" + //
				"\t??? - javax.swing.JSpinner value");
		context = AutoBinder.bind(model1, form2);
	}

	@Test
	public void test_bind_modelIsNull() throws Exception {
		thrown.expectIllegalArgumentException("'pojoA' may not be null.");
		context = AutoBinder.bind(null, form2);
	}

	@Test
	public void test_bind_formIsNull() throws Exception {
		thrown.expectIllegalArgumentException("'pojoB' may not be null.");
		context = AutoBinder.bind(model1, null);
	}

	@Test
	public void test_bind_missingAnnotation1() throws Exception {
		thrown.expectUnresolvedBindingException("unresolved bingings:\n" + //
				"\n" + //
				"\tjava.lang.String - de.fips.util.tinybinding.autobind.AutoBinderTest$TestForm2\n" + //
				"\t??? - javax.swing.JTextArea text\n" + //
				"\t??? - javax.swing.JToggleButton status\n" + //
				"\t??? - javax.swing.JSpinner value");
		context = AutoBinder.bind("", form2);
	}

	@Test
	public void test_bind_missingAnnotation2() throws Exception {
		thrown.expectUnresolvedBindingException("unresolved bingings:\n" + //
				"\n" + //
				"\tde.fips.util.tinybinding.autobind.AutoBinderTest$TestModel1 - java.lang.String\n" + //
				"\tjava.lang.String text - ???\n" + //
				"\tjava.lang.Boolean status - ???");
		context = AutoBinder.bind(model1, "");
	}

	@Test
	public void test_bind_unknownHint() throws Exception {
		thrown.expectIllegalArgumentException("Invalid hint 'unknownHint' used for field 'private javax.swing.JButton de.fips.util.tinybinding.autobind.AutoBinderTest$TestForm3.button'.\n"
				+ //
				"Only the following hints are allowed:\n" + //
				"\t[background, bounds, editable, enabled, selected, focus, foreground, title, text, tooltip, value]");
		context = AutoBinder.bind(model1, form3);
	}

	@Test
	public void test_bind_notSwingBinding() throws Exception {
		thrown.expectIllegalArgumentException("Field 'private javax.swing.JButton de.fips.util.tinybinding.autobind.AutoBinderTest$TestForm4.button' should be annotated with @SwingBindable.");
		context = AutoBinder.bind(model1, form4);
	}

	@BoundSetter
	@Getter
	@BoundPropertySupport
	public static class TestModel1 {
		@Bindable
		private String text;
		@Bindable
		private Boolean status;
	}

	@Getter
	@Setter
	public static class TestModel2 extends TestModel1 {
		private String ignoredField1;
		private List<String> ignoredField2;
		@Bindable
		private IObservableValue<Double> value = observe().value(new Double(0.0));
	}

	@Getter
	@Setter
	public static class TestModel3 {
		@Bindable
		private String text;
		@Bindable
		private Boolean status;
	}

	@Getter
	public static class TestForm1 extends JPanel {
		private static final long serialVersionUID = 1L;

		@SwingBindable(hint = "text")
		private final JTextArea text = new JTextArea();
		@SwingBindable(hint = "selected")
		private final JToggleButton status = new JToggleButton();

		public TestForm1() {
			super();
			setLayout(new GridLayout(2, 2));
			setName("form1");
			text.setName("text");
			add(text);
			status.setName("status");
			add(status);
		}
	}

	@Getter
	public static class TestForm2 extends TestForm1 {
		private static final long serialVersionUID = 1L;

		@SwingBindable(hint = "value")
		private JSpinner value = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 100.0, 1.0));

		public TestForm2() {
			super();
			setName("form2");
			value.setName("value");
			add(value);
		}
	}

	@Getter
	public static class TestForm3 extends TestForm1 {
		private static final long serialVersionUID = 1L;

		@SwingBindable(hint = "unknownHint")
		private JButton button = new JButton();

		public TestForm3() {
			super();
			setName("form3");
			button.setName("button");
			add(button);
		}
	}

	@Getter
	public static class TestForm4 extends TestForm1 {
		private static final long serialVersionUID = 1L;

		@Bindable
		private JButton button = new JButton();

		public TestForm4() {
			super();
			setName("form4");
			button.setName("button");
			add(button);
		}
	}
}
