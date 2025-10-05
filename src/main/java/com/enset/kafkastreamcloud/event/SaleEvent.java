package com.enset.kafkastreamcloud.event;

import java.util.Date;

public record SaleEvent(String product, String customer, Date date, double amount) {
}
