package com.library.entity;

import lombok.*;
import java.io.Serializable;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode
public class BorrowItemId implements Serializable {
    private Integer borrowId;
    private Integer bookId;
}
