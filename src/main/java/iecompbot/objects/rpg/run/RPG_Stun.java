package iecompbot.objects.rpg.run;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import static iecompbot.Main.MainDirectory;

public class RPG_Stun {

    public String dir;
    public int Team;
    public String P1ID;
    public int P1StunTurn;
    public String P2ID;
    public int P2StunTurn;
    public String P3ID;
    public int P3StunTurn;
    public String P4ID;
    public int P4StunTurn;
    public String P5ID;
    public int P5StunTurn;

    public void Save() throws IOException {
        File gamefile = new File(MainDirectory + "/storage/rpg/" + dir + "/stun" + Team + ".txt");
        try (FileWriter fwww = new FileWriter(gamefile, StandardCharsets.UTF_8);
             PrintWriter pwww = new PrintWriter(fwww)) {
            pwww.println(P1ID);
            pwww.println(P1StunTurn);
            pwww.println(P2ID);
            pwww.println(P2StunTurn);
            pwww.println(P3ID);
            pwww.println(P3StunTurn);
            pwww.println(P4ID);
            pwww.println(P4StunTurn);
            pwww.println(P5ID);
            pwww.println(P5StunTurn);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    public void Initialize() throws IOException {
        File gamefile = new File(MainDirectory + "/storage/rpg/" + dir + "/stun" + Team + ".txt");
        try (Scanner scangame = new Scanner(gamefile, StandardCharsets.UTF_8)) {
            P1ID = scangame.nextLine();
            P1StunTurn = Integer.parseInt(scangame.nextLine());
            P2ID = scangame.nextLine();
            P2StunTurn = Integer.parseInt(scangame.nextLine());
            P3ID = scangame.nextLine();
            P3StunTurn = Integer.parseInt(scangame.nextLine());
            P4ID = scangame.nextLine();
            P4StunTurn = Integer.parseInt(scangame.nextLine());
            P5ID = scangame.nextLine();
            P5StunTurn = Integer.parseInt(scangame.nextLine());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public RPG_Stun(String dir, int Team) {
        this.dir = dir;
        this.Team = Team;
    }

}
