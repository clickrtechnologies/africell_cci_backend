package com.africell.cci_backend.Controller;
import com.africell.cci_backend.Service.ToneCatalogueService;
import com.africell.cci_backend.dto.response.ToneCatalogueResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tone-catalogue")

public class ToneCatalogueController {
    private final ToneCatalogueService toneCatalogueService;
    public ToneCatalogueController(ToneCatalogueService toneCatalogueService) {
        this.toneCatalogueService = toneCatalogueService;

    }
    @GetMapping
    public ResponseEntity<?> getToneCatalogue(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDir) {

        try {

            List<ToneCatalogueResponse> tones =
                    toneCatalogueService.getTones(
                            search,
                            sortBy,
                            sortDir);

            return ResponseEntity.ok(tones);

        } catch (IllegalArgumentException e) {

            return ResponseEntity.badRequest().body(
                    Map.of(
                            "message", e.getMessage()
                    )
            );

        } catch (Exception e) {

            return ResponseEntity.internalServerError().body(
                    Map.of(
                            "message", "Unable to fetch tone catalogue"
                    )
            );
        }
    }
    // Get artists
    @GetMapping("/artists")
    public ResponseEntity<?> getArtists(
            @RequestParam(required = false)
            String search) {
                try {
                    List<String> artists = toneCatalogueService.getArtists(search);
                    return ResponseEntity.ok(artists);
                } catch (Exception e) {
                    return ResponseEntity.internalServerError().body(
                            Map.of(
                                    "message",
                                    "Unable to fetch artists"
                            )
                    );
                }
    }
    //Get categories
    @GetMapping("/categories")
    public ResponseEntity<?> getCategories(
            @RequestParam(required = false)
            String search) {
                try {
                    List<String> categories = toneCatalogueService.getCategories(search);
                    return ResponseEntity.ok(categories);
                } catch (Exception e) {
                    return ResponseEntity.internalServerError().body(
                            Map.of(
                                    "message",
                                    "Unable to fetch categories"
                            )
                    );
                }
    }



}
