package iecompbot.objects.rpg.run;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import static iecompbot.Main.MainDirectory;

public class RPG_Formation {

    public String dir;
    private final int Team;
    public String P1POS;
    public String P2POS;
    public String P3POS;
    public String P4POS;
    public String P1JoinPOS;
    public String P2JoinPOS;
    public String P3JoinPOS;
    public String P4JoinPOS;

    public void Initialize() throws IOException {
        File gamefile = new File(MainDirectory + "/storage/rpg/" + dir + "/formation" + Team + ".txt");
        try (Scanner scangame = new Scanner(gamefile, StandardCharsets.UTF_8)) {
            P1POS = scangame.nextLine();
            P2POS = scangame.nextLine();
            P3POS = scangame.nextLine();
            P4POS = scangame.nextLine();
            P1JoinPOS = scangame.nextLine();
            P2JoinPOS = scangame.nextLine();
            P3JoinPOS = scangame.nextLine();
            P4JoinPOS = scangame.nextLine();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public RPG_Formation(String dir, int Team) {
        this.dir = dir;
        this.Team = Team;
    }

}
