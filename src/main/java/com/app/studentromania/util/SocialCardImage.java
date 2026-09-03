package com.app.studentromania.util;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import javax.imageio.ImageIO;

/**
 * Turns an arbitrary faculty cover into a 1200x630 PNG suitable for an
 * {@code og:image}. Facebook / Messenger only render the large link card when the
 * image is close to 1.91:1 and at least 200px on the short side; the stored
 * covers are wide, ~340px-tall banners, so pasted faculty links either showed a
 * tiny cropped strip or no image at all. Here the source is letterboxed onto a
 * cream 1200x630 canvas (the site background) with the brand accent bar, giving a
 * predictable size we can also declare via {@code og:image:width/height}.
 */
public final class SocialCardImage {

    public static final int WIDTH = 1200;
    public static final int HEIGHT = 630;

    private static final Color BG = new Color(0xFF, 0xFC, 0xF5);
    private static final Color ACCENT = new Color(0xEB, 0x5E, 0x28);
    private static final int ACCENT_BAR = 14;

    private SocialCardImage() {
    }

    /**
     * @param source raw bytes of the cover image (png/jpg)
     * @return a 1200x630 PNG, or {@code null} if the source could not be decoded
     *         (the caller falls back to the generic share image)
     */
    public static byte[] render(byte[] source) {
        if (source == null || source.length == 0) {
            return null;
        }
        try {
            BufferedImage src = ImageIO.read(new ByteArrayInputStream(source));
            if (src == null) {
                return null;
            }

            BufferedImage out = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = out.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

            g.setColor(BG);
            g.fillRect(0, 0, WIDTH, HEIGHT);

            // Contain-fit the source, centered, leaving cream letterbox bars.
            double scale = Math.min((double) WIDTH / src.getWidth(), (double) HEIGHT / src.getHeight());
            int drawW = (int) Math.round(src.getWidth() * scale);
            int drawH = (int) Math.round(src.getHeight() * scale);
            int x = (WIDTH - drawW) / 2;
            int y = (HEIGHT - drawH) / 2;
            g.drawImage(src, x, y, drawW, drawH, null);

            g.setColor(ACCENT);
            g.fillRect(0, HEIGHT - ACCENT_BAR, WIDTH, ACCENT_BAR);
            g.dispose();

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ImageIO.write(out, "png", bos);
            return bos.toByteArray();
        } catch (Exception e) {
            return null;
        }
    }
}
