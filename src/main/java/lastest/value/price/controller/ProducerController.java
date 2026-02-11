package lastest.value.price.controller;

import jakarta.annotation.Nonnull;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lastest.value.price.model.InstrumentPriceModel;
import lastest.value.price.model.Response;
import lastest.value.price.service.ProducerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * This class is a producer controller.
 * it is the entry point of all the action performed by producer.
 */
@RestController
@Validated
@RequestMapping("/action/producer")
public class ProducerController {

    @Autowired
    private ProducerService producerService;


    /**
     * This controller endpoint is used to push list of instrument prices to interim batch.
     * If upload is successful ,this return the batch ID for further action for the producer
     *
     * @param requestPayload List of Instrument
     * @return batchId
     */
    @PostMapping
    public Response uploadPrice(@RequestBody @Valid @Size(max = 1000, message = "Maximum size of batch should be 1000") List<@Valid InstrumentPriceModel> requestPayload) {
        return producerService.updatePrice(requestPayload);
    }

    /**
     * This methode return the status of the batches using the batch ID.
     * If batch ID does not exist , it means batch is either marked completed or cancelled.
     * Hence, data is discarded or not available in the interim memory.
     *
     * @param id Long
     * @return Status of the batch .
     */
    @GetMapping
    public Response getBatchStatus(@Nonnull @RequestParam("id") Long id) {
        return producerService.getBatchStatus(id);
    }

    /**
     * This is used to cancel the batch which is in the interim memory.
     * If batch ID does not exist , it means batch is either marked completed or already cancelled.
     * Hence, batch details not available in the interim memory.
     *
     * @param id Long
     * @return Response with cancelled batch id.
     */
    @DeleteMapping
    public Response batchCancel(@Nonnull @RequestParam("id") Long id) {
        return producerService.batchCancel(id);
    }

    /**
     * This is used to mark the batch completed if available in the interim memory. It moves the batch data to it latest price memory.
     * Only those instrument price is moved to latest price memory which are latest based on asof value.
     * If batch id do not exist in the interim memory. It means batch is either cancelled or already completed.
     *
     * @param id batch id
     * @return Response
     */
    @PutMapping
    public Response batchComplete(@Nonnull @RequestParam("id") Long id) {
        return producerService.batchComplete(id);
    }

}
