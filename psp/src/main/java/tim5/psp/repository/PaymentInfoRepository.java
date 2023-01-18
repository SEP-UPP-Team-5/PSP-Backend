package tim5.psp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tim5.psp.model.PaymentInfo;

@Repository
public interface PaymentInfoRepository extends JpaRepository<PaymentInfo, Long> {

    PaymentInfo findByWebShopOrderId(String webShopOrderId);
}
