package tim5.psp.service;

import org.apache.commons.lang.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tim5.psp.dto.PaymentMethodDTO;
import tim5.psp.model.PaymentMethod;
import tim5.psp.repository.PaymentMethodRepository;

import java.util.List;

@Service
public class PaymentMethodService {

    @Autowired
    private PaymentMethodRepository paymentMethodRepository;

    public PaymentMethod createNewPaymentMethod(PaymentMethodDTO dto){
        PaymentMethod newMethod =  new PaymentMethod();
        newMethod.setMethodName(dto.getMethodName());
        newMethod.setMerchant(dto.getMerchant());  //for PayPal some of existing ids from developer account

        return paymentMethodRepository.save(newMethod);
    }

    public List<PaymentMethod> findAll(){return paymentMethodRepository.findAll();}
}
