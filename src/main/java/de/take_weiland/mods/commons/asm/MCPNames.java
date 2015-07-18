package de.take_weiland.mods.commons.asm;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import com.google.common.io.LineProcessor;
import cpw.mods.fml.common.launcher.FMLTweaker;
import de.take_weiland.mods.commons.internal.SevenCommons;
import net.minecraft.launchwrapper.Launch;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * <p>A helper class for working with obfuscated field names.</p>
 * <p>In the development environment the mappings file will automatically loaded. You can provide the location of a custom mappings file by
 * providing the system property {@code sevencommons.mappingsFile}.</p>
 *
 * @author diesieben07
 */
public final class MCPNames {

    private static final Map<String, String> fields;
    private static final Map<String, String> methods;

    private static final String SYS_PROP = "sevencommons.mappingsFile";

    private static final boolean DEV_ENV;

    static {
        DEV_ENV = (Boolean) Launch.blackboard.get("fml.deobfuscatedEnvironment");
        if (use()) {
            String mappingsDir;
            String prop = System.getProperty(SYS_PROP);
            if (prop == null) {
                // find the build.gradle by searching upwards from the CWD
                File cwd = new File("").getAbsoluteFile();
                File buildGradle = null;
                while (cwd != null && cwd.isDirectory()) {
                    File bg = new File(cwd, "build.gradle");
                    if (bg.isFile()) {
                        buildGradle = bg;
                        break;
                    }
                    cwd = cwd.getParentFile();
                }
                if (buildGradle == null) {
                    throw failFindMappings("Could not find build.gradle");
                }

                // find any mappings= setting in the build.gradle
                String mappings = tryFindMappings(buildGradle);
                if (mappings == null) {
                    // no mappings= setting, mappings are where the forge jar is
                    File forgeJar = new File(FMLTweaker.getJarLocation());
                    mappingsDir = forgeJar.getParent() + "/unpacked/conf/";
                } else {
                    // get the gradle cache
                    File gradleCache;
                    String overriddenGradleDir = System.getenv("GRADLE_USER_HOME");
                    if (overriddenGradleDir == null) {
                        gradleCache = new File(System.getProperty("user.home") + "/.gradle/");
                    } else {
                        gradleCache = new File(overriddenGradleDir);
                    }
                    if (!gradleCache.isDirectory()) {
                        throw failFindMappings(String.format("Failed to find gradle cache (tried %s)", gradleCache.getAbsolutePath()));
                    }

                    // determine the path inside the cache
                    String[] split = mappings.split("_");
                    if (split.length != 2) {
                        throw failFindMappings("Invalid mappings setting in build.gradle: " + mappings);
                    }
                    mappingsDir = gradleCache.getAbsolutePath() + "/caches/minecraft/de/oceanlabs/mcp/mcp_" + split[0] + "/" + split[1] + "/";
                }
            } else {
                // have the system property defined
                mappingsDir = prop;
            }

            fields = readMappings(new File(mappingsDir + "/fields.csv"));
            methods = readMappings(new File(mappingsDir + "/methods.csv"));
        } else {
            methods = fields = null;
        }
    }

    private static String tryFindMappings(File buildGradle) {
        try (BufferedReader reader = Files.newReader(buildGradle, StandardCharsets.UTF_8)) {
            String line;
            String mappings = null;
            CharMatcher matcher = CharMatcher.WHITESPACE.or(CharMatcher.is('"'));
            while ((line = reader.readLine()) != null) {
                line = matcher.removeFrom(line);
                if (line.startsWith("mappings=")) {
                    mappings = line.substring(9, line.length() - (line.endsWith(";") ? 1 : 0));
                    break;
                }
            }
            return mappings;
        } catch (IOException e) {
            throw failFindMappings("IOException reading build.gradle", e);
        }
    }

    private static Map<String, String> readMappings(File file) {
        if (!file.isFile()) {
            throw failFindMappings(String.format("Mappings file not found (tried %s).", file.getAbsolutePath()));
        }
        try {
            SevenCommons.LOGGER.info("Reading SRG->MCP mappings from " + file);
            return Files.readLines(file, StandardCharsets.UTF_8, new MCPFileParser());
        } catch (IOException e) {
            throw failFindMappings("IOException while parsing mappings file", e);
        }
    }

    private static RuntimeException failFindMappings(String reason) {
        return failFindMappings(reason, null);
    }

    private static RuntimeException failFindMappings(String reason, @Nullable Throwable cause) {
        return new RuntimeException("Could not find MCP Mappings. See MCPNames.java. Reason: " + reason, cause);
    }

    /**
     * <p>Whether the code is running in a development environment or not.</p>
     *
     * @return true if the code is running in development mode (use MCP instead of SRG names)
     */
    public static boolean use() {
        return DEV_ENV;
    }

    /**
     * <p>Get the correct name for the given SRG field based on the context.</p>
     *
     * @param srg the SRG name for a field
     * @return the input if the code is running outside of development mode or the matching MCP name otherwise
     */
    public static String field(String srg) {
        if (use()) {
            return fields.getOrDefault(srg, srg);
        } else {
            return srg;
        }
    }

    /**
     * <p>Get the correct name for the given SRG method based on the context.</p>
     *
     * @param srg the SRG name for a method
     * @return the input if the code is running outside of development mode or the matching MCP name otherwise
     */
    public static String method(String srg) {
        if (use()) {
            return methods.getOrDefault(srg, srg);
        } else {
            return srg;
        }
    }

    private static class MCPFileParser implements LineProcessor<Map<String, String>> {

        private static final Splitter splitter = Splitter.on(',').trimResults();
        private final Map<String, String> map = Maps.newHashMap();
        private boolean foundFirst;

        @Override
        public boolean processLine(String line) throws IOException {
            if (!foundFirst) {
                foundFirst = true;
                return true;
            }

            Iterator<String> splitted = splitter.split(line).iterator();
            try {
                String srg = splitted.next();
                String mcp = splitted.next();
                map.putIfAbsent(srg, mcp);
            } catch (NoSuchElementException e) {
                throw new IOException("Invalid Mappings file!", e);
            }

            return true;
        }

        @Override
        public Map<String, String> getResult() {
            return ImmutableMap.copyOf(map);
        }
    }

    private MCPNames() {
    }

}
