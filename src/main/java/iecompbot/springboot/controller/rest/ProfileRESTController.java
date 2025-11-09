package iecompbot.springboot.controller.rest;

import iecompbot.objects.profile.Profile;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Profiles", description = "Endpoints to access community profiles information.")
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class ProfileRESTController {

    @Operation(summary = "Get a profile by ID")
    @GetMapping("/profile/{id}.json")
    public Profile getProfileByID(@PathVariable long id) {
        return Profile.get(id);
    }

}
