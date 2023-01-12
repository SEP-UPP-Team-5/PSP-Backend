package tim5.psp.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
public class PaymentMethod {

    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false, unique = true)
    private Long id;
    private String methodName;
    private String merchant;
    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "subscription_id")
    private Subscription subscription;

}
