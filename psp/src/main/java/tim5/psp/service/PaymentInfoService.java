package tim5.psp.service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tim5.psp.dto.CreateTransactionDTO;
import tim5.psp.model.PaymentInfo;
import tim5.psp.repository.PaymentInfoRepository;

@Service
public class PaymentInfoService {

    @Autowired
    private PaymentInfoRepository  paymentInfoRepository;

    public PaymentInfo createTransactionFromOrderDetails(CreateTransactionDTO createTransactionDTO){

        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setAmount(createTransactionDTO.getAmount());
        paymentInfo.setDate(createTransactionDTO.getCreatingOrderTime());
        paymentInfo.setWebShopOrderId(createTransactionDTO.getWebShopOrderId());

        return paymentInfoRepository.save(paymentInfo);
    }
}
