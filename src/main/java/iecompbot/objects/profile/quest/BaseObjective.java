package iecompbot.objects.profile.quest;

import iecompbot.springboot.data.DatabaseObject;

public class BaseObjective<T> extends DatabaseObject<T> {
    protected long ID;
    protected String Description1;
    protected String Description2;
    protected String Description3;
    protected String Description4;
    protected String Description5;
    protected String Type1;
    protected String Type2;
    protected String Type3;
    protected String Type4;
    protected String Type5;
    protected int Progress1;
    protected Integer Progress2;
    protected Integer Progress3;
    protected Integer Progress4;
    protected Integer Progress5;
    protected int RequiredProgress1;
    protected Integer RequiredProgress2;
    protected Integer RequiredProgress3;
    protected Integer RequiredProgress4;
    protected Integer RequiredProgress5;

    public long getId() {
        return ID;
    }

    public Integer getRequiredProgress5() {
        return RequiredProgress5;
    }

    public Integer getRequiredProgress4() {
        return RequiredProgress4;
    }

    public Integer getRequiredProgress3() {
        return RequiredProgress3;
    }

    public Integer getRequiredProgress2() {
        return RequiredProgress2;
    }

    public int getRequiredProgress1() {
        return RequiredProgress1;
    }

    public Integer getProgress5() {
        return Progress5;
    }

    public Integer getProgress4() {
        return Progress4;
    }

    public Integer getProgress3() {
        return Progress3;
    }

    public Integer getProgress2() {
        return Progress2;
    }

    public int getProgress1() {
        return Progress1;
    }

    public String getType5() {
        return Type5;
    }

    public String getType4() {
        return Type4;
    }

    public String getType3() {
        return Type3;
    }

    public String getType2() {
        return Type2;
    }

    public String getType1() {
        return Type1;
    }

    public String getDescription5() {
        return Description5;
    }

    public String getDescription4() {
        return Description4;
    }

    public String getDescription3() {
        return Description3;
    }

    public String getDescription2() {
        return Description2;
    }

    public String getDescription1() {
        return Description1;
    }
}
