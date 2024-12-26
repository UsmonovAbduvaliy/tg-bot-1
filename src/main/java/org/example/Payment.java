package org.example;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class Payment {
    private Type type;
    private Integer amount;
    private Boolean next = true;
    private Boolean have = true;
    private Boolean come = true;

    @Override
    public String toString() {
        return "Type: "+type+" -> Amount: "+amount;
    }
}
