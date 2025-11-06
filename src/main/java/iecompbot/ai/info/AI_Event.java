package iecompbot.ai.info;

import iecompbot.objects.event.Event;
import iecompbot.objects.event.Event_Organiser;
import iecompbot.objects.event.Event_Team;
import net.dv8tion.jda.api.entities.User;

import java.util.List;
import java.util.stream.Collectors;

public class AI_Event {

    private final List<AI_EventTeam> Teams;

    private final String Name;
    private final String Description;
    private final String Description2;
    private final String TeaserLink;
    private final String IntroLink;
    private final String TrailerLink;
    private final String Type;
    private final String Trivia;
    private final String ChallongeLink;
    private final String GameName;
    private final String Winner;
    private final long StartedAtTimeEpochSecond;
    private final long CompletedAtTimeEpochSecond;
    private final List<AI_EventOrganiser> Organisers;
    private final List<String> Casters;

    public AI_Event(Event E) {
        Name = E.getName();
        Description = E.getDescription();
        Description2 = E.getDescription2();
        TeaserLink = E.getTeaser() != null ? "https://www.youtube.com/watch?v=" + E.getTeaser() : null;
        IntroLink = E.getIntro() != null ? "https://www.youtube.com/watch?v=" + E.getIntro() : null;
        TrailerLink = E.getTrailer() != null ? "https://www.youtube.com/watch?v=" + E.getTrailer() : null;
        Type = E.getType().name();
        Trivia = E.getTrivia();
        ChallongeLink = E.getLink();
        GameName = E.getGame().getEmojiFormatted() + " " + E.getGame().getFullName();
        Winner = E.getWinner();
        StartedAtTimeEpochSecond = E.getStartedAtTimeEpochSecond();
        CompletedAtTimeEpochSecond = E.getCompletedAtTimeEpochSecond();
        Organisers = E.getOrganisers().stream().map(AI_EventOrganiser::new).collect(Collectors.toList());
        Casters = E.getCasters().stream().map(User::getEffectiveName).collect(Collectors.toList());
        Teams = E.getTeams().stream().map(AI_EventTeam::new).collect(Collectors.toList());
    }

    public static class AI_EventOrganiser {
        private final String Name;
        private final String Role;

        public AI_EventOrganiser(Event_Organiser E) {
            Name = E.getUser().getEffectiveName();
            Role = E.getRole();
        }
    }
    public static class AI_EventTeam {
        private final String Name;
        private final int Position;
        private final List<String> PlayersNames;

        public AI_EventTeam(Event_Team E) {
            Name = E.getName();
            Position = E.getPosition();
            PlayersNames = E.getUsers().stream().map(User::getEffectiveName).collect(Collectors.toList());
            PlayersNames.addAll(E.getUnknown());
        }
    }
}
