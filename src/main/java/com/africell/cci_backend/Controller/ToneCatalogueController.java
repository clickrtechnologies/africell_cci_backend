package com.africell.cci_backend.Controller;

import com.africell.cci_backend.Entity.TblToneCatalogue;
import com.africell.cci_backend.Service.ToneCatalogueService;
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
            @RequestParam(required = false) String search) {

        try {

            List<TblToneCatalogue> tones =
                    toneCatalogueService.getTones(search);

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
}
