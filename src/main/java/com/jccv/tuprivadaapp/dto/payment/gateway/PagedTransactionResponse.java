package com.jccv.tuprivadaapp.dto.payment.gateway;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * DTO para respuesta paginada de transacciones.
 * Wrapper estandarizado para paginación que facilita consumo desde frontend.
 * 
 * @author TuPrivada Development Team
 * @version 1.0
 * @since 2026-01-21
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PagedTransactionResponse {
    
    /**
     * Lista de transacciones en la página actual
     */
    private List<TransactionListItemDTO> content;
    
    /**
     * Número total de elementos en todas las páginas
     */
    private Long totalElements;
    
    /**
     * Número total de páginas disponibles
     */
    private Integer totalPages;
    
    /**
     * Número de la página actual (0-indexed)
     */
    private Integer number;
    
    /**
     * Tamaño de la página (cantidad de elementos por página)
     */
    private Integer size;
    
    /**
     * Indica si es la primera página
     */
    private Boolean first;
    
    /**
     * Indica si es la última página
     */
    private Boolean last;
    
    /**
     * Indica si tiene página siguiente
     */
    private Boolean hasNext;
    
    /**
     * Indica si tiene página anterior
     */
    private Boolean hasPrevious;
    
    /**
     * Factory method para crear DTO desde Page de Spring Data.
     * 
     * @param page Página de transacciones de Spring Data
     * @return DTO de respuesta paginada
     */
    public static PagedTransactionResponse fromPage(Page<TransactionListItemDTO> page) {
        if (page == null) {
            return PagedTransactionResponse.builder()
                    .content(List.of())
                    .totalElements(0L)
                    .totalPages(0)
                    .number(0)
                    .size(0)
                    .first(true)
                    .last(true)
                    .hasNext(false)
                    .hasPrevious(false)
                    .build();
        }
        
        return PagedTransactionResponse.builder()
                .content(page.getContent())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .number(page.getNumber())
                .size(page.getSize())
                .first(page.isFirst())
                .last(page.isLast())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .build();
    }
}
