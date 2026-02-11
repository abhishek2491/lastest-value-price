package lastest.value.price.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;


public record InstrumentPriceModel(@NotBlank String id, @NotNull Instant asOf,
                                   @NotNull @Valid InstrumentPricePayloadModel instrumentPricePayloadModel) {
}


