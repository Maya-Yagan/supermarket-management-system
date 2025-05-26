package com.maya_yagan.sms.util;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.WritableImage;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;

import java.awt.image.BufferedImage;
import java.io.File;

public class PdfExportUtil {

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
}
