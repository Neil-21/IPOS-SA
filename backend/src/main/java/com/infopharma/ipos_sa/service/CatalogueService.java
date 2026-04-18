package com.infopharma.ipos_sa.service;

/**
 * CatalogueService
 * Business logic contract for the product catalogue. Implementations handle
 * CRUD for {@link com.infopharma.ipos_sa.entity.CatalogueItem}, keyword search,
 * stock additions (recording a {@link com.infopharma.ipos_sa.entity.StockDelivery}),
 * and generation of the low-stock report.
 */
import com.infopharma.ipos_sa.dto.LowStockReportItem;
import com.infopharma.ipos_sa.dto.StockAddRequest;
import com.infopharma.ipos_sa.entity.CatalogueItem;

import java.util.List;
import java.util.Optional;

public interface CatalogueService {
    CatalogueItem addItem(CatalogueItem item);
    CatalogueItem update(String itemId, CatalogueItem item);
    void delete(String itemId);
    Optional<CatalogueItem> findById(String itemId);
    List<CatalogueItem> findAll();
    List<CatalogueItem> search(String keyword);
    void addStock(String itemId, StockAddRequest request);
    List<LowStockReportItem> getLowStockReport();
}
