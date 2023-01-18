package tim5.psp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tim5.psp.dto.PaymentMethodDTO;
import tim5.psp.model.PaymentMethod;
import tim5.psp.repository.PaymentMethodRepository;

@Service
public class PaymentMethodService {

    @Autowired
    private PaymentMethodRepository paymentMethodRepository;

    public PaymentMethod createNewPaymentMethod(PaymentMethodDTO dto){
        PaymentMethod newMethod =  new PaymentMethod();
        newMethod.setMethodName(dto.getMethodName());
        newMethod.setMerchant(dto.getMerchant());

        return paymentMethodRepository.save(newMethod);

    }
}
