package tim5.psp.service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tim5.psp.model.PaymentInfo;
import tim5.psp.repository.PaymentInfoRepository;

@Service
public class PaymentInfoService {

    @Autowired
    private PaymentInfoRepository  paymentInfoRepository;

    public void createTransaction(PaymentInfo paymentInfo){
        paymentInfoRepository.save(paymentInfo);
    }
}
