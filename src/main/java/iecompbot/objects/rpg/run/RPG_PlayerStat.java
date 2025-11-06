package iecompbot.objects.rpg.run;

import iecompbot.interaction.Automation;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import static iecompbot.Main.MainDirectory;

public class RPG_PlayerStat {

    public String dir;
    public String id;
    public String Name;
    public String Element;
    public String Position;
    public String Gender;
    public String Kick;
    public String Dribble;
    public String Defense;
    public String Catch;
    public String Technique;
    public String Stamina;
    public int MaxGP;
    public int GP;
    public int MaxTP;
    public int TP;
    public String Move1;
    public String Move2;
    public String Move3;
    public String Move4;
    public String Skill1;
    public String Skill2;
    public String Spirit;
    public String MixiSkill1;
    public String MixiSkill2;

    public void Save() throws IOException {
        File gamefile = new File(MainDirectory + "/storage/rpg/" + dir + "/stats/" + id + ".txt");
        try (FileWriter fwww = new FileWriter(gamefile, StandardCharsets.UTF_8);
             PrintWriter pwww = new PrintWriter(fwww)) {
            pwww.println(Name);
            pwww.println(Element);
            pwww.println(Position);
            pwww.println(Gender);
            pwww.println(Kick);
            pwww.println(Dribble);
            pwww.println(Defense);
            pwww.println(Catch);
            pwww.println(Technique);
            pwww.println(Stamina);
            pwww.println(MaxGP);
            if (GP < 0) {
                GP = 0;
            }
            pwww.println(GP);
            pwww.println(MaxTP);
            if (TP < 0) {
                TP = 0;
            }
            pwww.println(TP);
            pwww.println(Move1);
            pwww.println(Move2);
            pwww.println(Move3);
            pwww.println(Move4);
            pwww.println(Skill1);
            pwww.println(Skill2);
            pwww.println(Spirit);
            pwww.println(MixiSkill1);
            pwww.println(MixiSkill2);
        } catch (Exception ignored) {}
    }
    public void Initialize() {
        File gamefile = new File(MainDirectory + "/storage/rpg/" + dir + "/stats/" + id + ".txt");
        if (gamefile.exists()) {
            try (Scanner scangame = new Scanner(gamefile, StandardCharsets.UTF_8)) {

                Name = scangame.nextLine();
                Element = scangame.nextLine();
                Position = scangame.nextLine();
                Gender = scangame.nextLine();
                Kick = scangame.nextLine();
                Dribble = scangame.nextLine();
                Defense = scangame.nextLine();
                Catch = scangame.nextLine();
                Technique = scangame.nextLine();
                Stamina = scangame.nextLine();
                MaxGP = Integer.parseInt(scangame.nextLine());
                GP = Integer.parseInt(scangame.nextLine());
                MaxTP = Integer.parseInt(scangame.nextLine());
                TP = Integer.parseInt(scangame.nextLine());
                Move1 = scangame.nextLine();
                Move2 = scangame.nextLine();
                Move3 = scangame.nextLine();
                Move4 = scangame.nextLine();
                Skill1 = scangame.nextLine();
                Skill2 = scangame.nextLine();
                Spirit = scangame.nextLine();
                MixiSkill1 = scangame.nextLine();
                MixiSkill2 = scangame.nextLine();
            } catch (IOException e) {
                Automation.handleException(e);
            }
        }
    }

    public RPG_PlayerStat(String dir, String id) {
        this.dir = dir;
        this.id = id;
        Initialize();
    }
}
