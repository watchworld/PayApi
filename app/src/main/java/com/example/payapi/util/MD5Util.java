package com.example.payapi.util;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


/**
 * 生成�?个MD5�?
 * 
 * @author Administrator
 *
 */
public class MD5Util {

    public static String encodePassword(String password) {
        if (password == null || password == "") {
            return password;
        }
        try {
			return getMD5(password.getBytes("utf-8"));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "";
		}
    }

    public static String getMD5(byte[] source) {
        String s = null;
        char hexDigits[] = { // 用来将字节转换成 16 进制表示的字�?
                '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
            md.update(source);
            byte tmp[] = md.digest(); // MD5 的计算结果是�?�? 128 位的长整数，
            // 用字节表示就�? 16 个字�?
            char str[] = new char[16 * 2]; // 每个字节�? 16 进制表示的话，使用两个字符，
            // �?以表示成 16 进制�?�? 32 个字�?
            int k = 0; // 表示转换结果中对应的字符位置
            for (int i = 0; i < 16; i++) { // 从第�?个字节开始，�? MD5 的每�?个字�?
                // 转换�? 16 进制字符的转�?
                byte byte0 = tmp[i]; // 取第 i 个字�?
                str[k++] = hexDigits[byte0 >>> 4 & 0xf]; // 取字节中�? 4 位的数字转换,
                // >>> 为�?�辑右移，将符号位一起右�?
                str[k++] = hexDigits[byte0 & 0xf]; // 取字节中�? 4 位的数字转换
            }
            s = new String(str); // 换后的结果转换为字符�?

        } catch (Exception e) {
            e.printStackTrace();
        }
        return s;
    }

    public final static String MD5(String inputStr) {
        // 用于加密的字�?
        char md5String[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
        try {
            // 使用utf-8字符集将�? String 编码�? byte序列，并将结果存储到�?个新�? byte数组�?
            byte[] btInput = inputStr.getBytes("utf-8");

            // 信息摘要是安全的单向哈希函数，它接收任意大小的数据，并输出固定长度的哈希值�??
            MessageDigest mdInst = MessageDigest.getInstance("MD5");

            // MessageDigest对象通过使用 update方法处理数据�? 使用指定的byte数组更新摘要
            mdInst.update(btInput);

            // 摘要更新之后，�?�过调用digest（）执行哈希计算，获得密�?
            byte[] md = mdInst.digest();

            // 把密文转换成十六进制的字符串形式
            int j = md.length;
            char str[] = new char[j * 2];
            int k = 0;
            for (int i = 0; i < j; i++) { // i = 0
                byte byte0 = md[i]; // 95
                str[k++] = md5String[byte0 >>> 4 & 0xf]; // 5
                str[k++] = md5String[byte0 & 0xf]; // F
            }

            // 返回经过加密后的字符�?
            return new String(str);

        } catch (Exception e) {
            return null;
        }
    }
    
	public static String encryption(String plain) {
		String re_md5 = new String();
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			try {
				md.update(plain.getBytes("utf-8"));
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			byte b[] = md.digest();

			int i;

			StringBuffer buf = new StringBuffer("");
			for (int offset = 0; offset < b.length; offset++) {
				i = b[offset];
				if (i < 0)
					i += 256;
				if (i < 16)
					buf.append("0");
				buf.append(Integer.toHexString(i));
			}

			re_md5 = buf.toString();

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return re_md5;
	}
}