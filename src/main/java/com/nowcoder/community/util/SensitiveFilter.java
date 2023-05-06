package com.nowcoder.community.util;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.swing.tree.TreeNode;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * @Date: create in 14:17 2023/3/11
 * @describe: 敏感词过滤器
 */
@Component
@Data
@Slf4j
public class SensitiveFilter {

    //替换符
    private static String REPLACEMENT = "***";

    //初始化根节点
    private TrieNode rootNode = new TrieNode();

    //当容器实例化这个Bean(sensitiveFilter)以后,在调用它的构造器之后, 这个方法就被调用
    @PostConstruct
    public void init() {
        try (InputStream is = this.getClass().getClassLoader().getResourceAsStream("sensitive-words.txt");
             BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        ) {
            String keyword;
            while ((keyword = reader.readLine()) != null) {
                // 添加到前缀树
                this.addKeyword(keyword);
            }
        } catch (Exception e) {
            log.error("加载敏感词文件失败: " + e.getMessage());
        }
    }

    //讲一个敏感词添加到前缀树当中去
    private void addKeyword(String keyword) {
        TrieNode tempNode = rootNode;
        for (int i = 0; i < keyword.length(); i++) {
            char c = keyword.charAt(i);
            TrieNode subNode = tempNode.getSubNode(c);
            //如果树中没有这个节点
            if (subNode == null) {
                //初始化子节点
                subNode = new TrieNode();
                tempNode.addSubNode(c, subNode);
            }
            //进入下一节点也就是子节点，进入下一轮循环
            tempNode = subNode;
            //设置结束标志
            if (i == keyword.length() - 1) {
                tempNode.setSiKetWordEnd(true);
            }
        }
    }

    /**
     * @Description: 过滤敏感词, 参数是传入的字符串，返回过滤后的文本
     * @Date: 2023/3/11 14:45
     */
    //代码比原代码多了一个判断：是否匹配到了敏感词的前缀，之前的代码匹配不到第一个敏感词
    public String filter(String text) {
        if (StringUtils.isBlank(text)) {
            return null;
        }
        TrieNode tempNode = rootNode;
        int begin = 0;
        int position = 0;
        StringBuilder sb = new StringBuilder();
        boolean matched = false; // 当前是否匹配到了敏感词前缀 false:匹配到了，true未匹配到
        while (position < text.length()) {
            char c = text.charAt(position);
            if (isSymbol(c)) {
                if (tempNode == rootNode) {
                    sb.append(c);
                    begin++;
                }
                position++;
                continue;
            }
            tempNode = tempNode.getSubNode(c);
            if (tempNode == null) {
                if (!matched) { // 如果当前未匹配敏感词前缀，则将当前字符加入结果
                    sb.append(text.charAt(begin));
                    begin++;
                    position = begin;
                } else { // 如果当前已匹配敏感词前缀，则继续匹配下一个字符
                    position++;
                }
                tempNode = rootNode;
                matched = false;
            } else if (tempNode.isSiKetWordEnd()) { // 匹配到了整个敏感词
                matched = true;
                sb.append(REPLACEMENT);
                position++;
                begin = position;
                tempNode = rootNode;
            } else { // 匹配到了敏感词前缀
                matched = true;
                position++;
            }
        }
        sb.append(text.substring(begin));
        return sb.toString();
    }
//    public String filter(String text) {
//        if (StringUtils.isBlank(text)) {
//            return null;
//        }
//
//        //声明三个指针 :1指向树的根节点， 2指向text的首位，3指向text 的首位，2是过滤词的首位，3是过滤词的末尾。
//        TrieNode tempNode = new TrieNode();
//        int begin = 0;
//        int position = 0;
//        //声明一个变量(返回值)
//        StringBuilder sb = new StringBuilder();
//
//        while (position < text.length()) {
//            char c = text.charAt(position);
//
//            //跳过符号
//            if(isSymbol(c)){
//                //若指针1处于根节点,将此符号计入结果 ,让指针2 向下走一步
//                if(tempNode == rootNode){
//                    sb.append(c);
//                    begin++;
//                }
//                //无论符号在开头还是中间,指针3都向下走一步
//                position++;
//                continue;
//            }
//            //检查下级节点
//            tempNode = tempNode.getSubNode(c);
//            if(tempNode == null){
//                //记录下begin的位置字符
//                sb.append(text.charAt(begin));
//                //树归位
//                tempNode = rootNode;
//                //2指针向前一步，3指针回调
//                begin++;
//                position = begin;
//            }else if(tempNode.isSiKetWordEnd()){
//                // 发现敏感词，将begin-position替换掉,2指针挪到3指针前面的一个位置，3指针同样
//                sb.append(REPLACEMENT);
//                begin = ++position;
//                //树归位
//                tempNode = rootNode;
//            } else{
//                //继续检查下一个字符
//                position++;
//            }
//        }
//        //将最后一批字符添加到sb  从2指针到text文本末尾
//        sb.append(text.substring(begin));
//        return sb.toString();
//    }

    //判断是否为符号
    private boolean isSymbol(Character s) {
        //是否为特殊符号， 0x2E80~0x9FFF是东亚文字范围
        return !CharUtils.isAsciiAlphanumeric(s) && (s < 0x2E80 || s > 0x9FFF);
    }

    //前缀树
    private class TrieNode {
        //敏感词结束的标识
        private boolean siKetWordEnd = false;

        //子节点(key--下级的节点字符，value--下级节点)
        private Map<Character, TrieNode> subNodes = new HashMap<>();


        public boolean isSiKetWordEnd() {
            return siKetWordEnd;
        }

        public void setSiKetWordEnd(boolean siKetWordEnd) {
            this.siKetWordEnd = siKetWordEnd;
        }

        //添加子节点的方法
        public void addSubNode(Character c, TrieNode node) {
            subNodes.put(c, node);
        }

        //获取子节点
        public TrieNode getSubNode(Character c) {
            return subNodes.get(c);
        }
    }
}
