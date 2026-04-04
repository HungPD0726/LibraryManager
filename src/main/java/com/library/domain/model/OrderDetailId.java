package com.library.domain.model;

import lombok.*;
import java.io.Serializable;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode
public class OrderDetailId implements Serializable {
    private Integer orderId;
    private Integer bookId;
}
