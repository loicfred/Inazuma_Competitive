package iecompbot.interaction.custom;

import iecompbot.interaction.cmdbreakdown.PageViewerCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.selections.SelectOption;
import net.dv8tion.jda.api.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.interactions.Interaction;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;

import java.util.ArrayList;
import java.util.List;

import static iecompbot.L10N.TL;

public class BuiltMessage extends MessageCreateBuilder {
    private final Interaction M;

    public void AddEmbed(EmbedBuilder embed) {
        this.addEmbeds(embed.build());
    }
    public void RemoveEmbed(int index) {
        this.getEmbeds().remove(index);
    }
    public void ClearEmbeds() {
        this.getEmbeds().clear();
    }


    public void EnablePagination(PageViewerCommand CMD, int AmountPerPages, int totalAmount) {
        List<SelectOption> Pages = new ArrayList<>();
        for (int i = 0; i < Math.ceil((double) totalAmount/AmountPerPages); i++) {
            if (Pages.size() < 25) {
                Pages.add(SelectOption.of(TL(M,"Page") + " " + (i + 1) + "...", "" + (i + 1))
                        .withDescription("[" + ((i * AmountPerPages) + 1) + "-" + Math.min(((i + 1) * AmountPerPages), totalAmount) + "/" + totalAmount + "] " + TL(M,"view-more", "" + (i + 1))));
            }
        }
        if (getComponents().size() < 5 && Pages.size() > 1) addComponents(ActionRow.of(StringSelectMenu.create(CMD.toCommand()).setPlaceholder(Pages.get(CMD.Page-1).getLabel()).setRequiredRange(1, 1).addOptions(Pages).build()));
    }

    public BuiltMessage(InteractionHook M) {
        this.M = M.getInteraction();
    }
    public BuiltMessage(Interaction M) {
        this.M = M;
    }
}
