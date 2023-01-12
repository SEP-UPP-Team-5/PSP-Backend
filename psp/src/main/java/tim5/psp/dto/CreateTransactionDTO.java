package tim5.psp.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateTransactionDTO {

    private String creatingOrderTime;
    private Double amount;
    private String webShopOrderId;
}
