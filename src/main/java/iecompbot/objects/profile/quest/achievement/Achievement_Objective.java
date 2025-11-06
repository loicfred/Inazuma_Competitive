package iecompbot.objects.profile.quest.achievement;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import iecompbot.objects.profile.quest.BaseObjective;
import iecompbot.objects.profile.quest.Objective;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static my.utilities.util.Utilities.GenerateRandomNumber;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE)
public class Achievement_Objective extends BaseObjective<Achievement_Objective> {
    private transient List<Objective> objs;

    public long AchievementID;

    public List<Objective> list() {
        if (objs == null) {
            objs = new ArrayList<>();
            objs.add(new Objective(1, Description1, Type1, Progress1, RequiredProgress1));
            if (Description2 != null) objs.add(new Objective(2, Description2, Type2, Progress2, RequiredProgress2));
            if (Description3 != null) objs.add(new Objective(3, Description3, Type3, Progress3, RequiredProgress3));
            if (Description4 != null) objs.add(new Objective(4, Description4, Type4, Progress4, RequiredProgress4));
            if (Description5 != null) objs.add(new Objective(5, Description5, Type5, Progress5, RequiredProgress5));
        }
        return objs;
    }

    protected void setProgress(String Type, int amount) {
        for (Objective O : list()) {
            if (O.getType().equals(Type) && O.getProgress() != amount) {
                if (O.I == 1) {
                    Progress1 = O.Set(amount);
                } else if (O.I == 2) {
                    Progress2 = O.Set(amount);
                } else if (O.I == 3) {
                    Progress3 = O.Set(amount);
                } else if (O.I == 4) {
                    Progress4 = O.Set(amount);
                } else if (O.I == 5) {
                    Progress5 = O.Set(amount);
                }
                Update();
            }
        }
    }

    private Achievement_Objective() {}
    public Achievement_Objective(long achievementid, Objective... o) {
        this.AchievementID = achievementid;
        init(List.of(o));
    }
    public Achievement_Objective(long achievementid, List<Objective> o) {
        this.AchievementID = achievementid;
        init(o);
    }
    private void init(List<Objective> i) {
        ID = Instant.now().toEpochMilli() + GenerateRandomNumber(1,999);
        for (Objective O : i) {
            if (Description1 == null) {
                RequiredProgress1 = O.getRequiredProgress();
                Description1 = O.getDescription();
                Type1 = O.getType();
                Progress1 = 0;
            } else if (Description2 == null) {
                RequiredProgress2 = O.getRequiredProgress();
                Description2 = O.getDescription();
                Type2 = O.getType();
                Progress2 = 0;
            } else if (Description3 == null) {
                RequiredProgress3 = O.getRequiredProgress();
                Description3 = O.getDescription();
                Type3 = O.getType();
                Progress3 = 0;
            } else if (Description4 == null) {
                RequiredProgress4 = O.getRequiredProgress();
                Description4 = O.getDescription();
                Type4 = O.getType();
                Progress4 = 0;
            } else if (Description5 == null) {
                RequiredProgress5 = O.getRequiredProgress();
                Description5 = O.getDescription();
                Type5 = O.getType();
                Progress5 = 0;
            }
        }
        Write();
    }

    public boolean areComplete() {
        return list().stream().allMatch(Objective::isComplete);
    }

    public static Achievement_Objective get(long id) {
        return getById(Achievement_Objective.class, id).orElse(null);
    }
    public static Achievement_Objective ofAchievement(long questID) {
        return getWhere(Achievement_Objective.class, "AchievementID = ?", questID).orElse(null);
    }

}
