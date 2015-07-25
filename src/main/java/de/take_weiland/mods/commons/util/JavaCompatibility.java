package de.take_weiland.mods.commons.util;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import org.apache.commons.lang3.JavaVersion;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.awt.*;
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

        Logger logger = LogManager.getLogger("STDERR");
        logger.error("");
        logger.error(StringUtils.repeat('=', 80));
        logger.error("SevenCommons requires Java 8 to be installed.");
        logger.error("Please install the latest Java 8 appropriate for your System from https://java.com/download/" +
                (SystemUtils.IS_OS_WINDOWS ? " or use the latest .exe launcher from https://minecraft.net/" : ""));
        logger.error("Thank you. The game will exit now.");
        logger.error(StringUtils.repeat('=', 80));
        logger.error("");

        if (!GraphicsEnvironment.isHeadless()) {
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
