package iecompbot.springboot.controller.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "Clans", description = "Endpoints to access community clans information.")
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class ClanRESTController {

    @Operation(summary = "Get a list of a clan IDs")
    @GetMapping("/clans/ids.json")
    @Cacheable(value = "apiclanids", key = "null")
    public List<Long> getClanIDs() {
        return iecompbot.objects.clan.Clan.listOpenPaused().stream().map(iecompbot.objects.clan.Clan::getID).collect(Collectors.toList());
    }

    @Operation(summary = "Get a clan by ID")
    @GetMapping("/clan/{id}.json")
    @Cacheable(value = "apiclan", key = "#id")
    public Clan getClanById(@PathVariable long id) {
        return new Clan(iecompbot.objects.clan.Clan.get(id));
    }

    @Operation(summary = "Get a list of a clan's members")
    @GetMapping("/clan/{id}/members.json")
    @Cacheable(value = "apiclanmembers", key = "#id")
    public List<ClanMember> getClanMembersOfClan(@PathVariable long id) {
        return iecompbot.objects.clan.ClanMember.ofClan(id).stream().map(ClanMember::new).collect(Collectors.toList());
    }

    @Operation(summary = "Get a list of all active clans")
    @GetMapping("/clans/active.json")
    @Cacheable(value = "apiclans", key = "null")
    public List<Clan> getClansOpenPaused() {
        return iecompbot.objects.clan.Clan.listOpenPaused().stream().map(Clan::new).collect(Collectors.toList());
    }


    public static class ClanMember {
        public long ClanID;
        public long UserID;
        public String Number;
        public String Nickname;
        public String CardPNGURL;
        public String CardGIFURL;
        public boolean isMainClan;

        public ClanMember(iecompbot.objects.clan.ClanMember CM) {
            this.ClanID = CM.getClanID();
            this.UserID = CM.getUserID();
            this.Number = CM.getNumber();
            this.Nickname = CM.getUser().getEffectiveName() + " (@" + CM.getUser().getName() + ")";
            this.CardPNGURL = CM.getCardPNGURL();
            this.CardGIFURL = CM.getCardGIFURL();
            this.isMainClan = CM.isMainClan();
        }
    }

    public static class Clan {
        public Long ID;
        public String Name;
        public String Tag;
        public String Colorcode;
        public String NationalityName;
        public String Status;
        public String Description;
        public String Requirements;
        public String History;

        public String WebsiteURL;
        public String TwitterURL;
        public String TwitchURL;
        public String YouTubeURL;
        public String InstagramURL;
        public String DiscordURL;
        public String TiktokURL;

        public int MemberCount;

        public Long ClanCaptainUserID;
        public String ClanCaptain;

        public Clan(iecompbot.objects.clan.Clan clan) {
            this.ID = clan.getID();
            this.Name = clan.getName();
            this.Tag = clan.getTag();
            this.Colorcode = clan.getColorcode();
            this.NationalityName = clan.getNationalityName();
            this.Status = clan.getStatus();
            this.Description = clan.getDescription();
            this.Requirements = clan.getRequirements();
            this.History = clan.getHistory();

            this.MemberCount = clan.getMemberCount();
            this.ClanCaptainUserID = clan.getCaptain().UserID;
            this.ClanCaptain = clan.getCaptain().getUser().getEffectiveName();
            this.WebsiteURL = clan.getWebsiteURL();
            this.TwitterURL = clan.getTwitterURL();
            this.TwitchURL = clan.getTwitchURL();
            this.YouTubeURL = clan.getYouTubeURL();
            this.InstagramURL = clan.getInstagramURL();
            this.DiscordURL = clan.getDiscordURL();
            this.TiktokURL = clan.getTiktokURL();
        }

    }
}
