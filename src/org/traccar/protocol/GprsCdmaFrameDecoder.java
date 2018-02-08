/*
 * Copyright 2013 Anton Tananaev (anton@traccar.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.traccar.protocol;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.frame.FrameDecoder;
import org.traccar.helper.Log;
import org.traccar.helper.StringFinder;

import java.nio.charset.StandardCharsets;

public class GprsCdmaFrameDecoder extends FrameDecoder {

    private static final int KEEP_ALIVE_LENGTH = 8;

    @Override
    protected Object decode(
            ChannelHandlerContext ctx, Channel channel, ChannelBuffer buf) throws Exception {

        if (buf.readableBytes() < KEEP_ALIVE_LENGTH) {
            return null;
        }

        String ddd = buf.toString(StandardCharsets.US_ASCII);

        if (ddd.indexOf(',') != -1 ){
            String[ ] ccc = ddd.split(",");
            int lenRespone = Integer.parseInt(ccc[0].substring(1), 16);
            if (ccc[0].charAt(0) == '$' && ccc[1].equals("R") && ccc[3].equals("#") && lenRespone + 1 == ddd.substring(3).length() ) {

                Log.warning("GPS设备发送注册指令请求：" + ddd);

                ChannelBuffer aaa = ChannelBuffers.dynamicBuffer();

                aaa.writeByte('$');
                aaa.writeByte('0');
                aaa.writeByte('8');
                aaa.writeByte(',');
                aaa.writeByte('R');
                aaa.writeByte('A');
                aaa.writeByte(',');
                aaa.writeByte('0');
                aaa.writeByte(',');
                aaa.writeByte('1');
                aaa.writeByte(',');
                aaa.writeByte('#');
                aaa.writeByte('\n');

                if (channel != null) {
                    channel.write(aaa);
                }
            }else {
                Log.warning("FrameDecoder GPS设备正在发送实时位置信息：" + ddd);
                ChannelBuffer frame = buf.readBytes(buf.readableBytes());
                return frame;
            }
        }

        return null;
    }

}
