package git.MurylloMonteiro.Ratbank.Model;

import java.time.LocalDateTime;
import java.util.UUID;
import java.math.BigDecimal;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class TransationModel {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private LocalDateTime dateTime;

    private String numberAccoutReceived;
    private String numberAccountEnvoy;
    
    private String pixKeyReceived;
    private String pixKeyEnvoy;
    
    private BigDecimal transationValue;
    private String type;
    
    // @Enumerated(EnumType.STRING)
    // private EnumTransationType transitionType;

}
