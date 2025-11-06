package iecompbot.objects.rpg;

import net.dv8tion.jda.api.entities.Message;

public class RPGField {
    public String channelid;
    public String channelname;
    public String channelmention;
    public String messageid;


    public RPGField(String s1, String s2, String s3, String s4) {
        channelid = s1;
        channelname = s2;
        channelmention = s3;
        messageid = s4;
    }

}
