package tim5.psp.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tim5.psp.dto.PaymentMethodDTO;
import tim5.psp.model.PaymentMethod;
import tim5.psp.service.PaymentMethodService;

@RestController
@RequestMapping(value = "/paymentMethod")
public class PaymentMethodController {

    @Autowired
    private PaymentMethodService paymentMethodService;

    @PostMapping(path = "/add")
    public ResponseEntity<?> addNewPaymentMethod(@RequestBody PaymentMethodDTO paymentMethodDTO){
        PaymentMethod newMethod = paymentMethodService.createNewPaymentMethod(paymentMethodDTO);
        return new ResponseEntity<>(newMethod, HttpStatus.CREATED);
    }

    @GetMapping("/getAll")
    public ResponseEntity<?> getAllSubscriptions(){
        return new ResponseEntity<>(paymentMethodService.findAll(), HttpStatus.OK);
    }
}
