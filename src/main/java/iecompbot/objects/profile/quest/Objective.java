package iecompbot.objects.profile.quest;

public class Objective {

    private final String Description;
    private final String Type;
    private final int RequiredProgress;
    private int Progress;
    public int I = 1;

    public Objective(int i, String description, String type, int progress, int requiredProgress1) {
        I = i;
        Description = description;
        Type = type;
        Progress = progress;
        RequiredProgress = requiredProgress1;
    }
    public Objective(String description, String type, int requiredProgress1) {
        Description = description;
        Type = type;
        Progress = 0;
        RequiredProgress = requiredProgress1;
    }

    public int Increment(int amount) {
        return Progress = Math.min(Progress + amount, RequiredProgress);
    }

    public int Set(int amount) {
        return Progress = Math.min(amount, RequiredProgress);
    }

    public String getDescription() {
        return Description;
    }
    public String getType() {
        return Type;
    }
    public int getProgress() {
        return Progress;
    }
    public int getRequiredProgress() {
        return RequiredProgress;
    }

    public boolean isComplete() {
        return Progress >= RequiredProgress;
    }
}
