package iecompbot.springboot.controller;

import iecompbot.objects.match.Game;
import iecompbot.objects.Nationality;
import iecompbot.objects.clan.Clan;
import iecompbot.objects.clan.ClanMember;
import iecompbot.objects.clan.ClanRole;
import iecompbot.objects.clan.items.ClanPermission;
import iecompbot.objects.profile.Profile;
import iecompbot.objects.server.ServerInfo;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static iecompbot.Main.Prefs;
import static iecompbot.img.ImgUtilities.getHexValue;
import static iecompbot.interaction.Automation.LogSlash;
import static iecompbot.objects.BotManagers.isBotOwner;
import static iecompbot.objects.BotManagers.isTournamentManager;
import static my.utilities.json.JSONItem.GSON;
import static my.utilities.util.Utilities.isNumeric;

@RestController
@CrossOrigin(origins = "*")
public class UpdateController {

    @GetMapping("/p/{id}/verify")
    public ResponseEntity<Void> verifyAccount(@RequestHeader("Authorization") String authorization, @PathVariable String id) throws IOException {
        try {
            Profile P = Profile.get(Long.parseLong(id));
            DiscordUser Editor = getDiscordUserByAuthorization(authorization);
            if (!Prefs.TestMode) LogSlash("**[Website][Profile]:** `" + Editor.global_name + "`: Visited the profile of **" + P.getUser().getEffectiveName() + "**.");
            if (Long.parseLong(Editor.id) == P.getId() || isBotOwner(P.getId())) {
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
        } catch (Exception ignored) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PostMapping("/p/update")
    public String update(@AuthenticationPrincipal OAuth2User principal, Profile profile) throws IOException {
        Object ID = principal.getAttribute("id");
        if (ID != null && Long.parseLong(ID.toString()) == profile.getId()) {
            profile.UpdateOnly("Signature", "ColorCode", "BirthdayEpochSecond", "NationalityName");
            return "redirect:/p/" + profile.getUser().getName() + "?success";
        }
        return "redirect:/p/" + profile.getUser().getName() + "?noperm";
    }
//    @PutMapping("/p/{id}/updatee")
//    public String verifyAccount(@RequestHeader("Authorization") String authorization, @PathVariable String id, @RequestBody Map<String, Object> payload) throws IOException {
//        try {
//            Profile P = Profile.get(Long.parseLong(id));
//            DiscordUser Editor = getDiscordUserByAuthorization(authorization);
//            if (Long.parseLong(Editor.id) != P.getId()) {
//                LogSlash("**[Website][Profile]:** `" + Editor.global_name + "`: Attempted to modify the profile of **" + P.getUser().getEffectiveName() + "**.");
//                return "You don't have the permission to modify this profile.";
//            }
//            String log = "";
//            int i = 0;
//            if (payload.get("nationality") != null) {
//                Nationality newNationality = Nationality.get(payload.get("nationality").toString());
//                if (!Objects.equals(P.getNationality(), newNationality)) {
//                    log = log + "> Nationality: `" + P.getNationality().getName() + "` -> `" + newNationality.getName() + "`\n";
//                    P.setNationality(newNationality);
//                    i++;
//                }
//            }
//
//            if (payload.get("colorCode") != null) {
//                String newColorCode = payload.get("colorCode").toString();
//                if (!newColorCode.equalsIgnoreCase(getHexValue(P.getColor()))) {
//                    log = log + "> Color: `" + getHexValue(P.getColor()) + "` -> `" + newColorCode + "`\n";
//                    P.setColor(Color.decode(newColorCode));
//                    i++;
//                }
//            }
//
//            if (payload.get("signature") != null) {
//                String newSignature = payload.get("signature").toString();
//                if (!newSignature.isEmpty() && !Objects.equals(P.getSignature(), newSignature)) {
//                    log = log + "> Signature: `" + P.getSignature() + "` -> `" + newSignature + "`\n";
//                    P.setSignature(newSignature);
//                    i++;
//                }
//            }
//
//            if (payload.get("history") != null) {
//                String newHistory = payload.get("history").toString();
//                if (!newHistory.isEmpty() && !Objects.equals(P.getHistory(), newHistory)) {
//                    log = log + "> History: `" + P.getHistory() + "` -> `" + newHistory + "`\n";
//                    P.setHistory(newHistory);
//                    i++;
//                }
//            }
//
//            if (payload.get("birthdayEpochSecond") != null) {
//                long newBirthday = Long.parseLong(payload.get("birthdayEpochSecond").toString());
//                if (P.getBirthday() == null || P.getBirthday().getEpochSecond() != newBirthday) {
//                    log = log + "> Birthday: `" + P.getBirthday() + "` -> <t:" + newBirthday + ":R>\n";
//                    P.setBirthday(newBirthday);
//                    i++;
//                }
//            }
//
//            if (payload.get("strikersFriendcode") != null) {
//                String newStrikersCode = payload.get("strikersFriendcode").toString();
//                if (!newStrikersCode.isEmpty() && !Objects.equals(P.getStrikersFriendcode(), newStrikersCode)) {
//                    log = log + "> Strikers Code: `" + P.getStrikersFriendcode() + "` -> `" + newStrikersCode + "`\n";
//                    P.setStrikersFriendcode(newStrikersCode);
//                    i++;
//                }
//            }
//
//            if (payload.get("xtremeFriendcode") != null) {
//                String newXtremeCode = payload.get("xtremeFriendcode").toString();
//                if (!newXtremeCode.isEmpty() && !Objects.equals(P.getXtremeFriendcode(), newXtremeCode)) {
//                    log = log + "> Xtreme Code: `" + P.getXtremeFriendcode() + "` -> `" + newXtremeCode + "`\n";
//                    P.setXtremeFriendcode(newXtremeCode);
//                    i++;
//                }
//            }
//
//            if (payload.get("switchFriendcode") != null) {
//                String newValue = payload.get("switchFriendcode").toString();
//                if (!newValue.isEmpty() && !Objects.equals(P.getSwitchFriendcode(), newValue)) {
//                    log = log + "> Switch Code: `" + P.getSwitchFriendcode() + "` -> `" + newValue + "`\n";
//                    P.setSwitchFriendcode(newValue);
//                    i++;
//                }
//            }
//
//            if (payload.get("hasPrivateProfile") != null) {
//                boolean newValue = Boolean.parseBoolean(payload.get("hasPrivateProfile").toString());
//                if (P.hasPrivateProfile() != newValue) {
//                    log = log + "> hasPrivateProfile: `" + P.hasPrivateProfile() + "` -> `" + newValue + "`\n";
//                    P.setPrivateProfile(newValue);
//                    i++;
//                }
//            }
//
//            if (payload.get("hasTournamentNotification") != null) {
//                boolean newValue = Boolean.parseBoolean(payload.get("hasTournamentNotification").toString());
//                if (P.hasTournamentNotification() != newValue) {
//                    log = log + "> hasTournamentNotification: `" + P.hasTournamentNotification() + "` -> `" + newValue + "`\n";
//                    P.setTournamentNotification(newValue);
//                    i++;
//                }
//            }
//
//            if (payload.get("hasMatchmakingNotification") != null) {
//                boolean newValue = Boolean.parseBoolean(payload.get("hasMatchmakingNotification").toString());
//                if (P.hasMatchmakingNotification() != newValue) {
//                    log = log + "> hasMatchmakingNotification: `" + P.hasMatchmakingNotification() + "` -> `" + newValue + "`\n";
//                    P.setMatchmakingNotification(newValue);
//                    i++;
//                }
//            }
//
//            if (payload.get("hasGIF") != null) {
//                boolean newValue = Boolean.parseBoolean(payload.get("hasGIF").toString());
//                if (P.hasGIF() != newValue) {
//                    log = log + "> hasGIF: `" + P.hasGIF() + "` -> `" + newValue + "`\n";
//                    P.setGIF(newValue);
//                    i++;
//                }
//            }
//            if (i == 0)  return "No changes mades.";
//            LogSlash("**[Website][Profile]:** `" + Editor.global_name + "`: Edited the profile of **" + P.getUser().getEffectiveName() + "** with **" + i + "** new values:\n" + log);
//            P.Update();
//            return "Successfully updated " + i + " values for your profile!";
//        } catch (Exception ignored) {
//            return "An error occurred.";
//        }
//    }


    @GetMapping("/c/{id}/verify")
    public ResponseEntity<Void> verifyCaptain(@RequestHeader("Authorization") String authorization, @PathVariable String id) throws IOException {
        try {
            Clan C = Clan.get(Long.parseLong(id));
            DiscordUser Editor = getDiscordUserByAuthorization(authorization);
            if (!Prefs.TestMode) LogSlash("**[Website][Clan]:** `" + Editor.global_name + "`: Visited the clan **" + C.getName() + "**.");
            if (!C.getMemberById(Long.parseLong(Editor.id)).getPermissions().isEmpty()) {
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
        } catch (Exception ignored) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
    @PutMapping("/c/{id}/update")
    public String verifyCaptain(@RequestHeader("Authorization") String authorization, @PathVariable String id, @RequestBody Map<String, Object> payload) throws IOException {
        try {
            Clan C = Clan.get(Long.parseLong(id));
            if (C == null) return "Clan not found.";
            DiscordUser Editor = getDiscordUserByAuthorization(authorization);
            String log = "";
            int i = 0;
            ClanMember CM = C.getMemberById(Long.parseLong(Editor.id));
            if (CM.getPermissions().isEmpty()) {
                LogSlash("**[Website][Clan]:** `" + Editor.global_name + "`: Attempted to modify the clan **" + C.getName() + "**.");
                return "You don't have the permission to modify this clan.";
            }

            if (CM.hasPermission(ClanPermission.ADMINISTRATOR) && payload.get("name") != null) {
                String newValue = payload.get("name").toString();
                if (!newValue.equalsIgnoreCase(C.getName())) {
                    log = log + "> Name: `" + C.getName() + "` -> `" + newValue + "`\n";
                    C.setName(C.getName(), newValue, null);
                    i++;
                }
            }

            if (CM.hasPermission(ClanPermission.ADMINISTRATOR) && payload.get("tag") != null) {
                String newValue = payload.get("tag").toString();
                if (!newValue.equalsIgnoreCase(C.getTag())) {
                    log = log + "> Tag: `" + C.getTag() + "` -> `" + newValue + "`\n";
                    C.setTag(C.getTag(), newValue, null);
                    i++;
                }
            }

            if (CM.hasPermission(ClanPermission.MANAGE_INFORMATION) && payload.get("nationality") != null) {
                Nationality newNationality = Nationality.get(payload.get("nationality").toString());
                if (!Objects.equals(C.getNationality(), newNationality)) {
                    log = log + "> Nationality: `" + C.getNationality().getName() + "` -> `" + newNationality.getName() + "`\n";
                    C.setNationality(newNationality);
                    i++;
                }
            }

            if (CM.hasPermission(ClanPermission.MANAGE_INFORMATION) && payload.get("colorCode") != null) {
                String newColorCode = payload.get("colorCode").toString();
                if (!newColorCode.equalsIgnoreCase(getHexValue(C.getColor()))) {
                    log = log + "> Color: `" + getHexValue(C.getColor()) + "` -> `" + newColorCode + "`\n";
                    C.setColor(Color.decode(newColorCode));
                    i++;
                }
            }

            if (CM.hasPermission(ClanPermission.MANAGE_INFORMATION) && payload.get("description") != null) {
                String newDescription = payload.get("description").toString();
                if (!newDescription.isEmpty() && !Objects.equals(C.getDescription(), newDescription)) {
                    log = log + "> Description: `" + C.getDescription() + "` -> `" + newDescription + "`\n";
                    C.setDescription(newDescription);
                    i++;
                }
            }

            if (CM.hasPermission(ClanPermission.MANAGE_INFORMATION) && payload.get("requirements") != null) {
                String newRequirements = payload.get("requirements").toString();
                if (!newRequirements.isEmpty() && !Objects.equals(C.getRequirements(), newRequirements)) {
                    log = log + "> Requirements: `" + C.getRequirements() + "` -> `" + newRequirements + "`\n";
                    C.setRequirements(newRequirements);
                    i++;
                }
            }

            if (CM.hasPermission(ClanPermission.MANAGE_INFORMATION) && payload.get("history") != null) {
                String newHistory = payload.get("history").toString();
                if (!newHistory.isEmpty() && !Objects.equals(C.getHistory(), newHistory)) {
                    log = log + "> History: `" + C.getHistory() + "` -> `" + newHistory + "`\n";
                    C.setHistory(newHistory);
                    i++;
                }
            }




            if (CM.hasPermission(ClanPermission.MANAGE_INFORMATION) && payload.get("website") != null) {
                String newValue = payload.get("website").toString();
                if (!newValue.isEmpty() && !Objects.equals(C.getWebsiteURL(), newValue)) {
                    log = log + "> Website: `" + C.getWebsiteURL() + "` -> `" + newValue + "`\n";
                    C.setWebsiteURL(newValue);
                    i++;
                }
            }
            if (CM.hasPermission(ClanPermission.MANAGE_INFORMATION) && payload.get("youtube") != null) {
                String newValue = payload.get("youtube").toString();
                if (!newValue.isEmpty() && !Objects.equals(C.getYouTubeURL(), newValue)) {
                    log = log + "> YouTube: `" + C.getYouTubeURL() + "` -> `" + newValue + "`\n";
                    C.setYouTubeURL(newValue);
                    i++;
                }
            }
            if (CM.hasPermission(ClanPermission.MANAGE_INFORMATION) && payload.get("twitch") != null) {
                String newValue = payload.get("twitch").toString();
                if (!newValue.isEmpty() && !Objects.equals(C.getTwitchURL(), newValue)) {
                    log = log + "> Twitch: `" + C.getTwitchURL() + "` -> `" + newValue + "`\n";
                    C.setTwitchURL(newValue);
                    i++;
                }
            }
            if (CM.hasPermission(ClanPermission.MANAGE_INFORMATION) && payload.get("tiktok") != null) {
                String newValue = payload.get("tiktok").toString();
                if (!newValue.isEmpty() && !Objects.equals(C.getTiktokURL(), newValue)) {
                    log = log + "> Tiktok: `" + C.getTiktokURL() + "` -> `" + newValue + "`\n";
                    C.setTiktokURL(newValue);
                    i++;
                }
            }
            if (CM.hasPermission(ClanPermission.MANAGE_INFORMATION) && payload.get("instagram") != null) {
                String newValue = payload.get("instagram").toString();
                if (!newValue.isEmpty() && !Objects.equals(C.getInstagramURL(), newValue)) {
                    log = log + "> Instagram: `" + C.getInstagramURL() + "` -> `" + newValue + "`\n";
                    C.setInstagramURL(newValue);
                    i++;
                }
            }
            if (CM.hasPermission(ClanPermission.MANAGE_INFORMATION) && payload.get("twitter") != null) {
                String newValue = payload.get("twitter").toString();
                if (!newValue.isEmpty() && !Objects.equals(C.getTwitterURL(), newValue)) {
                    log = log + "> Twitter: `" + C.getTwitterURL() + "` -> `" + newValue + "`\n";
                    C.setTwitterURL(newValue);
                    i++;
                }
            }
            if (CM.hasPermission(ClanPermission.MANAGE_INFORMATION) && payload.get("discord") != null) {
                String newValue = payload.get("discord").toString();
                if (!newValue.isEmpty() && !Objects.equals(C.getDiscordURL(), newValue)) {
                    log = log + "> Discord: `" + C.getDiscordURL() + "` -> `" + newValue + "`\n";
                    C.setDiscordURL(newValue);
                    i++;
                }
            }
            if (i == 0) return "No changes mades.";
            LogSlash("**[Website][Clan]:** `" + Editor.global_name + "`: Edited **" + C.getName() + "** with **" + i + "** new values:\n" + log);
            C.Update();
            return "Successfully updated " + i + " values for your clan!";
        } catch (Exception ignored) {
            return "An error occurred.";
        }
    }
    @PutMapping("/c/{id}/member/{memberid}/update")
    public String updateClanMember(@RequestHeader("Authorization") String authorization, @PathVariable String id, @PathVariable String memberid, @RequestBody Map<String, Object> payload) {
        try {
            Clan C = Clan.get(Long.parseLong(id));
            if (C == null) return "Clan member not found.";
            DiscordUser Editor = getDiscordUserByAuthorization(authorization);
            String log = "";
            int i = 0;
            ClanMember Me = C.getMemberById(Long.parseLong(Editor.id));
            ClanMember Target = ClanMember.get(Long.parseLong(memberid));
            if (Me.getPermissions().isEmpty()) {
                LogSlash("**[Website][Clan]:** `" + Editor.global_name + "`: Attempted to modify the clan **" + C.getName() + "**.");
                return "You don't have the permission to modify this clan.";
            }

            if (Me.hasPermission(ClanPermission.MANAGE_MEMBER)) {
                if (payload.get("Number") != null) {
                    String newValue = payload.get("Number").toString();
                    if (isNumeric(newValue) && !newValue.equals(Target.getNumber())) {
                        log = log + "> Number: `" + C.getName() + "` -> `" + newValue + "`\n";
                        Target.setNumber(newValue);
                        i++;
                    }
                }
                for (ClanRole OldTask : new ArrayList<>(Target.getClanRoles())) {
                    if (OldTask.canGiveOrRemove(Me)) {
                        Target.removeRole(OldTask);
                    }
                }
                if (payload.get("Roles") instanceof List<?> roles) {
                    for (Object taskObj : roles) {
                        if (taskObj instanceof String task) {
                            ClanRole CR = ClanRole.get(Long.parseLong(task));
                            if (CR != null && CR.canGiveOrRemove(Me)) {
                                Target.addRole(CR);
                                i++;
                            }
                        }
                    }
                }
            }

            if (i == 0) return "No changes mades.";
            LogSlash("**[Website][Clan]:** `" + Editor.global_name + "`: Edited **" + C.getName() + "** with **" + i + "** new values:\n" + log);
            C.Update();
            return "Successfully updated " + i + " values for your clan!";
        } catch (Exception ignored) {
            return "An error occurred.";
        }
    }
    @PutMapping("/c/{id}/roles/check")
    public String getInteractableRoles(@RequestHeader("Authorization") String authorization, @PathVariable String id) {
        try {
            Clan C = Clan.get(Long.parseLong(id));
            DiscordUser Editor = getDiscordUserByAuthorization(authorization);
            ClanMember CM = C.getMemberById(Long.parseLong(Editor.id));
            return C.getClanRoles().stream().filter(CR -> CR.canGiveOrRemove(CM)).map(ClanRole::getName).collect(Collectors.joining(","));
        } catch (Exception ignored) {
            return "";
        }
    }


    @GetMapping("/s/{id}/verify")
    public ResponseEntity<Void> verifyAdmin(@RequestHeader("Authorization") String authorization, @PathVariable String id) throws IOException {
        try {
            DiscordUser Editor = getDiscordUserByAuthorization(authorization);
            ServerInfo I = ServerInfo.get(id);
            if (!Prefs.TestMode) LogSlash("**[Website][Guild]:** `" + Editor.global_name + "`: Visited the server **" + I.getName() + "**.");
            if (I.getGuild() == null) return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            Member Me = I.getGuild().getMemberById(Editor.id);
            if (Me != null && Me.hasPermission(Permission.ADMINISTRATOR)) {
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
        } catch (Exception ignored) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
    @PutMapping("/s/{id}/update")
    public String verifyAdmin(@RequestHeader("Authorization") String authorization, @PathVariable String id, @RequestBody Map<String, Object> payload) throws IOException {
        try {
            DiscordUser Editor = getDiscordUserByAuthorization(authorization);
            ServerInfo I = ServerInfo.get(id);
            if (I.getGuild() == null) return "I am not on this server!";
            Member Me = I.getGuild().getMemberById(Editor.id);
            if (Me == null) {
                LogSlash("**[Website][Guild]:** `" + Editor.global_name + "`: Attempted to modify the guild of **" + I.getGuild().getName() + "**.");
                return "You are not on this server!";
            }
            if (!Me.hasPermission(Permission.ADMINISTRATOR)) {
                LogSlash("**[Website][Guild]:** `" + Editor.global_name + "`: Attempted to modify the guild of **" + I.getGuild().getName() + "**.");
                return "You don't have the permission to modify this guild.";
            }



            String log = "";
            int i = 0;

            if (payload.get("invitelink") != null) {
                String newValue = payload.get("invitelink").toString();
                if (!newValue.equalsIgnoreCase(I.PermanentInviteLink)) {
                    log = log + "> Invite Link: `" + I.PermanentInviteLink + "` -> `" + newValue + "`\n";
                    I.setPermanentInviteLink(newValue);
                    i++;
                }
            }

            if (payload.get("clanrole") != null) {
                boolean newValue = Boolean.parseBoolean(payload.get("clanrole").toString());
                if (I.areClanRolesAllowed != newValue) {
                    log += "> Clan Roles: `" + I.areClanRolesAllowed + "` -> `" + newValue + "`\n";
                    I.setClanRolesAllowed(newValue);
                    i++;
                }
            }
            if (payload.get("clantag") != null) {
                boolean newValue = Boolean.parseBoolean(payload.get("clantag").toString());
                if (I.areClanTagsAllowed != newValue) {
                    log += "> Clan Tag: `" + I.areClanTagsAllowed + "` -> `" + newValue + "`\n";
                    I.setClanTagsAllowed(newValue);
                    i++;
                }
            }
            if (payload.get("winneroles") != null) {
                boolean newValue = Boolean.parseBoolean(payload.get("winneroles").toString());
                if (I.areWinnerRolesAllowed != newValue) {
                    log += "> Winner Roles: `" + I.areWinnerRolesAllowed + "` -> `" + newValue + "`\n";
                    I.setWinnerRolesAllowed(newValue);
                    i++;
                }
            }
            if (payload.get("adminonlyscore") != null) {
                boolean newValue = Boolean.parseBoolean(payload.get("adminonlyscore").toString());
                if (I.AdminAcceptOnly != newValue) {
                    log += "> Admin only accept scores: `" + I.AdminAcceptOnly + "` -> `" + newValue + "`\n";
                    I.setAdminAcceptOnly(newValue);
                    i++;
                }
            }
            if (payload.get("history") != null) {
                String newHistory = payload.get("history").toString();
                if (!newHistory.isEmpty() && !Objects.equals(I.getHistory(), newHistory)) {
                    log = log + "> History: `" + I.getHistory() + "` -> `" + newHistory + "`\n";
                    I.setHistory(newHistory);
                    i++;
                }
            }

            if (payload.get("boardvrbeta") != null) {
                boolean newValue = Boolean.parseBoolean(payload.get("boardvrbeta").toString());
                if (I.isGameBoardAllowed(Game.get("IEVRBETA")) != newValue) {
                    log += "> boardvrbeta: `" + I.isGameBoardAllowed(Game.get("IEVRBETA")) + "` -> `" + newValue + "`\n";
                    I.setGameBoardAllowed(newValue, Game.get("IEVRBETA"));
                    i++;
                }
            }
            if (payload.get("boardvr") != null) {
                boolean newValue = Boolean.parseBoolean(payload.get("boardvr").toString());
                if (I.isGameBoardAllowed(Game.get("IEVR")) != newValue) {
                    log += "> boardvr: `" + I.isGameBoardAllowed(Game.get("IEVR")) + "` -> `" + newValue + "`\n";
                    I.setGameBoardAllowed(newValue, Game.get("IEVR"));
                    i++;
                }
            }
            if (payload.get("boardgalaxy") != null) {
                boolean newValue = Boolean.parseBoolean(payload.get("boardgalaxy").toString());
                if (I.isGameBoardAllowed(Game.get("IEGOGLX")) != newValue) {
                    log += "> boardgalaxy: `" + I.isGameBoardAllowed(Game.get("IEGOGLX")) + "` -> `" + newValue + "`\n";
                    I.setGameBoardAllowed(newValue, Game.get("IEGOGLX"));
                    i++;
                }
            }
            if (payload.get("boardcs") != null) {
                boolean newValue = Boolean.parseBoolean(payload.get("boardcs").toString());
                if (I.isGameBoardAllowed(Game.get("IEGOCS")) != newValue) {
                    log += "> boardcs: `" + I.isGameBoardAllowed(Game.get("IEGOCS")) + "` -> `" + newValue + "`\n";
                    I.setGameBoardAllowed(newValue, Game.get("IEGOCS"));
                    i++;
                }
            }
            if (payload.get("boardgo1") != null) {
                boolean newValue = Boolean.parseBoolean(payload.get("boardgo1").toString());
                if (I.isGameBoardAllowed(Game.get("IEGO1")) != newValue) {
                    log += "> boardgo1: `" + I.isGameBoardAllowed(Game.get("IEGO1")) + "` -> `" + newValue + "`\n";
                    I.setGameBoardAllowed(newValue, Game.get("IEGO1"));
                    i++;
                }
            }
            if (payload.get("boardsd") != null) {
                boolean newValue = Boolean.parseBoolean(payload.get("boardsd").toString());
                if (I.isGameBoardAllowed(Game.get("IESD")) != newValue) {
                    log += "> boardsd: `" + I.isGameBoardAllowed(Game.get("IESD")) + "` -> `" + newValue + "`\n";
                    I.setGameBoardAllowed(newValue, Game.get("IESD"));
                    i++;
                }
            }
            if (payload.get("boardie3") != null) {
                boolean newValue = Boolean.parseBoolean(payload.get("boardie3").toString());
                if (I.isGameBoardAllowed(Game.get("IE3")) != newValue) {
                    log += "> boardie3: `" + I.isGameBoardAllowed(Game.get("IE3")) + "` -> `" + newValue + "`\n";
                    I.setGameBoardAllowed(newValue, Game.get("IE3"));
                    i++;
                }
            }
            if (payload.get("boardie2") != null) {
                boolean newValue = Boolean.parseBoolean(payload.get("boardie2").toString());
                if (I.isGameBoardAllowed(Game.get("IE2")) != newValue) {
                    log += "> boardie2: `" + I.isGameBoardAllowed(Game.get("IE2")) + "` -> `" + newValue + "`\n";
                    I.setGameBoardAllowed(newValue, Game.get("IE2"));
                    i++;
                }
            }
            if (payload.get("boardie1") != null) {
                boolean newValue = Boolean.parseBoolean(payload.get("boardie1").toString());
                if (I.isGameBoardAllowed(Game.get("IE1")) != newValue) {
                    log += "> boardie1: `" + I.isGameBoardAllowed(Game.get("IE1")) + "` -> `" + newValue + "`\n";
                    I.setGameBoardAllowed(newValue, Game.get("IE1"));
                    i++;
                }
            }
            if (payload.get("boardstrikers") != null) {
                boolean newValue = Boolean.parseBoolean(payload.get("boardstrikers").toString());
                if (I.isGameBoardAllowed(Game.get("IEGOSTR")) != newValue) {
                    log += "> boardstrikers: `" + I.isGameBoardAllowed(Game.get("IEGOSTR")) + "` -> `" + newValue + "`\n";
                    I.setGameBoardAllowed(newValue, Game.get("IEGOSTR"));
                    i++;
                }
            }
            if (payload.get("boardxtreme") != null) {
                boolean newValue = Boolean.parseBoolean(payload.get("boardxtreme").toString());
                if (I.isGameBoardAllowed(Game.get("IEGOSTRXTR")) != newValue) {
                    log += "> boardxtreme: `" + I.isGameBoardAllowed(Game.get("IEGOSTRXTR")) + "` -> `" + newValue + "`\n";
                    I.setGameBoardAllowed(newValue, Game.get("IEGOSTRXTR"));
                    i++;
                }
            }

            if (payload.get("globalrank") != null) {
                boolean newValue = Boolean.parseBoolean(payload.get("globalrank").toString());
                if (I.areGlobalRankAllowed != newValue) {
                    log += "> Global Ranks: `" + I.areGlobalRankAllowed + "` -> `" + newValue + "`\n";
                    I.setGlobalRankAllowed(newValue);
                    i++;
                }
            }
            if (payload.get("rankvrbeta") != null) {
                boolean newValue = Boolean.parseBoolean(payload.get("rankvrbeta").toString());
                if (I.isGameRankAllowed(Game.get("IEVRBETA")) != newValue) {
                    log += "> rankvrbeta: `" + I.isGameRankAllowed(Game.get("IEVRBETA")) + "` -> `" + newValue + "`\n";
                    I.setGameRanksAllowed(newValue, Game.get("IEVRBETA"));
                    i++;
                }
            }
            if (payload.get("rankvr") != null) {
                boolean newValue = Boolean.parseBoolean(payload.get("rankvr").toString());
                if (I.isGameRankAllowed(Game.get("IEVR")) != newValue) {
                    log += "> rankvr: `" + I.isGameRankAllowed(Game.get("IEVR")) + "` -> `" + newValue + "`\n";
                    I.setGameRanksAllowed(newValue, Game.get("IEVR"));
                    i++;
                }
            }
            if (payload.get("rankgalaxy") != null) {
                boolean newValue = Boolean.parseBoolean(payload.get("rankgalaxy").toString());
                if (I.isGameRankAllowed(Game.get("IEGOGLX")) != newValue) {
                    log += "> rankgalaxy: `" + I.isGameRankAllowed(Game.get("IEGOGLX")) + "` -> `" + newValue + "`\n";
                    I.setGameRanksAllowed(newValue, Game.get("IEGOGLX"));
                    i++;
                }
            }
            if (payload.get("rankcs") != null) {
                boolean newValue = Boolean.parseBoolean(payload.get("rankcs").toString());
                if (I.isGameRankAllowed(Game.get("IEGOCS")) != newValue) {
                    log += "> rankcs: `" + I.isGameRankAllowed(Game.get("IEGOCS")) + "` -> `" + newValue + "`\n";
                    I.setGameRanksAllowed(newValue, Game.get("IEGOCS"));
                    i++;
                }
            }
            if (payload.get("rankgo1") != null) {
                boolean newValue = Boolean.parseBoolean(payload.get("rankgo1").toString());
                if (I.isGameRankAllowed(Game.get("IEGO1")) != newValue) {
                    log += "> rankgo1: `" + I.isGameRankAllowed(Game.get("IEGO1")) + "` -> `" + newValue + "`\n";
                    I.setGameRanksAllowed(newValue, Game.get("IEGO1"));
                    i++;
                }
            }
            if (payload.get("ranksd") != null) {
                boolean newValue = Boolean.parseBoolean(payload.get("ranksd").toString());
                if (I.isGameRankAllowed(Game.get("IESD")) != newValue) {
                    log += "> ranksd: `" + I.isGameRankAllowed(Game.get("IESD")) + "` -> `" + newValue + "`\n";
                    I.setGameRanksAllowed(newValue, Game.get("IESD"));
                    i++;
                }
            }
            if (payload.get("rankie3") != null) {
                boolean newValue = Boolean.parseBoolean(payload.get("rankie3").toString());
                if (I.isGameRankAllowed(Game.get("IE3")) != newValue) {
                    log += "> rankie3: `" + I.isGameRankAllowed(Game.get("IE3")) + "` -> `" + newValue + "`\n";
                    I.setGameRanksAllowed(newValue, Game.get("IE3"));
                    i++;
                }
            }
            if (payload.get("rankie2") != null) {
                boolean newValue = Boolean.parseBoolean(payload.get("rankie2").toString());
                if (I.isGameRankAllowed(Game.get("IE2")) != newValue) {
                    log += "> rankie2: `" + I.isGameRankAllowed(Game.get("IE2")) + "` -> `" + newValue + "`\n";
                    I.setGameRanksAllowed(newValue, Game.get("IE2"));
                    i++;
                }
            }
            if (payload.get("rankie1") != null) {
                boolean newValue = Boolean.parseBoolean(payload.get("rankie1").toString());
                if (I.isGameRankAllowed(Game.get("IE1")) != newValue) {
                    log += "> rankie1: `" + I.isGameRankAllowed(Game.get("IE1")) + "` -> `" + newValue + "`\n";
                    I.setGameRanksAllowed(newValue, Game.get("IE1"));
                    i++;
                }
            }
            if (payload.get("rankstrikers") != null) {
                boolean newValue = Boolean.parseBoolean(payload.get("rankstrikers").toString());
                if (I.isGameRankAllowed(Game.get("IEGOSTR")) != newValue) {
                    log += "> rankstrikers: `" + I.isGameRankAllowed(Game.get("IEGOSTR")) + "` -> `" + newValue + "`\n";
                    I.setGameRanksAllowed(newValue, Game.get("IEGOSTR"));
                    i++;
                }
            }
            if (payload.get("rankxtreme") != null) {
                boolean newValue = Boolean.parseBoolean(payload.get("rankxtreme").toString());
                if (I.isGameRankAllowed(Game.get("IEGOSTRXTR")) != newValue) {
                    log += "> rankxtreme: `" + I.isGameRankAllowed(Game.get("IEGOSTRXTR")) + "` -> `" + newValue + "`\n";
                    I.setGameBoardAllowed(newValue, Game.get("IEGOSTRXTR"));
                    i++;
                }
            }
            if (i == 0)  return "No changes mades.";
            LogSlash("**[Website][Guild]:** `" + Editor.global_name + "`: Edited the server of **" + I.getGuild().getName() + "** with **" + i + "** new values:\n" + log);
            I.Update();
            return "Successfully updated " + i + " values for your server!";
        } catch (Exception ignored) {
            return "An error occurred.";
        }
    }

    @GetMapping("/e/verify")
    public ResponseEntity<Void> verifyTO(@RequestHeader("Authorization") String authorization) throws IOException {
        try {
            DiscordUser Editor = getDiscordUserByAuthorization(authorization);
            if (!Prefs.TestMode) LogSlash("**[Website][Guild]:** `" + Editor.global_name + "`: Visited an event page.");
            if (!isTournamentManager(Long.parseLong(Editor.id))) return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            return ResponseEntity.ok().build();
        } catch (Exception ignored) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
    @PutMapping("/e/{id}/update")
    public String verifyTO(@RequestHeader("Authorization") String authorization, @PathVariable String id, @RequestBody Map<String, Object> payload) throws IOException {
        try {
            DiscordUser Editor = getDiscordUserByAuthorization(authorization);
            ServerInfo I = ServerInfo.get(id);
            if (I.getGuild() == null) return "I am not on this server!";
            Member Me = I.getGuild().getMemberById(Editor.id);
            if (Me == null) {
                LogSlash("**[Website][Guild]:** `" + Editor.global_name + "`: Attempted to modify the guild of **" + I.getGuild().getName() + "**.");
                return "You are not on this server!";
            }
            if (!Me.hasPermission(Permission.ADMINISTRATOR)) {
                LogSlash("**[Website][Guild]:** `" + Editor.global_name + "`: Attempted to modify the guild of **" + I.getGuild().getName() + "**.");
                return "You don't have the permission to modify this guild.";
            }



            String log = "";
            int i = 0;

            if (payload.get("invitelink") != null) {
                String newValue = payload.get("invitelink").toString();
                if (I.PermanentInviteLink == null || I.PermanentInviteLink != null && !newValue.equalsIgnoreCase(I.PermanentInviteLink)) {
                    log = log + "> Invite Link: `" + I.PermanentInviteLink + "` -> `" + newValue + "`\n";
                    I.setPermanentInviteLink(newValue);
                    i++;
                }
            }

            if (payload.get("clanrole") != null) {
                boolean newValue = Boolean.parseBoolean(payload.get("clanrole").toString());
                if (I.areClanRolesAllowed != newValue) {
                    log += "> Clan Roles: `" + I.areClanRolesAllowed + "` -> `" + newValue + "`\n";
                    I.setClanRolesAllowed(newValue);
                    i++;
                }
            }
            if (payload.get("clantag") != null) {
                boolean newValue = Boolean.parseBoolean(payload.get("clantag").toString());
                if (I.areClanTagsAllowed != newValue) {
                    log += "> Clan Tag: `" + I.areClanTagsAllowed + "` -> `" + newValue + "`\n";
                    I.setClanTagsAllowed(newValue);
                    i++;
                }
            }
            if (payload.get("winneroles") != null) {
                boolean newValue = Boolean.parseBoolean(payload.get("winneroles").toString());
                if (I.areWinnerRolesAllowed != newValue) {
                    log += "> Winner Roles: `" + I.areWinnerRolesAllowed + "` -> `" + newValue + "`\n";
                    I.setWinnerRolesAllowed(newValue);
                    i++;
                }
            }
            if (payload.get("adminonlyscore") != null) {
                boolean newValue = Boolean.parseBoolean(payload.get("adminonlyscore").toString());
                if (I.AdminAcceptOnly != newValue) {
                    log += "> Admin only accept scores: `" + I.AdminAcceptOnly + "` -> `" + newValue + "`\n";
                    I.setAdminAcceptOnly(newValue);
                    i++;
                }
            }
            if (payload.get("history") != null) {
                String newHistory = payload.get("history").toString();
                if (!newHistory.isEmpty() && !Objects.equals(I.getHistory(), newHistory)) {
                    log = log + "> History: `" + I.getHistory() + "` -> `" + newHistory + "`\n";
                    I.setHistory(newHistory);
                    i++;
                }
            }

            if (payload.get("boardvrbeta") != null) {
                boolean newValue = Boolean.parseBoolean(payload.get("boardvrbeta").toString());
                if (I.isGameBoardAllowed(Game.get("IEVRBETA")) != newValue) {
                    log += "> boardvrbeta: `" + I.isGameBoardAllowed(Game.get("IEVRBETA")) + "` -> `" + newValue + "`\n";
                    I.setGameBoardAllowed(newValue, Game.get("IEVRBETA"));
                    i++;
                }
            }
            if (payload.get("boardvr") != null) {
                boolean newValue = Boolean.parseBoolean(payload.get("boardvr").toString());
                if (I.isGameBoardAllowed(Game.get("IEVR")) != newValue) {
                    log += "> boardvr: `" + I.isGameBoardAllowed(Game.get("IEVR")) + "` -> `" + newValue + "`\n";
                    I.setGameBoardAllowed(newValue, Game.get("IEVR"));
                    i++;
                }
            }
            if (payload.get("boardgalaxy") != null) {
                boolean newValue = Boolean.parseBoolean(payload.get("boardgalaxy").toString());
                if (I.isGameBoardAllowed(Game.get("IEGOGLX")) != newValue) {
                    log += "> boardgalaxy: `" + I.isGameBoardAllowed(Game.get("IEGOGLX")) + "` -> `" + newValue + "`\n";
                    I.setGameBoardAllowed(newValue, Game.get("IEGOGLX"));
                    i++;
                }
            }
            if (payload.get("boardcs") != null) {
                boolean newValue = Boolean.parseBoolean(payload.get("boardcs").toString());
                if (I.isGameBoardAllowed(Game.get("IEGOCS")) != newValue) {
                    log += "> boardcs: `" + I.isGameBoardAllowed(Game.get("IEGOCS")) + "` -> `" + newValue + "`\n";
                    I.setGameBoardAllowed(newValue, Game.get("IEGOCS"));
                    i++;
                }
            }
            if (payload.get("boardgo1") != null) {
                boolean newValue = Boolean.parseBoolean(payload.get("boardgo1").toString());
                if (I.isGameBoardAllowed(Game.get("IEGO1")) != newValue) {
                    log += "> boardgo1: `" + I.isGameBoardAllowed(Game.get("IEGO1")) + "` -> `" + newValue + "`\n";
                    I.setGameBoardAllowed(newValue, Game.get("IEGO1"));
                    i++;
                }
            }
            if (payload.get("boardsd") != null) {
                boolean newValue = Boolean.parseBoolean(payload.get("boardsd").toString());
                if (I.isGameBoardAllowed(Game.get("IESD")) != newValue) {
                    log += "> boardsd: `" + I.isGameBoardAllowed(Game.get("IESD")) + "` -> `" + newValue + "`\n";
                    I.setGameBoardAllowed(newValue, Game.get("IESD"));
                    i++;
                }
            }
            if (payload.get("boardie3") != null) {
                boolean newValue = Boolean.parseBoolean(payload.get("boardie3").toString());
                if (I.isGameBoardAllowed(Game.get("IE3")) != newValue) {
                    log += "> boardie3: `" + I.isGameBoardAllowed(Game.get("IE3")) + "` -> `" + newValue + "`\n";
                    I.setGameBoardAllowed(newValue, Game.get("IE3"));
                    i++;
                }
            }
            if (payload.get("boardie2") != null) {
                boolean newValue = Boolean.parseBoolean(payload.get("boardie2").toString());
                if (I.isGameBoardAllowed(Game.get("IE2")) != newValue) {
                    log += "> boardie2: `" + I.isGameBoardAllowed(Game.get("IE2")) + "` -> `" + newValue + "`\n";
                    I.setGameBoardAllowed(newValue, Game.get("IE2"));
                    i++;
                }
            }
            if (payload.get("boardie1") != null) {
                boolean newValue = Boolean.parseBoolean(payload.get("boardie1").toString());
                if (I.isGameBoardAllowed(Game.get("IE1")) != newValue) {
                    log += "> boardie1: `" + I.isGameBoardAllowed(Game.get("IE1")) + "` -> `" + newValue + "`\n";
                    I.setGameBoardAllowed(newValue, Game.get("IE1"));
                    i++;
                }
            }
            if (payload.get("boardstrikers") != null) {
                boolean newValue = Boolean.parseBoolean(payload.get("boardstrikers").toString());
                if (I.isGameBoardAllowed(Game.get("IEGOSTR")) != newValue) {
                    log += "> boardstrikers: `" + I.isGameBoardAllowed(Game.get("IEGOSTR")) + "` -> `" + newValue + "`\n";
                    I.setGameBoardAllowed(newValue, Game.get("IEGOSTR"));
                    i++;
                }
            }
            if (payload.get("boardxtreme") != null) {
                boolean newValue = Boolean.parseBoolean(payload.get("boardxtreme").toString());
                if (I.isGameBoardAllowed(Game.get("IEGOSTRXTR")) != newValue) {
                    log += "> boardxtreme: `" + I.isGameBoardAllowed(Game.get("IEGOSTRXTR")) + "` -> `" + newValue + "`\n";
                    I.setGameBoardAllowed(newValue, Game.get("IEGOSTRXTR"));
                    i++;
                }
            }

            if (payload.get("globalrank") != null) {
                boolean newValue = Boolean.parseBoolean(payload.get("globalrank").toString());
                if (I.areGlobalRankAllowed != newValue) {
                    log += "> Global Ranks: `" + I.areGlobalRankAllowed + "` -> `" + newValue + "`\n";
                    I.setGlobalRankAllowed(newValue);
                    i++;
                }
            }
            if (payload.get("rankvrbeta") != null) {
                boolean newValue = Boolean.parseBoolean(payload.get("rankvrbeta").toString());
                if (I.isGameRankAllowed(Game.get("IEVRBETA")) != newValue) {
                    log += "> rankvrbeta: `" + I.isGameRankAllowed(Game.get("IEVRBETA")) + "` -> `" + newValue + "`\n";
                    I.setGameRanksAllowed(newValue, Game.get("IEVRBETA"));
                    i++;
                }
            }
            if (payload.get("rankvr") != null) {
                boolean newValue = Boolean.parseBoolean(payload.get("rankvr").toString());
                if (I.isGameRankAllowed(Game.get("IEVR")) != newValue) {
                    log += "> rankvr: `" + I.isGameRankAllowed(Game.get("IEVR")) + "` -> `" + newValue + "`\n";
                    I.setGameRanksAllowed(newValue, Game.get("IEVR"));
                    i++;
                }
            }
            if (payload.get("rankgalaxy") != null) {
                boolean newValue = Boolean.parseBoolean(payload.get("rankgalaxy").toString());
                if (I.isGameRankAllowed(Game.get("IEGOGLX")) != newValue) {
                    log += "> rankgalaxy: `" + I.isGameRankAllowed(Game.get("IEGOGLX")) + "` -> `" + newValue + "`\n";
                    I.setGameRanksAllowed(newValue, Game.get("IEGOGLX"));
                    i++;
                }
            }
            if (payload.get("rankcs") != null) {
                boolean newValue = Boolean.parseBoolean(payload.get("rankcs").toString());
                if (I.isGameRankAllowed(Game.get("IEGOCS")) != newValue) {
                    log += "> rankcs: `" + I.isGameRankAllowed(Game.get("IEGOCS")) + "` -> `" + newValue + "`\n";
                    I.setGameRanksAllowed(newValue, Game.get("IEGOCS"));
                    i++;
                }
            }
            if (payload.get("rankgo1") != null) {
                boolean newValue = Boolean.parseBoolean(payload.get("rankgo1").toString());
                if (I.isGameRankAllowed(Game.get("IEGO1")) != newValue) {
                    log += "> rankgo1: `" + I.isGameRankAllowed(Game.get("IEGO1")) + "` -> `" + newValue + "`\n";
                    I.setGameRanksAllowed(newValue, Game.get("IEGO1"));
                    i++;
                }
            }
            if (payload.get("ranksd") != null) {
                boolean newValue = Boolean.parseBoolean(payload.get("ranksd").toString());
                if (I.isGameRankAllowed(Game.get("IESD")) != newValue) {
                    log += "> ranksd: `" + I.isGameRankAllowed(Game.get("IESD")) + "` -> `" + newValue + "`\n";
                    I.setGameRanksAllowed(newValue, Game.get("IESD"));
                    i++;
                }
            }
            if (payload.get("rankie3") != null) {
                boolean newValue = Boolean.parseBoolean(payload.get("rankie3").toString());
                if (I.isGameRankAllowed(Game.get("IE3")) != newValue) {
                    log += "> rankie3: `" + I.isGameRankAllowed(Game.get("IE3")) + "` -> `" + newValue + "`\n";
                    I.setGameRanksAllowed(newValue, Game.get("IE3"));
                    i++;
                }
            }
            if (payload.get("rankie2") != null) {
                boolean newValue = Boolean.parseBoolean(payload.get("rankie2").toString());
                if (I.isGameRankAllowed(Game.get("IE2")) != newValue) {
                    log += "> rankie2: `" + I.isGameRankAllowed(Game.get("IE2")) + "` -> `" + newValue + "`\n";
                    I.setGameRanksAllowed(newValue, Game.get("IE2"));
                    i++;
                }
            }
            if (payload.get("rankie1") != null) {
                boolean newValue = Boolean.parseBoolean(payload.get("rankie1").toString());
                if (I.isGameRankAllowed(Game.get("IE1")) != newValue) {
                    log += "> rankie1: `" + I.isGameRankAllowed(Game.get("IE1")) + "` -> `" + newValue + "`\n";
                    I.setGameRanksAllowed(newValue, Game.get("IE1"));
                    i++;
                }
            }
            if (payload.get("rankstrikers") != null) {
                boolean newValue = Boolean.parseBoolean(payload.get("rankstrikers").toString());
                if (I.isGameRankAllowed(Game.get("IEGOSTR")) != newValue) {
                    log += "> rankstrikers: `" + I.isGameRankAllowed(Game.get("IEGOSTR")) + "` -> `" + newValue + "`\n";
                    I.setGameRanksAllowed(newValue, Game.get("IEGOSTR"));
                    i++;
                }
            }
            if (payload.get("rankxtreme") != null) {
                boolean newValue = Boolean.parseBoolean(payload.get("rankxtreme").toString());
                if (I.isGameRankAllowed(Game.get("IEGOSTRXTR")) != newValue) {
                    log += "> rankxtreme: `" + I.isGameRankAllowed(Game.get("IEGOSTRXTR")) + "` -> `" + newValue + "`\n";
                    I.setGameBoardAllowed(newValue, Game.get("IEGOSTRXTR"));
                    i++;
                }
            }
            if (i == 0)  return "No changes mades.";
            LogSlash("**[Website][Guild]:** `" + Editor.global_name + "`: Edited the server of **" + I.getGuild().getName() + "** with **" + i + "** new values:\n" + log);
            I.Update();
            return "Successfully updated " + i + " values for your server!";
        } catch (Exception ignored) {
            return "An error occurred.";
        }
    }



    private DiscordUser getDiscordUserByAuthorization(String authorization) throws Exception {
        String discordApiUrl = "https://discord.com/api/users/@me";
        URL url = URI.create(discordApiUrl).toURL();
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Authorization", authorization);
        StringBuilder response = new StringBuilder();
        try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
        }
        return GSON.fromJson(response.toString(), DiscordUser.class);
    }

    public static class DiscordUser implements Serializable {
        public String id;
        public String username;
        public String avatar;
        public String discriminator;
        public int public_flags;
        public int flags;
        public String banner;
        public Integer accent_color;
        public String global_name;
        public Object avatar_decoration_data;
        public Object collectibles;
        public String banner_color;
        public Object clan;
        public Object primary_guild;
        public boolean mfa_enabled;
        public String locale;
        public int premium_type;
    }
}
