package tim5.psp.controller;

import com.netflix.discovery.shared.Applications;
import com.netflix.eureka.EurekaServerContextHolder;
import com.netflix.eureka.registry.PeerAwareInstanceRegistry;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.context.annotation.Bean;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import tim5.psp.dto.CreateTransactionDTO;
import tim5.psp.dto.PaymentConfirmationDTO;
import tim5.psp.dto.PaymentMethodDTO;
import tim5.psp.model.PaymentInfo;
import tim5.psp.service.PaymentInfoService;

import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RestController
@CrossOrigin
@RequestMapping(value = "/paymentInfo")
public class PaymentInfoController {

    @Autowired
    private PaymentInfoService paymentInfoService;

    @LoadBalanced
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }


    @PostMapping(path = "/create")
    public ResponseEntity<?> createTransaction(@RequestBody CreateTransactionDTO createTransactionDTO){
        paymentInfoService.createTransactionFromOrderDetails(createTransactionDTO);
        System.out.println("psp");
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PostMapping(path = "/send/{transactionId}") // TODO: add payment method
    public ResponseEntity<?> sendTransactionInfo(@PathVariable Long transactionId, @RequestBody PaymentMethodDTO paymentMethodDTO){

        PaymentInfo transaction = paymentInfoService.sendTransactionInfo(transactionId, paymentMethodDTO);

        PeerAwareInstanceRegistry registry = EurekaServerContextHolder.getInstance().getServerContext().getRegistry();
        Applications applications = registry.getApplications();

        applications.getRegisteredApplications().forEach((registeredApplication) -> {
            registeredApplication.getInstances().forEach((instance) -> {
                System.out.println("name:" + instance.getAppName() + "/" + "instanceId:"+ instance.getInstanceId() + "/" + "hostName:" + instance.getHostName() +"/" + "port" + instance.getPort());
                if(paymentMethodDTO.getMethodName().equals(instance.getAppName()))
                    System.out.println("Posalji na ovaj servis");
            });
        });

        String payPalUrl = "http://localhost:8082/orders/create"; // TODO: from payment method
        String bitcoinUrl = "http://localhost:8085/orders/create"; // TODO: from payment method
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        //TODO: headers.setBearerAuth(token);
        JSONObject obj = new JSONObject();
        try {
            obj.put("totalAmount", transaction.getAmount());
            obj.put("transactionId", transaction.getId());
            obj.put("orderId", transaction.getWebShopOrderId());
            obj.put("merchantId", "BAGSGQXCCH7WU");  //za paypal, za bitcoin se ne salje
        } catch (JSONException e) {
            e.printStackTrace();
        }

        HttpEntity<String> request = new HttpEntity<>(obj.toString(), headers);

        String approvalUrl = restTemplate().postForObject(bitcoinUrl, request, String.class);
        System.out.println("poslato sa psp na servis za placanje");
        System.out.println(approvalUrl);
        return new ResponseEntity<>(approvalUrl, HttpStatus.CREATED);

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


}
