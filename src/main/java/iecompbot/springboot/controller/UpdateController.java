package iecompbot.springboot.controller;

import iecompbot.objects.clan.Clan;
import iecompbot.objects.profile.Profile;
import iecompbot.objects.server.ServerInfo;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import static iecompbot.objects.BotManagers.*;

@RestController
@CrossOrigin(origins = "*")
public class UpdateController {

    @PostMapping("/p/update")
    public String updateProfile(@AuthenticationPrincipal OAuth2User principal, Profile profile) {
        Object ID = principal.getAttribute("id");
        if (ID != null && Long.parseLong(ID.toString()) == profile.getID() || isBotOwner(Long.parseLong(ID.toString()))) {
            profile.UpdateOnly("Signature", "History", "ColorCode", "NationalityName",
                    "WebsiteURL", "DiscordURL", "TwitchURL", "YouTubeURL", "TiktokURL", "InstagramURL",
                    "hasMatchmakingNotification", "hasTournamentNotification", "hasGIF",
                    "StrikersFriendcode", "XtremeFriendcode", "SwitchFriendcode");
            return "redirect:/p/" + profile.getUser().getName() + "?success";
        }
        return "redirect:/p/" + profile.getUser().getName() + "?noperm";
    }
    @PostMapping("/c/update")
    public String updateClan(@AuthenticationPrincipal OAuth2User principal, Clan clan) {
        Object ID = principal.getAttribute("id");
        if (ID != null && Long.parseLong(ID.toString()) == clan.getCaptain().getID() || isClanManager(Long.parseLong(ID.toString()))) {
            clan.UpdateOnly("Name", "Tag", "Description", "Requirements", "History",
                    "WebsiteURL", "DiscordURL", "TwitchURL", "YouTubeURL", "TiktokURL", "InstagramURL");
            return "redirect:/c/" + clan.getID() + "?success";
        }
        return "redirect:/c/" + clan.getID() + "?noperm";
    }
    @PostMapping("/s/update")
    public String updateServer(@AuthenticationPrincipal OAuth2User principal, ServerInfo server) {
        Object ID = principal.getAttribute("id");
        if (ID != null && Long.parseLong(ID.toString()) == server.getOwner().getIdLong() || isBotOwner(Long.parseLong(ID.toString()))) {
            server.UpdateOnly("History",
                    "PermanentInviteLink", "WebsiteURL", "TwitchURL", "YouTubeURL", "TiktokURL", "InstagramURL",
                    "areClanRolesAllowed", "areClanTagsAllowed", "areWinnerRolesAllowed", "AdminAcceptOnly",
                    "isVRBetaBoardAllowed", "isVROHBoardAllowed", "isGalaxyBoardAllowed", "isCSBoardAllowed", "isGO1BoardAllowed", "isSDBoardAllowed", "isIE3BoardAllowed", "isIE2BoardAllowed", "isIE1BoardAllowed", "isStrikersBoardAllowed", "isXtremeBoardAllowed",
                    "areGlobalRankAllowed", "areVRBetaRanksAllowed", "areVROHRanksAllowed", "areGalaxyRanksAllowed", "areCSRanksAllowed", "areGO1RanksAllowed", "areSDRanksAllowed", "areIE3RanksAllowed", "areIE2RanksAllowed", "areIE1RanksAllowed", "areStrikersRanksAllowed", "areXtremeRanksAllowed");
            return "redirect:/s/" + server.getID() + "?success";
        }
        return "redirect:/s/" + server.getID() + "?noperm";
    }
}
