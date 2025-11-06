package iecompbot.interaction.custom;

import iecompbot.interaction.cmdbreakdown.PageViewerCommand;
import iecompbot.interaction.cmdbreakdown.profile.FilterCommand;
import iecompbot.interaction.cmdbreakdown.profile.GamesCommand;
import iecompbot.objects.match.Game;
import net.dv8tion.jda.api.components.MessageTopLevelComponent;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.selections.SelectOption;
import net.dv8tion.jda.api.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.Interaction;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;

import java.util.ArrayList;
import java.util.List;

import static iecompbot.L10N.TL;

public class BuiltMessageE extends MessageEditBuilder {
    private final Interaction M;
    private final List<MessageTopLevelComponent> Components = new ArrayList<>(5);
    private final List<MessageEmbed> Embeds = new ArrayList<>(10);

    public void addComponents(MessageTopLevelComponent... components) {
        for (MessageTopLevelComponent c : components) if (getComponents().size() < 5) Components.add(c);
        setComponents(Components);
    }
    public void addEmbeds(MessageEmbed... embeds) {
        for (MessageEmbed c : embeds) if (getComponents().size() < 5) Embeds.add(c);
        setEmbeds(Embeds);
    }

    public void EnablePagination(PageViewerCommand CMD, int AmountPerPages, int totalAmount) {
        List<SelectOption> Pages = new ArrayList<>();
        for (int i = 0; i < Math.ceil((double) totalAmount/AmountPerPages); i++) {
            if (Pages.size() < 25) {
                Pages.add(SelectOption.of(TL(M,"Page") + " " + (i + 1) + "...", "" + (i + 1)).withDefault(i + 1 == CMD.Page)
                        .withDescription("[" + ((i * AmountPerPages) + 1) + "-" + Math.min(((i + 1) * AmountPerPages), totalAmount) + "/" + totalAmount + "] " + TL(M,"view-more", "" + (i + 1))));
            }
        }
        if (getComponents().size() < 5 && Pages.size() > 1) Components.add(ActionRow.of(StringSelectMenu.create(CMD.toCommand()).setPlaceholder(Pages.get(CMD.Page-1).getLabel()).setRequiredRange(1, 1).addOptions(Pages).build()));
        setComponents(Components);
    }
    public void EnableFilter(FilterCommand CMD, int minRange, int maxRange, SelectOption... options) {
        if (getComponents().size() < 5 && options.length > 1) Components.add(ActionRow.of(StringSelectMenu.create(CMD.toCommand())
                .setPlaceholder(TL(M,"Filter")).setRequiredRange(minRange, maxRange)
                .addOptions(options).build()));
        setComponents(Components);
    }
    public void EnableGames(GamesCommand CMD, int minRange, int maxRange) {
        if (getComponents().size() < 5) Components.add(ActionRow.of(StringSelectMenu.create(CMD.toCommand())
                .setPlaceholder(TL(M, "All")).setRequiredRange(minRange, maxRange)
                .addOptions(Game.getSelectOptions(M, CMD.Games)).build()));
        setComponents(Components);
    }

    public BuiltMessageE(InteractionHook M) {
        this.M = M.getInteraction();
    }
    public BuiltMessageE(Interaction M) {
        this.M = M;
    }
}
