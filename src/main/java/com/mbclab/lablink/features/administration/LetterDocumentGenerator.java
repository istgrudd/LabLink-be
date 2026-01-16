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
        
        // 1. Attempt standard run-by-run replacement
        for (XWPFRun run : paragraph.getRuns()) {
            String text = run.getText(0);
            if (text != null) {
                text = replaceText(text, letterNumber, formattedDate, data);
                run.setText(text, 0);
            }
        }

        // 2. Check if placeholders still exist (Split Run Issue)
        // If the paragraph text still contains "${", it means the placeholder was split across runs
        String fullText = paragraph.getText();
        if (fullText != null && fullText.contains("${")) {
            // Apply replacement on the full text
            String replacedText = replaceText(fullText, letterNumber, formattedDate, data);
            
            // If the text actually changed, it means we fixed a split run
            if (!replacedText.equals(fullText)) {
                // We need to rebuild the paragraph to fix the split runs
                // Strategy: Capture style of the first run, clear all, add new run with full text
                
                // Capture format from first run if available
                String fontFamily = "Times New Roman";
                double fontSize = 12.0;
                boolean isBold = false;
                boolean isItalic = false;
                
                if (!paragraph.getRuns().isEmpty()) {
                    XWPFRun first = paragraph.getRuns().get(0);
                    if (first.getFontFamily() != null) fontFamily = first.getFontFamily();
                    if (first.getFontSizeAsDouble() != null) fontSize = first.getFontSizeAsDouble();
                    isBold = first.isBold();
                    isItalic = first.isItalic();
                }

                // Remove all existing runs
                // Loop backwards to avoid index shifting issues
                for (int i = paragraph.getRuns().size() - 1; i >= 0; i--) {
                    paragraph.removeRun(i);
                }

                // Create new single run
                XWPFRun newRun = paragraph.createRun();
                newRun.setText(replacedText);
                newRun.setFontFamily(fontFamily);
                newRun.setFontSize(fontSize);
                newRun.setBold(isBold);
                newRun.setItalic(isItalic);
            }
        }
    }

    private String replaceText(String text, String letterNumber, String formattedDate, Map<String, String> data) {
        // Replace built-in placeholders
        text = text.replace("${nomor_surat}", letterNumber);
        text = text.replace("${tanggal_surat}", formattedDate);
        text = text.replace("${nama_pemohon}", safeGet(data.get("nama_pemohon")));
        text = text.replace("${nim_pemohon}", safeGet(data.get("nim_pemohon")));
        text = text.replace("${waktu_mulai}", safeGet(data.get("waktu_mulai")));
        text = text.replace("${waktu_selesai}", safeGet(data.get("waktu_selesai")));
        text = text.replace("${nama_kegiatan}", safeGet(data.get("nama_kegiatan")));
        
        // Replace custom placeholders
        for (Map.Entry<String, String> entry : data.entrySet()) {
            String placeholder = "${" + entry.getKey().toLowerCase() + "}";
            String value = safeGet(entry.getValue());
            text = text.replace(placeholder, value);
        }
        return text;
    }

    private String safeGet(String value) {
        return value != null ? value : "";
    }

    /**
     * Get list of available templates.
     */
    public String[] getAvailableTemplates() {
        return new String[]{
            "Surat Peminjaman Videotron MBC",
            "surat_peminjaman",
            "surat_undangan",
            "surat_perizinan",
            "surat_peringatan"
        };
    }
}
