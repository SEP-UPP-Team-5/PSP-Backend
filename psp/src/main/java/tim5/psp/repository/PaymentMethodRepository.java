package tim5.psp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tim5.psp.model.PaymentMethod;
import org.springframework.stereotype.Repository;
@Repository
public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, Long> {

}
