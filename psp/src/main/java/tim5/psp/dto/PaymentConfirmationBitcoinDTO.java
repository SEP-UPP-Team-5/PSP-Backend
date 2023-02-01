package tim5.psp.dto;

import lombok.Data;

@Data
public class PaymentConfirmationBitcoinDTO {

    private String webShopOrderId;
    private String bitcoinWalletAddress;

}
