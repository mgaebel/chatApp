package ChatApp.service;

import ChatApp.domain.metrics.Metric;
import ChatApp.repository.MetricRepository;
import com.google.common.collect.Iterators;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
public class MetricEndpointController {

    @Autowired
    private MetricRepository metricRepository;

    @RequestMapping(value = "/metrics", method = RequestMethod.GET)
    public List<Metric> metrics(){
        return Collections.list(Iterators.asEnumeration(metricRepository.findAll().iterator()));
    }

    @RequestMapping(value = "/metric/:id", method = RequestMethod.GET)
    public Metric metric(@RequestPart String id){
        return metricRepository.findOne(Long.valueOf(id));
    }
}
