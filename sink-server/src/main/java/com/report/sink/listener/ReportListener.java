package com.report.sink.listener;

import com.report.sink.handler.SinkHandler;
import com.report.sink.properties.DataSourceProperty;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author heqin
 */
@Component
public class ReportListener {

    private final Logger logger = LoggerFactory.getLogger(ReportListener.class);

    @Resource
    private SinkHandler sinkHandler;

    @KafkaListener(topics = "log-sink", containerFactory = "batchManualFactory")
    public void onReportMessage(List<ConsumerRecord<String, String>> records, Acknowledgment acknowledgment) {
        try {
            sinkHandler.run(records);
        }catch (Exception e) {
            logger.error("report-main error", e);
        }

        acknowledgment.acknowledge();
    }
}
