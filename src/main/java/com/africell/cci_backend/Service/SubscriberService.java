package com.africell.cci_backend.Service;

import com.africell.cci_backend.Entity.TblSubscription;
import com.africell.cci_backend.Entity.TblTryNBuy;
import com.africell.cci_backend.Repository.TblToneCatalogueRepository;
import com.africell.cci_backend.Repository.TblSubscriptionRepository;
import com.africell.cci_backend.Repository.TblTryNBuyRepository;
import com.africell.cci_backend.dto.SubscriberResponse;
import org.springframework.stereotype.Service;
import com.africell.cci_backend.dto.request.ActivationRequest;
import com.africell.cci_backend.dto.response.ActivationResponse;
import com.africell.cci_backend.Entity.TblBillingId;
import com.africell.cci_backend.Repository.TblBillingRepository;
import com.africell.cci_backend.dto.response.DeactivateResponse;
import jakarta.persistence.EntityManager;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.StoredProcedureQuery;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import jakarta.persistence.PersistenceContext;
import java.util.Optional;

@Service
public class SubscriberService {
    private final TblSubscriptionRepository subscriptionRepository;
    private final TblTryNBuyRepository tryNBuyRepository;
    private final TblToneCatalogueRepository toneCatalogueRepository;
    private final TblBillingRepository billingRepository;

    @PersistenceContext
    private EntityManager entityManager;
    public SubscriberService(TblSubscriptionRepository subscriptionRepository,
                             TblTryNBuyRepository tryNBuyRepository,
                             TblToneCatalogueRepository toneCatalogueRepository,
                             TblBillingRepository billingRepository) {
        this.subscriptionRepository = subscriptionRepository;
        this.tryNBuyRepository = tryNBuyRepository;
        this.toneCatalogueRepository = toneCatalogueRepository;
        this.billingRepository = billingRepository;

    }

    //Find Subscriber
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

                // Get tone name
                toneCatalogueRepository.findByToneCode(sub.getToneCode())
                        .ifPresent(tone ->
                                response.setToneName(tone.getToneName()));

                return Optional.of(response);

            } else {
                //check Try-N-buy

                Optional<TblTryNBuy> tryNBuy =
                        tryNBuyRepository.findById(msisdn);

                if (tryNBuy.isPresent()) {

                    TblTryNBuy tryBuy = tryNBuy.get();

                    SubscriberResponse response = new SubscriberResponse();
                    response.setMobileNumber(String.valueOf(msisdn));
                    response.setSubscribePlan("TSUBTNB");
                    response.setToneCode(tryBuy.getToneCode());

                    response.setBillingDate(null);
                    response.setRenewalDate(null);

                    response.setCurrentActivity("Current active RBT found");

                    // Get tone name
                    toneCatalogueRepository.findByToneCode(tryBuy.getToneCode())
                            .ifPresent(tone ->
                                    response.setToneName(tone.getToneName()));

                    return Optional.of(response);

                } else {
                    // New subscriber

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

    // Activate RBT
    @Transactional
    public ActivationResponse activateSubscriber(ActivationRequest request) {

        try {
            //validate request
            if (request == null
                    || request.getMsisdn() == null
                    || request.getToneCode() == null
                    || request.getToneName() == null
                    || request.getPackName() == null) {

                throw new IllegalArgumentException(
                        "MSISDN, tone code, tone name and pack name are required"
                );
            }

            //validate tone
            var tone = toneCatalogueRepository
                    .findByToneCode(request.getToneCode())
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "Tone code not found: " + request.getToneCode()
                            )
                    );

            if (!tone.getToneName().equalsIgnoreCase(request.getToneName())) {

                throw new IllegalArgumentException(
                        "Tone code and tone name do not match"
                );
            }

            // Stored procedure
            StoredProcedureQuery procedure =
                    entityManager.createStoredProcedureQuery("PROC_SUB_UNSUB");

            procedure.registerStoredProcedureParameter(
                    "IN_ANI",
                    Long.class,
                    ParameterMode.IN
            );

            procedure.registerStoredProcedureParameter(
                    "IN_PID",
                    String.class,
                    ParameterMode.IN
            );

            procedure.registerStoredProcedureParameter(
                    "IN_TONECODE",
                    String.class,
                    ParameterMode.IN
            );

            procedure.registerStoredProcedureParameter(
                    "IN_REQMODE",
                    String.class,
                    ParameterMode.IN
            );

            procedure.registerStoredProcedureParameter(
                    "IN_LANG",
                    String.class,
                    ParameterMode.IN
            );

            procedure.registerStoredProcedureParameter(
                    "IN_ACTION",
                    String.class,
                    ParameterMode.IN
            );

            procedure.registerStoredProcedureParameter(
                    "IN_PROMONAME",
                    String.class,
                    ParameterMode.IN
            );

            procedure.registerStoredProcedureParameter(
                    "IN_PROMOID",
                    String.class,
                    ParameterMode.IN
            );

            procedure.setParameter("IN_ANI", request.getMsisdn());

            procedure.setParameter("IN_PID", request.getPackName());

            procedure.setParameter("IN_TONECODE", request.getToneCode());

            procedure.setParameter("IN_REQMODE", "CCI");

            procedure.setParameter("IN_LANG", "fr");

            procedure.setParameter("IN_ACTION", "S");

            procedure.setParameter("IN_PROMONAME", "NA");

            procedure.setParameter("IN_PROMOID", "NA");

            procedure.execute();

            LocalDateTime now = LocalDateTime.now();

            Optional<TblSubscription> existingSubscription =
                    subscriptionRepository.findById(request.getMsisdn());

            TblSubscription subscription;

            if (existingSubscription.isPresent()) {

                subscription = existingSubscription.get();

                subscription.setReqDate(now);
                subscription.setBillingDate(now);
                subscription.setRenewDate(
                        calculateRenewalDate(request.getPackName(), now)
                );

                subscription.setPackName(request.getPackName());
                subscription.setProductId(request.getPackName());

                subscription.setReqMode("CCI");
                subscription.setLang("fr");

                subscription.setToneCode(request.getToneCode());

                subscription.setStatus(1);

                subscription.setPromoName("NA");
                subscription.setPromoId("NA");

            }

            else {
                //Create subscription
                subscription = new TblSubscription();

                subscription.setMsisdn(request.getMsisdn());

                subscription.setSubDate(now);
                subscription.setReqDate(now);
                subscription.setBillingDate(now);

                subscription.setRenewDate(
                        calculateRenewalDate(request.getPackName(), now)
                );

                subscription.setPackName(request.getPackName());

                subscription.setProductId(request.getPackName());

                subscription.setReqMode("CCI");

                subscription.setToneCode(request.getToneCode());

                subscription.setLang("fr");

                subscription.setStatus(1);

                subscription.setPromoName("NA");
                subscription.setPromoId("NA");

                subscription.setUserStatus("NEW");

            }
            // Save subscription
            subscription = subscriptionRepository.save(subscription);
            // Build response
            ActivationResponse response = new ActivationResponse();

            response.setMsisdn(subscription.getMsisdn());
            response.setSubDate(subscription.getSubDate());
            response.setReqDate(subscription.getReqDate());
            response.setBillingDate(subscription.getBillingDate());
            response.setRenewDate(subscription.getRenewDate());

            response.setPackName(subscription.getPackName());
            response.setTransId(subscription.getTransId());
            response.setServiceId(subscription.getServiceId());
            response.setProductId(subscription.getProductId());

            response.setReqMode(subscription.getReqMode());

            response.setToneCode(subscription.getToneCode());
            response.setToneName(tone.getToneName());

            response.setLang(subscription.getLang());
            response.setStatus(subscription.getStatus());
            response.setAmount(subscription.getAmount());

            response.setChargingStatus(subscription.getChargingStatus());
            response.setNoOfRetries(subscription.getNoOfRetries());

            response.setFallbackString(subscription.getFallbackString());
            response.setFallbackPacks(subscription.getFallbackPacks());

            response.setUserStatus(subscription.getUserStatus());
            response.setPromoName(subscription.getPromoName());
            response.setPromoId(subscription.getPromoId());

            response.setCurrentBalance(subscription.getCurrentBalance());

            return response;


        } catch (IllegalArgumentException e) {

            throw e;

        } catch (Exception e) {

            throw new RuntimeException(
                    "Error while activating subscriber: "
                            + request.getMsisdn(),
                    e
            );
        }
    }
    //Deactivate RBT
    @Transactional
    public DeactivateResponse deactivateSubscriber(Long msisdn) {

        try {
            // Validate MSISDN
            if (msisdn == null) {
                throw new IllegalArgumentException(
                        "MSISDN is required"
                );
            }

            // Check whether subscriber has an active subscription
            Optional<TblSubscription> subscription =
                    subscriptionRepository.findById(msisdn);

            if (subscription.isEmpty()) {
                throw new IllegalArgumentException(
                        "No active subscription found for MSISDN: " + msisdn
                );
            }
            // Stored procedure
            StoredProcedureQuery procedure =
                    entityManager.createStoredProcedureQuery("PROC_SUB_UNSUB");

            procedure.registerStoredProcedureParameter(
                    "IN_ANI",
                    Long.class,
                    ParameterMode.IN
            );

            procedure.registerStoredProcedureParameter(
                    "IN_PID",
                    String.class,
                    ParameterMode.IN
            );

            procedure.registerStoredProcedureParameter(
                    "IN_TONECODE",
                    String.class,
                    ParameterMode.IN
            );

            procedure.registerStoredProcedureParameter(
                    "IN_REQMODE",
                    String.class,
                    ParameterMode.IN
            );

            procedure.registerStoredProcedureParameter(
                    "IN_LANG",
                    String.class,
                    ParameterMode.IN
            );

            procedure.registerStoredProcedureParameter(
                    "IN_ACTION",
                    String.class,
                    ParameterMode.IN
            );

            procedure.registerStoredProcedureParameter(
                    "IN_PROMONAME",
                    String.class,
                    ParameterMode.IN
            );

            procedure.registerStoredProcedureParameter(
                    "IN_PROMOID",
                    String.class,
                    ParameterMode.IN
            );

            // Set procedure parameters
            procedure.setParameter(
                    "IN_ANI",
                    msisdn
            );

            procedure.setParameter(
                    "IN_PID",
                    subscription.get().getProductId()
            );

            procedure.setParameter(
                    "IN_TONECODE",
                    subscription.get().getToneCode()
            );

            procedure.setParameter(
                    "IN_REQMODE",
                    "CCI"
            );

            procedure.setParameter(
                    "IN_LANG",
                    "fr"
            );

            // IMPORTANT:
            // U = Unsubscribe / Deactivate
            procedure.setParameter(
                    "IN_ACTION",
                    "U"
            );

            procedure.setParameter(
                    "IN_PROMONAME",
                    "NA"
            );

            procedure.setParameter(
                    "IN_PROMOID",
                    "NA"
            );

            // Execute procedure
            procedure.execute();

            return new DeactivateResponse(
                    msisdn,
                    "RBT deactivated successfully"
            );

        } catch (IllegalArgumentException e) {

            throw e;

        } catch (Exception e) {

            throw new RuntimeException(
                    "Error while deactivating subscriber: " + msisdn,
                    e
            );
        }
    }


    private LocalDateTime calculateRenewalDate(
            String packName,
            LocalDateTime billingDate) {

        if (packName == null || packName.isBlank()) {
            throw new IllegalArgumentException("Pack name is required");
        }

        TblBillingId billingId = billingRepository
                .findByProductId(packName)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Product not found: " + packName
                        )
                );

        if (billingId.getValidity() == null || billingId.getValidity() <= 0) {
            throw new IllegalArgumentException(
                    "Invalid validity for product: " + packName
            );
        }

        return billingDate.plusDays(billingId.getValidity());
    }
    @Transactional
    public void processTryNBuy(
            Long msisdn,
            String toneCode) {

        if (msisdn == null) {
            throw new IllegalArgumentException(
                    "MSISDN is required"
            );
        }

        if (toneCode == null || toneCode.isBlank()) {
            throw new IllegalArgumentException(
                    "Tone code is required"
            );
        }

        LocalDateTime now = LocalDateTime.now();

        Optional<TblTryNBuy> existing =
                tryNBuyRepository.findById(msisdn);

        TblTryNBuy tryNBuy;

        if (existing.isPresent()) {

            // Existing Try N Buy record → update
            tryNBuy = existing.get();

            tryNBuy.setToneCode(toneCode);
            tryNBuy.setReqDate(now);
            tryNBuy.setStatus(2);

        } else {

            // New Try N Buy record → insert
            tryNBuy = new TblTryNBuy();

            tryNBuy.setMsisdn(msisdn);
            tryNBuy.setToneCode(toneCode);
            tryNBuy.setReqDate(now);
            /*
             * Try N Buy status
             * 2 = active/processed Try N Buy
             */
            tryNBuy.setStatus(2);
        }

        tryNBuyRepository.save(tryNBuy);
    }
}
