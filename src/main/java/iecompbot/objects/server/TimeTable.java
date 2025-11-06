package iecompbot.objects.server;

import iecompbot.interaction.Automation;
import retrofit2.http.PUT;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static iecompbot.Main.MainDirectory;

public class TimeTable {

    public String ID;
    public List<Event> Events = new ArrayList<>();

    public TimeTable(String ID) {
        this.ID = ID;
        Initialize();
    }

    public void Initialize() {
        File f = new File(MainDirectory + "/server/" + ID + "/timetable.txt");
        if (f.exists()) {
            try (Scanner scan = new Scanner(f, StandardCharsets.UTF_8)) {
                while (scan.hasNextLine()) {
                    String line = scan.nextLine();
                    Events.add(new Event(ID, line.split(" --- ")[0], line.split(" --- ")[1], line.split(" --- ")[2] ,line.split(" --- ")[3]));
                }
            } catch (IOException e) {
                Automation.handleException(e);
            }
        }
    }
    public void Save() {
        File f = new File(MainDirectory + "/server/" + ID + "/timetable.txt");
        try (FileWriter fw = new FileWriter(f, StandardCharsets.UTF_8);
             PrintWriter pw = new PrintWriter(fw)) {
            String s = "";
            for (Event E : Events) {
                s = s + "\n" + E.Name + " --- " + E.Game + " --- " + E.Start + " --- " + E.End;
            }
            pw.print(s.replaceFirst("\n", ""));
        } catch (IOException e) {
            Automation.handleException(e);
        }
    }







    public class Event {
        public String GuildID;
        public String Name;
        public String Game;

        public String Start;
        public String End;

        public Event(String guildid, String name, String game, String start, String end) {
            GuildID = guildid;
            Name = name;
            Game = game;
            Start = start;
            End = end;
        }


    }

}