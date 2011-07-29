package de.fips.util.tinybinding;

import static org.fest.assertions.Assertions.assertThat;

import org.junit.Rule;
import org.junit.Test;

import de.fips.util.tinybinding.junit.Std;

public class VersionTest {
	@Rule
	public final Std stdOut = Std.out();

	@Test
	public void whenInvoked_getVersion_returnsCurrentVersion() throws Exception {
		// run + assert
		assertThat(Version.getVersion()).contains("1.0.4-HEAD");
	}

	@Test
	public void whenInvokedWithoutArguments_main_printsCurrentVersionToStdOut() throws Exception {
		// setup
		final String[] args = new String[0];
		// run
		Version.main(args);
		// assert
		assertThat(stdOut.getContent()).contains("1.0.4-HEAD");
	}

	@Test
	public void whenInvokedWithoutArguments_main_printsProjectNameAndCurrentVersionToStdOut() throws Exception {
		// setup
		final String[] args = new String[] { "arg1" };
		// run
		Version.main(args);
		// assert
		assertThat(stdOut.getContent()).contains("tinybinding 1.0.4-HEAD");
	}
}
