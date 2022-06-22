package com.evilu.modstaller.parser;

import java.io.File;

import com.evilu.modstaller.model.Mod;
import com.evilu.modstaller.version.SemanticVersion;
import com.evilu.modstaller.version.Version;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

/**
* ModJarParserTest
*/
public class ModJarParserTest {

	@Test
	public void testSparseManifest() throws Throwable {
		final File jarFile = new File(getClass().getClassLoader().getResource("modJars/sparseManifest.jar").toURI());
		final Mod mod = ModJarParser.parse(jarFile);

		assertThat(mod)
			.isNotNull()
			.extracting(Mod::getVersion)
			.isEqualTo(new SemanticVersion("1.0.18", 1, 0, 18, 0, null));

	}

	
}
