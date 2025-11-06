package iecompbot.objects.server.tournament;

import iecompbot.objects.match.Game;
import iecompbot.objects.server.tournament.challonge.server.SChallonge_Tournament;
import iecompbot.springboot.data.DatabaseObject;
import net.dv8tion.jda.api.entities.User;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static iecompbot.objects.BotManagers.isTournamentManager;

public class BaseTournament<T> extends DatabaseObject<T> {
    private transient Game G;

    protected long ID;
    protected String Name;
    protected String Type;
    protected String Description;
    protected String GameCode;
    protected String ResultImageURL;
    protected String SignupImageURL;
    protected int ParticipantCount;
    protected int SignupCap;
    protected int VSAmount = 1;

    protected String Modifiers;


    protected boolean isInscriptionByBot = true;
    protected boolean isPublic = true;
    protected long RegistrationStartEpochSecond = 0;
    protected long StartedAtTimeEpochSecond = 0;
    protected long StartAtTimeEpochSecond = 0;
    protected long UpdatedAtTimeEpochSecond;
    protected long CreatedAtTimeEpochSecond = 0;
    protected long CompletedAtTimeEpochSecond = 0;

    public long getId() {
        return ID;
    }

    public int getVSAmount() {
        return VSAmount;
    }

    public String getName() {
        return Name;
    }

    public String getDescription() {
        return Description;
    }

    public String getResultImageURL() {
        return cleanDiscordMediaUrl(ResultImageURL);

    }

    public Game getGame() {
        return G == null ? G = Game.get(GameCode) : G;
    }

    public int getParticipantCount() {
        return ParticipantCount;
    }

    public int getSignupCap() {
        return SignupCap;
    }

    public Instant getStartAtTime() {
        return Instant.ofEpochSecond(StartAtTimeEpochSecond);
    }

    public Instant getStartedAtTime() {
        return Instant.ofEpochSecond(StartedAtTimeEpochSecond);
    }

    public String getSignupImageURL() {
        return SignupImageURL;
    }

    public boolean isInscriptionByBot() {
        return isInscriptionByBot;
    }

    public Instant getRegistrationStart() {
        return Instant.ofEpochSecond(RegistrationStartEpochSecond);
    }

    public long getStartedAtTimeEpochSecond() {
        return StartedAtTimeEpochSecond;
    }

    public long getStartAtTimeEpochSecond() {
        return StartAtTimeEpochSecond;
    }

    public Instant getUpdatedAtTime() {
        return Instant.ofEpochSecond(UpdatedAtTimeEpochSecond);
    }

    public Instant getCreatedAtTime() {
        return Instant.ofEpochSecond(CreatedAtTimeEpochSecond);
    }

    public long getCompletedAtTimeEpochSecond() {
        return CompletedAtTimeEpochSecond;
    }

    public Instant getCompletedAtTime() {
        return Instant.ofEpochSecond(CompletedAtTimeEpochSecond);
    }

    public boolean isPublic() {
        return isPublic;
    }

    public boolean isOrganiser(User user) {
        return isTournamentManager(user);
    }

    public static List<BaseTournament<?>> getAll() throws Exception {
        return new ArrayList<>(SChallonge_Tournament.list(true));
    }


    static String cleanDiscordMediaUrl(String urlStr) {
        try {
            URL url = URI.create(urlStr).toURL();
            String baseUrl = url.getProtocol() + "://" + url.getHost() + url.getPath();

            // Parse query parameters
            String query = url.getQuery();
            if (query == null || query.isEmpty()) return baseUrl;

            StringBuilder newQuery = new StringBuilder();
            String[] params = query.split("&");

            for (String param : params) {
                if (param.startsWith("width=") ||
                        param.startsWith("height=")) {
                    continue; // Skip these
                }

                if (!param.isEmpty()) {
                    if (!newQuery.isEmpty()) newQuery.append("&");
                    newQuery.append(param);
                }
            }

            return !newQuery.isEmpty() ? baseUrl + "?" + newQuery : baseUrl;

        } catch (MalformedURLException e) {
            return urlStr; // Return original if error
        }
    }

    public boolean hasModifierAnonymous() {
        return Modifiers != null && Modifiers.contains("ANONYMOUS");
    }
    public boolean hasModifierNoReveal() {
        return Modifiers != null && Modifiers.contains("NOREVEAL");
    }
}