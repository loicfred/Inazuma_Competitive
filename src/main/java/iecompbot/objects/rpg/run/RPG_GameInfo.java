package iecompbot.objects.rpg.run;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import static iecompbot.Main.MainDirectory;

public class RPG_GameInfo {

    public String dir;
    public String channelid;
    public String channelname;
    public String channelmention;
    public String messageid;
    public String CaptainP1ID;
    public String CaptainP2ID;
    public String IDOfCurrentTurn;
    public int TurnsLeft;
    public double MatchMinute;
    public int P1Goals;
    public int P2Goals;
    public int Half;
    public boolean hasProlong;
    public boolean hasPenalty;
    public double MinutePerAction;
    public String Game;

    public void Initialize() throws IOException {
        File gamefile = new File(MainDirectory + "/storage/rpg/" + dir + "/game.txt");
        if (gamefile.exists()) {
            try ( Scanner scangame = new Scanner(gamefile, StandardCharsets.UTF_8)) {
                String channels = scangame.nextLine();
                channelid = channels.split(" - ")[0];
                channelname = channels.split(" - ")[1];
                channelmention = channels.split(" - ")[2];
                messageid = channels.split(" - ")[3];

                String capid = scangame.nextLine();
                CaptainP1ID = capid.split(" - ")[0];
                CaptainP2ID = capid.split(" - ")[1];

                String turn = scangame.nextLine();
                IDOfCurrentTurn = turn.split(": ")[0];
                TurnsLeft = Integer.parseInt(turn.split(": ")[1]);

                String goals = scangame.nextLine();
                P1Goals = Integer.parseInt(goals.split(" - ")[0]);
                P2Goals = Integer.parseInt(goals.split(" - ")[1]);

                String penaltyprolong = scangame.nextLine();
                MinutePerAction = Double.parseDouble(penaltyprolong.split(" - ")[0]);
                hasProlong = Boolean.parseBoolean(penaltyprolong.split(" - ")[1]);
                hasPenalty = Boolean.parseBoolean(penaltyprolong.split(" - ")[2]);

                String g = scangame.nextLine();
                Game = g.split(" - ")[0];
                Half = Integer.parseInt(g.split(" - ")[1]);
                MatchMinute = Double.parseDouble(g.split(" - ")[2]);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    public void RPGSaveChanges() throws IOException {
        File gamefile = new File(MainDirectory + "/storage/rpg/" + dir + "/game.txt");
        try (FileWriter fwww = new FileWriter(gamefile, StandardCharsets.UTF_8);
             PrintWriter pwww = new PrintWriter(fwww)) {
            pwww.println(channelid + " - " + channelname + " - " + channelmention + " - " + messageid);
            pwww.println(CaptainP1ID + " - " + CaptainP2ID);
            pwww.println(IDOfCurrentTurn + ": " + TurnsLeft);
            pwww.println(P1Goals + " - " + P2Goals);
            pwww.println(MinutePerAction + " - " + hasProlong + " - " + hasPenalty);
            pwww.println(Game + " - " + Half + " - " + MatchMinute);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public RPG_GameInfo(String dir) {
        this.dir = dir;
    }
}
