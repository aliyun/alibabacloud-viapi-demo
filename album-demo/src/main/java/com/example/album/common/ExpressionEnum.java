package com.example.album.common;

public enum ExpressionEnum {
        neutral ("neutral", "中性"),
        happiness("happiness", "高兴"),
        surprise("surprise", "惊讶"),
        sadness ("sadness","伤心"),
        anger("anger", "生气"),
        disgust("disgust","厌恶"),
        fear ("fear","害怕");

        String name;
        String nameEn;
        ExpressionEnum(String nameEn, String name) {
            this.name = name;
            this.nameEn = nameEn;
        }

        public static String getNameByNameEn(String nameEn){
            for(ExpressionEnum expressionEnum: ExpressionEnum.values()) {
                if (expressionEnum.nameEn.equals(nameEn)) {
                    return expressionEnum.name;
                }
            }
            return  "";
        }
}
