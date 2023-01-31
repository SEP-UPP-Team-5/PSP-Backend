package tim5.psp.service;
import com.netflix.discovery.converters.Auto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tim5.psp.dto.CreateTransactionDTO;
import tim5.psp.dto.PaymentConfirmationDTO;
import tim5.psp.dto.PaymentMethodDTO;
import tim5.psp.model.PaymentInfo;
import tim5.psp.model.PaymentMethod;
import tim5.psp.model.Subscription;
import tim5.psp.repository.PaymentInfoRepository;
import tim5.psp.repository.PaymentMethodRepository;
import tim5.psp.repository.SubscriptionRepository;

@Service
public class PaymentInfoService {

    @Autowired
    private PaymentInfoRepository  paymentInfoRepository;

    @Autowired
    private PaymentMethodRepository paymentMethodRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    public PaymentInfo createTransactionFromOrderDetails(CreateTransactionDTO createTransactionDTO){
        Subscription subscription = subscriptionRepository.findByApiKey(createTransactionDTO.getApiKey());
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setAmount(createTransactionDTO.getAmount());
        paymentInfo.setWebShopOrderId(createTransactionDTO.getWebShopOrderId());
        paymentInfo.setWebShopTimestamp(createTransactionDTO.getWebShopTimestamp());
        paymentInfo.setIsPaid(false);
        paymentInfo.setSubscription(subscription);

        return paymentInfoRepository.save(paymentInfo);
    }

    public PaymentInfo sendTransactionInfo(Long transactionId, Long methodId){
        PaymentInfo transaction = paymentInfoRepository.findById(transactionId).get();
        PaymentMethod method = paymentMethodRepository.findById(methodId).get();
        transaction.setPaymentMethod(method.getMethodName());
        transaction.setMerchantId(method.getMerchant_id());
        return paymentInfoRepository.save(transaction);
    }

    public PaymentInfo findOne(Long id){
        return paymentInfoRepository.findById(id).get();
    }

    public Boolean markAsPayed(PaymentConfirmationDTO dto) {
        PaymentInfo transaction = paymentInfoRepository.findByWebShopOrderId(dto.getWebShopOrderId());
        transaction.setIsPaid(true);
        transaction.setPayerId(dto.getPayerId());
        transaction.setMerchantId(dto.getBitcoinWalletAddress());
        paymentInfoRepository.save(transaction);

        return null;
    }

    public PaymentInfo markAsPaid(String webShopOrderId) {
        PaymentInfo transaction = paymentInfoRepository.findByWebShopOrderId(webShopOrderId);
        transaction.setIsPaid(true);
        paymentInfoRepository.save(transaction);
        return transaction;
    }

    public void save(PaymentInfo paymentInfo){
        paymentInfoRepository.save(paymentInfo);
    }
}
