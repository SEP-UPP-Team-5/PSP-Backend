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

import java.util.*;

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

    public PaymentMethod addPaymentMethodToWebShop(Long paymentMethodId, Long subscriptionId){
        Subscription subscription = subscriptionRepository.findById(subscriptionId).get();
        PaymentMethod method = paymentMethodRepository.findById(paymentMethodId).get();
        method.setSubscription(subscription);

        Set<PaymentMethod> methods = subscription.getMethods();
        methods.add(method);
        subscription.setMethods(methods);
        subscriptionRepository.save(subscription);

        return paymentMethodRepository.save(method);
    }

   /* public PaymentMethod removePaymentMethodFromWebShop(Long paymentMethodId, Long subscriptionId){
        Subscription subscription = subscriptionRepository.findById(subscriptionId).get();
        PaymentMethod method = paymentMethodRepository.findById(paymentMethodId).get();
        method.getSubscription().getMethods().remove(method); //odavde uklonio
        subscription.getMethods().remove(method);

        Set<PaymentMethod> methods = subscription.getMethods();
        for (Iterator<PaymentMethod> iterator = methods.iterator(); iterator.hasNext();) {
            PaymentMethod m =  iterator.next();
            if (m.getId().equals(method.getId())) {
                iterator.remove();
                subscription.getMethods().remove(method);
                subscriptionRepository.save(subscription);
            }
        }
        return paymentMethodRepository.save(method);
    }

*/

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

