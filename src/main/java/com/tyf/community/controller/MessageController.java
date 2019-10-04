package com.tyf.community.controller;

import com.sun.org.apache.xpath.internal.operations.Mod;
import com.tyf.community.entity.Message;
import com.tyf.community.entity.Page;
import com.tyf.community.entity.User;
import com.tyf.community.service.MessageService;
import com.tyf.community.service.UserService;
import com.tyf.community.util.CommunityUtil;
import com.tyf.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

@Controller
public class MessageController {
    @Autowired
    private MessageService messageService;
    @Autowired
    private HostHolder hostHolder;
    @Autowired
    private UserService userService;

    /**
     * 私信列表
     * @param model
     * @param page
     * @return
     */
    @RequestMapping(path = "/letter/list",method = RequestMethod.GET)
    public String getLetterList(Model model, Page page){
        User user = hostHolder.getUser();
        //分页
        page.setLimit(5);
        page.setPath("/letter/list");
        page.setRows(messageService.findConversationCount(user.getId()));

        //会话列表
        List<Message> conversationList = messageService.findConversations(user.getId(),page.getOffset(),page.getLimit());
        List<Map<String,Object>> conversations = new ArrayList<>();
        if(conversationList != null){
            for(Message message : conversationList){
                Map<String,Object> map = new HashMap<>();
                map.put("conversation",message);
                map.put("letterCount",messageService.findLetterCount(message.getConversationId()));
                int unreadCount = messageService.findLetterUnreadCount(user.getId(),message.getConversationId());
                map.put("unreadCount",unreadCount);
                int targetId = user.getId() == message.getFromId() ? message.getToId() : message.getFromId();
                map.put("target",userService.findUserById(targetId));

                conversations.add(map);
            }
        }
        model.addAttribute("conversations",conversations);

        //查询用户总共未读消息数量
        int letterUnreadCount = messageService.findLetterUnreadCount(user.getId(),null);
        model.addAttribute("letterUnreadCount",letterUnreadCount);

        return "/site/letter";
    }

    /**
     * 列表私信详情接口
     * @param conversationId
     * @param page
     * @param model
     * @return
     */
    @RequestMapping(path = "/letter/detail/{conversationId}",method = RequestMethod.GET)
    public String sendLetterDetail(@PathVariable("conversationId") String conversationId, Page page, Model model){
        // 分页信息
        page.setLimit(5);
        page.setPath("/letter/detail/" + conversationId);
        page.setRows(messageService.findLetterCount(conversationId));

        //私信列表
        List<Message> letterList = messageService.findLetters(conversationId,page.getOffset(),page.getLimit());
        List<Map<String,Object>> letters = new ArrayList<>();
        if(letterList != null){
            for(Message message : letterList){
                Map<String,Object> map = new HashMap<>();
                map.put("letter",message);
                map.put("fromUser",userService.findUserById(message.getFromId()));
                letters.add(map);
            }
        }
        model.addAttribute("letters",letters);

        //私信目标
        model.addAttribute("target",getLetterTarget(conversationId));

        //设置已读
        List<Integer> ids = getLetterIds(letterList);
        if(!ids.isEmpty()){
            messageService.readMessage(ids);
        }

        return "/site/letter-detail";
    }

    /**
     * 得到私信目标
     * @param conversationId
     * @return
     */
    private User getLetterTarget(String conversationId){
        String[] ids = conversationId.split("_");
        int id0 = Integer.parseInt(ids[0]);
        int id1 = Integer.parseInt(ids[1]);
        if(hostHolder.getUser().getId() == id0){
            return userService.findUserById(id1);
        }else{
            return userService.findUserById(id0);
        }
    }

    /**
     * 查找所有未读的信息，使得调用它的方法可以将未读改为已读
     * @param letterList
     * @return
     */
    private List<Integer> getLetterIds(List<Message> letterList) {
        List<Integer> ids = new ArrayList<>();

        if (letterList != null) {
            for (Message message : letterList) {
                if (hostHolder.getUser().getId() == message.getToId() && message.getStatus() == 0) {
                    ids.add(message.getId());
                }
            }
        }

        return ids;
    }

    /**
     * 发送私信
     * @param toName
     * @param content
     * @return
     */
    @RequestMapping(path = "/letter/send",method = RequestMethod.POST)
    @ResponseBody
    public String sendLetter(String toName, String content){
         User target = userService.findUserByName(toName);
         if(target == null){
             return CommunityUtil.getJSONString(1,"目标用户不存在");
         }
         Message message = new Message();
         message.setFromId(hostHolder.getUser().getId());
         message.setToId(target.getId());
         if(message.getFromId() < message.getToId()){
             message.setConversationId(message.getFromId() + "_" + message.getToId());
         }else{
             message.setConversationId(message.getToId() + "_" + message.getFromId());
         }
         message.setContent(content);
         message.setCreateTime(new Date());
         messageService.addMessage(message);

         return CommunityUtil.getJSONString(0);
    }

    /**
     * 删除信息
     * @param id
     * @return
     */
    @RequestMapping(path = "/letter/delete",method = RequestMethod.POST)
    @ResponseBody
    public String deleteLetter(int id){
        System.out.println(id);
         if(messageService.deleteMessage(id)){
             return CommunityUtil.getJSONString(0);
         }else{
             return CommunityUtil.getJSONString(1,"删除失败");
         }
    }

}
