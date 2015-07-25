
package de.take_weiland.mods.commons.internal;

import com.google.common.base.Charsets;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin.MCVersion;
import net.minecraft.launchwrapper.Launch;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.JavaVersion;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.awt.*;
import java.io.*;
import java.net.URISyntaxException;
import java.util.Map;

@MCVersion("1.7.10")
@IFMLLoadingPlugin.SortingIndex(1001) // get after deobfuscation
@IFMLLoadingPlugin.TransformerExclusions({
        "de.take_weiland.mods.commons.asm.",
        "de.take_weiland.mods.commons.internal.transformers.",
        "de.take_weiland.mods.commons.internal.exclude.",
        "de.take_weiland.mods.commons.util.JavaUtils",
        "de.take_weiland.mods.commons.util.Logging",
        "de.take_weiland.mods.commons.util.Listenable",
        "de.take_weiland.mods.commons.reflect.",
        "de.take_weiland.mods.commons.sync.",
        "de.take_weiland.mods.commons.nbt.ToNbt"
})
// warning! This class is compiled separately from all other SevenCommons classes
// this means: this class cannot reference anything else and nobody else can reference this
// this is because this class is compiled for Java 6 to enable the warning message
public final class SevenCommonsLoader implements IFMLLoadingPlugin {

    @Override
    public String[] getASMTransformerClass() {
        // must do this here, because constructor is too early for FMLCommonHandler
        if (!SystemUtils.isJavaVersionAtLeast(JavaVersion.JAVA_1_8)) {
            displayJava8Warning();
            FMLCommonHandler.instance().exitJava(-1, false);
        }

        return new String[]{
                "de.take_weiland.mods.commons.internal.transformers.SCVisitorTransformerWrapper"
        };
    }

    private static void displayJava8Warning() {
        if (GraphicsEnvironment.isHeadless()) {
            PrintWriter rawOut = new PrintWriter(new OutputStreamWriter(new FileOutputStream(FileDescriptor.err), Charsets.UTF_8));
            try {
                rawOut.println();
                rawOut.println(StringUtils.repeat('=', 80));
                rawOut.println("SevenCommons requires Java 8 to be installed.");
                rawOut.print("Please install the latest Java 8 appropriate for your System from https://java.com/download/");
                if (SystemUtils.IS_OS_WINDOWS) {
                    rawOut.println(" or use the latest .exe launcher from https://minecraft.net/");
                } else {
                    rawOut.println();
                }
                rawOut.println("Thank you. The game will exit now.");
                rawOut.println(StringUtils.repeat('=', 80));
                rawOut.println();
            } finally {
                IOUtils.closeQuietly(rawOut);
            }
        } else {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {

            }

            JLabel label = new JLabel();
            Font font = label.getFont();

            // create some css from the label's font
            StringBuilder style = new StringBuilder("font-family:" + font.getFamily() + ";")
                    .append("font-weight:")
                    .append(font.isBold() ? "bold" : "normal")
                    .append(";")
                    .append("font-size:")
                    .append(font.getSize()).append("pt;");

            JEditorPane ep = new JEditorPane("text/html", "<html><body style=\"" + style + "\">" +
                    "<strong>SevenCommons requires Java 8 to be installed.</strong><br />" +
                    "Please install the latest Java 8 appropriate for your System from <a href=\"https://java.com/download/\">java.com/download</a>" +
                    " or use the latest .exe launcher from <a href=\"https://minecraft.net/\">minecraft.net</a>.<br /><br />" +
                    "Thank you. The game will exit now." +
                    "</body></html>");

            //noinspection Convert2Lambda
            ep.addHyperlinkListener(new HyperlinkListener() {
                @Override
                public void hyperlinkUpdate(HyperlinkEvent e) {
                    if (e.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED))
                        try {
                            Desktop.getDesktop().browse(e.getURL().toURI());
                        } catch (Exception ignored) {
                        }
                }
            });
            ep.setEditable(false);
            ep.setBackground(label.getBackground());

            JOptionPane.showMessageDialog(null, ep, "Java 8 required", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    public String getModContainerClass() {
        return "de.take_weiland.mods.commons.internal.SevenCommons";
    }

    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {
        File source = (File) data.get("coremodLocation");
        if (source == null) { // this is usually in a dev env
            try {
                source = new File(getClass().getProtectionDomain().getCodeSource().getLocation().toURI());
            } catch (URISyntaxException e) {
                throw new RuntimeException("Failed to acquire source location for SevenCommons!", e);
            }
        }
        // cant set this directly, we cannot reference anybody else
        Launch.blackboard.put("__sevencommons.source", source);
    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }

}
