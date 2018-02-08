/*
 * Copyright 2013 - 2015 Anton Tananaev (anton@traccar.org)
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

import org.apache.commons.lang.StringUtils;
import org.apache.poi.util.StringUtil;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.traccar.BaseProtocolDecoder;
import org.traccar.Context;
import org.traccar.DeviceSession;
import org.traccar.helper.Log;
import org.traccar.helper.Parser;
import org.traccar.helper.PatternBuilder;
import org.traccar.helper.UnitsConverter;
import org.traccar.model.Position;

import java.net.SocketAddress;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Pattern;

import com.iigeo.coord.*;
import org.traccar.model.Server;


public class GprsCdmaProtocolDecoder extends BaseProtocolDecoder {

    public GprsCdmaProtocolDecoder(GprsCdmaProtocol protocol) {
        super(protocol);
    }

    @Override
    protected Object decode(
            Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {

        ChannelBuffer buf = (ChannelBuffer) msg;

        String ddd = buf.toString(StandardCharsets.US_ASCII);

        System.out.println(ddd);

        if (ddd.indexOf(',') != -1 ){
            String[ ] ccc = ddd.split(",");
            int lenRespone = Integer.parseInt(ccc[0].substring(1), 16);
            if (ccc[0].charAt(0) == '$' && (ccc[1].equals("S") || ccc[1].equals("H"))  && ccc[ ccc.length - 1 ].equals("#") && lenRespone + 1 == ddd.substring(3).length() ) {

                Log.warning("ProtocolDecoder GPS设备正在发送实时位置信息：" + ddd);

                Position position = new Position();
                position.setProtocol(getProtocolName());

                DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, ccc[2]);

                if (deviceSession == null) {
                    return null;
                }

                position.setDeviceId(deviceSession.getDeviceId());

//                time begin
                StringBuilder sb3 = new StringBuilder();

                DateFormat dateFormat2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                sb3.append(String.valueOf("20"));
                sb3.append(String.valueOf(ccc[12].substring(4,6)));
                sb3.append(String.valueOf("-"));
                sb3.append(String.valueOf(ccc[12].substring(2,4)));
                sb3.append(String.valueOf("-"));
                sb3.append(String.valueOf(ccc[12].substring(0,2)));
                sb3.append(String.valueOf(" "));
                sb3.append(String.valueOf(ccc[12].substring(7,9)));
                sb3.append(String.valueOf(":"));
                sb3.append(String.valueOf(ccc[12].substring(9,11)));
                sb3.append(String.valueOf(":"));
                sb3.append(String.valueOf(ccc[12].substring(11,13)));

                System.out.println(sb3.toString());

                Date myDate2 = dateFormat2.parse(utc2Local(sb3.toString(), "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm:ss"));

//                time end

                position.setTime(myDate2);

                Double longtitude = Double.valueOf(ccc[6].substring(0,3)) + (Double.valueOf(ccc[6].substring(3)) / 60);
                Double lantitude = Double.valueOf(ccc[4].substring(0,2)) + (Double.valueOf(ccc[4].substring(2)) / 60);

                Server server1 = Context.getPermissionsManager().getServer();

                String mapName = server1.getMap();

                LatLngPoint latlong =  CoordTransform.WGS84ToBD09(lantitude, longtitude );

                if ( mapName.equals("baidu") ){
                    position.setLongitude(latlong.getLonY()); // 经度
                    position.setLatitude(latlong.getLatX()); // 纬度
                }else {
                    position.setLongitude(longtitude); // 经度
                    position.setLatitude(lantitude); // 纬度
                }

                position.setAltitude(0);

                if (StringUtils.isNotBlank(ccc[7])){
                    position.setSpeed(UnitsConverter.knotsFromKph(Double.valueOf(ccc[7])));
                }else {
                    position.setSpeed(UnitsConverter.knotsFromKph(0));
                }

                if (StringUtils.isNotBlank(ccc[8])){
                    position.setCourse(Double.valueOf(ccc[8]));
                }else {
                    position.setCourse(Double.valueOf(0));
                }

                int satellites = 0;

                if (StringUtils.isNotBlank(ccc[10])){
                    satellites = Integer.valueOf(ccc[10]);
                }

                position.setValid(satellites != 0);
                position.set(Position.KEY_SATELLITES, satellites);

                if (StringUtils.isNotBlank(ccc[17])){
                    position.set(Position.KEY_BATTERY, Double.valueOf(ccc[17]));
                }else {
                    position.set(Position.KEY_BATTERY, 0);
                }

                return position;

            }
        }
        return null;
    }

    public static String utc2Local(String utcTime, String utcTimePatten, String localTimePatten) {
        SimpleDateFormat utcFormater = new SimpleDateFormat(utcTimePatten);
        utcFormater.setTimeZone(TimeZone.getTimeZone("UTC"));//时区定义并进行时间获取
        Date gpsUTCDate = null;
        try {
            gpsUTCDate = utcFormater.parse(utcTime);
        } catch (ParseException e) {
            e.printStackTrace();
            return utcTime;
        }
        SimpleDateFormat localFormater = new SimpleDateFormat(localTimePatten);
        localFormater.setTimeZone(TimeZone.getDefault());
        String localTime = localFormater.format(gpsUTCDate.getTime());
        return localTime;
    }
}
