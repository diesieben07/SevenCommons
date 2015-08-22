package de.take_weiland.mods.commons.util;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.FMLLaunchHandler;
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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.reflect.Method;

/**
 * <p>Utility class for outdated Java installations.</p>
 *
 * @author diesieben07
 */
@SuppressWarnings("Convert2Lambda")
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

        final boolean displayExeNotice = SystemUtils.IS_OS_WINDOWS && FMLLaunchHandler.side().isClient();

        Logger logger = LogManager.getLogger("STDERR");
        logger.error("");
        logger.error(StringUtils.repeat('=', 80));
        logger.error("SevenCommons requires Java 8 to be installed.");
        logger.error("Please install the latest Java 8 appropriate for your System from https://java.com/download/" +
                (displayExeNotice ? " or use the latest .exe launcher from https://minecraft.net/" : ""));
        logger.error("Thank you. The game will exit now.");
        logger.error(StringUtils.repeat('=', 80));
        logger.error("");

        if (!GraphicsEnvironment.isHeadless()) {
            final Object mutex = new Object();

            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
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

                    JTextPane text = new JTextPane();
                    text.setContentType("text/html");
                    text.setText("<html><body style=\"" + style + "\">" +
                            "<strong>SevenCommons requires Java 8 to be installed.</strong><br />" +
                            "Please install the latest Java 8 appropriate for your System from <a href=\"https://java.com/download/\">java.com/download</a>" +
                            (displayExeNotice ? " or use the latest .exe launcher from <a href=\"https://minecraft.net/\">minecraft.net</a>" : "") +
                            ".<br /><br />Thank you. The game will exit now." +
                            "</body></html>");

                    text.setEditable(false);
                    text.setHighlighter(null);
                    text.setBackground(label.getBackground());

                    text.setMargin(new Insets(20, 20, 20, 20));

                    text.addHyperlinkListener(new HyperlinkListener() {
                        @Override
                        public void hyperlinkUpdate(HyperlinkEvent e) {
                            if (e.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED))
                                try {
                                    Desktop.getDesktop().browse(e.getURL().toURI());
                                } catch (Exception ignored) {
                                }
                        }
                    });

                    final JFrame frame = new JFrame("Java 8 required");

                    JButton button = new JButton("Exit");
                    button.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            frame.dispose();
                        }
                    });

                    JPanel panel = new JPanel();
                    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
                    panel.add(text);
                    panel.add(button);
                    panel.add(Box.createVerticalStrut(20));

                    frame.setContentPane(panel);
                    frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
                    frame.setResizable(false);
                    frame.addWindowListener(new WindowAdapter() {
                        @Override
                        public void windowClosed(WindowEvent e) {
                            synchronized (mutex) {
                                mutex.notify();
                            }
                        }
                    });

                    frame.pack();
                    frame.setLocationRelativeTo(null);
                    frame.setVisible(true);
                    frame.toFront();
                }
            });

            //noinspection SynchronizationOnLocalVariableOrMethodParameter
            synchronized (mutex) {
                try {
                    mutex.wait();
                } catch (InterruptedException e) {
                    //ignore
                }
            }
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
