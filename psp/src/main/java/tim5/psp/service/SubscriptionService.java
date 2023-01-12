package tim5.psp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import tim5.psp.dto.PaymentMethodDTO;
import tim5.psp.dto.SubscriptionDTO;
import tim5.psp.model.PaymentMethod;
import tim5.psp.model.Subscription;
import tim5.psp.model.TokenUtils;
import tim5.psp.repository.PaymentMethodRepository;
import tim5.psp.repository.SubscriptionRepository;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class SubscriptionService {

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private PaymentMethodRepository paymentMethodRepository;

    @Autowired
    private TokenUtils token;
    public Subscription subscribeWebShop(SubscriptionDTO dto){

        Subscription subscription = new Subscription();
        subscription.setWebShopURI(dto.getWebShopURI());
        subscription.setApiKey(BCrypt.hashpw(token.generateToken(dto.getWebShopURI()), BCrypt.gensalt()));

        return subscriptionRepository.save(subscription);
    }

    public PaymentMethod addPaymentMethodToWebShop(PaymentMethodDTO dto, Long subscriptionId){
        Subscription subscription = subscriptionRepository.findById(subscriptionId).get();

        PaymentMethod method = new PaymentMethod();
        method.setMethodName(dto.getMethodName());
        method.setMerchant(dto.getMerchant());
        method.setSubscription(subscription);

        Set<PaymentMethod> methods = subscription.getMethods();
        methods.add(method);
        subscription.setMethods(methods);
        return paymentMethodRepository.save(method);
    }

    /*public Subscription unsubscribeWebShop(String webShopURI, SubscriptionChangeDTO dto){

        Subscription subscription = subscriptionRepository.findByWebShopURI(webShopURI);
        subscription.setPayPal(dto.getPayPal());
        subscription.setBitcoin(dto.getBitcoin());
        subscription.setCreditCard(dto.getCreditCard());
        subscription.setQrCode(dto.getQrCode());

        return subscriptionRepository.save(subscription);
    }*/

    public Set<PaymentMethod> getSubscribedPaymentMethodsForWebShop(String apiKey){

        Subscription subscription = subscriptionRepository.findByApiKey(apiKey);
        Set<PaymentMethod> enabledMethods = new HashSet<>();

        for(PaymentMethod method : paymentMethodRepository.findAll()){
            if(method.getSubscription().getId().equals(subscription.getId()))
                enabledMethods.add(method);
        }

        return enabledMethods;
    }

    public Subscription save(Subscription subscription){
        return subscriptionRepository.save(subscription);
    }

}

