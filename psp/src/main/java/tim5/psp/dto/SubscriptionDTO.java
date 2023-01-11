package tim5.psp.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import tim5.psp.model.PaymentMethod;

import java.util.List;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class SubscriptionDTO {

    private String webShopURI;
    private Set<PaymentMethod> methods;
}
