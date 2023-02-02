package tim5.psp.service;

import com.netflix.appinfo.ApplicationInfoManager;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.collection.internal.StandardBagSemantics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tim5.psp.dto.PaymentMethodDTO;
import tim5.psp.dto.SubscriptionDTO;
import tim5.psp.model.PaymentMethod;
import tim5.psp.model.Subscription;
import tim5.psp.model.TokenUtils;
import tim5.psp.repository.PaymentMethodRepository;
import tim5.psp.repository.SubscriptionRepository;

import java.util.*;

@Service
public class SubscriptionService {

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private PaymentMethodRepository paymentMethodRepository;

    @Autowired
    private ApplicationInfoManager infoManager;

    @Autowired
    private TokenUtils token;

    public Subscription subscribeWebShop(SubscriptionDTO dto) {
        Subscription subscription = new Subscription(null, dto.getWebShopURI(), token.generateToken(dto.getWebShopURI(), "transaction_permission"), dto.getSuccessUrl(), dto.getFailedUrl(), dto.getErrorUrl(), new HashSet<>());
        return subscriptionRepository.save(subscription);
    }

    public PaymentMethod addPaymentMethodToWebShop(Long paymentMethodId, Long subscriptionId) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId).get();
        PaymentMethod method = paymentMethodRepository.findById(paymentMethodId).get();
        method.setSubscription(subscription);

        Set<PaymentMethod> methods = subscription.getMethods();
        methods.add(method);
        subscription.setMethods(methods);
        String permission = "";
        for (PaymentMethod m : methods) {
            permission += m.getMethodName() + ",";
        }
        permission += "transaction_permission";
        subscription.setApiKey(token.generateToken(subscription.getWebShopURI(), permission));
        subscriptionRepository.save(subscription);


        sendApiKeyToWebShop(subscription);

        return paymentMethodRepository.save(method);
    }

    private void sendApiKeyToWebShop(Subscription subscription) {
        String hostName = infoManager.getInfo().getHostName();
        Integer port = infoManager.getInfo().getPort();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        JSONObject obj = new JSONObject();
        try {
            obj.put("apiKey", subscription.getApiKey());
            obj.put("url", "http://" + hostName + ":" + port);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        HttpEntity<String> request = new HttpEntity<>(obj.toString(), headers);
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.postForObject(subscription.getWebShopURI() + "/purchase/createSubscription", request, String.class);

    }

    public PaymentMethod removePaymentMethodFromWebShop(Long paymentMethodId, Long subscriptionId) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId).get();
        PaymentMethod method = paymentMethodRepository.findById(paymentMethodId).get();
        method.getSubscription().getMethods().remove(method); //odavde uklonio

        Set<PaymentMethod> methods = subscription.getMethods();
        for (Iterator<PaymentMethod> iterator = methods.iterator(); iterator.hasNext(); ) {
            PaymentMethod m = iterator.next();
            if (m.getId().equals(method.getId())) {
                iterator.remove();
                subscription.getMethods().remove(method);
                subscriptionRepository.save(subscription);
            }
        }
        return paymentMethodRepository.save(method);
    }

    public Set<PaymentMethod> getSubscribedPaymentMethodsForWebShop(String apiKey) {

        Subscription subscription = subscriptionRepository.findByApiKey(apiKey);
        Set<PaymentMethod> enabledMethods = new HashSet<>();

        for (PaymentMethod method : paymentMethodRepository.findAll()) {
            if (method.getSubscription().getId().equals(subscription.getId()))
                enabledMethods.add(method);
        }

        return enabledMethods;
    }

    public Subscription save(Subscription subscription) {
        return subscriptionRepository.save(subscription);
    }

    public List<Subscription> findAll() {
        return subscriptionRepository.findAll();
    }

    public Subscription getByApiKey(String apiKey){
        for(Subscription subscription:findAll())
            if(subscription.getApiKey().equals(apiKey))
                return subscription;
        return null;
    }
}

