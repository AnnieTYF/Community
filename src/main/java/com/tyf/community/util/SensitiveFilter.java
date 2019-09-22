package com.tyf.community.util;

import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;


@Component
public class SensitiveFilter {

    private static final Logger logger = LoggerFactory.getLogger(SensitiveFilter.class);

    private static final String REPLACEMENT = "***";

    //初始化根节点
    private TrieNode rootNode = new TrieNode();
    //这是一个初始化方法，PostConstruct在构造函数之后执行,init()方法之前执行
    @PostConstruct
    public void init(){
        try(
        //读取敏感词文件
        //如果把方法放在try的括号里，try在执行后就会自动关闭输入流
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("sensitive-words.txt");
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        ){
            String keyword;
            while((keyword = reader.readLine()) != null){
                //添加到前缀树中
                this.addKeyWord(keyword);
            }
        }catch (IOException e){
         logger.error("加载敏感词文件失败：" + e.getMessage());
        }

    }

    /**
     * 将一个敏感词加到前缀树中
     */
    private void addKeyWord(String keyword){
        TrieNode tempNode = rootNode;
        for(int i = 0; i<keyword.length() ;i++){
            char c = keyword.charAt(i);
            TrieNode subNode = tempNode.getSubNode(c);

            if(subNode == null){
                //初始化子节点
                subNode = new TrieNode();
                tempNode.addSubNode(c,subNode);
            }
            //指向子节点，进入下一轮循环
            tempNode = subNode;

            //设置结束标识
            if(i == keyword.length() - 1){
                tempNode.setKeyWordEnd(true);
            }
        }
    }


    /**
     * 滤敏感词方法
     * @param text 有待过滤的文本
     * @return 返回替换掉后的字符串
     */
    public String filter(String text){
        if(StringUtils.isBlank(text)){
            return null;
        }
        //指针1 指向树
        TrieNode tempNode = rootNode;
        //指针2
        int begin = 0;
        //指针3
        int position = 0;
        //结果
        StringBuilder stringBuilder = new StringBuilder();

        while (begin < text.length()){
            char c = text.charAt(position);
            //过滤掉符号
            if(isSymbol(c)){
                //若指针1处于根节点，将此符号计入结果，让指针2向下走一步
                if (tempNode == rootNode){
                    stringBuilder.append(c);
                    begin ++ ;
                }
                //无论符号在开头或中间，指针3都向下走一步
                position ++;
                continue;
            }
            //检测下节点
            tempNode = tempNode.getSubNode(c);

            if(tempNode == null){
                //没有敏感词，以begin开头的字符串不是敏感词
               stringBuilder.append(text.charAt(begin));
               //进入下一个位置
               position = ++begin;
               //重新指向根节点
                tempNode = rootNode;
            }else if(tempNode.isKeyWordEnd()){
                //发现敏感词，将bigin到position的字符替换掉
                stringBuilder.append(REPLACEMENT);
                //进入下一个位置
                begin = ++position;
                //重新指向根节点
                tempNode = rootNode;
            }else{
                //检查下一个字符
                if(position < text.length() - 1) {
                    position++;
                }
            }
        }
        //将最后一批字符计入结束
        stringBuilder.append(text.substring(begin));

        return stringBuilder.toString();
    }

    /**
     * 判断是否为符号
     */
    private boolean isSymbol(Character c){
        // 0x2E80~0x9FFF 是东亚文字范围
        return !CharUtils.isAsciiAlphanumeric(c) && (c < 0x2E80 || c > 0x9FFF);
    }

    /**
     * 定义内部类，实现前缀树数据结构
     */
    private class TrieNode{

        //关键词结束的标志
        private boolean isKeyWordEnd = false;

        //子节点(key是下级字符，value是下级节点)
        private Map<Character,TrieNode> subNode = new HashMap<>();

        public boolean isKeyWordEnd() {
            return isKeyWordEnd;
        }

        public void setKeyWordEnd(boolean keyWordEnd) {
            isKeyWordEnd = keyWordEnd;
        }
        //添加子节点
        public void addSubNode(Character c, TrieNode node){
            subNode.put(c,node);
        }

        public TrieNode getSubNode(Character c){
            return subNode.get(c);
        }

    }
}
