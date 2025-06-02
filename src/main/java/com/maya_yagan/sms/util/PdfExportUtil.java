package com.maya_yagan.sms.util;

import com.maya_yagan.sms.payment.controller.PaymentPageController;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.WritableImage;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class PdfExportUtil {

    private static final float MARGIN = 12f;
    private static final PDFont FONT_BOLD = PDType1Font.HELVETICA_BOLD;
    private static final PDFont FONT_REG  = PDType1Font.HELVETICA;

    public static void exportNodeToPdf(Node node, File file) {
        try {
            // Snapshot the node
            WritableImage fxImage = node.snapshot(new SnapshotParameters(), null);
            BufferedImage image = SwingFXUtils.fromFXImage(fxImage, null);

            // Create PDF document
            try (PDDocument doc = new PDDocument()) {
                PDPage page = new PDPage(new PDRectangle(image.getWidth(), image.getHeight()));
                doc.addPage(page);

                var pdfImage = LosslessFactory.createFromImage(doc, image);
                try (PDPageContentStream contentStream = new PDPageContentStream(doc, page)) {
                    contentStream.drawImage(pdfImage, 0, 0);
                }

                doc.save(file);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void exportReceiptToPdf(PaymentPageController c, File file) {

        try (PDDocument doc = new PDDocument()) {

            // ---------- 1) snapshot of the items grid (full height!) ----------
            BufferedImage itemsImg = snapshotNode(c.getItemsScrollPane().getContent());

            // ---------- 2) snapshot of the barcode image ----------
            BufferedImage barcodeImg = SwingFXUtils.fromFXImage(
                    c.getBarcodeImageView().getImage(), null);

            // ---------- 3) prepare a small “till roll” page ----------
            float pageWidthPx  = 280f;          // ~ 72 dpi → 3.9 inch
            float pageHeightPx = 800f;          // will extend if we need more
            PDPage page = new PDPage(new PDRectangle(pageWidthPx, pageHeightPx));
            doc.addPage(page);

            float y = pageHeightPx - MARGIN;    // start at top
            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {

                // ---------- HEADER ----------
                y = drawCenteredText(cs, FONT_BOLD, 14,
                        c.getMarketNameLabel().getText(), pageWidthPx, y);

                y = drawMultiline(cs, FONT_REG, 10,
                        c.getAddressText().getText(),      pageWidthPx, y);
                y = drawMultiline(cs, FONT_REG, 10,
                        "Phone: " + c.getPhoneLabel().getText(),
                        pageWidthPx, y);

                y = drawKeyValue(cs, "Date",        c.getDateLabel().getText(),          y);
                y = drawKeyValue(cs, "Receipt No",  c.getReceiptNumberLabel().getText(), y);
                y = drawKeyValue(cs, "Cashier",     c.getEmployeeNameLabel().getText(),  y);

                y = drawSeparator(cs, pageWidthPx, y);

                // ---------- ITEMS ----------
                PDImageXObject itemsX = LosslessFactory.createFromImage(doc, itemsImg);
                float scale = (pageWidthPx - 2 * MARGIN) / itemsImg.getWidth();
                float h = itemsImg.getHeight() * scale;
                cs.drawImage(itemsX, MARGIN, y - h, itemsImg.getWidth() * scale, h);
                y -= h + 6;

                y = drawSeparator(cs, pageWidthPx, y);

                // ---------- TOTALS ----------
                y = drawKeyValue(cs, "Sub-Total",
                        c.getSubtotalLabel().getText(), y);
                y = drawKeyValue(cs, "Tax",
                        c.getTaxLabel().getText(), y);
                y = drawKeyValue(cs, "TOTAL",
                        c.getTotalCostLabel().getText(),
                        y, FONT_BOLD, 12);

                // ---------- Payment ----------
                y = drawKeyValue(cs, "Paid Amount",
                        c.getPaidAmountLabel().getText(), y);
                y = drawKeyValue(cs, "Change Given",
                        c.getChangeGivenLabel().getText(), y);
                y = drawKeyValue(cs, "Payment Method",
                        c.getPaymentMethodLabel().getText(), y);
                y = drawKeyValue(cs, "Receipt Status",
                        c.getReceiptStatusLabel().getText(), y);

                y = drawSeparator(cs, pageWidthPx, y);

                // ---------- BARCODE ----------
                PDImageXObject codeX = LosslessFactory.createFromImage(doc, barcodeImg);
                scale = (pageWidthPx - 2 * MARGIN) / barcodeImg.getWidth();
                h = barcodeImg.getHeight() * scale;
                cs.drawImage(codeX, MARGIN, y - h, barcodeImg.getWidth() * scale, h);
            }

            doc.save(file);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /* ------------------------------------------------- helpers — keep private */

    private static BufferedImage snapshotNode(Node n) {
        n.applyCss();
        if (n instanceof Parent p) { p.layout(); }
        WritableImage wi = new WritableImage(
                (int)Math.ceil(n.prefWidth(-1)),
                (int)Math.ceil(n.prefHeight(-1)));
        return SwingFXUtils.fromFXImage(n.snapshot(null, wi), null);
    }

    private static float drawCenteredText(PDPageContentStream cs, PDFont f, int size,
                                          String txt, float pageW, float y) throws IOException {
        cs.beginText();
        cs.setFont(f, size);
        float textW = f.getStringWidth(txt) / 1000 * size;
        cs.newLineAtOffset((pageW - textW) / 2, y);
        cs.showText(txt);
        cs.endText();
        return y - size - 2;
    }

    private static float drawMultiline(PDPageContentStream cs, PDFont f, int size,
                                       String txt, float pageW, float y) throws IOException {
        for (String line : txt.split("\n")) {
            cs.beginText();
            cs.setFont(f, size);
            cs.newLineAtOffset(MARGIN, y);
            cs.showText(line);
            cs.endText();
            y -= size + 2;
        }
        return y;
    }

    private static float drawKeyValue(PDPageContentStream cs, String key, String value,
                                      float y) throws IOException {
        return drawKeyValue(cs, key, value, y, FONT_REG, 10);
    }

    private static float drawKeyValue(PDPageContentStream cs, String key, String value,
                                      float y, PDFont font, int size) throws IOException {
        cs.beginText();
        cs.setFont(font, size);
        cs.newLineAtOffset(MARGIN, y);
        cs.showText(String.format("%-12s %s", key + ':', value));
        cs.endText();
        return y - size - 2;
    }

    private static float drawSeparator(PDPageContentStream cs, float pageW, float y) throws IOException {
        cs.moveTo(MARGIN, y);
        cs.lineTo(pageW - MARGIN, y);
        cs.stroke();
        return y - 10;
    }
}
