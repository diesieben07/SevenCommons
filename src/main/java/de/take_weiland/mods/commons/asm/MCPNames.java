package de.take_weiland.mods.commons.asm;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import com.google.common.io.LineProcessor;
import cpw.mods.fml.common.launcher.FMLTweaker;
import de.take_weiland.mods.commons.internal.SevenCommons;
import net.minecraft.launchwrapper.Launch;

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
                File forgeJar = new File(FMLTweaker.getJarLocation());
                mappingsDir = forgeJar.getParent() + "/unpacked/conf/";
            } else {
                mappingsDir = prop;
            }

            fields = readMappings(new File(mappingsDir + "/fields.csv"));
            methods = readMappings(new File(mappingsDir + "/methods.csv"));
        } else {
            methods = fields = null;
        }
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

    private static Map<String, String> readMappings(File file) {
        if (!file.isFile()) {
            throw new RuntimeException("Couldn't find MCP mappings. Please provide system property " + SYS_PROP);
        }
        try {
            SevenCommons.LOGGER.trace("Reading SRG->MCP mappings from " + file);
            return Files.readLines(file, StandardCharsets.UTF_8, new MCPFileParser());
        } catch (IOException e) {
            throw new RuntimeException("Couldn't read SRG->MCP mappings", e);
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
