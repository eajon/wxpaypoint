package cn.csfz.wxpaypoint.util;

import android.graphics.Bitmap;
import android.graphics.Canvas;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.util.Hashtable;

public class QrCodeUtil2 {


//生成二维码开始

    /**
     * 生成二维码图片
     *
     * @param content         内容
     * @param width           要生成的二维码宽度
     * @param height          要生成的二维码高度
     * @param logoBmp         logo图片
     * @param margin          边框的值
     * @return 生成的二维码
     */
    public static Bitmap writeQRImage(String content, int width, int height
            , Bitmap logoBmp, int margin) {
        Hashtable<EncodeHintType, String> hints = new Hashtable<EncodeHintType, String>();
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        // 图像数据转换，使用了矩阵转换
        BitMatrix bitMatrix = null;
        Bitmap bitmap = null;
        try {
            bitMatrix = new QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, width, height, hints);
            //更改边框大小，更改后需要重新计算宽高
            bitMatrix = updateBit(bitMatrix, margin);
            width = bitMatrix.getWidth();
            height = bitMatrix.getHeight();

            int[] pixels = new int[width * height];
            // 按照二维码的算法，逐个生成二维码的图片，两个for循环是图片横列扫描的结果
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    if (bitMatrix.get(x, y)) {
                        pixels[y * width + x] = 0xff000000;
                    } else//其他的地方为白色
                        pixels[y * width + x] = 0xffffffff;
                }
            }
            // 生成二维码图片的格式，使用ARGB_8888
            bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            //设置像素矩阵的范围
            bitmap.setPixels(pixels, 0, width, 0, 0, width, height);

        } catch (WriterException e) {
            e.printStackTrace();
        }
        if (logoBmp != null && bitmap != null)
            bitmap = addLogoToQRImage(logoBmp, bitmap, width, height);

        return bitmap;
    }

    /**
     * 设置边框大小
     *
     * @param matrix
     * @param margin 边框大小
     * @return
     */
    private static BitMatrix updateBit(BitMatrix matrix, int margin) {

        int tempM = margin * 2;
        int[] rec = matrix.getEnclosingRectangle(); // 获取二维码图案的属性
        int resWidth = rec[2] + tempM;
        int resHeight = rec[3] + tempM;
        BitMatrix resMatrix = new BitMatrix(resWidth, resHeight); // 按照自定义边框生成新的BitMatrix
        resMatrix.clear();
        for (int i = margin; i < resWidth - margin; i++) { // 循环，将二维码图案绘制到新的bitMatrix中
            for (int j = margin; j < resHeight - margin; j++) {
                if (matrix.get(i - margin + rec[0], j - margin + rec[1])) {
                    resMatrix.set(i, j);
                }
            }
        }
        return resMatrix;
    }

    /**
     * 添加logo
     *
     * @param logoBmp logo
     * @param qRImage 二维码图片
     * @param width   二维码宽度
     * @param height  二维码高度
     * @return
     */
    private static Bitmap addLogoToQRImage(Bitmap logoBmp, Bitmap qRImage, int width, int height) {
        // 获取图片宽高
        int logoWidth = logoBmp.getWidth();
        int logoHeight = logoBmp.getHeight();
        if (logoWidth == 0 || logoHeight == 0) {
            return qRImage;
        }
        // 图片绘制在二维码中央，合成二维码图片
        // logo大小为二维码整体大小的1/6
        float scaleFactor = width * 1.0f / 4 / logoWidth;
        try {
            Canvas canvas = new Canvas(qRImage);
            canvas.drawBitmap(qRImage, 0, 0, null);
            canvas.scale(scaleFactor, scaleFactor, width / 2,
                    height / 2);
            canvas.drawBitmap(logoBmp, (width - logoWidth) / 2,
                    (height - logoHeight) / 2, null);
            canvas.save();
            canvas.restore();
            return qRImage;
        } catch (Exception e) {
            qRImage = null;
            e.getStackTrace();
        }
        return qRImage;
    }
}
