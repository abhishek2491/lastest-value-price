package lastest.value.price.service;

import lastest.value.price.model.InstrumentPriceModel;
import lastest.value.price.model.Response;
import lastest.value.price.repository.InstrumentPrice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import static lastest.value.price.constant.InstrumentPriceConstant.*;

@Service
public class ProducerService {
    /**
     * This map saves the batch details with key as batch and values as the map of instrument deatails and its id as key.
     * This map act as interim memory for the application.
     * If batch details which are completed or cancelled also need to be persisted,
     * then we can use value as a custom object rather than Map<String, InstrumentPriceModel> which preserve the status of
     * batch as well.
     */
    public static final ConcurrentMap<Long, Map<String, InstrumentPriceModel>> intrimPrice = new ConcurrentHashMap<>();
    private static final Logger log = LoggerFactory.getLogger(ProducerService.class);

    @Autowired
    private InstrumentPrice instrumentPrice;

    public Response getBatchStatus(Long id) {
        Response rs;
        try {
            if (intrimPrice.containsKey(id)) {
                rs = new Response(true, BATCH_RUNNING_STATUS + INPROGRESS, null);
            } else {
                rs = new Response(false, STATUS_IS_NOT_INPROGRESS_STATE, null);

            }
        } catch (Exception ex) {
            log.error(BATCH_STATUS_ERROR + ex.getStackTrace());
            rs = new Response(false, BATCH_STATUS_ERROR, null);
        }
        return rs;

    }

    public Response updatePrice(List<InstrumentPriceModel> requestPayload) {
        Response rs;

        try {
            var validData = requestPayload.stream().collect(Collectors.toMap(InstrumentPriceModel::id, Function.identity()));
            var batchId = InstrumentPrice.getBatchId().getAndIncrement();
            intrimPrice.put(batchId, validData);
            rs = new Response(true, PROGRESS_BATCH_ID + batchId, null);
        } catch (Exception e) {
            log.error(ERROR_PROCESSING_DATA + e);
            rs = new Response(false, ERROR_PROCESSING_DATA, null);
        }
        return rs;

    }

    public Response batchCancel(Long id) {
        Response rs;
        try {
            if (intrimPrice.containsKey(id)) {
                intrimPrice.remove(id);
                rs = new Response(true, BATCH_CANCELLED + id, null);
            } else {
                rs = new Response(true, DO_NOT_EXIST_BATCH_ID + id, null);
            }
        } catch (Exception e) {
            log.error(ERROR_WHILE_CANCELLING_BATCH + e);
            rs = new Response(false, ERROR_WHILE_CANCELLING_BATCH, null);
        }
        return rs;
    }

    public Response batchComplete(Long id) {
        Response rs;
        try {
            if (intrimPrice.containsKey(id)) {
                var batch = intrimPrice.get(id);
                //New map is created to make sure complete batch is pushed to lastest price map.
                //To perform the operation atomically.
                var lastestPrice = new ConcurrentHashMap<>(instrumentPrice.getLastestPrice());
                for (InstrumentPriceModel priceModel : batch.values()) {
                    lastestPrice.merge(priceModel.id(), priceModel, (old, newdata) -> newdata.asOf().isAfter(old.asOf()) ? newdata : old);
                }
                instrumentPrice.setLastestPrice(lastestPrice);
                intrimPrice.remove(id);
                rs = new Response(true, COMPLETED_BATCH_ID + id, null);
            } else {
                rs = new Response(true, STATUS_IS_NOT_INPROGRESS_STATE + id, null);
            }
        } catch (Exception e) {
            log.error(ERROR_OCCURED_WHILE_COMPLETING_BATCH + e);
            rs = new Response(false, ERROR_OCCURED_WHILE_COMPLETING_BATCH, null);
        }
        return rs;
    }
}
