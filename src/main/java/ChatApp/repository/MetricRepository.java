package ChatApp.repository;

import ChatApp.domain.metrics.Metric;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface MetricRepository extends CrudRepository<Metric, Long> {
}
