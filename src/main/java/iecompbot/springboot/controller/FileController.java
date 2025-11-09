package iecompbot.springboot.controller;

import iecompbot.objects.profile.item.Item;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static iecompbot.Main.MainDirectory;
import static iecompbot.springboot.data.DatabaseObject.doQueryValue;

@Tag(name = "File Accessor", description = "Endpoints to access files such as icons or logos.")
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class FileController {

    @Operation(summary = "Get the image of a league by ID")
    @GetMapping("/img/league/{id}.png")
    @Cacheable(value = "leagueimg", key = "#id")
    public ResponseEntity<byte[]> getLeagueImg(@PathVariable Long id) {
        try {
            byte[] imageBytes = doQueryValue(byte[].class,"SELECT Image FROM league WHERE ID = ?", id).orElse(null);
            return process(imageBytes);
        } catch (Exception ignored) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Get the image tier of a league by ID")
    @GetMapping("/img/league/tier/{id}.png")
    @Cacheable(value = "leaguetierimg", key = "#id")
    public ResponseEntity<byte[]> getLeagueTierImg(@PathVariable Long id) {
        try {
            byte[] imageBytes = doQueryValue(byte[].class,"SELECT Image FROM league_tier WHERE ID = ?", id).orElse(null);
            return process(imageBytes);
        } catch (Exception ignored) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Get the emblem of a clan by ID")
    @GetMapping("/img/clan/{id}/emblem.png")
    @Cacheable(value = "clanemblem", key = "#id")
    public ResponseEntity<byte[]> getClanEmblem(@PathVariable Long id) {
        try {
            byte[] imageBytes = doQueryValue(byte[].class,"SELECT Emblem FROM clan WHERE ID = ?", id).orElse(null);
            return process(imageBytes);
        } catch (Exception ignored) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Get the image of an item by ID")
    @GetMapping("/img/item/{id}.png")
    @Cacheable(value = "itemimg", key = "#id + '-' + #i")
    public ResponseEntity<byte[]> getItemImg(@PathVariable Long id, @RequestParam int i) {
        try {
            Item I = Item.get(id);
            byte[] imageBytes = i == 3 ? I.getImage3() : i == 2 ? I.getImage2() : I.getImage();
            return process(imageBytes);
        } catch (Exception ignored) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Get an image asset from an event by name")
    @GetMapping("/img/event/{id}/{filename}.png")
    @Cacheable(value = "eventimg", key = "#id + '-' + #filename")
    public ResponseEntity<Resource> getEventImage(@PathVariable String id, @PathVariable String filename) {
        try {
            System.err.println(MainDirectory + "/assets/img/event/" + id + "/" + filename + ".png");
            Path filePath = Paths.get(MainDirectory + "/assets/img/event/" + id + "/" + filename + ".png").normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (!resource.exists()) return ResponseEntity.notFound().build();
            String mimeType = Files.probeContentType(filePath);
            MediaType mediaType = MediaType.parseMediaType(mimeType != null ? mimeType : "application/octet-stream");
            return ResponseEntity.ok().contentType(mediaType).header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"").body(resource);
        } catch (Exception ignored) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Get the image of a tournament by ID")
    @GetMapping("/img/server/{serverid}/{tournamentid}/img.png")
    @Cacheable(value = "eventimg", key = "#serverid + '-' + #tournamentid")
    public ResponseEntity<Resource> getTournamentImage(@PathVariable Long serverid, @PathVariable Long tournamentid) {
        try {
            Path filePath = Paths.get(MainDirectory + "/server/" + serverid + "/img/tournaments/" + tournamentid + ".png").normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (!resource.exists()) return ResponseEntity.notFound().build();
            String mimeType = Files.probeContentType(filePath);
            MediaType mediaType = MediaType.parseMediaType(mimeType != null ? mimeType : "application/octet-stream");
            return ResponseEntity.ok().contentType(mediaType).header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"").body(resource);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    private ResponseEntity<byte[]> process(byte[] imageBytes) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_PNG);
        return new ResponseEntity<>(imageBytes, headers, HttpStatus.OK);
    }
}
