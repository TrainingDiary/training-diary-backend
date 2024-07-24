package com.project.trainingdiary.util;

public class ConvertCloudFrontUrlUtil {

  private static final String CLOUDFRONT_URL = "https://dolakp01zo8g4.cloudfront.net/";

  public static String convertToCloudFrontUrl(String s3Url) {
    if (s3Url != null && s3Url.contains(".s3.")) {
      int index = s3Url.indexOf(".com/");
      if (index != -1) {
        String path = s3Url.substring(index + 5);     // ".com/" 이후의 경로를 추출
        return CLOUDFRONT_URL + path;
      }
    }
    return s3Url;
  }

}
