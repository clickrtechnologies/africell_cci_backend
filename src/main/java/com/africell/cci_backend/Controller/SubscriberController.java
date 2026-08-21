package com.africell.cci_backend.Controller;
import com.africell.cci_backend.Service.SubscriberService;
import com.africell.cci_backend.dto.SubscriberResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.africell.cci_backend.dto.request.ActivationRequest;
import com.africell.cci_backend.dto.response.ActivationResponse;
import com.africell.cci_backend.dto.response.DeactivateResponse;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.Map;
import java.util.Optional;
/* Subscriber-related APIs */
@RestController
@RequestMapping("/api/subscriber")

public class SubscriberController {
    private final SubscriberService subscriberService;

    public SubscriberController(SubscriberService subscriberService) {
        this.subscriberService = subscriberService;
    }
    /* GET API */
    @GetMapping("/{msisdn}")
    public ResponseEntity<?> getSubscriber(@PathVariable Long msisdn) {
        Optional<SubscriberResponse> result = subscriberService.findSubscriber(msisdn);

        if (result.isPresent()) {
            return ResponseEntity.ok(result.get());
        }else {
            Map<String, Object> notFound = Map.of(
                    "message", "Subscriber not found",
                    "msisdn", msisdn
            );

            return ResponseEntity.status(404).body(notFound);
        }
    }
    /* POST API */

    @PostMapping("/activate")
    public ResponseEntity<?> activateSubscriber(
            @RequestBody ActivationRequest request) {

        try {

            ActivationResponse response =
                    subscriberService.activateSubscriber(request);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {

            return ResponseEntity.badRequest().body(
                    Map.of(
                            "message", e.getMessage()
                    )
            );

        } catch (Exception e) {

            return ResponseEntity.internalServerError().body(
                    Map.of(
                            "message", "Unable to activate subscriber"
                    )
            );
        }
    }
    /* POST API to deactivate RBT for a subscriber. */
    @PostMapping("/deactivate")
    public ResponseEntity<?> deactivateSubscriber(
            @RequestHeader("msisdn") Long msisdn) {

        try {

            DeactivateResponse response =
                    subscriberService.deactivateSubscriber(msisdn);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {

            return ResponseEntity.badRequest().body(
                    Map.of(
                            "message", e.getMessage()
                    )
            );

        } catch (Exception e) {
            /* Handles unexpected server-side errors */

            return ResponseEntity.internalServerError().body(
                    Map.of(
                            "message", "Unable to deactivate subscriber"
                    )
            );
        }
    }
}
