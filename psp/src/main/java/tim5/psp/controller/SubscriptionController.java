package tim5.psp.controller;

import com.netflix.appinfo.ApplicationInfoManager;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.PathParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.context.annotation.Bean;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import tim5.psp.dto.PaymentMethodDTO;
import tim5.psp.dto.SubscriptionDTO;
import tim5.psp.model.PaymentMethod;
import tim5.psp.model.Subscription;
import tim5.psp.model.TokenUtils;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import tim5.psp.service.SubscriptionService;
import java.util.Set;

@RestController
@RequestMapping(value = "/subscriptions")
public class SubscriptionController {
    @Autowired
    private SubscriptionService subscriptionService;

    @Autowired
    private TokenUtils jwtUtil;

    @Autowired
    private ApplicationInfoManager infoManager;
    Logger logger = LoggerFactory.getLogger(SubscriptionController.class);

    @LoadBalanced
    @Bean
    public RestTemplate getRestTemplate() {
        return new RestTemplate();
    }

    @PostMapping( path = "/create")
    public ResponseEntity<String> subscribeWebShop(@RequestBody SubscriptionDTO dto, HttpServletRequest servletRequest) {
        try {
            Subscription subscription = subscriptionService.subscribeWebShop(dto);
            logger.info("Subscription with id: " + subscription.getId() + " successfully added for the web shop: " + dto.getWebShopURI());

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
            getRestTemplate().postForObject(dto.getWebShopURI() + "/purchase/createSubscription", request, String.class);

            return new ResponseEntity<>(subscription.getApiKey(), HttpStatus.OK);

        } catch (Exception e) {
            logger.error("Exception with adding subscription. Error is: " + e);
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    @GetMapping("/subscribedMethods/{apiKey}")
    public ResponseEntity<?> getSubscribedPaymentMethodsForWebShop(@PathVariable String apiKey) {

        try {
            Set<PaymentMethod> subscribedMethods = subscriptionService.getSubscribedPaymentMethodsForWebShop(apiKey);
            logger.info("Overview of subscribed payment methods for web shop with id: " + apiKey );
            return new ResponseEntity<>(subscribedMethods, HttpStatus.OK);

        } catch (Exception e) {
            logger.error("Exception while getting payment methods. Error is: " + e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/addMethod/{subscriptionId}/{paymentMethodId}")
    public ResponseEntity<?> addMethodToWebShop(@PathVariable Long subscriptionId, @PathVariable Long paymentMethodId){
        subscriptionService.addPaymentMethodToWebShop(paymentMethodId, subscriptionId);
        return new ResponseEntity<>(HttpStatus.OK);
    }
/*
    @PostMapping("/removeMethod/{subscriptionId}/{paymentMethodId}")
    public ResponseEntity<?> removeMethodFromWebShop(@PathVariable Long subscriptionId, @PathVariable Long paymentMethodId){
        subscriptionService.removePaymentMethodFromWebShop(paymentMethodId, subscriptionId);
        return new ResponseEntity<>(HttpStatus.OK);
    }
*/
}
