package lastest.value.price.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lastest.value.price.model.Response;
import lastest.value.price.service.ConsumerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequestMapping("/action/consumer")
public class ConsumerController {
    @Autowired
    private ConsumerService consumerService;

    /**
     * This endpoint is used to get the price of the instruments which are present in latest price memory.
     *
     * @param idlist list of ids
     * @return response with the instrument prices.
     */
    @PostMapping
    public Response getLastPrice(@RequestBody @Valid List<@NotBlank String> idlist) {

        return consumerService.getLastPrice(idlist);

    }


}
