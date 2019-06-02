import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessageUnpacker;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class TestSpeed {

    static byte[] data;

    public static void main(String[] args) throws IOException {
        File jpegFile = new File("/home/anton/large.jpg");
        BufferedImage image = ImageIO.read(jpegFile);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", outputStream);
        data = outputStream.toByteArray();
        System.out.println(data.length);

       /* long startTime = System.currentTimeMillis();
        for (int i = 0; i < 1000; i++) {
            byteBufferWork();
        }
        System.out.println("ByteBuffer: "+(System.currentTimeMillis()-startTime));

        startTime = System.currentTimeMillis();

        for (int i = 0; i < 1000; i++) {
            msgPackWork();
        }

        System.out.println("MsgPack: "+(System.currentTimeMillis()-startTime));

        startTime = System.currentTimeMillis();

        for (int i = 0; i < 1000; i++) {
            pureByteArrayWork();
        }

        System.out.println("pureByteArrayWork: "+(System.currentTimeMillis()-startTime));*/

        ByteArrayInputStream bis = new ByteArrayInputStream(pureByteArrayWork());
        BufferedImage bImage2 = ImageIO.read(bis);
        ImageIO.write(bImage2, "jpg", new File("output.jpg") );
    }

    static void byteBufferWork(){
        ByteBuffer buffer = ByteBuffer.allocate(data.length+12);
        buffer.putInt(1);
        buffer.putInt(2);
        buffer.putInt(data.length);
        buffer.put(data);
        //System.out.println(buffer.array().length);
        ByteBuffer buffer1 = ByteBuffer.wrap(buffer.array());
        int int1 = buffer1.getInt();
        int int2 = buffer1.getInt();
        int int3 = buffer1.getInt();
        byte[] arr = new byte[int3];
        buffer1.get(arr);

        //System.out.println(int1+" "+int2+" "+int3);

    }

    static void msgPackWork() throws IOException {

        MessageBufferPacker packer = MessagePack.newDefaultBufferPacker();
        packer.packInt(1);
        packer.packInt(2);

        packer.packBinaryHeader(data.length);
        packer.writePayload(data);
        packer.close();

        MessageUnpacker unpacker = MessagePack.newDefaultUnpacker(packer.toByteArray());

        int int1 = unpacker.unpackInt();
        int int2 = unpacker.unpackInt();
        int int3 = unpacker.unpackBinaryHeader();
        byte[] arr = unpacker.readPayload(int3);
    }

    static byte[] pureByteArrayWork(){
        byte[] result = new byte[data.length+12];
        System.arraycopy(toBytes(1),0,result,0,4);
        System.arraycopy(toBytes(2), 0, result, 4, 4);
        System.arraycopy(toBytes(data.length), 0, result, 8, 4);
        System.arraycopy(data,0,result,12, data.length);

        byte[] result1 = new byte[4];

        System.arraycopy(result,0,result1,0,4);
        int int1 = fromByte(result1);
        System.arraycopy(result, 4, result1, 0, 4);
        int int2 = fromByte(result1);
        System.arraycopy(result, 8, result1, 0, 4);
        int int3 = fromByte(result1);

        System.out.println(int1+" "+int2+" "+int3);

        byte[] resultData = new byte[int3];
        System.arraycopy(result, 12, resultData, 0, int3);
        return resultData;
    }

    static byte[] toBytes(int i)
    {
        byte[] result = new byte[4];

        result[0] = (byte) (i >> 24);
        result[1] = (byte) (i >> 16);
        result[2] = (byte) (i >> 8);
        result[3] = (byte) (i /*>> 0*/);

        return result;
    }

    static int fromByte(byte[] bytes) {
        return bytes[0] << 24 | (bytes[1] & 0xFF) << 16 | (bytes[2] & 0xFF) << 8 | (bytes[3] & 0xFF);
    }


}
