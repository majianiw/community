package com.nowcoder.community.controller;

import com.alibaba.fastjson.JSONObject;
import com.nowcoder.community.annotation.LoginRequired;
import com.nowcoder.community.entity.Message;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.MessageService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.HtmlUtils;

import java.util.*;

/**
 * @Date: create in 11:31 2023/3/14
 * @describe: 私信列表
 */
@Controller
public class MessageController implements CommunityConstant {

    @Autowired
    private MessageService messageService;

    @Autowired
    HostHolder hostHolder;

    @Autowired
    UserService userService;

    //私信列表
    @GetMapping("/letter/list")
    @LoginRequired
    public String getLetterList(Model model, Page page) {
        User user = hostHolder.getUser();
        //分页
        page.setLimit(5);
        page.setPath("/letter/list");
        page.setRows(messageService.findConversationCount(user.getId()));
        //会话列表，得到与用户对话的所有人
        List<Message> conversationList = messageService.findConversations(user.getId(), page.getOffset(), page.getLimit());
        //定义一个list 存放所有的会话
        List<Map<String, Object>> conversations = new ArrayList<>();
        if (conversationList != null) {
            //遍历
            for (Message message : conversationList) {
                //定义一个map来存放一个会话的数据
                Map<String, Object> map = new HashMap<>();
                //查询私信
                map.put("conversation", message);
                //查询与某一个用户消息数
                map.put("letterCount", messageService.findLetterCount(message.getConversationId()));
                //查询于某个用户的未读消息数
                map.put("unreadCount", messageService.findLetterUnreadCount(user.getId(), message.getConversationId()));
                //查询是在于哪个用户对话  用于前端查询用户头像
                int targetId = user.getId() == message.getFromId() ? message.getToId() : message.getFromId();
                map.put("target", userService.findUserById(targetId));
                //把与某一个用户的会员放入总的列表中
                conversations.add(map);
            }
        }
        model.addAttribute("conversations", conversations);

        //查询所有的未读消息数量
        int letterUnReadCount = messageService.findLetterUnreadCount(user.getId(), null);
        model.addAttribute("letterUnreadCount", letterUnReadCount);
        int noticeUnreadCount = messageService.findNoticeUnreadCount(user.getId(), null);
        model.addAttribute("noticeUnreadCount", noticeUnreadCount);
        return "/site/letter";
    }

    //私信详情页面
    @GetMapping("/letter/detail/{conversationId}")
    public String getLetterDetail(@PathVariable("conversationId") String conversationId, Page page, Model model) {
        //分页
        page.setLimit(5);
        page.setPath("/letter/detail/" + conversationId);
        page.setRows(messageService.findLetterCount(conversationId));

        //私信列表
        List<Message> letterList = messageService.findLetters(conversationId, page.getOffset(), page.getLimit());
        List<Map<String, Object>> letters = new ArrayList<>();
        if (letters != null) {
            for (Message message : letterList) {
                Map<String, Object> map = new HashMap<>();
                map.put("letter", message);
                map.put("fromUser", userService.findUserById(message.getFromId()));
                letters.add(map);
            }
        }
        model.addAttribute("letters", letters);
        //获取与用户对话的用户（私信目标）
        model.addAttribute("target", getLetterTarget(conversationId));

        //设置已读
        List<Integer> ids = getLetterIds(letterList);
        if (!ids.isEmpty()) {
            messageService.readMessage(ids);
        }
        return "/site/letter-detail";
    }

    //获取与用户对话的用户
    private User getLetterTarget(String conversationId) {
        String[] ids = conversationId.split("_");
        int id0 = Integer.parseInt(ids[0]);
        int id1 = Integer.parseInt(ids[1]);

        if (hostHolder.getUser().getId() == id0) {
            return userService.findUserById(id1);
        }
        return userService.findUserById(id0);
    }

    //把未读消息变为已读
    private List<Integer> getLetterIds(List<Message> letterList) {
        List<Integer> ids = new ArrayList<>();
        //遍历一个会话的所有消息,当前用户要是接受者 并且确定消息未读就添加到ids
        if (letterList != null) {
            for (Message message : letterList) {
                if (message.getToId() == hostHolder.getUser().getId() && message.getStatus() == 0) {
                    ids.add(message.getId());
                }
            }
        }
        return ids;
    }

    //发送私信
    @PostMapping("/letter/send")
    @ResponseBody
    public String sendLetter(String toName, String content) {
        User target = userService.findUserByName(toName);
        if (target == null) {
            return CommunityUtil.getJSONString(1, "目标用户不存在");
        }
        Message message = new Message();
        message.setFromId(hostHolder.getUser().getId());
        message.setToId(target.getId());
        if (message.getFromId() < message.getToId()) {
            message.setConversationId(message.getFromId() + "_" + message.getToId());
        } else {
            message.setConversationId(message.getToId() + "_" + message.getFromId());
        }
        message.setContent(content);
        message.setCreateTime(new Date());
        messageService.addMessage(message);
        return CommunityUtil.getJSONString(0);
    }

    //系统通知的列表
    @GetMapping("/notice/list")
    public String getNoticeList(Model model) {
        User user = hostHolder.getUser();

        //查询评论类的通知
        Message message = messageService.findLatestNotice(user.getId(), TOPIC_COMMENT);
        Map<String, Object> messageVO;
        if (message != null) {
            messageVO = new HashMap<>();
            if (message != null) {
                messageVO.put("message", message);

                String content = HtmlUtils.htmlUnescape(message.getContent());
                Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);

                messageVO.put("user", userService.findUserById((Integer) data.get("userId")));
                messageVO.put("entityType", data.get("entityType"));
                messageVO.put("entityId", data.get("entityId"));
                messageVO.put("postId", data.get("postId"));

                int count = messageService.findNoticeCount(user.getId(), TOPIC_COMMENT);
                messageVO.put("count", count);

                int unread = messageService.findNoticeUnreadCount(user.getId(), TOPIC_COMMENT);
                messageVO.put("unread", unread);
            }
            model.addAttribute("commentNotice", messageVO);
        }
        //查询点赞类的通知
        message = messageService.findLatestNotice(user.getId(), TOPIC_LIKE);
        messageVO = new HashMap<>();
        if (message != null) {
            if (message != null) {
                messageVO.put("message", message);

                String content = HtmlUtils.htmlUnescape(message.getContent());
                HashMap<String, Object> data = JSONObject.parseObject(content, HashMap.class);

                messageVO.put("user", userService.findUserById((Integer) data.get("userId")));
                messageVO.put("entityType", data.get("entityType"));
                messageVO.put("entityId", data.get("entityId"));
                messageVO.put("postId", data.get("postId"));

                int count = messageService.findNoticeCount(user.getId(), TOPIC_LIKE);
                messageVO.put("count", count);

                int unread = messageService.findNoticeUnreadCount(user.getId(), TOPIC_LIKE);
                messageVO.put("unread", unread);
            }
            model.addAttribute("likeNotice", messageVO);
        }
        //查询关注类的通知
        message = messageService.findLatestNotice(user.getId(), TOPIC_FOLLOW);
        messageVO = new HashMap<>();
        if (message != null) {
            if (message != null) {
                messageVO.put("message", message);

                String content = HtmlUtils.htmlUnescape(message.getContent());
                HashMap<String, Object> data = JSONObject.parseObject(content, HashMap.class);

                messageVO.put("user", userService.findUserById((Integer) data.get("userId")));
                messageVO.put("entityType", data.get("entityType"));
                messageVO.put("entityId", data.get("entityId"));

                int count = messageService.findNoticeCount(user.getId(), TOPIC_FOLLOW);
                messageVO.put("count", count);

                int unread = messageService.findNoticeUnreadCount(user.getId(), TOPIC_FOLLOW);
                messageVO.put("unread", unread);
            }
            model.addAttribute("followNotice", messageVO);
        }
        //查询未读消息数量 私信和系统通知

        int letterUnreadCount = messageService.findLetterUnreadCount(user.getId(), null);
        model.addAttribute("letterUnreadCount", letterUnreadCount);
        int noticeUnreadCount = messageService.findNoticeUnreadCount(user.getId(), null);
        model.addAttribute("noticeUnreadCount", noticeUnreadCount);

        return "/site/notice";
    }

    //从通知界面  获取详情页
    @GetMapping("/notice/detail/{topic}")
    public String getNoticeDetail(@PathVariable("topic") String topic, Page page, Model model) {
        User user = hostHolder.getUser();
        page.setLimit(5);
        page.setPath("/notice/detail/" + topic);
        page.setRows(messageService.findNoticeCount(user.getId(), topic));

        List<Message> noticeList = messageService.findNotices(user.getId(), topic, page.getOffset(), page.getLimit());
        List<Map<String, Object>> noticeVoList = new ArrayList<>();
        if (noticeList != null) {
            for (Message notice : noticeList) {
                Map<String, Object> map = new HashMap<>();
                //通知
                map.put("notice", notice);
                //内容
                String content = HtmlUtils.htmlUnescape(notice.getContent());
                Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);
                map.put("user", userService.findUserById((int) data.get("userId")));
                map.put("entityType", data.get("entityType"));
                map.put("entityId", data.get("entityId"));
                map.put("postId", data.get("postId"));
                //通知的作者
                map.put("fromUser", userService.findUserById(notice.getFromId()));
                noticeVoList.add(map);
            }
        }
        model.addAttribute("notices", noticeVoList);

        //设置已读
        List<Integer> ids = getLetterIds(noticeList);
        if (!ids.isEmpty()) {
            messageService.readMessage(ids);
        }

        return "/site/notice-detail";
    }
}









