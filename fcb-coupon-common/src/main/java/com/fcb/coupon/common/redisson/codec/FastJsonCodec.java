package com.fcb.coupon.common.redisson.codec;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import org.redisson.client.codec.BaseCodec;
import org.redisson.client.protocol.Decoder;
import org.redisson.client.protocol.Encoder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class FastJsonCodec extends BaseCodec {
    public static final FastJsonCodec INSTANCE = new FastJsonCodec();

    private final static ParserConfig parserConfig = new ParserConfig();

    static {
        parserConfig.setAutoTypeSupport(true);
    }

    private final Encoder encoder = in -> {
        ByteBuf out = ByteBufAllocator.DEFAULT.buffer();
        try {
            ByteBufOutputStream os = new ByteBufOutputStream(out);
            JSON.writeJSONString(os, StandardCharsets.UTF_8, in, SerializerFeature.WriteClassName);
            return os.buffer();
        } catch (IOException e) {
            out.release();
            throw e;
        } catch (Exception e) {
            out.release();
            throw new IOException(e);
        }
    };

    private final Decoder<Object> decoder = (buf, state) ->
            JSON.parseObject(new ByteBufInputStream(buf), StandardCharsets.UTF_8, Object.class, parserConfig);

    @Override
    public Decoder<Object> getValueDecoder() {
        return decoder;
    }

    @Override
    public Encoder getValueEncoder() {
        return encoder;
    }
}