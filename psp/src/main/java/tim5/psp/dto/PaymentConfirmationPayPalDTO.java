package tim5.psp.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PaymentConfirmationPayPalDTO {

    private String webShopOrderId;
    private String payerId;
}
