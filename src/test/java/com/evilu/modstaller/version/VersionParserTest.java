package com.evilu.modstaller.version;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
* VersionParserTest
*/
public class VersionParserTest {

	@Test
	public void testCreateVersionRangeFormat() {
		final VersionRange range = VersionParser.parse("[mc1.18.1_v0.4.1,)");
		assertThat(range)
			.isNotNull()
			.isInstanceOfSatisfying(
				BoundVersionRange.class,
				r -> assertThat(r)
					.extracting(BoundVersionRange::getStartVersion, BoundVersionRange::getEndVersion)
					.containsExactly(Version.of("mc1.18.1_v0.4.1"), Version.MAX)
			);
	}
	
}
