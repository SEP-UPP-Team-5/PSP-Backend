package tim5.psp.controller;

import com.netflix.discovery.shared.*;
import com.netflix.eureka.EurekaServerContextHolder;
import com.netflix.eureka.registry.PeerAwareInstanceRegistry;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import tim5.psp.dto.*;
import tim5.psp.model.PaymentInfo;
import tim5.psp.model.PaymentMethod;
import tim5.psp.service.PaymentInfoService;
import tim5.psp.service.PaymentMethodService;
import tim5.psp.service.SubscriptionService;
import tim5.psp.service.TokenService;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@RestController
@CrossOrigin
@RequestMapping(value = "/paymentInfo")
public class PaymentInfoController {

    @Autowired
    private PaymentInfoService paymentInfoService;

    @Autowired
    private PaymentMethodService paymentMethodService;

    @Autowired
    private SubscriptionService subscriptionService;

    @Autowired
    private Environment env;

    @Autowired
    private TokenService tokenService;

    @LoadBalanced
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }


    @PostMapping(path = "/create")
    public ResponseEntity<?> createTransaction(@RequestBody CreateTransactionDTO createTransactionDTO) throws URISyntaxException {
        if(subscriptionService.getByApiKey(createTransactionDTO.getApiKey())==null)
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);

        PaymentInfo paymentInfo = paymentInfoService.createTransactionFromOrderDetails(createTransactionDTO);
        URI pspFrontendUrl = new URI(env.getProperty("psp.frontend.host"));

        CreateTransactionResponseDTO dto = new CreateTransactionResponseDTO();
        dto.setTransactionId(paymentInfo.getId());
        dto.setUrl(pspFrontendUrl.toString());
        dto.setToken(tokenService.generateTokenPayment(paymentInfo.getSubscription()));
        System.out.println("psp");
        return new ResponseEntity<>(dto,HttpStatus.CREATED);
    }

    @GetMapping(path = "/paymentMethods/{transactionId}")
    public ResponseEntity<?> getPaymentMethodsForWebShop(@PathVariable Long transactionId){
        PaymentInfo transaction = paymentInfoService.findOne(transactionId);
        Set<PaymentMethod> methods = transaction.getSubscription().getMethods();
        return new ResponseEntity<>(methods, HttpStatus.OK);
    }


    @PostMapping(path = "/send/{transactionId}/{methodId}")
    public ResponseEntity<?> sendTransactionInfo(@RequestHeader("Authorization") String token, @PathVariable Long transactionId, @PathVariable Long methodId) throws JSONException {
        if(!tokenService.validateToken(token, "transaction_permission"))
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);

        PaymentInfo transaction = paymentInfoService.sendTransactionInfo(transactionId, methodId);
        PaymentMethod method = paymentMethodService.findOne(methodId);

        if(!tokenService.validateToken(token, method.getMethodName()))
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);

        String url = getUrlFromPaymentMethod(method);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        JSONObject obj = new JSONObject();
        try {
            obj.put("totalAmount", transaction.getAmount());
            obj.put("transactionId", transaction.getId());
            obj.put("orderId", transaction.getWebShopOrderId());
            obj.put("merchantId", method.getMerchant_id() );
            obj.put("merchantPassword", method.getMerchant_password() );
            obj.put("merchantTimestamp", transaction.getWebShopTimestamp());
            obj.put("successUrl", transaction.getSubscription().getSuccessUrl());
            obj.put("failedUrl", transaction.getSubscription().getFailedUrl());
            obj.put("errorUrl", transaction.getSubscription().getErrorUrl());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        HttpEntity<String> request = new HttpEntity<>(obj.toString(), headers);
        RestTemplate restTemplate = new RestTemplate();
        String paymentServiceResponse = restTemplate.postForObject(url, request, String.class);
        System.out.println("Sent info from PSP to: "  + method.getMethodServiceName());
        System.out.println(paymentServiceResponse);

        return new ResponseEntity<>(paymentServiceResponse, HttpStatus.CREATED);
    }

    private String getUrlFromPaymentMethod(PaymentMethod method) {
        PeerAwareInstanceRegistry registry = EurekaServerContextHolder.getInstance().getServerContext().getRegistry();
        Applications applications = registry.getApplications();

        for(Application registeredApplication : applications.getRegisteredApplications()){
            System.out.println();
            if(registeredApplication.getName().equals(method.getMethodServiceName())){
                System.out.println("Send info to this eureka client: " + registeredApplication.getName());
                if(method.getMethodName().equals("Credit Card") || method.getMethodName().equals("QR Code"))
                    return "http://" + registeredApplication.getInstances().get(0).getIPAddr() + ":" + registeredApplication.getInstances().get(0).getPort() + "/payment/" + getBankPathParam(method.getMethodName());
                else
                    return "http://" + registeredApplication.getInstances().get(0).getIPAddr() + ":" + registeredApplication.getInstances().get(0).getPort() + "/orders/create";
            }
        }

        return "";
    }

    private String getBankPathParam(String methodName) {
        return methodName.equals("Credit Card") ? "card" : "qr";
    }


    @PostMapping(path = "/confirmPayPal")
    public ResponseEntity<?> confirmPayPalPayment(@RequestBody PaymentConfirmationPayPalDTO dto){

        PaymentInfo paymentInfo = paymentInfoService.markAsPaidPayPalTransaction(dto);
        String pspUrl = paymentInfo.getSubscription().getWebShopURI() + "/purchase/confirm/" + dto.getWebShopOrderId();
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        JSONObject obj = new JSONObject();
        try {
            obj.put("webShopOrderId", dto.getWebShopOrderId());

        } catch (JSONException e) {
            e.printStackTrace();
        }

        HttpEntity<String> request = new HttpEntity<>(obj.toString(), headers);
        String captureOrderResponse = restTemplate.postForObject(pspUrl, request, String.class);
        System.out.println("captureOrderResponse");
        System.out.println(captureOrderResponse);

        return new ResponseEntity<>(dto, HttpStatus.OK);
    }


    @PostMapping(path = "/confirmBitcoin")
    public ResponseEntity<?> confirmBitcoinPayment(@RequestBody PaymentConfirmationBitcoinDTO dto){

        PaymentInfo paymentInfo = paymentInfoService.markAsPaidBitcoinTransaction(dto);
        String pspUrl = paymentInfo.getSubscription().getWebShopURI() + "/purchase/confirm/" + dto.getWebShopOrderId();
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        JSONObject obj = new JSONObject();
        try {
            obj.put("webShopOrderId", dto.getWebShopOrderId());

        } catch (JSONException e) {
            e.printStackTrace();
        }

        HttpEntity<String> request = new HttpEntity<>(obj.toString(), headers);
        String captureOrderResponse = restTemplate.postForObject(pspUrl, request, String.class);
        System.out.println("captureOrderResponse");
        System.out.println(captureOrderResponse);

        return new ResponseEntity<>(dto, HttpStatus.OK);
    }


    @PostMapping(path = "/confirmBank")
    public ResponseEntity<?> confirmBankPayment(@RequestBody PaymentConfirmationBankDTO dto){
        if(dto.getStatus().equals("SUCCESS")){
            PaymentInfo paymentInfo = paymentInfoService.markAsPaid(dto.getMerchantOrderId());
            String pspUrl = paymentInfo.getSubscription().getWebShopURI() + "/purchase/confirm/" + dto.getMerchantOrderId();
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            JSONObject obj = new JSONObject();
            try {
                obj.put("webShopOrderId", dto.getMerchantOrderId());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            HttpEntity<String> request = new HttpEntity<>(obj.toString(), headers);
            String captureOrderResponse = restTemplate.postForObject(pspUrl, request, String.class);
            System.out.println("captureOrderResponse");
            System.out.println(captureOrderResponse);
        }
        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    @GetMapping(path = "/findEurekaClients/{methodName}")
    public String findEurekaClient(@PathVariable String methodName){
        PeerAwareInstanceRegistry registry = EurekaServerContextHolder.getInstance().getServerContext().getRegistry();
        Applications applications = registry.getApplications();

        for(Application registeredApplication : applications.getRegisteredApplications()){
            System.out.println();
            if(registeredApplication.getName().equals(methodName)){
                System.out.println("Send info to this eureka client: " + registeredApplication.getName());
                return registeredApplication.getInstances().get(0).getIPAddr() + registeredApplication.getInstances().get(0).getPort();
            }
        }

        return "";
    }

    @GetMapping(path = "/getAllEurekaClients")
    public List<String> findEurekaClients(@RequestHeader("Authorization") String token){
        System.out.println(token);
        PeerAwareInstanceRegistry registry = EurekaServerContextHolder.getInstance().getServerContext().getRegistry();
        Applications applications = registry.getApplications();
        List<String> serviceNames = new ArrayList<>();

        for(Application registeredApplication : applications.getRegisteredApplications()){
            System.out.println("Eureka client app name: "  + registeredApplication.getName() + ", instance name: " +  registeredApplication.getInstances().get(0).getAppName() +  ", IP address: " + registeredApplication.getInstances().get(0).getIPAddr() + ", port: " + registeredApplication.getInstances().get(0).getPort());
            serviceNames.add(registeredApplication.getName());
        }
        return serviceNames;
    }

    @PostMapping(path = "/decode/{token}")
    public ResponseEntity<?> decodeToken(@PathVariable String token) throws JSONException {
        return new ResponseEntity<>(tokenService.decodeJWTToken(token), HttpStatus.OK);
    }


}
