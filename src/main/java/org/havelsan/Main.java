package org.havelsan;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.IntegerDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) {
        Map<String, SensorData> sensorDataMap = new HashMap<>();

        Consumer<String, String> consumer = createConsumer();

        String topic = "sensor-data-topic";
        consumer.subscribe(Collections.singleton(topic));

        ObjectMapper objectMapper = new ObjectMapper();

        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
            records.forEach(record -> {
                try {
                    if (!"sensor-data".equals(record.key())) {
                        return;
                    }

                    String key = record.key();
                    String jsonValue = record.value();

                    SensorData sensorData = objectMapper.readValue(jsonValue, SensorData.class);
                    sensorDataMap.put(sensorData.getSensorId(), sensorData);
                    if (sensorDataMap.size() == 2) {
                        calculateCoordinate(sensorDataMap);
                        sensorDataMap.clear();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }

    private static void calculateCoordinate(Map<String, SensorData> sensorDataMap) {
        List<SensorData> sensorData = new ArrayList<>(sensorDataMap.values());

        SensorData sensorData1 = sensorData.get(0);
        SensorData sensorData2 = sensorData.get(1);

        double b1 = calculateYAxisIntersection(sensorData1);
        double b2 = calculateYAxisIntersection(sensorData2);

        double m1 = Math.tan(Math.toRadians(sensorData1.getBearing()));
        double m2 = Math.tan(Math.toRadians(sensorData2.getBearing()));

        if (m1 == m2) {
            System.out.println("Calculation failed!");
            return;
        }

        double x = (b2 - b1) / (m1 - m2);
        double y = m1 * x + b1;

        if (x > 1000d || x < -1000d || y > 1000d || y < -1000d) {
            System.out.println("Target calculated outside of 1000x1000 field!");
        } else {
            System.out.println("Target Coordinate: x = " + x + ", y = " + y);
        }
    }

    private static double calculateYAxisIntersection(SensorData sensorData) {
        double x = sensorData.getPoint().getX();
        double y = sensorData.getPoint().getY();
        double slope = Math.tan(Math.toRadians(sensorData.getBearing()));
        return y - slope * x;
    }

    private static Consumer<String, String> createConsumer() {
        // Kafka server address and port
        String bootstrapServers = "localhost:9092";

        // Kafka properties
        Properties properties = new Properties();
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, "string-consumer-group");
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

        // Kafka consumer
        return new KafkaConsumer<>(properties);
    }
}