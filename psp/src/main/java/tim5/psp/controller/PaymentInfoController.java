package tim5.psp.controller;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import tim5.psp.dto.CreateTransactionDTO;
import tim5.psp.model.PaymentInfo;
import tim5.psp.service.PaymentInfoService;

import java.util.Date;

@RestController
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

    @PostMapping(path = "/send/{transactionId}") //todo: dodaj rezim za placanje
    public ResponseEntity<?> sendTransactionInfo(@PathVariable Long transactionId){
        PaymentInfo transaction = paymentInfoService.findOne(transactionId);

        String payPalUrl = "http://localhost:8082/orders/create";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        //TODO: headers.setBearerAuth(token);
        JSONObject obj = new JSONObject();
        try {
            obj.put("totalAmount", transaction.getAmount());
            obj.put("transactionId", transaction.getId());
            obj.put("orderId", transaction.getWebShopOrderId());
            obj.put("merchantId", "BAGSGQXCCH7WU"); //TODO: set merchant id from payment method
        } catch (JSONException e) {
            e.printStackTrace();
        }

        HttpEntity<String> request = new HttpEntity<>(obj.toString(), headers);

        restTemplate().postForObject(payPalUrl, request, String.class);
        System.out.println("poslato sa psp na paypal");
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

        @PostMapping("/confirm")
        public String paymentConfirmation (@RequestBody String webShopOrderId){
        System.out.println(webShopOrderId);


        return "paid";

   }



}
