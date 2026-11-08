package com.wynh.microservices.common;

import org.apache.avro.Schema;
import org.apache.avro.generic.*;

public final class OrderEvents {
    public static final String TOPIC = "order-confirmed";
    public static final Schema SCHEMA = new Schema.Parser().parse("""
        {"type":"record","name":"OrderConfirmed","namespace":"com.wynh.microservices.events","fields":[
          {"name":"eventId","type":"string"},{"name":"orderNumber","type":"string"},
          {"name":"customerId","type":"string"},{"name":"email","type":"string"},
          {"name":"skuCode","type":"string"},{"name":"quantity","type":"int"},
          {"name":"total","type":"string"},{"name":"occurredAt","type":"string"}
        ]}
        """);

    private OrderEvents() { }

    public static GenericRecord confirmed(String eventId, String orderNumber, String customerId, String email,
            String skuCode, int quantity, String total, String occurredAt) {
        return new GenericRecordBuilder(SCHEMA).set("eventId", eventId).set("orderNumber", orderNumber)
                .set("customerId", customerId).set("email", email).set("skuCode", skuCode)
                .set("quantity", quantity).set("total", total).set("occurredAt", occurredAt).build();
    }
}
