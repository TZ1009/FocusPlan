package com.example.focusplan.ai;

import java.util.Locale;

/**
 * 轻量 AI 分类模块：
 * 通过备注文本 + 类型 + 金额做一个简单的规则分类。
 * 后续可以替换为真正的 ML/LLM 模型，但接口不变。
 */
public class AiCategoryClassifier {

    /**
     * @param note  备注文本
     * @param amount 金额
     * @param type   收支类型："支出" 或 "收入"
     * @return 推荐分类，如："餐饮"、"交通"、"购物"、"房租"、"工资"、"其他"
     */
    public static String classify(String note, double amount, String type) {
        if (note == null) note = "";
        String text = note.toLowerCase(Locale.ROOT)
                .replace(" ", "")
                .replace("　", "");

        // ===================== 基于关键词的简单分类规则 =====================

        // 1. 餐饮相关
        if (containsAny(text, "饭", "餐", "吃", "外卖", "奶茶", "咖啡", "奶盖",
                "汉堡", "炸鸡", "烧烤", "火锅", "面", "米线", "茶", "饮料")) {
            return "餐饮";
        }

        // 2. 交通相关
        if (containsAny(text, "地铁", "公交", "巴士", "滴滴", "打车", "出租", "网约车",
                "高铁", "火车", "动车", "机票", "飞机", "打的", "车费", "过路费", "高速费")) {
            return "交通";
        }

        // 3. 购物/日用品
        if (containsAny(text, "淘宝", "京东", "拼多多", "网购", "买衣服", "衣服", "裤子",
                "鞋", "化妆品", "护肤", "口红", "日用品", "超市", "便利店", "商场")) {
            return "购物";
        }

        // 4. 房租/住宿
        if (containsAny(text, "房租", "租金", "房东", "押金", "租房", "住宿", "酒店", "宾馆")) {
            return "房租/住宿";
        }

        // 5. 学习/教育
        if (containsAny(text, "学费", "培训", "网课", "考试", "补习", "教材", "书", "论文")) {
            return "教育/学习";
        }

        // 6. 工资/收入类
        if ("收入".equals(type)) {
            if (containsAny(text, "工资", "薪水", "发薪", "奖金", "兼职", "红包", "报销", "利息")) {
                return "工资/收入";
            }
            // 金额较大、备注为空时，也可以默认为收入
            if (amount >= 1000 && text.isEmpty()) {
                return "工资/收入";
            }
        }

        // 7. 数额辅助判断：大额支出可能是房租/学费 等
        if ("支出".equals(type) && amount > 2000) {
            if (containsAny(text, "学费")) {
                return "教育/学习";
            }
            return "大额支出";
        }

        // 默认分类
        return "其他";
    }

    private static boolean containsAny(String text, String... keys) {
        for (String k : keys) {
            if (text.contains(k.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }
}
