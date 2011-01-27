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
package de.fips.util.tinybinding;

import static lombok.With.with;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;

import lombok.Application;
import lombok.SwingInvokeLater;

public class DemoLauncher implements Application {

	public static Class<?>[] getDemoClasses() {
		return new Class<?>[] {SimpleDemo.class, SearchDemo.class};
	}
	
	public ComboBoxModel getComboBoxModel() {
		DefaultComboBoxModel model = new DefaultComboBoxModel();
		for (Class<?> clazz : getDemoClasses()) {
			model.addElement(clazz.getSimpleName());
		}
		return model;
	}
	
	@SwingInvokeLater
	public void runApp(final String[] arg0) throws Throwable {
		final JComboBox combobox = new JComboBox();
		combobox.setPreferredSize(new Dimension(150, combobox.getPreferredSize().height));
		combobox.setModel(getComboBoxModel());
		with(new JFrame("DemoLauncher"),
			setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE),
			setLayout(new BorderLayout(5, 5)),
			getContentPane().add(combobox, BorderLayout.WEST),
			getContentPane().add(new JButton(new LaunchAction(combobox)), BorderLayout.EAST),
			pack(),
			setVisible(true));
		
	}
	
	public static class LaunchAction extends AbstractAction {
		private final JComboBox combobox;
		
		public LaunchAction(final JComboBox combobox) {
			super("Launch");
			this.combobox = combobox;
		}
		
		@Override
		public void actionPerformed(ActionEvent event) {
			Class<?> clazz = getDemoClasses()[combobox.getSelectedIndex()];
			try {
				clazz.getMethod("main", String[].class).invoke(null, new Object[] {new String[0]});
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
