package iecompbot.objects;

import iecompbot.interaction.Automation;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static iecompbot.Main.*;

public class CommunityServer {
    private String lang = "";

    public List<Server> Servers = new ArrayList<>();

    public void Initialize() {
        try (Scanner scan = new Scanner(new File(MainDirectory + "/storage/community/" + lang + ".txt"), StandardCharsets.UTF_8)) {
            while (scan.hasNextLine()) {
                String[] line = scan.nextLine().split(" --- ");
                Servers.add(new Server(line[0], line[1], line[2], line[3]));
            }
        } catch (IOException e) {
            Automation.handleException(e);
        }
    }


    public CommunityServer(String lang) {
        this.lang = lang;
        Initialize();
    }

    public class Server {
        public String flag = "";
        public String id = "";
        public String name = "";
        public String link = "";

        public Server(String f, String i, String n, String l) {
            this.flag = f;
            this.id = i;
            this.name = n;
            this.link = l;
        }
    }
}
