package org.peimari.maastokanta.backend;

/**
 *
 * @author Matti Tahvonen <matti@vaadin.com>
 */
public class KtUtil {

    public static String ktToShortForm(String kt) {
        String part1 = removeLeadingZeros(kt.substring(0, 3));
        String part2 = removeLeadingZeros(kt.substring(3, 6));
        String part3 = removeLeadingZeros(kt.substring(6, 10));
        String part4 = removeLeadingZeros(kt.substring(10, 14));
        return part1 + "-" + part2 + "-" + part3 + "-" + part4;
    }

    public static String ktToLongForm(String kt) {
        if (kt.contains("-")) {
            String[] split = kt.split("-");
            if (split.length != 4) {
                throw new IllegalArgumentException();
            }
            return zeroPad(split[0], 3) + zeroPad(split[1], 3) + zeroPad(
                    split[2], 4) + zeroPad(split[3], 4);
        } else if (kt.length() == 14) {
            return kt;
        } else {
            throw new IllegalArgumentException();
        }
    }

    private static  String zeroPad(String str, int lenght) {
        while (str.length() < lenght) {
            str = "0" + str;
        }
        return str;
    }

    private static  String removeLeadingZeros(String str) {
        while (str.startsWith("0")) {
            str = str.substring(1);
        }
        return str;
    }

}
