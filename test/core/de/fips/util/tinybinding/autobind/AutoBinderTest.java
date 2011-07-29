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

import static de.fips.util.tinybinding.Observables.observe;
import static org.fest.assertions.Assertions.assertThat;

import java.util.List;

import javax.swing.JButton;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JToggleButton;
import javax.swing.SpinnerNumberModel;

import lombok.Getter;
import lombok.Setter;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import de.fips.util.tinybinding.IBindingContext;
import de.fips.util.tinybinding.IObservableValue;
import de.fips.util.tinybinding.autobind.AutoBinder;
import de.fips.util.tinybinding.junit.ExpectedException;

/**
 * Tests the {@link AutoBinder}.
 *
 * @author Philipp Eichhorn
 */
public class AutoBinderTest {
	@Rule
	public final ExpectedException thrown = ExpectedException.none();

	private TestModel model;
	private TestModel2 model2;
	private TestModel3 model3;
	private TestForm form;
	private TestForm2 form2;
	private TestForm3 form3;
	private TestForm4 form4;
	private IBindingContext context;

	@Before
	public void setUp() {
		model = new TestModel();
		model2 = new TestModel2();
		model3 = new TestModel3();
		form = new TestForm();
		form2 = new TestForm2();
		form3 = new TestForm3();
		form4 = new TestForm4();
	}

	@After
	public void shutDown() {
		if (context != null) {
			context.unbindAll();
		}
	}

	@Test
	public void test_bind() throws Exception {
		context = AutoBinder.bind(model, form);
		form.getText().setText("All new input");
		assertThat(model.getText().get()).isEqualTo("All new input");
		model.getStatus().set(Boolean.TRUE);
		assertThat(form.getStatus().isSelected()).isTrue();
	}

	@Test
	public void test_bind_pojo() throws Exception {
		context = AutoBinder.bind(model3, form);
		form.getText().setText("All new input");
		form.getStatus().setSelected(true);
		assertThat(model3.getText()).isEqualTo("All new input");
		assertThat(model3.getStatus()).isTrue();
	}

	@Test
	public void test_bind_subclassing() throws Exception {
		context = AutoBinder.bind(model2, form2);
		form2.getText().setText("All new input");
		assertThat(model2.getText().get()).isEqualTo("All new input");
		model2.getStatus().set(Boolean.TRUE);
		assertThat(form2.getStatus().isSelected()).isTrue();
		model2.getValue().set(new Double(33.3));
		assertThat((Double)form2.getValue().getValue()).isEqualTo(33.3);
	}

	@Test
	public void test_bind_missingFormField() throws Exception {
		thrown.expectNoSuchFieldException("unresolved bingings:\n" + //
				"\n" + //
				"\tde.fips.util.tinybinding.autobind.AutoBinderTest$TestModel2 - de.fips.util.tinybinding.autobind.AutoBinderTest$TestForm\n" + //
				"\tvalue - ???");
		context = AutoBinder.bind(model2, form);
	}

	@Test
	public void test_bind_missingModelField() throws Exception {
		thrown.expectNoSuchFieldException("unresolved bingings:\n" + //
				"\n" + //
				"\tde.fips.util.tinybinding.autobind.AutoBinderTest$TestModel - de.fips.util.tinybinding.autobind.AutoBinderTest$TestForm2\n" + //
				"\t??? - value");
		context = AutoBinder.bind(model, form2);
	}

	@Test
	public void test_bind_modelIsNull() throws Exception {
		thrown.expectIllegalArgumentException("'pojoA' may not be null.");
		context = AutoBinder.bind(null, form2);
	}

	@Test
	public void test_bind_formIsNull() throws Exception {
		thrown.expectIllegalArgumentException("'pojoB' may not be null.");
		context = AutoBinder.bind(model, null);
	}

	@Test
	public void test_bind_missingAnnotation1() throws Exception {
		thrown.expectNoSuchFieldException("unresolved bingings:\n" + //
				"\n" + //
				"\tjava.lang.String - de.fips.util.tinybinding.autobind.AutoBinderTest$TestForm2\n" + //
				"\t??? - text\n" + //
				"\t??? - status\n" + //
				"\t??? - value");
		context = AutoBinder.bind("", form2);
	}

	@Test
	public void test_bind_missingAnnotation2() throws Exception {
		thrown.expectNoSuchFieldException("unresolved bingings:\n" + //
				"\n" + //
				"\tde.fips.util.tinybinding.autobind.AutoBinderTest$TestModel - java.lang.String\n" + //
				"\ttext - ???\n" + //
				"\tstatus - ???");
		context = AutoBinder.bind(model, "");
	}

	@Test
	public void test_bind_unknownHint() throws Exception {
		thrown.expectIllegalArgumentException("Invalid hint 'unknownHint' used for field 'private javax.swing.JButton de.fips.util.tinybinding.autobind.AutoBinderTest$TestForm3.button'.\n" + //
				"Only the following hints are allowed:\n" + //
				"\t[background, bounds, editable, enabled, selected, focus, foreground, title, text, tooltip, value]");
		context = AutoBinder.bind(model, form3);
	}

	@Test
	public void test_bind_notSwingBinding() throws Exception {
		thrown.expectIllegalArgumentException("Field 'private javax.swing.JButton de.fips.util.tinybinding.autobind.AutoBinderTest$TestForm4.button' should be annotated with @SwingBindable.");
		context = AutoBinder.bind(model, form4);
	}

	@Bindable @Getter @Setter
	public static class TestModel {
		private IObservableValue<String> text = observe().nil();
		private IObservableValue<Boolean> status = observe().nil();
	}

	@Getter @Setter
	public static class TestModel2 extends TestModel {
		private String ignoredField1;
		private List<String> ignoredField2;
		@Bindable
		private IObservableValue<Double> value = observe().value(new Double(0.0));
	}

	@Bindable @Getter @Setter
	public static class TestModel3 {
		private String text;
		private Boolean status;
	}

	@Getter
	public static class TestForm {
		@SwingBindable(hint="text")
		private JTextArea text = new JTextArea();
		@SwingBindable(hint="selected")
		private JToggleButton status = new JToggleButton();
	}

	@Getter
	public static class TestForm2 extends TestForm {
		@SwingBindable(hint="value")
		private JSpinner value = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 100.0, 1.0));
	}

	@Getter
	public static class TestForm3 extends TestForm {
		@SwingBindable(hint="unknownHint")
		private JButton button = new JButton();
	}

	@Getter
	public static class TestForm4 extends TestForm {
		@Bindable
		private JButton button = new JButton();
	}
}
