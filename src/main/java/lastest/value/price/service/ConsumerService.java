package lastest.value.price.service;

import lastest.value.price.model.Response;
import lastest.value.price.repository.InstrumentPrice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

import static lastest.value.price.constant.InstrumentPriceConstant.*;

@Service
public class ConsumerService {
    private static final Logger log = LoggerFactory.getLogger(ConsumerService.class);
    @Autowired
    private InstrumentPrice instrumentPrice;


    public Response getLastPrice(List<String> idlist) {
        Response rs;
        //filtering & checking if string in list is not null and empty
        var list = idlist.stream().filter(s -> s != null && !s.isBlank()).toList();
        try {
            if (list.isEmpty()) {
                rs = new Response(false, DO_NOT_CONTAIN_VALID_VALUE, null);

            } else {

                var lastPrice = instrumentPrice.getLastestPrice();
                var serachedPrices = list.stream().map(id -> lastPrice.getOrDefault(id, null)).filter(Objects::nonNull).toList();
                rs = new Response(true, SUCCESSFULLY_FETCHED_PRICES, serachedPrices);
            }
        } catch (Exception e) {
            log.error(WHILE_FETCHING_THE_PRICE);
            rs = new Response(false, WHILE_FETCHING_THE_PRICE, null);
        }
        return rs;
    }
}
