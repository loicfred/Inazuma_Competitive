package iecompbot.objects.clan.interclan;

import iecompbot.objects.match.BaseDuel;
import iecompbot.objects.match.Game;

import java.time.Instant;
import java.util.List;

public class Interclan_Duel extends BaseDuel<Interclan_Duel> {
    private transient Interclan I;

    public long InterclanID;

    private Interclan_Duel() {}
    public Interclan_Duel(Interclan I, long p1ID, long p2ID, Game G) {
        this.ID = Instant.now().toEpochMilli();
        this.I = I;
        this.G = G;
        InterclanID = I.getId();
        P1ID = p1ID;
        P2ID = p2ID;
        GameCode = G.getCode();
        Write();
    }

    public Interclan getInterclan() {
        return I == null ? I = Interclan.get(InterclanID) : I;
    }

    public void setP1ID(long p1ID) {
        P1ID = p1ID;
    }

    public void setP2ID(long p2ID) {
        P2ID = p2ID;
    }

    public void setGame(Game g) {
        GameCode = g.toString();
    }

    public static List<Interclan_Duel> get(Interclan I) {
        List<Interclan_Duel> IDs = getAllWhere(Interclan_Duel.class, "InterclanID = ?", I.getId());
        for (Interclan_Duel d : IDs) {d.I = I;}
        return IDs;
    }

    public static List<Interclan_Duel> get(Interclan I, Game G) {
        List<Interclan_Duel> l = getAllWhere(Interclan_Duel.class, "InterclanID = ? AND GameCode = ?", I.getId(), G.getCode());
        for (Interclan_Duel i : l) {i.I = I;i.G = G;}
        return l;
    }
}
