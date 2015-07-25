package de.take_weiland.mods.commons.util;

import com.google.common.base.Charsets;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.JavaVersion;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.awt.*;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.reflect.Method;

/**
 * <p>Utility class for outdated Java installations.</p>
 *
 * @author diesieben07
 */
public final class JavaCompatibility {

    /**
     * <p>Display a warning to the user if the game is not running on Java 8.</p>
     * <p>If your coremod (not regular mod!) requires Java 8, compile your {@code IFMLLoadingPlugin} for Java 6 and call this method
     * from {@link IFMLLoadingPlugin#getASMTransformerClass()}. This will display a warning to the user if Java 8 is not installed.</p>
     * <p>This class will always be compiled for Java 6.</p>
     * <p>For regular mods Java 8 can be used always, since SevenCommons will require it before any regular mods load.</p>
     *
     * @param exit true to exit the game if Java 8 was not found, you almost always want this
     */
    public static void requireJava8(boolean exit) {
        if (SystemUtils.isJavaVersionAtLeast(JavaVersion.JAVA_1_8)) {
            return;
        }

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
        if (exit) {
            try {
                Class<?> clazz = Class.forName("java.lang.Shutdown");
                Method method = clazz.getDeclaredMethod("exit", int.class);
                method.setAccessible(true);
                method.invoke(null, -1);
            } catch (Throwable t) {
                FMLCommonHandler.instance().exitJava(-1, false);
            }
        }
    }

    private JavaCompatibility() {
    }
}
