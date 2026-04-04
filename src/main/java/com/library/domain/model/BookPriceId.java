package com.library.domain.model;

import lombok.*;
import java.io.Serializable;
import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode
public class BookPriceId implements Serializable {
    private Integer bookId;
    private Integer priceId;
    private LocalDate startDate;
}
