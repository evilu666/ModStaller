package com.evilu.modstaller.version;

import java.util.BitSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import com.evilu.modstaller.antlr.VersionRangeLexer;
import com.evilu.modstaller.antlr.VersionRangeParser;
import com.evilu.modstaller.antlr.VersionRangeParser.BoundRangeContext;
import com.evilu.modstaller.antlr.VersionRangeParser.ExactVersionContext;
import com.evilu.modstaller.antlr.VersionRangeParser.ExactVersionWithMcAndForgeContext;
import com.evilu.modstaller.antlr.VersionRangeParser.ExactVersionWithMcContext;
import com.evilu.modstaller.antlr.VersionRangeParser.MavenRangeContext;
import com.evilu.modstaller.antlr.VersionRangeParser.PrefixRangeContext;
import com.evilu.modstaller.antlr.VersionRangeParser.PrefixRangeWithMcAndForgeContext;
import com.evilu.modstaller.antlr.VersionRangeParser.PrefixRangeWithMcContext;
import com.evilu.modstaller.antlr.VersionRangeParser.PrefixVersionContext;
import com.evilu.modstaller.antlr.VersionRangeParser.VersionContext;
import com.evilu.modstaller.util.StreamUtil;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.ATNConfigSet;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.tree.TerminalNode;

/**
 * VersionParser
 */
public interface VersionParser {

    public static VersionRange parse(final String version) {
        final VersionRangeLexer lexer = new VersionRangeLexer(new ANTLRInputStream(version));
        final VersionRangeParser parser = new VersionRangeParser(new CommonTokenStream(lexer));

        parser.addErrorListener(new ANTLRErrorListener() {
            @Override
            public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
                throw new InvalidVersionException(version, msg);
            }

            @Override
            public void reportAmbiguity(Parser recognizer, DFA dfa, int startIndex, int stopIndex, boolean exact, BitSet ambigAlts, ATNConfigSet configs) {

            }

            @Override
            public void reportAttemptingFullContext(Parser recognizer, DFA dfa, int startIndex, int stopIndex, BitSet conflictingAlts, ATNConfigSet configs) {

            }

            @Override
            public void reportContextSensitivity(Parser recognizer, DFA dfa, int startIndex, int stopIndex, int prediction, ATNConfigSet configs) {

            }
        });

        final VersionRangeParser.RContext root = parser.r();

        if (parser.getNumberOfSyntaxErrors() > 0) {
            throw new InvalidVersionException(version);
        }

        if (Objects.nonNull(root.exp().mavenRange())) return parse(root.exp().mavenRange());
        if (Objects.nonNull(root.exp().prefixRange())) return parse(root.exp().prefixRange());
        if (Objects.nonNull(root.exp().exactVersion())) return parse(root.exp().exactVersion());

        throw new InvalidVersionException(version);
    }

    public static Version parseVersion(final String version) {
        final VersionRange range = parse(version);
        if (range instanceof Version) return (Version) range;

        throw new InvalidVersionException(version);
    }

    private static VersionRange parse(final MavenRangeContext mavenRange) {
        if (Objects.nonNull(mavenRange.exactRange())) {
            return parse(mavenRange.exactRange().exactVersion());
        }

        return parse(mavenRange.boundRange());
    }

    private static VersionRange parse(final BoundRangeContext boundVersion) {
        if (boundVersion.exactVersion().size() < 1) throw new InvalidVersionException(boundVersion.getText());

        final Version startVersion, endVersion;
        if (boundVersion.exactVersion().size() == 1) {
            if (boundVersion.g == 1) {
                startVersion = parse(boundVersion.exactVersion(0));
                endVersion = null;
            } else {
                endVersion = parse(boundVersion.exactVersion(0));
                startVersion = null;
            }
        } else {
            startVersion = parse(boundVersion.exactVersion(0));
            endVersion = parse(boundVersion.exactVersion(0));
        }

        final boolean isStartInclusive = isPresent(boundVersion.INCLUSIVE_BOUND_START());
        final boolean isEndInclusive = isPresent(boundVersion.INCLUSIVE_BOUND_END());

        return new BoundVersionRange(boundVersion.getText(), startVersion, endVersion, isStartInclusive, isEndInclusive);
    }

    private static VersionRange parse(final PrefixRangeContext prefixRange) {
        if (isPresent(prefixRange.prefixVersion())) return parse(prefixRange.prefixVersion());
        if (isPresent(prefixRange.prefixRangeWithMc())) return parse(prefixRange.prefixRangeWithMc());
        if (isPresent(prefixRange.prefixRangeWithMcAndForge())) return parse(prefixRange.prefixRangeWithMcAndForge());

        throw new InvalidVersionException(prefixRange.getText());
    }

    private static ForgeVersionRange parse(final PrefixRangeWithMcContext prefixRange) {
        return new ForgeVersionRange(prefixRange.getText(), null, parse(prefixRange.prefixVersion(0)), parse(prefixRange.prefixVersion(1)));
    }

    private static ForgeVersionRange parse(final PrefixRangeWithMcAndForgeContext prefixRange) {
        return new ForgeVersionRange(prefixRange.getText(), parse(prefixRange.prefixVersion(2)), parse(prefixRange.prefixVersion(0)), parse(prefixRange.prefixVersion(1)));
    }

    private static PrefixVersionRange parse(final PrefixVersionContext prefixVersion) {
        final List<Integer> parts = prefixVersion.VERSION_PART()
            .stream()
            .map(TerminalNode::getText)
            .map(Integer::parseInt)
            .collect(Collectors.toList());

        if (parts.size() < 4) StreamUtil.repeating((Integer) null, 4 - parts.size()).forEach(parts::add);

        return new PrefixVersionRange(prefixVersion.getText(), parts.get(0), parts.get(1), parts.get(2), parts.get(3));
    }

    private static Version parse(final ExactVersionContext exactVersion) {
        if (Objects.nonNull(exactVersion.exactVersionWithMcAndForge())) return parse(exactVersion.exactVersionWithMcAndForge());
        if (Objects.nonNull(exactVersion.exactVersionWithMc())) return parse(exactVersion.exactVersionWithMc());
        if (Objects.nonNull(exactVersion.version())) return parse(exactVersion.version());

        throw new InvalidVersionException(exactVersion.getText());
    }

    private static ForgeVersion parse(final ExactVersionWithMcAndForgeContext version) {
        return new ForgeVersion(version.getText(), parse(version.version(2)), parse(version.version(0)), parse(version.version(1)));
    }

    private static ForgeVersion parse(final ExactVersionWithMcContext version) {
        if (version.version().size() < 1) throw new InvalidVersionException(version.getText());

        return new ForgeVersion(version.getText(), null, version.version().size() > 1 ? parse(version.version(1)) : null, parse(version.version(0)));
    }


    private static SemanticVersion parse(final VersionContext version) {
        final List<Integer> versionParts = version.VERSION_PART()
            .stream()
            .map(TerminalNode::getText)
            .map(Integer::parseInt)
            .collect(Collectors.toList());

        if (versionParts.size() > 4) {
            throw new InvalidVersionException(version.getText());
        }

        
        if (versionParts.size() < 4) 
            StreamUtil.repeating(0, 4 - versionParts.size()).forEach(versionParts::add);

        return new SemanticVersion(version.getText(), versionParts.get(0), versionParts.get(1), versionParts.get(2), versionParts.get(3), null);
    }

    private static boolean isPresent(final Object o) {
        return o != null;
    }

    private static <T> Optional<T> optional(final T ctx) {
        return Optional.ofNullable(ctx);
    }
    
}
