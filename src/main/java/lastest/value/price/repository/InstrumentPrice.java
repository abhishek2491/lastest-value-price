package lastest.value.price.repository;

import lastest.value.price.model.InstrumentPriceModel;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Getter
@Setter
@Component
public class InstrumentPrice {
    private static Map<String, InstrumentPriceModel> lastestPrice = new ConcurrentHashMap<>();
    private static AtomicLong batchId = new AtomicLong(1);

    public static AtomicLong getBatchId() {
        return batchId;
    }

    public static void setBatchId(AtomicLong batchId) {
        InstrumentPrice.batchId = batchId;
    }

    public Map<String, InstrumentPriceModel> getLastestPrice() {
        return lastestPrice;
    }

    public void setLastestPrice(Map<String, InstrumentPriceModel> lastestPrice) {
        InstrumentPrice.lastestPrice = lastestPrice;
    }

}
