package com.aozainkmc.core;

import java.util.ArrayList;
import java.util.List;

public final class SemanticTags {
    private SemanticTags() {
    }

    public static List<String> forWord(String word) {
        List<String> tags = new ArrayList<>();
        tags.add("word." + word);
        switch (word) {
            case "火", "炎", "焱", "燃", "灼" -> {
                tags.add("element.fire");
                tags.add("quality.heat");
                tags.add("quality.light");
            }
            case "水", "冰", "雨", "淼", "泉" -> {
                tags.add("element.water");
                tags.add("quality.flow");
                tags.add("quality.cool");
            }
            case "木", "林", "森", "草", "生", "心", "沁", "蕊", "命", "救" -> {
                tags.add("element.wood");
                tags.add("quality.growth");
                tags.add("quality.life");
            }
            case "忍", "耐", "韧", "坚", "稳" -> {
                tags.add("quality.body");
                tags.add("quality.endure");
                tags.add("quality.defense");
            }
            case "净", "洁", "清" -> {
                tags.add("element.light");
                tags.add("quality.purify");
                tags.add("quality.cleanse");
            }
            case "力", "气", "忿", "怒" -> {
                tags.add("quality.force");
                tags.add("quality.rage");
                tags.add("operator.amplify");
            }
            case "金", "铁", "铜", "银", "锋" -> {
                tags.add("element.metal");
                tags.add("quality.cut");
                tags.add("quality.conduct");
            }
            case "土", "山", "石", "岩", "垚" -> {
                tags.add("element.earth");
                tags.add("quality.stable");
                tags.add("quality.block");
            }
            case "风", "行", "飞", "飍" -> {
                tags.add("element.wind");
                tags.add("quality.motion");
                tags.add("quality.spread");
            }
            case "光", "日", "明", "照" -> {
                tags.add("element.light");
                tags.add("quality.reveal");
                tags.add("quality.purify");
            }
            case "影", "夜", "暗", "隐" -> {
                tags.add("element.shadow");
                tags.add("quality.hide");
                tags.add("quality.drain");
            }
            case "封" -> tags.add("operator.seal");
            case "引" -> tags.add("operator.attract");
            case "散" -> tags.add("operator.disperse");
            default -> tags.add("semantic.unclassified");
        }
        return tags;
    }
}
