package git.MurylloMonteiro.Ratbank.Repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import git.MurylloMonteiro.Ratbank.Model.TransationModel;

public interface TrasationRepository extends JpaRepository<TransationModel, UUID> {
    List<TransationModel> findByNumberAccountEnvoyOrNumberAccoutReceivedOrderByDateTimeDesc(
            String sender, String receiver);
}
