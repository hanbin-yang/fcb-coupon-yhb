package com.fcb.coupon.common.midplatform;

import com.caucho.hessian.io.*;
import org.w3c.dom.Node;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/**
*
* @author liubingpei
* @date 14:56 2021-7-22
*/
public class SoaHessianInput extends AbstractHessianInput {
    private static Field _detailMessageField;
    protected SerializerFactory _serializerFactory;
    protected ArrayList _refs;
    private InputStream _is;
    protected int _peek = -1;
    private String _method;
    private Throwable _replyFault;
    private StringBuffer _sbuf = new StringBuffer();
    private boolean _isLastChunk;
    private int _chunkLength;

    public SoaHessianInput() {
    }

    public SoaHessianInput(InputStream is, SerializerFactory factory)
    {
        this._serializerFactory = factory;
        init(is);
    }

    @Override
    public void setSerializerFactory(SerializerFactory factory) {
        this._serializerFactory = factory;
    }

    @Override
    public void init(InputStream is) {
        this._is = is;
        this._method = null;
        this._isLastChunk = true;
        this._chunkLength = 0;
        this._peek = -1;
        this._refs = null;
        this._replyFault = null;
    }
    @Override
    public String getMethod() {
        return this._method;
    }

    @Override
    public int readCall() throws IOException {
        int tag = this.read();
        if (tag != 99) {
            throw this.error("expected hessian call ('c') at " + this.codeName(tag));
        } else {
            int major = this.read();
            int minor = this.read();
            return (major << 16) + minor;
        }
    }

    @Override
    public void skipOptionalCall() throws IOException {
        int tag = this.read();
        if (tag == 99) {
            this.read();
            this.read();
        } else {
            this._peek = tag;
        }

    }
    @Override
    public String readMethod() throws IOException {
        int tag = this.read();
        if (tag != 109) {
            throw this.error("expected hessian method ('m') at " + this.codeName(tag));
        } else {
            int d1 = this.read();
            int d2 = this.read();
            this._isLastChunk = true;
            this._chunkLength = d1 * 256 + d2;
            this._sbuf.setLength(0);

            int ch;
            while((ch = this.parseChar()) >= 0) {
                this._sbuf.append((char)ch);
            }

            this._method = this._sbuf.toString();
            return this._method;
        }
    }
    @Override
    public void startCall() throws IOException {
        this.readCall();

        while(this.readHeader() != null) {
            this.readObject();
        }

        this.readMethod();
    }
    @Override
    public void completeCall() throws IOException {
        int tag = this.read();
        if (tag != 122) {
            throw this.error("expected end of call ('z') at " + this.codeName(tag) + ".  Check method arguments and ensure method overloading is enabled if necessary");
        }
    }
    @Override
    public Object readReply(Class expectedClass) throws Throwable {
        int tag = this.read();
        if (tag != 114) {
            this.error("expected hessian reply at " + this.codeName(tag));
        }

        int major = this.read();
        int minor = this.read();
        tag = this.read();
        if (tag == 102) {
            throw this.prepareFault();
        } else {
            this._peek = tag;
            Object value = this.readObject(expectedClass);
            this.completeValueReply();
            return value;
        }
    }
    @Override
    public void startReply() throws Throwable {
        int tag = this.read();
        if (tag != 114) {
            this.error("expected hessian reply at " + this.codeName(tag));
        }

        int major = this.read();
        int minor = this.read();
        tag = this.read();
        if (tag == 102) {
            throw this.prepareFault();
        } else {
            this._peek = tag;
        }
    }

    private Throwable prepareFault() throws IOException {
        HashMap fault = this.readFault();
        Object detail = fault.get("detail");
        String message = (String)fault.get("message");
        if (detail instanceof Throwable) {
            this._replyFault = (Throwable)detail;
            if (message != null && _detailMessageField != null) {
                try {
                    _detailMessageField.set(this._replyFault, message);
                } catch (Throwable var5) {
                }
            }

            return this._replyFault;
        } else {
            String code = (String)fault.get("code");
            this._replyFault = new HessianServiceException(message, code, detail);
            return this._replyFault;
        }
    }
    @Override
    public void completeReply() throws IOException {
        int tag = this.read();
        if (tag != 122) {
            this.error("expected end of reply at " + this.codeName(tag));
        }

    }

    public void completeValueReply() throws IOException {
        int tag = this.read();
        if (tag != 122) {
            this.error("expected end of reply at " + this.codeName(tag));
        }

    }
    @Override
    public String readHeader() throws IOException {
        int tag = this.read();
        if (tag != 72) {
            this._peek = tag;
            return null;
        } else {
            this._isLastChunk = true;
            this._chunkLength = (this.read() << 8) + this.read();
            this._sbuf.setLength(0);

            int ch;
            while((ch = this.parseChar()) >= 0) {
                this._sbuf.append((char)ch);
            }

            return this._sbuf.toString();
        }
    }
    @Override
    public void readNull() throws IOException {
        int tag = this.read();
        switch(tag) {
            case 78:
                return;
            default:
                throw this.expect("null", tag);
        }
    }
    @Override
    public boolean readBoolean() throws IOException {
        int tag = this.read();
        switch(tag) {
            case 68:
                return this.parseDouble() == 0.0D;
            case 69:
            case 71:
            case 72:
            case 74:
            case 75:
            case 77:
            case 79:
            case 80:
            case 81:
            case 82:
            case 83:
            default:
                throw this.expect("boolean", tag);
            case 70:
                return false;
            case 73:
                return this.parseInt() == 0;
            case 76:
                return this.parseLong() == 0L;
            case 78:
                return false;
            case 84:
                return true;
        }
    }
    @Override
    public int readInt() throws IOException {
        int tag = this.read();
        switch(tag) {
            case 68:
                return (int)this.parseDouble();
            case 70:
                return 0;
            case 73:
                return this.parseInt();
            case 76:
                return (int)this.parseLong();
            case 84:
                return 1;
            default:
                throw this.expect("int", tag);
        }
    }
    @Override
    public long readLong() throws IOException {
        int tag = this.read();
        switch(tag) {
            case 68:
                return (long)this.parseDouble();
            case 70:
                return 0L;
            case 73:
                return (long)this.parseInt();
            case 76:
                return this.parseLong();
            case 84:
                return 1L;
            default:
                throw this.expect("long", tag);
        }
    }
    @Override
    public double readDouble() throws IOException {
        int tag = this.read();
        switch(tag) {
            case 68:
                return this.parseDouble();
            case 70:
                return 0.0D;
            case 73:
                return (double)this.parseInt();
            case 76:
                return (double)this.parseLong();
            case 84:
                return 1.0D;
            default:
                throw this.expect("long", tag);
        }
    }
    @Override
    public long readUTCDate() throws IOException {
        int tag = this.read();
        if (tag != 100) {
            throw this.error("expected date at " + this.codeName(tag));
        } else {
            long b64 = (long)this.read();
            long b56 = (long)this.read();
            long b48 = (long)this.read();
            long b40 = (long)this.read();
            long b32 = (long)this.read();
            long b24 = (long)this.read();
            long b16 = (long)this.read();
            long b8 = (long)this.read();
            return (b64 << 56) + (b56 << 48) + (b48 << 40) + (b40 << 32) + (b32 << 24) + (b24 << 16) + (b16 << 8) + b8;
        }
    }
    @Override
    public String readString() throws IOException {
        int tag = this.read();
        switch(tag) {
            case 68:
                return String.valueOf(this.parseDouble());
            case 73:
                return String.valueOf(this.parseInt());
            case 76:
                return String.valueOf(this.parseLong());
            case 78:
                return null;
            case 83:
            case 88:
            case 115:
            case 120:
                this._isLastChunk = tag == 83 || tag == 88;
                this._chunkLength = (this.read() << 8) + this.read();
                this._sbuf.setLength(0);

                int ch;
                while((ch = this.parseChar()) >= 0) {
                    this._sbuf.append((char)ch);
                }

                return this._sbuf.toString();
            default:
                throw this.expect("string", tag);
        }
    }
    @Override
    public Node readNode() throws IOException {
        int tag = this.read();
        switch(tag) {
            case 78:
                return null;
            case 83:
            case 88:
            case 115:
            case 120:
                this._isLastChunk = tag == 83 || tag == 88;
                this._chunkLength = (this.read() << 8) + this.read();
                throw this.error("Can't handle string in this context");
            default:
                throw this.expect("string", tag);
        }
    }
    @Override
    public byte[] readBytes() throws IOException {
        int tag = this.read();
        switch(tag) {
            case 66:
            case 98:
                this._isLastChunk = tag == 66;
                this._chunkLength = (this.read() << 8) + this.read();
                ByteArrayOutputStream bos = new ByteArrayOutputStream();

                int data;
                while((data = this.parseByte()) >= 0) {
                    bos.write(data);
                }

                return bos.toByteArray();
            case 78:
                return null;
            default:
                throw this.expect("bytes", tag);
        }
    }

    private HashMap readFault() throws IOException {
        HashMap map = new HashMap();

        int code;
        for(code = this.read(); code > 0 && code != 122; code = this.read()) {
            this._peek = code;
            Object key = this.readObject();
            Object value = this.readObject();
            if (key != null && value != null) {
                map.put(key, value);
            }
        }

        if (code != 122) {
            throw this.expect("fault", code);
        } else {
            return map;
        }
    }
    @Override
    public Object readObject(Class cl) throws IOException {
        if (cl != null && cl != Object.class) {
            int tag = this.read();
            String type;
            switch(tag) {
                case 77:
                    type = this.readType();
                    Deserializer reader;
                    if ("".equals(type)) {
                        reader = this._serializerFactory.getDeserializer(cl);
                    } else {
                        reader = this._serializerFactory.getObjectDeserializer(type);
                    }

                    return reader.readMap(this);
                case 78:
                    return null;
                case 82:
                    int ref = this.parseInt();
                    return this._refs.get(ref);
                case 86:
                    type = this.readType();
                    int length = this.readLength();
                    reader = this._serializerFactory.getObjectDeserializer(type);
                    if (cl != reader.getType() && cl.isAssignableFrom(reader.getType())) {
                        return reader.readList(this, length);
                    }

                    reader = this._serializerFactory.getDeserializer(cl);
                    Object v = reader.readList(this, length);
                    return v;
                case 114:
                    type = this.readType();
                    String url = this.readString();
                    return this.resolveRemote(type, url);
                default:
                    this._peek = tag;
                    Object value = this._serializerFactory.getDeserializer(cl).readObject(this);
                    return value;
            }
        } else {
            return this.readObject();
        }
    }
    @Override
    public Object readObject() throws IOException {
        int tag = this.read();
        String type;
        int data;
        switch(tag) {
            case 66:
            case 98:
                this._isLastChunk = tag == 66;
                this._chunkLength = (this.read() << 8) + this.read();
                ByteArrayOutputStream bos = new ByteArrayOutputStream();

                while((data = this.parseByte()) >= 0) {
                    bos.write(data);
                }

                return bos.toByteArray();
            case 67:
            case 69:
            case 71:
            case 72:
            case 74:
            case 75:
            case 79:
            case 80:
            case 81:
            case 85:
            case 87:
            case 89:
            case 90:
            case 91:
            case 92:
            case 93:
            case 94:
            case 95:
            case 96:
            case 97:
            case 99:
            case 101:
            case 102:
            case 103:
            case 104:
            case 105:
            case 106:
            case 107:
            case 108:
            case 109:
            case 110:
            case 111:
            case 112:
            case 113:
            case 116:
            case 117:
            case 118:
            case 119:
            default:
                throw this.error("unknown code for readObject at " + this.codeName(tag));
            case 68:
                return this.parseDouble();
            case 70:
                return false;
            case 73:
                return this.parseInt();
            case 76:
                return this.parseLong();
            case 77:
                type = this.readType();
                // 将欧电云对象转换成自己的对象
                type = ODianYunObjectConvertEnum.getFpcObject(type);
                return this._serializerFactory.readMap(this, type);
            case 78:
                return null;
            case 82:
                data = this.parseInt();
                return this._refs.get(data);
            case 83:
            case 115:
                this._isLastChunk = tag == 83;
                this._chunkLength = (this.read() << 8) + this.read();
                this._sbuf.setLength(0);

                while((data = this.parseChar()) >= 0) {
                    this._sbuf.append((char)data);
                }

                return this._sbuf.toString();
            case 84:
                return true;
            case 86:
                type = this.readType();
                int length = this.readLength();
                return this._serializerFactory.readList(this, length, type);
            case 88:
            case 120:
                this._isLastChunk = tag == 88;
                this._chunkLength = (this.read() << 8) + this.read();
                return this.parseXML();
            case 100:
                return new Date(this.parseLong());
            case 114:
                type = this.readType();
                String url = this.readString();
                return this.resolveRemote(type, url);
        }
    }
    @Override
    public Object readRemote() throws IOException {
        String type = this.readType();
        String url = this.readString();
        return this.resolveRemote(type, url);
    }
    @Override
    public Object readRef() throws IOException {
        return this._refs.get(this.parseInt());
    }
    @Override
    public int readListStart() throws IOException {
        return this.read();
    }
    @Override
    public int readMapStart() throws IOException {
        return this.read();
    }
    @Override
    public boolean isEnd() throws IOException {
        int code = this.read();
        this._peek = code;
        return code < 0 || code == 122;
    }
    @Override
    public void readEnd() throws IOException {
        int code = this.read();
        if (code != 122) {
            throw this.error("unknown code at " + this.codeName(code));
        }
    }
    @Override
    public void readMapEnd() throws IOException {
        int code = this.read();
        if (code != 122) {
            throw this.error("expected end of map ('z') at " + this.codeName(code));
        }
    }
    @Override
    public void readListEnd() throws IOException {
        int code = this.read();
        if (code != 122) {
            throw this.error("expected end of list ('z') at " + this.codeName(code));
        }
    }
    @Override
    public int addRef(Object ref) {
        if (this._refs == null) {
            this._refs = new ArrayList();
        }

        this._refs.add(ref);
        return this._refs.size() - 1;
    }
    @Override
    public void setRef(int i, Object ref) {
        this._refs.set(i, ref);
    }
    @Override
    public void resetReferences() {
        if (this._refs != null) {
            this._refs.clear();
        }

    }

    public Object resolveRemote(String type, String url) throws IOException {
        HessianRemoteResolver resolver = this.getRemoteResolver();
        return resolver != null ? resolver.lookup(type, url) : new HessianRemote(type, url);
    }
    @Override
    public String readType() throws IOException {
        int code = this.read();
        if (code != 116) {
            this._peek = code;
            return "";
        } else {
            this._isLastChunk = true;
            this._chunkLength = (this.read() << 8) + this.read();
            this._sbuf.setLength(0);

            int ch;
            while((ch = this.parseChar()) >= 0) {
                this._sbuf.append((char)ch);
            }

            return this._sbuf.toString();
        }
    }
    @Override
    public int readLength() throws IOException {
        int code = this.read();
        if (code != 108) {
            this._peek = code;
            return -1;
        } else {
            return this.parseInt();
        }
    }

    private int parseInt() throws IOException {
        int b32 = this.read();
        int b24 = this.read();
        int b16 = this.read();
        int b8 = this.read();
        return (b32 << 24) + (b24 << 16) + (b16 << 8) + b8;
    }

    private long parseLong() throws IOException {
        long b64 = (long)this.read();
        long b56 = (long)this.read();
        long b48 = (long)this.read();
        long b40 = (long)this.read();
        long b32 = (long)this.read();
        long b24 = (long)this.read();
        long b16 = (long)this.read();
        long b8 = (long)this.read();
        return (b64 << 56) + (b56 << 48) + (b48 << 40) + (b40 << 32) + (b32 << 24) + (b24 << 16) + (b16 << 8) + b8;
    }

    private double parseDouble() throws IOException {
        long b64 = (long)this.read();
        long b56 = (long)this.read();
        long b48 = (long)this.read();
        long b40 = (long)this.read();
        long b32 = (long)this.read();
        long b24 = (long)this.read();
        long b16 = (long)this.read();
        long b8 = (long)this.read();
        long bits = (b64 << 56) + (b56 << 48) + (b48 << 40) + (b40 << 32) + (b32 << 24) + (b24 << 16) + (b16 << 8) + b8;
        return Double.longBitsToDouble(bits);
    }

    Node parseXML() throws IOException {
        throw new UnsupportedOperationException();
    }

    private int parseChar() throws IOException {
        while(this._chunkLength <= 0) {
            if (this._isLastChunk) {
                return -1;
            }

            int code = this.read();
            switch(code) {
                case 83:
                case 88:
                    this._isLastChunk = true;
                    this._chunkLength = (this.read() << 8) + this.read();
                    break;
                case 115:
                case 120:
                    this._isLastChunk = false;
                    this._chunkLength = (this.read() << 8) + this.read();
                    break;
                default:
                    throw this.expect("string", code);
            }
        }

        --this._chunkLength;
        return this.parseUTF8Char();
    }

    private int parseUTF8Char() throws IOException {
        int ch = this.read();
        if (ch < 128) {
            return ch;
        } else {
            int ch1;
            int ch2;
            if ((ch & 224) == 192) {
                ch1 = this.read();
                ch2 = ((ch & 31) << 6) + (ch1 & 63);
                return ch2;
            } else if ((ch & 240) == 224) {
                ch1 = this.read();
                ch2 = this.read();
                int v = ((ch & 15) << 12) + ((ch1 & 63) << 6) + (ch2 & 63);
                return v;
            } else {
                throw this.error("bad utf-8 encoding at " + this.codeName(ch));
            }
        }
    }

    private int parseByte() throws IOException {
        while(this._chunkLength <= 0) {
            if (this._isLastChunk) {
                return -1;
            }

            int code = this.read();
            switch(code) {
                case 66:
                    this._isLastChunk = true;
                    this._chunkLength = (this.read() << 8) + this.read();
                    break;
                case 98:
                    this._isLastChunk = false;
                    this._chunkLength = (this.read() << 8) + this.read();
                    break;
                default:
                    throw this.expect("byte[]", code);
            }
        }

        --this._chunkLength;
        return this.read();
    }
    @Override
    public InputStream readInputStream() throws IOException {
        int tag = this.read();
        switch(tag) {
            case 66:
            case 98:
                this._isLastChunk = tag == 66;
                this._chunkLength = (this.read() << 8) + this.read();
                return new InputStream() {
                    boolean _isClosed = false;
                    @Override
                    public int read() throws IOException {
                        if (!this._isClosed && SoaHessianInput.this._is != null) {
                            int ch = SoaHessianInput.this.parseByte();
                            if (ch < 0) {
                                this._isClosed = true;
                            }

                            return ch;
                        } else {
                            return -1;
                        }
                    }
                    @Override
                    public int read(byte[] buffer, int offset, int length) throws IOException {
                        if (!this._isClosed && SoaHessianInput.this._is != null) {
                            int len = SoaHessianInput.this.read(buffer, offset, length);
                            if (len < 0) {
                                this._isClosed = true;
                            }

                            return len;
                        } else {
                            return -1;
                        }
                    }
                    @Override
                    public void close() throws IOException {
                        while(this.read() >= 0) {
                        }

                        this._isClosed = true;
                    }
                };
            case 78:
                return null;
            default:
                throw this.expect("inputStream", tag);
        }
    }

    int read(byte[] buffer, int offset, int length) throws IOException {
        int readLength;
        int code;
        for(readLength = 0; length > 0; this._chunkLength -= code) {
            while(this._chunkLength <= 0) {
                if (this._isLastChunk) {
                    return readLength == 0 ? -1 : readLength;
                }

                code = this.read();
                switch(code) {
                    case 66:
                        this._isLastChunk = true;
                        this._chunkLength = (this.read() << 8) + this.read();
                        break;
                    case 98:
                        this._isLastChunk = false;
                        this._chunkLength = (this.read() << 8) + this.read();
                        break;
                    default:
                        throw this.expect("byte[]", code);
                }
            }

            code = this._chunkLength;
            if (length < code) {
                code = length;
            }

            code = this._is.read(buffer, offset, code);
            offset += code;
            readLength += code;
            length -= code;
        }

        return readLength;
    }

    public final int read() throws IOException {
        int ch;
        if (this._peek >= 0) {
            ch = this._peek;
            this._peek = -1;
            return ch;
        } else {
            ch = this._is.read();
            return ch;
        }
    }
    @Override
    public void close() {
        this._is = null;
    }
    @Override
    public Reader getReader() {
        return null;
    }

    protected IOException expect(String expect, int ch) {
        return this.error("expected " + expect + " at " + this.codeName(ch));
    }

    protected String codeName(int ch) {
        return ch < 0 ? "end of file" : "0x" + Integer.toHexString(ch & 255) + " (" + (char)ch + ")";
    }

    protected IOException error(String message) {
        return this._method != null ? new HessianProtocolException(this._method + ": " + message) : new HessianProtocolException(message);
    }

    static {
        try {
            _detailMessageField = Throwable.class.getDeclaredField("detailMessage");
            _detailMessageField.setAccessible(true);
        } catch (Throwable var1) {
        }

    }
}

