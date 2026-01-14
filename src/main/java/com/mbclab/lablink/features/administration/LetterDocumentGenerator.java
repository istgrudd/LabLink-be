package com.mbclab.lablink.features.administration;

import lombok.RequiredArgsConstructor;
import org.apache.poi.xwpf.usermodel.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;

/**
 * Service untuk generate dokumen Word dari template.
 * Template disimpan di resources/templates/letters/
 * 
 * Placeholder yang didukung:
 * - {{NOMOR_SURAT}}
 * - {{TANGGAL}}
 * - {{PERIHAL}}
 * - {{TUJUAN}}
 * - {{ISI_SURAT}}
 * - {{LAMPIRAN}}
 * - {{PEMBUAT}}
 */
@Service
@RequiredArgsConstructor
public class LetterDocumentGenerator {

    private final LetterNumberGenerator letterNumberGenerator;

    private static final DateTimeFormatter DATE_FORMATTER = 
            DateTimeFormatter.ofPattern("d MMMM yyyy", new Locale("id", "ID"));

    /**
     * Generate dokumen Word dengan placeholder terisi.
     * 
     * @param templateName Nama template (tanpa extension), e.g., "surat_peminjaman"
     * @param letterType Jenis surat (PMJ, IZN, dll)
     * @param category Kategori (RK, INT, EXT, WSH)
     * @param data Map berisi data untuk mengisi placeholder
     * @return ByteArrayOutputStream berisi dokumen Word
     */
    public byte[] generateDocument(
            String templateName,
            String letterType,
            String category,
            Map<String, String> data) throws IOException {
        
        // Generate nomor surat
        LocalDate issueDate = LocalDate.now();
        String letterNumber = letterNumberGenerator.generate(letterType, category, issueDate);
        String formattedDate = issueDate.format(DATE_FORMATTER);
        
        // Load template
        String templatePath = "templates/letters/" + templateName + ".docx";
        ClassPathResource resource = new ClassPathResource(templatePath);
        
        if (!resource.exists()) {
            throw new RuntimeException("Template tidak ditemukan: " + templatePath);
        }
        
        try (InputStream is = resource.getInputStream();
             XWPFDocument document = new XWPFDocument(is);
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            
            // Replace placeholders in paragraphs
            for (XWPFParagraph paragraph : document.getParagraphs()) {
                replacePlaceholdersInParagraph(paragraph, letterNumber, formattedDate, data);
            }
            
            // Replace placeholders in tables
            for (XWPFTable table : document.getTables()) {
                for (XWPFTableRow row : table.getRows()) {
                    for (XWPFTableCell cell : row.getTableCells()) {
                        for (XWPFParagraph paragraph : cell.getParagraphs()) {
                            replacePlaceholdersInParagraph(paragraph, letterNumber, formattedDate, data);
                        }
                    }
                }
            }
            
            document.write(out);
            return out.toByteArray();
        }
    }

    private void replacePlaceholdersInParagraph(
            XWPFParagraph paragraph,
            String letterNumber,
            String formattedDate,
            Map<String, String> data) {
        
        for (XWPFRun run : paragraph.getRuns()) {
            String text = run.getText(0);
            if (text != null) {
                // Replace built-in placeholders
                text = text.replace("{{NOMOR_SURAT}}", letterNumber);
                text = text.replace("{{TANGGAL}}", formattedDate);
                
                // Replace custom placeholders from data map
                for (Map.Entry<String, String> entry : data.entrySet()) {
                    String placeholder = "{{" + entry.getKey().toUpperCase() + "}}";
                    String value = entry.getValue() != null ? entry.getValue() : "";
                    text = text.replace(placeholder, value);
                }
                
                run.setText(text, 0);
            }
        }
    }

    /**
     * Get list of available templates.
     */
    public String[] getAvailableTemplates() {
        return new String[]{
            "surat_peminjaman",
            "surat_undangan",
            "surat_perizinan",
            "surat_peringatan"
        };
    }
}
