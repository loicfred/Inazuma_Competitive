package iecompbot.objects.rpg.run;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import static iecompbot.Main.MainDirectory;

public class RPG_Dual {

    public String dir;
    public String InitiatorCaptainID;
    public String OpponentCaptainID;
    public String InitiatorID;
    public String InitiatorPOS;
    public String OpponentID;
    public String OpponentPOS;
    public String InitiatorMove;
    public String OpponentMove;
    public String InitiatorAction;
    public String OpponentAction;

    public void Save() throws IOException {
        File gamefile = new File(MainDirectory + "/storage/rpg/" + dir + "/dual.txt");
        try (FileWriter fwww = new FileWriter(gamefile, StandardCharsets.UTF_8);
             PrintWriter pwww = new PrintWriter(fwww)) {
            pwww.println(InitiatorCaptainID);
            pwww.println(OpponentCaptainID);
            pwww.println(InitiatorID);
            pwww.println(InitiatorPOS);
            pwww.println(OpponentID);
            pwww.println(OpponentPOS);
            pwww.println(InitiatorMove);
            pwww.println(OpponentMove);
            pwww.println(InitiatorAction);
            pwww.println(OpponentAction);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    public void Initialize() throws IOException {
        File dual = new File(MainDirectory + "/storage/rpg/" + dir + "/dual.txt");
        try (Scanner scandual = new Scanner(dual, StandardCharsets.UTF_8)) {
            InitiatorCaptainID = scandual.nextLine();
            OpponentCaptainID = scandual.nextLine();
            InitiatorID = scandual.nextLine();
            InitiatorPOS = scandual.nextLine();
            OpponentID = scandual.nextLine();
            OpponentPOS = scandual.nextLine();
            InitiatorMove = scandual.nextLine();
            OpponentMove = scandual.nextLine();
            InitiatorAction = scandual.nextLine();
            OpponentAction = scandual.nextLine();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void delete() throws IOException {
        File gamefile = new File(MainDirectory + "/storage/rpg/" + dir + "/dual.txt");
        gamefile.delete();
    }
    public RPG_Dual(String dir) {
        this.dir = dir;
    }

}
