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

        Optional<TblSubscription> subscription = subscriptionRepository.findById(msisdn);
        if (subscription.isPresent()) {
            return Optional.of(buildFromSubscription(subscription.get()));
        }

        Optional<TblTryNBuy> tryNBuy = tryNBuyRepository.findById(msisdn);
        if (tryNBuy.isPresent()) {
            return Optional.of(buildFromTryNBuy(tryNBuy.get()));
        }

        return Optional.empty();
    }

    private SubscriberResponse buildFromSubscription(TblSubscription sub) {
        SubscriberResponse response = new SubscriberResponse();
        response.setMobileNumber(String.valueOf(sub.getMsisdn()));
        response.setSubscribedPlan(sub.getPackName());
        response.setToneCode(sub.getToneCode());
        response.setBillingDate(sub.getBillingDate());
        response.setRenewalDate(sub.getRenewDate());
        response.setCurrentActivity("Current active RBT found");

        toneCatalogueRepository.findByToneCode(sub.getToneCode())
                .ifPresent(tone -> response.setToneName(tone.getToneName()));

        return response;
    }

    private SubscriberResponse buildFromTryNBuy(TblTryNBuy tryNBuy) {
        SubscriberResponse response = new SubscriberResponse();
        response.setMobileNumber(String.valueOf(tryNBuy.getMsisdn()));
        response.setToneCode(tryNBuy.getToneCode());
        response.setCurrentActivity("Try & Buy activity found");

        toneCatalogueRepository.findByToneCode(tryNBuy.getToneCode())
                .ifPresent(tone -> response.setToneName(tone.getToneName()));

        return response;
    }
}
