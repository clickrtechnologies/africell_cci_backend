package com.africell.cci_backend.Controller;
import com.africell.cci_backend.Service.SubscriberService;
import com.africell.cci_backend.dto.SubscriberResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.africell.cci_backend.dto.request.ActivationRequest;
import com.africell.cci_backend.dto.response.ActivationResponse;

import java.util.Map;
import java.util.Optional;
@RestController
@RequestMapping("/api/subscriber")

public class SubscriberController {
    private final SubscriberService subscriberService;

    public SubscriberController(SubscriberService subscriberService) {
        this.subscriberService = subscriberService;
    }

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
}
