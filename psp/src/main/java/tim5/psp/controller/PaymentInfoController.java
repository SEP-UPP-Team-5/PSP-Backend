package tim5.psp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tim5.psp.dto.CreateTransactionDTO;
import tim5.psp.model.PaymentInfo;
import tim5.psp.service.PaymentInfoService;

@RestController
@RequestMapping(value = "/paymentInfo")
public class PaymentInfoController {

    @Autowired
    private PaymentInfoService paymentInfoService;

    @PostMapping(path = "/create")
    public ResponseEntity<?> createTransaction(@RequestBody CreateTransactionDTO createTransactionDTO){
        paymentInfoService.createTransactionFromOrderDetails(createTransactionDTO);
        System.out.println("psp");
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

}
