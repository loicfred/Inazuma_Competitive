package iecompbot;

import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import club.minnced.discord.webhook.send.component.ComponentElement;
import club.minnced.discord.webhook.send.component.button.Button;
import club.minnced.discord.webhook.send.component.layout.ActionRow;
import club.minnced.discord.webhook.send.component.select.StringSelectMenu;
import my.utilities.util.Utilities;
import net.dv8tion.jda.api.components.Component;
import net.dv8tion.jda.api.components.MessageTopLevelComponentUnion;
import net.dv8tion.jda.api.components.actionrow.ActionRowChildComponentUnion;
import net.dv8tion.jda.api.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.internal.components.actionrow.ActionRowImpl;
import net.dv8tion.jda.internal.components.buttons.ButtonImpl;
import net.dv8tion.jda.internal.components.selections.SelectMenuImpl;
import net.dv8tion.jda.internal.components.selections.StringSelectMenuImpl;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static iecompbot.L10N.TL;
import static iecompbot.L10N.TLG;

public class Utility extends Utilities {
    
    
    public static String formatRelativeTimeTL(Duration duration) {
        long seconds = duration.getSeconds();
        if (seconds < 60) {
            return seconds == 1 ? "1 second" : seconds + " seconds";
        } else if (seconds < 3600) {
            long minutes = seconds / 60;
            long remainingSeconds = seconds % 60;
            return minutes + (minutes == 1 ? " minute" : " minutes") +
                    (remainingSeconds > 0 ? " and " + remainingSeconds + " seconds" : "");
        } else if (seconds < 86400) {
            long hours = seconds / 3600;
            long remainingMinutes = (seconds % 3600) / 60;
            return hours + (hours == 1 ? " hour" : " hours") +
                    (remainingMinutes > 0 ? " and " + remainingMinutes + " minutes" : "");
        } else if (seconds < 2592000) {
            long days = seconds / 86400;
            long remainingHours = (seconds % 86400) / 3600;
            long remainingMinutes = (seconds % 3600) / 60;
            return days + (days == 1 ? " day" : " days") +
                    (remainingHours > 0 ? " and " + remainingHours + " hours" : "") +
                    (remainingMinutes > 0 ? " and " + remainingMinutes + " minutes" : "");

        } else if (seconds < 31536000) {
            long months = seconds / 2592000;
            long remainingDays = (seconds % 2592000) / 86400;
            long remainingHours = (seconds % 86400) / 3600;
            return months + (months == 1 ? " month" : " months") +
                    (remainingDays > 0 ? " and " + remainingDays + " days" : "") +
                    (remainingHours > 0 ? " and " + remainingHours + " hours" : "");
        } else {
            long years = seconds / 31536000;
            long remainingMonths = (seconds % 31536000) / 2592000;
            return years + (years == 1 ? " year" : " years") +
                    (remainingMonths > 0 ? " and " + remainingMonths + " months" : "");
        }
    }
    public static String formatRelativeTimeTL(InteractionHook M, Duration duration) {
        if (M == null) return formatRelativeTimeTL(duration);
        long seconds = duration.getSeconds();

        if (seconds < 60) {
            return seconds == 1 ? "1 " + TL(M,"second") : seconds + " " + TL(M,"seconds");
        } else if (seconds < 3600) {
            long minutes = seconds / 60;
            long remainingSeconds = seconds % 60;
            return minutes + (minutes == 1 ? " " + TL(M,"minute") : " " + TL(M,"minutes")) +
                    (remainingSeconds > 0 ? " " + TL(M,"and") + " " + remainingSeconds + " " + TL(M,"seconds") : "");
        } else if (seconds < 86400) {
            long hours = seconds / 3600;
            long remainingMinutes = (seconds % 3600) / 60;
            return hours + (hours == 1 ? " " + TL(M,"hour") : " " + TL(M,"hours")) +
                    (remainingMinutes > 0 ? " " + TL(M,"and") + " " + remainingMinutes + " " + TL(M,"minutes") : "");
        } else if (seconds < 2592000) {
            long days = seconds / 86400;
            long remainingHours = (seconds % 86400) / 3600;
            long remainingMinutes = (seconds % 3600) / 60;
            return days + (days == 1 ? " " + TL(M,"day") : " " + TL(M,"days")) +
                    (remainingHours > 0 ? " " + TL(M,"and") + " " + remainingHours + " " + TL(M,"hours") : "") +
                    (remainingMinutes > 0 ? " " + TL(M,"and") + " " + remainingMinutes + " " + TL(M,"minutes") : "");

        } else if (seconds < 31536000) {
            long months = seconds / 2592000;
            long remainingDays = (seconds % 2592000) / 86400;
            long remainingHours = (seconds % 86400) / 3600;
            return months + (months == 1 ? " " + TL(M,"month") : " " + TL(M,"months")) +
                    (remainingDays > 0 ? " " + TL(M,"and") + " " + remainingDays + " " + TL(M,"days") : "") +
                    (remainingHours > 0 ? " " + TL(M,"and") + " " + remainingHours + " " + TL(M,"hours") : "");
        } else {
            long years = seconds / 31536000;
            long remainingMonths = (seconds % 31536000) / 2592000;
            return years + (years == 1 ? " " + TL(M,"year") : " " + TL(M,"years")) +
                    (remainingMonths > 0 ? " " + TL(M,"and") + " " + remainingMonths + " " + TL(M,"months") : "");
        }
    }
    public static String formatRelativeTimeTLG(Guild G, Duration duration) {
        long seconds = duration.getSeconds();

        if (seconds < 60) {
            return seconds == 1 ? "1 " + TLG(G,"second") : seconds + " " + TLG(G,"seconds");
        } else if (seconds < 3600) {
            long minutes = seconds / 60;
            long remainingSeconds = seconds % 60;
            return minutes + (minutes == 1 ? " " + TLG(G,"minute") : " " + TLG(G,"minutes")) +
                    (remainingSeconds > 0 ? " " + TLG(G,"and") + " " + remainingSeconds + " " + TLG(G,"seconds") : "");
        } else if (seconds < 86400) {
            long hours = seconds / 3600;
            long remainingMinutes = (seconds % 3600) / 60;
            return hours + (hours == 1 ? " " + TLG(G,"hour") : " " + TLG(G,"hours")) +
                    (remainingMinutes > 0 ? " " + TLG(G,"and") + " " + remainingMinutes + " " + TLG(G,"minutes") : "");
        } else if (seconds < 2592000) {
            long days = seconds / 86400;
            long remainingHours = (seconds % 86400) / 3600;
            long remainingMinutes = (seconds % 3600) / 60;
            return days + (days == 1 ? " " + TLG(G,"day") : " " + TLG(G,"days")) +
                    (remainingHours > 0 ? " " + TLG(G,"and") + " " + remainingHours + " " + TLG(G,"hours") : "") +
                    (remainingMinutes > 0 ? " " + TLG(G,"and") + " " + remainingMinutes + " " + TLG(G,"minutes") : "");

        } else if (seconds < 31536000) {
            long months = seconds / 2592000;
            long remainingDays = (seconds % 2592000) / 86400;
            long remainingHours = (seconds % 86400) / 3600;
            return months + (months == 1 ? " " + TLG(G,"month") : " " + TLG(G,"months")) +
                    (remainingDays > 0 ? " " + TLG(G,"and") + " " + remainingDays + " " + TLG(G,"days") : "") +
                    (remainingHours > 0 ? " " + TLG(G,"and") + " " + remainingHours + " " + TLG(G,"hours") : "");
        } else {
            long years = seconds / 31536000;
            long remainingMonths = (seconds % 31536000) / 2592000;
            return years + (years == 1 ? " " + TLG(G,"year") : " " + TLG(G,"years")) +
                    (remainingMonths > 0 ? " " + TLG(G,"and") + " " + remainingMonths + " " + TLG(G,"months") : "");
        }
    }

    public static double TournamentParticipantMultiplier(int participants) {
        if (participants <= 8) {
            //return 0.25;
        }
        if (participants < 16) {
            //return 0.5;
        }
        if (false) {
            return ((double) participants / 32);
        }
        return 1 + (participants * 0.005);
    }
    public static double TournamentRewardPoints(int Position, double Multiplier) {
        if (Position == 1) {
            return 1 * Multiplier;
        } else if (Position == 2) {
            return 0.5 * Multiplier;
        } else if (Position == 3) {
            return 0.25 * Multiplier;
        } else if (Position == 4) {
            return 0.125 * Multiplier;
        } else if (Position <= 6) {
            return 0.0625 * Multiplier;
        } else if (Position <= 9) {
            return 0.03125 * Multiplier;
        } else if (Position <= 17) {
            return 0.015625 * Multiplier;
        } else if (Position <= 33) {
            return 0.0078125 * Multiplier;
        } else if (Position <= 65) {
            return 0.00390625 * Multiplier;
        }
        return 0;
    }

    public static List<WebhookEmbed> parseEmbedBuilders(List<MessageEmbed> list) {
        List<WebhookEmbed> l = new ArrayList<>();
        for (MessageEmbed E : list) {
            WebhookEmbedBuilder EE = new WebhookEmbedBuilder();
            EE.setDescription(E.getDescription());
            EE.setTimestamp(E.getTimestamp());
            EE.setImageUrl(E.getImage() != null ? E.getImage().getUrl() : null);
            EE.setColor(E.getColor() != null ? E.getColor().getRGB() : null);
            EE.setThumbnailUrl(E.getThumbnail() != null ? E.getThumbnail().getUrl(): null);
            EE.setTitle(E.getTitle() != null ? new WebhookEmbed.EmbedTitle(E.getTitle(), null) : null);
            EE.setAuthor(E.getAuthor() != null && E.getAuthor().getName() != null ? new WebhookEmbed.EmbedAuthor(E.getAuthor().getName(), E.getAuthor().getIconUrl(), E.getAuthor().getUrl()) : null);
            EE.setFooter(E.getFooter() != null && E.getFooter().getText() != null ? new WebhookEmbed.EmbedFooter(E.getFooter().getText(), E.getFooter().getIconUrl()) : null);
            if (!E.getFields().isEmpty()) {
                for (MessageEmbed.Field F : E.getFields()) {
                    EE.addField(new WebhookEmbed.EmbedField(F.isInline(), F.getName(), F.getValue()));
                }
            }
            l.add(EE.build());
        }
        return l;
    }
    public static List<WebhookEmbed> parseEmbedBuilders(MessageEmbed... list) {
        return parseEmbedBuilders(Arrays.stream(list).collect(Collectors.toList()));
    }
    public static ActionRow parseComponentBuilder(List<Component> list) {
        ComponentElement[] components  = new ComponentElement[list.size()];
        for (int i = 0; i < list.size(); i++) {
            Component component = list.get(i);
            if (component instanceof net.dv8tion.jda.api.components.buttons.Button B) {
                components[i] = new Button(parseButtonStyle(B.getStyle()), B.getCustomId() != null ? B.getCustomId() : B.getUrl() != null ? B.getUrl() : "N/A").setLabel(B.getLabel()).setDisabled(B.isDisabled());
            } else if (component instanceof net.dv8tion.jda.api.components.selections.StringSelectMenu B) {
                components[i] = new StringSelectMenu(B.getCustomId()).setDisabled(B.isDisabled()).setMaxValues(B.getMaxValues()).setMinValues(B.getMaxValues()).setPlaceholder(B.getPlaceholder());
            }
        }
        return ActionRow.of(components);
    }

    public static ActionRow parseComponentBuilder(Component... list) {
        return parseComponentBuilder(Arrays.stream(list).collect(Collectors.toList()));
    }
    public static Button.Style parseButtonStyle(ButtonStyle style) {
        return style.equals(ButtonStyle.LINK) ? Button.Style.LINK
                : style.equals(ButtonStyle.DANGER) ? Button.Style.DANGER
                : style.equals(ButtonStyle.SUCCESS) ? Button.Style.SUCCESS
                : style.equals(ButtonStyle.SECONDARY) ? Button.Style.SECONDARY
                : Button.Style.PRIMARY;
    }



    public static void showImageInFrame(BufferedImage image) {
        // Create a new JFrame (window)
        JFrame frame = new JFrame("Image Display");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Close this window when done

        // Create a JPanel to display the image
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // Draw the image
                g.drawImage(image, 0, 0, null);
            }

            @Override
            public Dimension getPreferredSize() {
                // Set the panel size to match the image size
                return new Dimension(image.getWidth(), image.getHeight());
            }
        };

        // Add the panel to the frame
        frame.getContentPane().add(panel);
        frame.pack();  // Adjust the frame size to fit the content
        frame.setLocationRelativeTo(null);  // Center the frame on the screen
        frame.setVisible(true);  // Make the frame visible
    }

    public static boolean isColorcodeValid(InteractionHook M, String text) {
        if (isColorcodeValid(text)) return true;
        M.editOriginal(TL(M, "error-invalid-colorcode")).queue();
        return false;
    }

    public static String getComponentFullID(Message M, String id) {
        return getComponentFullID(M.getComponents(), id);
    }
    public static String getComponentFullID(List<MessageTopLevelComponentUnion> comps, String id) {
        if (comps != null) for (MessageTopLevelComponentUnion ARs : comps) {
            if (ARs instanceof ActionRowImpl AR) {
                for (ActionRowChildComponentUnion compo : AR.getComponents()) {
                    if (compo instanceof StringSelectMenuImpl B && B.getCustomId().startsWith(id))
                        return B.getCustomId();
                    else if (compo instanceof SelectMenuImpl B && B.getCustomId().equals(id))
                        return B.getCustomId();
                    else if (compo instanceof ButtonImpl B && B.getCustomId() != null && B.getCustomId().startsWith(id))
                        return B.getCustomId();
                }
            }
        }
        return id;
    }

}