package iecompbot.interaction.cmdbreakdown.profile;

import iecompbot.interaction.cmdbreakdown.IDCommand;
import iecompbot.objects.profile.Profile;
import net.dv8tion.jda.api.entities.User;

public class ProfileCommand extends IDCommand {
    private transient Profile P = null;
    private transient User User = null;

    public ProfileCommand(String command) {
        super(command);
        if (ID != null) P = Profile.get(ID);
        if (ID != null) User = P.getUser();
    }
    public ProfileCommand(User user) {
        super("");
        this.User = user;
        this.ID = user.getIdLong();
        this.P = Profile.get(user);
    }
    public ProfileCommand(long userid) {
        super("");
        this.ID = userid;
        this.P = Profile.get(userid);
    }
    public ProfileCommand(Profile pf) {
        super("");
        this.ID = pf.getID();
        this.P = pf;
    }


    public synchronized User getUser() throws Exception {
        return User == null && ID != null ? User = getProfile().getUser() : User;
    }
    public synchronized Profile getProfile() {
        return P == null && ID != null ? P = Profile.get(ID) : P;
    }
}
