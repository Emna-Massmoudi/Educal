package com.elearning.elearning_api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WalletFormateurResponse {
    private Long formateurId;
    private BigDecimal totalGagne;
    private BigDecimal totalCommissionPlateforme;
    private Integer paiementsApprouves;
}
