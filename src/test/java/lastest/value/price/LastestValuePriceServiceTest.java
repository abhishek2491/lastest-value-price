package lastest.value.price;

import lastest.value.price.model.InstrumentPriceModel;
import lastest.value.price.model.InstrumentPricePayloadModel;
import lastest.value.price.model.Response;
import lastest.value.price.repository.InstrumentPrice;
import lastest.value.price.service.ConsumerService;
import lastest.value.price.service.ProducerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;


class LastestValuePriceServiceTest {

    @Mock
    private InstrumentPrice instrumentPrice;
    @InjectMocks
    private ProducerService producerService;
    @InjectMocks
    private ConsumerService consumerService;

    Map<String, InstrumentPriceModel> dataSetup() {
        InstrumentPriceModel data1 = new InstrumentPriceModel("1", Instant.now(), new InstrumentPricePayloadModel("100"));
        InstrumentPriceModel data2 = new InstrumentPriceModel("2", Instant.now(), new InstrumentPricePayloadModel("200"));
        InstrumentPriceModel data3 = new InstrumentPriceModel("3", Instant.now(), new InstrumentPricePayloadModel("300"));
        Map<String, InstrumentPriceModel> latestprice = new ConcurrentHashMap<>();
        latestprice.put(data1.id(), data1);
        latestprice.put(data2.id(), data2);
        latestprice.put(data3.id(), data3);
        return latestprice;
    }

    @BeforeEach
    void emptyIntrim() {
        ProducerService.intrimPrice.clear();
        InstrumentPrice.setBatchId(new AtomicLong(1));
    }

    @Test
    void uploaddataToIntrim() {
        MockitoAnnotations.openMocks(this);
        Response rs = producerService.updatePrice(List.of(new InstrumentPriceModel("5", Instant.now(), new InstrumentPricePayloadModel("500")), new InstrumentPriceModel("2", Instant.now().plus(2, ChronoUnit.HOURS), new InstrumentPricePayloadModel("400"))));
        assertTrue(rs.success());
        assertEquals(1, ProducerService.intrimPrice.size());

    }

    @Test
    void cancelledBatch() {
        MockitoAnnotations.openMocks(this);
        producerService.updatePrice(List.of(new InstrumentPriceModel("5", Instant.now(), new InstrumentPricePayloadModel("500")), new InstrumentPriceModel("2", Instant.now(), new InstrumentPricePayloadModel("400"))));
        producerService.updatePrice(List.of(new InstrumentPriceModel("51", Instant.now(), new InstrumentPricePayloadModel("5001")), new InstrumentPriceModel("21", Instant.now().plus(2, ChronoUnit.HOURS), new InstrumentPricePayloadModel("4001"))));
        assertEquals(2, ProducerService.intrimPrice.size());
        producerService.batchCancel(1L);
        assertEquals(1, ProducerService.intrimPrice.size());
        producerService.batchCancel(10L);
        assertEquals(1, ProducerService.intrimPrice.size());
        assertEquals("51", ProducerService.intrimPrice.get(2L).get("51").id());
        assertEquals("21", ProducerService.intrimPrice.get(2L).get("21").id());
        producerService.batchCancel(2L);
    }

    @Test
    void getBatchStatus() {
        MockitoAnnotations.openMocks(this);
        producerService.updatePrice(List.of(new InstrumentPriceModel("5", Instant.now(), new InstrumentPricePayloadModel("500")), new InstrumentPriceModel("2", Instant.now(), new InstrumentPricePayloadModel("400"))));
        producerService.updatePrice(List.of(new InstrumentPriceModel("51", Instant.now(), new InstrumentPricePayloadModel("5001")), new InstrumentPriceModel("21", Instant.now().plus(2, ChronoUnit.HOURS), new InstrumentPricePayloadModel("4001"))));
        assertEquals(2, ProducerService.intrimPrice.size());
        Response rs = producerService.getBatchStatus(1L);
        assertTrue(rs.success());
        rs = producerService.getBatchStatus(2L);
        assertTrue(rs.success());
        rs = producerService.getBatchStatus(3L);
        assertFalse(rs.success());
    }

    @Test
    void completeBatchAndGetPrice() {
        MockitoAnnotations.openMocks(this);
        when(instrumentPrice.getLastestPrice()).thenReturn(dataSetup()).thenReturn(dataSetup());
        doNothing().when(instrumentPrice).setLastestPrice(anyMap());
        when(instrumentPrice.getLastestPrice()).thenReturn(dataSetup()).thenReturn(dataSetup());
        producerService.updatePrice(List.of(new InstrumentPriceModel("5", Instant.now(), new InstrumentPricePayloadModel("500")), new InstrumentPriceModel("2", Instant.now(), new InstrumentPricePayloadModel("400"))));
        producerService.updatePrice(List.of(new InstrumentPriceModel("51", Instant.now(), new InstrumentPricePayloadModel("5001")), new InstrumentPriceModel("21", Instant.now().plus(2, ChronoUnit.HOURS), new InstrumentPricePayloadModel("4001"))));
        assertEquals(2, ProducerService.intrimPrice.size());
        producerService.batchComplete(1L);
        assertEquals(1, ProducerService.intrimPrice.size());
        Response rs = consumerService.getLastPrice(List.of("1", "3"));
        assertEquals(2, ((List) rs.data()).size());
        rs = consumerService.getLastPrice(List.of("1", "100"));
        assertEquals(1, ((List) rs.data()).size());
    }

}
