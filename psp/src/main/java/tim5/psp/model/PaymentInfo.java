package tim5.psp.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private Long id;
    private Double amount;//sa web shopa
    private String paymentMethod; // nakon odabira sa psp fronta
    private String payerId; //sa servisa za placanje
    // TODO: proveriti da li ovo treba
    private String merchantId; //ovo se zna sa psp u okviru payment methoda
    private String webShopOrderId; //sa web shopa
    private LocalDateTime webShopTimestamp; //sa web shopa
    private Boolean isPaid; //posle potvrde sa payment servisa

    @ManyToOne
    private Subscription subscription;


}
