package com.africell.cci_backend.Controller;
import com.africell.cci_backend.Service.SubscriberService;
import com.africell.cci_backend.dto.SubscriberResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
        }

        Map<String, Object> notFound = Map.of(
                "message", "Subscriber not found",
                "msisdn", msisdn
        );
        return ResponseEntity.status(404).body(notFound);
    }
}
