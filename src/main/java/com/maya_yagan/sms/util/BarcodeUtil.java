package com.maya_yagan.sms.util;
import com.google.zxing.*;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.oned.Code128Writer;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

import java.awt.image.BufferedImage;

public final class BarcodeUtil {

    private static final int WIDTH  = 300;   // px
    private static final int HEIGHT = 100;   // px

    private BarcodeUtil() {}

    public static Image code128(String data) throws WriterException {
        BitMatrix matrix = new Code128Writer()
                .encode(data, BarcodeFormat.CODE_128, WIDTH, HEIGHT);
        BufferedImage buffered = MatrixToImageWriter.toBufferedImage(matrix);
        return SwingFXUtils.toFXImage(buffered, null);
    }
}