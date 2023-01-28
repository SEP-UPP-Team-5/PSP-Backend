package tim5.psp.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
    private String date; //vreme kreiranja narudzbine
    private Double amount;//sa web shopa
    private String paymentMethod;
    private String payerId; //sa servisa za placanje
    private String merchantId; //ovo se zna sa psp u okviru payment methoda
    private String webShopOrderId; //sa web shopa
    private Boolean isPaid;

    @OneToOne
    private Subscription subscription;


}
