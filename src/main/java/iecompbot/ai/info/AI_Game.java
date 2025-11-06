package iecompbot.ai.info;


import iecompbot.springboot.data.DatabaseObject;

public class AI_Game {

    public String GameName;
    public String Mechanic;
    public String Meta;
    public String BestChoices;

    public AI_Game(DatabaseObject.Row row) {
        GameName = row.getAsString("FullName");
        Mechanic = row.getAsString("Mechanic");
        Meta = row.getAsString("Meta");
        BestChoices = row.getAsString("BestChoices");
    }
}
