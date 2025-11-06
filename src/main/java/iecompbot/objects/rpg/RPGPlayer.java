package iecompbot.objects.rpg;

public class RPGPlayer {
    String ID;
    String Name;
    String MiximaxID;
    public RPGPlayer(String id, String n, String m) {
        this.ID = id;
        this.Name = n;
        this.MiximaxID = m;
    }
}