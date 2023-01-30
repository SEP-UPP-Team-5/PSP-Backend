package tim5.psp.controller;

import com.netflix.discovery.shared.*;
import com.netflix.eureka.EurekaServerContextHolder;
import com.netflix.eureka.registry.PeerAwareInstanceRegistry;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import tim5.psp.dto.CreateTransactionDTO;
import tim5.psp.dto.CreateTransactionResponseDTO;
import tim5.psp.dto.PaymentConfirmationDTO;
import tim5.psp.model.PaymentInfo;
import tim5.psp.model.PaymentMethod;
import tim5.psp.service.PaymentInfoService;
import tim5.psp.service.PaymentMethodService;
import java.util.Set;

@RestController
@CrossOrigin
@RequestMapping(value = "/paymentInfo")
public class PaymentInfoController {

    @Autowired
    private PaymentInfoService paymentInfoService;

    @Autowired
    private PaymentMethodService paymentMethodService;

    @LoadBalanced
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }


    @PostMapping(path = "/create")
    public ResponseEntity<?> createTransaction(@RequestBody CreateTransactionDTO createTransactionDTO){
        PaymentInfo paymentInfo = paymentInfoService.createTransactionFromOrderDetails(createTransactionDTO);
        CreateTransactionResponseDTO dto = new CreateTransactionResponseDTO();
        dto.setTransactionId(paymentInfo.getId());
        dto.setUrl("http://localhost:55131/methods/");
        System.out.println("psp");
        return new ResponseEntity<>(dto,HttpStatus.CREATED);
    }

    @GetMapping(path = "/paymentMethods/{transactionId}")
    public ResponseEntity<?> getPaymentMethodsForWebShop(@PathVariable Long transactionId){
        PaymentInfo transaction = paymentInfoService.findOne(transactionId);
        Set<PaymentMethod> methods = transaction.getSubscription().getMethods();
        return new ResponseEntity<>(methods, HttpStatus.OK);
    }


    @PostMapping(path = "/send/{transactionId}/{methodId}") // oov se poziva kad se klikne na izabrani nacin placanja
    public ResponseEntity<?> sendTransactionInfo(@PathVariable Long transactionId, @PathVariable Long methodId){

        PaymentInfo transaction = paymentInfoService.sendTransactionInfo(transactionId, methodId);
        PaymentMethod method = paymentMethodService.findOne(methodId);

        String url = getUrlFromPaymentMethod(method);

        //String payPalUrl = "http://localhost:8082/orders/create";
        //String bitcoinUrl = "http://localhost:8085/orders/create";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        //TODO: headers.setBearerAuth(token);
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
        String approvalUrl = restTemplate.postForObject(url, request, String.class);
        System.out.println("poslato sa psp na servis za placanje");
        System.out.println(approvalUrl);
        return new ResponseEntity<>(approvalUrl, HttpStatus.CREATED);

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


    @PostMapping(path = "/confirm")
    public ResponseEntity<?> confirmPayment(@RequestBody PaymentConfirmationDTO dto){
        paymentInfoService.markAsPayed(dto);

        String pspUrl = "http://localhost:8081/purchase/confirm/" + dto.getWebShopOrderId();

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


}
