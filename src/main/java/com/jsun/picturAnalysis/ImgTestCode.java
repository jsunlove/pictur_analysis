package com.jsun.picturAnalysis;


import cn.hutool.core.util.ObjectUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import com.jsun.picturAnalysis.pojo.ImgInfo;
import lombok.extern.slf4j.Slf4j;

import java.io.File;

/**
 * @className: ImgTestCode
 * @date 2022/1/12 10:25
 */
@Slf4j
public class ImgTestCode {

    public static void main(String[] args) throws Exception {
        String filePath = "";
        File file = new File(filePath);
        readImageInfo(file);
    }

    /**
     * 提取照片里面的信息
     *
     * @param file 照片文件
     */
    private static void readImageInfo(File file) throws Exception {
        Metadata metadata = ImageMetadataReader.readMetadata(file);

        // 打印出所有的信息
        getAllInfoOfImg(metadata);

        // 获取图片信息
        ImgInfo infoOfImg = getInfoOfImg(metadata);
        log.info("获取到的图片信息：{}", infoOfImg);

        double lat = latLng2Decimal(infoOfImg.getLatitude());
        double lng = latLng2Decimal(infoOfImg.getLongitude());
        if (ObjectUtil.isNull(lat) || ObjectUtil.isNull(lng)) {
            log.info("没有经纬度");
            return;
        }

        //经纬度转地主使用百度api
        convertGpsToLocation(lat, lng);
    }

    /**
     * 获取图片所有信息
     * @param metadata
     */
    public static void getAllInfoOfImg(Metadata metadata){
        for (Directory directory : metadata.getDirectories()) {
            for (Tag tag : directory.getTags()) {
                log.info("【{}】 - {} = {}", directory.getName(), tag.getTagName(), tag.getDescription());
            }
            if (directory.hasErrors()) {
                for (String error : directory.getErrors()) {
                    log.error("ERROR:{}", error);
                }
            }
        }
    }

    /**
     * 获取图片部分信息
     * @param metadata
     * @return
     */
    public static ImgInfo getInfoOfImg(Metadata metadata) {
        ImgInfo imgInfo = new ImgInfo();
        for (Directory directory : metadata.getDirectories()) {
            // 提取的信息有很多，但是我们只需要部分
            if ("GPS".equals(directory.getName()) || "Exif IFD0".equals(directory.getName())) {
                for (Tag tag : directory.getTags()) {
                    String tagName = tag.getTagName();  //标签名
                    String desc = tag.getDescription(); //标签信息
                    switch (tagName) {
                        case "Image Height":
                            imgInfo.setHeight(desc);
                            break;
                        case "Image Width":
                            imgInfo.setWidth(desc);
                            break;
                        case "Date/Time":
                            imgInfo.setOriginal(desc);
                            break;
                        case "GPS Latitude":
                            imgInfo.setLatitude(desc);
                            break;
                        case "GPS Longitude":
                            imgInfo.setLongitude(desc);
                            break;
                        default:
                            break;
                    }
                }
            }
        }

        return imgInfo;
    }

    /**
     * 经纬度格式  转换为  度分秒格式 ,如果需要的话可以调用该方法进行转换
     *
     * @param point 坐标点
     */
    public static String pointToLatlong(String point) {
        double du = Double.parseDouble(point.substring(0, point.indexOf("°")).trim());
        double fen = Double.parseDouble(point.substring(point.indexOf("°") + 1, point.indexOf("'")).trim());
        double miao = Double.parseDouble(point.substring(point.indexOf("'") + 1, point.indexOf("\"")).trim());
        double duStr = du + fen / 60 + miao / 60 / 60;
        return Double.toString(duStr);
    }

    /**
     * 经纬度坐标格式转换（* °转十进制格式）
     *
     * @param gps 经纬度
     * @return 转换后的结果
     */
    public static double latLng2Decimal(String gps) {
        String a = gps.split("°")[0].replace(" ", "");
        String b = gps.split("°")[1].split("'")[0].replace(" ", "");
        String c = gps.split("°")[1].split("'")[1].replace(" ", "").replace("\"", "");
        return Double.parseDouble(a) + Double.parseDouble(b) / 60 + Double.parseDouble(c) / 60 / 60;
    }

    /**
     * api_key：注册的百度api的key
     * coords：经纬度坐标
     * http://api.map.baidu.com/reverse_geocoding/v3/?ak="+api_key+"&output=json&coordtype=wgs84ll&location="+coords
     * 经纬度转地址信息
     *
     * @param gps_latitude  维度
     * @param gps_longitude 精度
     */
    private static void convertGpsToLocation(double gps_latitude, double gps_longitude) {
        String apiKey = "YNxcSCAphFvuPD4LwcgWXwC3SEZZc7Ra";

        String res = "";
        String url = "http://api.map.baidu.com/reverse_geocoding/v3/?ak=" + apiKey + "&output=json&coordtype=wgs84ll&location=" + (gps_latitude + "," + gps_longitude);
        log.error("【url】" + url);

        res = HttpUtil.get(url);
        JSONObject object = JSON.parseObject(res);
        if (object.containsKey("result")) {
            JSONObject result = object.getJSONObject("result");
            if (result.containsKey("addressComponent")) {
                JSONObject address = object.getJSONObject("result").getJSONObject("addressComponent");
                log.info("拍摄地点：" + address.get("country") + " " + address.get("province") + " " + address.get("city") + " " + address.get("district") + " "
                        + address.get("street") + " " + result.get("formatted_address") + " " + result.get("business"));
            }
        }
    }


}

