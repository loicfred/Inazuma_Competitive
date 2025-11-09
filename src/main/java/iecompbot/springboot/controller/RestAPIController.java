package iecompbot.springboot.controller;

import iecompbot.objects.clan.Clan;
import iecompbot.objects.clan.ClanMember;
import iecompbot.objects.profile.Profile;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class RestAPIController {

    @GetMapping("/clan/{id}.json")
    public Clan getClanById(@PathVariable long id) {
        return Clan.get(id);
    }
    @GetMapping("/clan/{id}/members.json")
    public List<ClanMember> getClanMembersOfClan(@PathVariable long id) {
        List<ClanMember> CM = ClanMember.ofClan(id);
        for (ClanMember C : CM) C.Nickname = C.getUser().getEffectiveName() + " (@" + C.getUser().getName() + ")";
        return CM;
    }
    @GetMapping("/profile/{id}.json")
    public Profile getClanMembersById(@PathVariable long id) {
        return Profile.get(id);
    }

    @GetMapping("/clans/ids.json")
    public List<Long> getClanIDs() {
        return Clan.listOpenPaused().stream().map(Clan::getID).collect(Collectors.toList());
    }

    @GetMapping("/clans/active.json")
    public List<Clan> getClansOpenPaused() {
        return Clan.listOpenPaused();
    }

}
