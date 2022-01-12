package com.jsun.picturAnalysis.pojo;

import lombok.Data;

/**
 * 图片信息
 *
 * @className: ImgInfo
 * @date 2022/1/12 13:49
 */
@Data
public class ImgInfo {
    // 图片宽
    private String height;
    // 图片高
    private String width;
    // 拍摄时间
    private String original;
    // 纬度
    private String latitude;
    // 经度
    private String longitude;

}
