package iecompbot.objects.rpg.run;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import static iecompbot.Main.MainDirectory;

public class RPG_TeamPositions {

    public String dir;
    public int Team;
    public String P1ID;
    public String P1POS;
    public String P2ID;
    public String P2POS;
    public String P3ID;
    public String P3POS;
    public String P4ID;
    public String P4POS;
    public String P5ID;
    public String P5POS;

    public void Save() throws IOException {
        File gamefile = new File(MainDirectory + "/storage/rpg/" + dir + "/pos" + Team + ".txt");
        try (FileWriter fwww = new FileWriter(gamefile, StandardCharsets.UTF_8);
             PrintWriter pwww = new PrintWriter(fwww)) {
            pwww.println(P1ID);
            pwww.println(P1POS);
            pwww.println(P2ID);
            pwww.println(P2POS);
            pwww.println(P3ID);
            pwww.println(P3POS);
            pwww.println(P4ID);
            pwww.println(P4POS);
            pwww.println(P5ID);
            pwww.println(P5POS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void Initialize() throws IOException {
        File gamefile = new File(MainDirectory + "/storage/rpg/" + dir + "/pos" + Team + ".txt");
        try (Scanner scangame = new Scanner(gamefile, StandardCharsets.UTF_8)) {
            P1ID = scangame.nextLine();
            P1POS = scangame.nextLine();
            P2ID = scangame.nextLine();
            P2POS = scangame.nextLine();
            P3ID = scangame.nextLine();
            P3POS = scangame.nextLine();
            P4ID = scangame.nextLine();
            P4POS = scangame.nextLine();
            P5ID = scangame.nextLine();
            P5POS = scangame.nextLine();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    public RPG_TeamPositions(String dir, int Team) {
        this.dir = dir;
        this.Team = Team;
    }

}
