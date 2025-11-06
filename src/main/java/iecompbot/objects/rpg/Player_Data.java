package iecompbot.objects.rpg;

import iecompbot.Main;
import iecompbot.interaction.Automation;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class Player_Data {
    String name;

    public String Team;
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
    public int MaxTP;
    public String Move1;
    public String Move2;
    public String Move3;
    public String Move4;
    public String Skill1;
    public String Skill2;
    public String Spirit;
    public int Cost;


    public void Initialize() {
        URL url = Main.class.getResource("/rpg/players/name/");
        String path = url.getPath().replaceAll("%20", " ");
        File dir = new File(path);
        for (File teams : dir.listFiles()) {
            for (File player : teams.listFiles()) {
                if (player.getName().contains(name)) {
                    try (Scanner scangame = new Scanner(player, StandardCharsets.UTF_8)) {
                        Team = teams.getName();
                        Element = scangame.nextLine();
                        Position = scangame.nextLine();
                        Gender = scangame.nextLine();
                        Kick = scangame.nextLine();
                        Dribble = scangame.nextLine();
                        Defense = scangame.nextLine();
                        Catch = scangame.nextLine();
                        Technique = scangame.nextLine();
                        Stamina = scangame.nextLine();
                        MaxGP = Integer.parseInt(scangame.nextLine().substring(4));
                        MaxTP = Integer.parseInt(scangame.nextLine().substring(4));
                        Move1 = scangame.nextLine().substring(3);
                        Move2 = scangame.nextLine().substring(3);
                        Move3 = scangame.nextLine().substring(3);
                        Move4 = scangame.nextLine().substring(3);
                        Skill1 = scangame.nextLine().substring(3);
                        Skill2 = scangame.nextLine().substring(3);
                        Spirit = scangame.nextLine().substring(4);
                        Cost = scangame.nextInt();
                    } catch (IOException e) {
                        Automation.handleException(e);
                    }
                }
            }
        }
    }

    public Player_Data(String name) {
        this.name = name;
        Initialize();
    }
}