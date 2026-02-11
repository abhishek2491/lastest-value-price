package lastest.value.price.model;

import jakarta.annotation.Nonnull;
import jakarta.validation.constraints.NotBlank;

public record InstrumentPricePayloadModel (@NotBlank String price) {

}