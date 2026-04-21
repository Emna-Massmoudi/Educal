package com.elearning.elearning_api.service;

import com.elearning.elearning_api.entity.Certificat;
import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;

@Service
public class CertificatPdfService {

    private static final Charset PDF_CHARSET = Charset.forName("windows-1252");
    private static final float PAGE_WIDTH = 595f;
    private static final float PAGE_HEIGHT = 842f;
    private static final float SHEET_X = 46f;
    private static final float SHEET_Y = 72f;
    private static final float SHEET_WIDTH = 503f;
    private static final float SHEET_HEIGHT = 698f;

    public byte[] generate(Certificat certificat) {
        StringBuilder content = new StringBuilder();

        appendFilledRectangle(content, 0f, 0f, PAGE_WIDTH, PAGE_HEIGHT, 0.97f, 0.98f, 1f);
        appendFilledRectangle(content, SHEET_X, SHEET_Y, SHEET_WIDTH, SHEET_HEIGHT, 1f, 1f, 1f);
        appendStrokedRectangle(content, SHEET_X, SHEET_Y, SHEET_WIDTH, SHEET_HEIGHT, 4f, 0.96f, 0.69f, 0.18f);
        appendFilledRectangle(content, 82f, 694f, 431f, 28f, 0.17f, 0.31f, 0.95f);

        appendCenteredText(content, "F1", 11f, 708f, "CERTIFICAT DE REUSSITE", 1f, 1f, 1f);
        appendCenteredText(content, "F2", 34f, 646f, "EduNet", 0.06f, 0.10f, 0.24f);
        appendCenteredText(content, "F1", 21f, 612f, "Attestation de completion", 0.17f, 0.19f, 0.27f);

        appendCenteredText(content, "F1", 13f, 560f,
                "Ce document confirme que l apprenant a complete avec succes", 0.37f, 0.43f, 0.53f);
        appendCenteredText(content, "F1", 13f, 540f,
                "le parcours de formation et satisfait les exigences de validation.", 0.37f, 0.43f, 0.53f);

        List<String> studentLines = wrapText(safeText(certificat.getEtudiant().getNom()), 26);
        float studentY = 482f;
        for (String line : studentLines) {
            appendCenteredText(content, "F2", 26f, studentY, line, 0.17f, 0.31f, 0.95f);
            studentY -= 30f;
        }

        appendCenteredText(content, "F1", 14f, studentY - 8f, "a valide avec succes la formation", 0.37f, 0.43f, 0.53f);

        List<String> courseLines = wrapText(safeText(certificat.getCours().getTitre()), 38);
        float courseY = studentY - 58f;
        for (String line : courseLines) {
            appendCenteredText(content, "F2", 22f, courseY, line, 0.10f, 0.14f, 0.25f);
            courseY -= 26f;
        }

        appendLine(content, 150f, 248f, 445f, 248f, 1.4f, 0.87f, 0.91f, 0.96f);
        appendCenteredText(content, "F1", 12f, 220f,
                "Code de verification: " + safeText(certificat.getCode()), 0.35f, 0.40f, 0.49f);
        appendCenteredText(content, "F1", 12f, 198f,
                "Date d emission: " + formatDate(certificat.getDateObtention()), 0.35f, 0.40f, 0.49f);
        appendCenteredText(content, "F1", 12f, 150f,
                "Plateforme EduNet - Formation en ligne", 0.35f, 0.40f, 0.49f);

        return buildPdf(content.toString());
    }

    public String buildFileName(Certificat certificat) {
        String titleSlug = slugify(certificat.getCours().getTitre());
        return "certificat-edunet-" + titleSlug + "-" + safeText(certificat.getCode()).toLowerCase(Locale.ROOT) + ".pdf";
    }

    private byte[] buildPdf(String contentStream) {
        byte[] contentBytes = toPdfBytes(contentStream);
        List<byte[]> objects = List.of(
                toPdfBytes("1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n"),
                toPdfBytes("2 0 obj\n<< /Type /Pages /Count 1 /Kids [3 0 R] >>\nendobj\n"),
                toPdfBytes(
                        "3 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] /Resources << /Font << /F1 4 0 R /F2 5 0 R >> >> /Contents 6 0 R >>\nendobj\n"),
                toPdfBytes("4 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica /Encoding /WinAnsiEncoding >>\nendobj\n"),
                toPdfBytes("5 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica-Bold /Encoding /WinAnsiEncoding >>\nendobj\n"),
                toPdfBytes("6 0 obj\n<< /Length " + contentBytes.length + " >>\nstream\n" + contentStream + "\nendstream\nendobj\n"));

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        List<Integer> offsets = new ArrayList<>();
        writeBytes(output, toPdfBytes("%PDF-1.4\n%\u00E2\u00E3\u00CF\u00D3\n"));
        offsets.add(0);

        for (byte[] object : objects) {
            offsets.add(output.size());
            writeBytes(output, object);
        }

        int xrefOffset = output.size();
        writeBytes(output, toPdfBytes("xref\n0 " + (objects.size() + 1) + "\n"));
        writeBytes(output, toPdfBytes("0000000000 65535 f \n"));
        for (int index = 1; index < offsets.size(); index++) {
            writeBytes(output, toPdfBytes(String.format(Locale.ROOT, "%010d 00000 n \n", offsets.get(index))));
        }
        writeBytes(output, toPdfBytes("trailer\n<< /Size " + (objects.size() + 1) + " /Root 1 0 R >>\n"));
        writeBytes(output, toPdfBytes("startxref\n" + xrefOffset + "\n%%EOF"));

        return output.toByteArray();
    }

    private void appendFilledRectangle(StringBuilder builder, float x, float y, float width, float height,
                                       float red, float green, float blue) {
        builder.append(pdfNumber(red)).append(' ')
                .append(pdfNumber(green)).append(' ')
                .append(pdfNumber(blue)).append(" rg\n")
                .append(pdfNumber(x)).append(' ')
                .append(pdfNumber(y)).append(' ')
                .append(pdfNumber(width)).append(' ')
                .append(pdfNumber(height)).append(" re\nf\n");
    }

    private void appendStrokedRectangle(StringBuilder builder, float x, float y, float width, float height,
                                        float strokeWidth, float red, float green, float blue) {
        builder.append(pdfNumber(red)).append(' ')
                .append(pdfNumber(green)).append(' ')
                .append(pdfNumber(blue)).append(" RG\n")
                .append(pdfNumber(strokeWidth)).append(" w\n")
                .append(pdfNumber(x)).append(' ')
                .append(pdfNumber(y)).append(' ')
                .append(pdfNumber(width)).append(' ')
                .append(pdfNumber(height)).append(" re\nS\n");
    }

    private void appendLine(StringBuilder builder, float startX, float startY, float endX, float endY,
                            float strokeWidth, float red, float green, float blue) {
        builder.append(pdfNumber(red)).append(' ')
                .append(pdfNumber(green)).append(' ')
                .append(pdfNumber(blue)).append(" RG\n")
                .append(pdfNumber(strokeWidth)).append(" w\n")
                .append(pdfNumber(startX)).append(' ')
                .append(pdfNumber(startY)).append(" m\n")
                .append(pdfNumber(endX)).append(' ')
                .append(pdfNumber(endY)).append(" l\nS\n");
    }

    private void appendCenteredText(StringBuilder builder, String fontName, float fontSize, float y, String text,
                                    float red, float green, float blue) {
        float x = (PAGE_WIDTH - estimateTextWidth(text, fontSize, "F2".equals(fontName))) / 2f;
        appendText(builder, fontName, fontSize, x, y, text, red, green, blue);
    }

    private void appendText(StringBuilder builder, String fontName, float fontSize, float x, float y, String text,
                            float red, float green, float blue) {
        builder.append("BT\n/")
                .append(fontName)
                .append(' ')
                .append(pdfNumber(fontSize))
                .append(" Tf\n")
                .append(pdfNumber(red)).append(' ')
                .append(pdfNumber(green)).append(' ')
                .append(pdfNumber(blue)).append(" rg\n1 0 0 1 ")
                .append(pdfNumber(x)).append(' ')
                .append(pdfNumber(y)).append(" Tm\n(")
                .append(escapePdfText(text))
                .append(") Tj\nET\n");
    }

    private List<String> wrapText(String text, int maxCharacters) {
        String normalized = safeText(text).replaceAll("\\s+", " ").trim();
        if (normalized.isEmpty()) {
            return List.of("");
        }

        List<String> lines = new ArrayList<>();
        StringBuilder currentLine = new StringBuilder();

        for (String word : normalized.split(" ")) {
            if (currentLine.isEmpty()) {
                currentLine.append(word);
                continue;
            }

            if (currentLine.length() + word.length() + 1 <= maxCharacters) {
                currentLine.append(' ').append(word);
                continue;
            }

            lines.add(currentLine.toString());
            currentLine = new StringBuilder(word);
        }

        if (!currentLine.isEmpty()) {
            lines.add(currentLine.toString());
        }

        return lines;
    }

    private float estimateTextWidth(String text, float fontSize, boolean bold) {
        float factor = bold ? 0.56f : 0.51f;
        return safeText(text).length() * fontSize * factor;
    }

    private String formatDate(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "Date indisponible";
        }

        String[] months = {
                "janvier", "fevrier", "mars", "avril", "mai", "juin",
                "juillet", "aout", "septembre", "octobre", "novembre", "decembre"
        };

        return String.format(
                Locale.ROOT,
                "%02d %s %d",
                dateTime.getDayOfMonth(),
                months[dateTime.getMonthValue() - 1],
                dateTime.getYear()
        );
    }

    private String slugify(String value) {
        String normalized = Normalizer.normalize(safeText(value), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
        return normalized.isBlank() ? "formation" : normalized;
    }

    private String escapePdfText(String value) {
        return safeText(value)
                .replace("\\", "\\\\")
                .replace("(", "\\(")
                .replace(")", "\\)");
    }

    private String safeText(String value) {
        return value == null ? "" : value.replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", " ").trim();
    }

    private String pdfNumber(float value) {
        String number = String.format(Locale.US, "%.2f", value);
        return number.replaceAll("0+$", "").replaceAll("\\.$", "");
    }

    private byte[] toPdfBytes(String value) {
        return value.getBytes(PDF_CHARSET);
    }

    private void writeBytes(ByteArrayOutputStream output, byte[] bytes) {
        output.write(bytes, 0, bytes.length);
    }
}
