package com.africell.cci_backend.Service;

import com.africell.cci_backend.Entity.TblSubscription;
import com.africell.cci_backend.Entity.TblTryNBuy;
import com.africell.cci_backend.Repository.TblToneCatalogueRepository;
import com.africell.cci_backend.Repository.TblSubscriptionRepository;
import com.africell.cci_backend.Repository.TblTryNBuyRepository;
import com.africell.cci_backend.dto.SubscriberResponse;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class SubscriberService {
    private final TblSubscriptionRepository subscriptionRepository;
    private final TblTryNBuyRepository tryNBuyRepository;
    private final TblToneCatalogueRepository toneCatalogueRepository;

    public SubscriberService(TblSubscriptionRepository subscriptionRepository,
                             TblTryNBuyRepository tryNBuyRepository,
                             TblToneCatalogueRepository toneCatalogueRepository) {
        this.subscriptionRepository = subscriptionRepository;
        this.tryNBuyRepository = tryNBuyRepository;
        this.toneCatalogueRepository = toneCatalogueRepository;
    }

    public Optional<SubscriberResponse> findSubscriber(Long msisdn) {

        try {

            // First search in Subscription table
            Optional<TblSubscription> subscription =
                    subscriptionRepository.findById(msisdn);

            if (subscription.isPresent()) {

                TblSubscription sub = subscription.get();

                SubscriberResponse response = new SubscriberResponse();

                response.setMobileNumber(String.valueOf(msisdn));
                response.setSubscribePlan(sub.getProductId());
                response.setToneCode(sub.getToneCode());
                response.setBillingDate(sub.getBillingDate());
                response.setRenewalDate(sub.getRenewDate());
                response.setCurrentActivity("Current active RBT found");

                toneCatalogueRepository.findByToneCode(sub.getToneCode())
                        .ifPresent(tone ->
                                response.setToneName(tone.getToneName()));

                return Optional.of(response);

            } else {

                // If not found, search in Try & Buy table
                Optional<TblTryNBuy> tryNBuy =
                        tryNBuyRepository.findById(msisdn);

                if (tryNBuy.isPresent()) {

                    TblTryNBuy tryBuy = tryNBuy.get();

                    SubscriberResponse response = new SubscriberResponse();

                    response.setMobileNumber(String.valueOf(msisdn));
                    response.setSubscribePlan("TSUBTNB");
                    response.setToneCode(tryBuy.getToneCode());

                    // Billing and renewal dates are null for Try & Buy
                    response.setBillingDate(null);
                    response.setRenewalDate(null);

                    response.setCurrentActivity("Current active RBT found");

                    toneCatalogueRepository.findByToneCode(tryBuy.getToneCode())
                            .ifPresent(tone ->
                                    response.setToneName(tone.getToneName()));

                    return Optional.of(response);

                } else {

                    // MSISDN not found in either table
                    SubscriberResponse response = new SubscriberResponse();

                    response.setMobileNumber(null);
                    response.setSubscribePlan(null);
                    response.setToneCode(null);
                    response.setToneName(null);
                    response.setBillingDate(null);
                    response.setRenewalDate(null);
                    response.setCurrentActivity(null);

                    return Optional.of(response);
                }
            }

        } catch (Exception e) {

            throw new RuntimeException(
                    "Error while searching subscriber: " + msisdn, e);
        }
    }
}
