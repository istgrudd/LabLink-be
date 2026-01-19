package com.mbclab.lablink.features.finance;

import com.mbclab.lablink.features.finance.dto.CategoryRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FinanceDataInitializer implements CommandLineRunner {

    private final FinanceCategoryRepository categoryRepository;
    private final FinanceService financeService;

    @Override
    public void run(String... args) throws Exception {
        if (categoryRepository.count() == 0) {
            System.out.println("Initializing Default Finance Categories...");

            // INCOME
            createCategory("Iuran Anggota", "INCOME", "Pemasukan dari iuran anggota bulanan");
            createCategory("Hibah", "INCOME", "Dana hibah penelitian atau universitas");
            createCategory("Dana Proyek", "INCOME", "Pemasukan dari proyek eksternal");
            createCategory("Sponsorship", "INCOME", "Dana dari sponsor kegiatan");
            createCategory("Pemasukan Lainnya", "INCOME", "Pemasukan lain-lain");

            // EXPENSE
            createCategory("Perlengkapan Lab", "EXPENSE", "Pembelian alat atau perlengkapan lab");
            createCategory("Server & Hosting", "EXPENSE", "Biaya sewa server, domain, dan cloud");
            createCategory("Kegiatan / Event", "EXPENSE", "Biaya operasional kegiatan atau acara");
            createCategory("Konsumsi", "EXPENSE", "Biaya konsumsi rapat atau kegiatan");
            createCategory("Transportasi", "EXPENSE", "Biaya transportasi dinas");
            createCategory("Bahan Habis Pakai", "EXPENSE", "ATK, tinta printer, dll");
            createCategory("Maintenance", "EXPENSE", "Biaya perbaikan atau perawatan aset");
            createCategory("Pengeluaran Lainnya", "EXPENSE", "Pengeluaran lain-lain");
            
            System.out.println("Finance Categories Initialized.");
        }
    }

    private void createCategory(String name, String type, String description) {
        CategoryRequest request = new CategoryRequest();
        request.setName(name);
        request.setType(type);
        request.setDescription(description);
        financeService.createCategory(request);
    }
}
