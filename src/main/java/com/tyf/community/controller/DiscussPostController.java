package com.tyf.community.controller;

import com.tyf.community.entity.Comment;
import com.tyf.community.entity.DiscussPost;
import com.tyf.community.entity.Page;
import com.tyf.community.entity.User;
import com.tyf.community.service.CommentService;
import com.tyf.community.service.DiscussPostService;
import com.tyf.community.service.LikeService;
import com.tyf.community.service.UserService;
import com.tyf.community.util.CommunityConstant;
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
@RequestMapping("/discuss")
public class DiscussPostController implements CommunityConstant {
    @Autowired
    private DiscussPostService discussPostService;
    @Autowired
    private UserService userService;
    @Autowired
    private CommentService commentService;
    @Autowired
    private LikeService likeService;

    @Autowired
    private HostHolder hostHolder;

    @RequestMapping(path = "/add", method = RequestMethod.POST)
    @ResponseBody
    public String addDiscussPost(String title, String content) {
        User user = hostHolder.getUser();
        if(user == null){
           return CommunityUtil.getJSONString(403,"尚未登陆");
        }
        DiscussPost discussPost = new DiscussPost();
        discussPost.setUserId(user.getId());
        discussPost.setTitle(title);
        discussPost.setContent(content);
        discussPost.setCreateTime(new Date());
        discussPostService.addDiscussPost(discussPost);
        //报错的情况，将来统一处理
        return CommunityUtil.getJSONString(0,"发布成功");
    }

    /**
     * 帖子详情接口
     * @return
     */
    @RequestMapping(path = "/detail/{discussPostId}",method = RequestMethod.GET)
    public String findDiscussPostById(@PathVariable("discussPostId") int discussPostId, Model model, Page page){
        //帖子
        DiscussPost discussPost = discussPostService.findDiscussPostById(discussPostId);
        model.addAttribute("post",discussPost);
        //作者
        User user  = userService.findUserById(discussPost.getUserId());
        model.addAttribute("user",user);
        //赞
        long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, discussPostId);
        model.addAttribute("likeCount",likeCount);
        int likeStatus = hostHolder.getUser()== null ? 0 : likeService.findEntityLikeStatus(hostHolder.getUser().getId(),ENTITY_TYPE_POST,discussPostId);
        model.addAttribute("likeStatus",likeStatus);

        //评论分页信息
        page.setLimit(5);
        page.setPath("/discuss/detail/" + discussPostId);
        //也可以从评论里面取，但是既然有这个字段而且效率更高，为什么不用呢
        page.setRows(discussPost.getCommentCount());

        // 评论: 给帖子的评论
        // 回复: 给评论的评论
        // 评论列表
        List<Comment> commentList =
                commentService.findCommentsByEntity(ENTITY_TYPE_POST,discussPost.getId(),page.getOffset(),page.getLimit());
        //评论VO(viewObject)列表
        List<Map<String,Object>> commentVOList = new ArrayList<>();
        if(commentList != null) {
            for (Comment comment : commentList) {
                 //评论VO
                Map<String,Object> commentVO = new HashMap<>();
                //评论
                commentVO.put("comment",comment);
                //用户
                commentVO.put("user",userService.findUserById(comment.getUserId()));
                //点赞
                likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, comment.getId());
                commentVO.put("likeCount",likeCount);
                likeStatus = hostHolder.getUser()== null ? 0 : likeService.findEntityLikeStatus(hostHolder.getUser().getId(),ENTITY_TYPE_COMMENT,comment.getId());
                commentVO.put("likeStatus",likeStatus);

                //回复列表
                List<Comment> replyList = commentService.findCommentsByEntity(ENTITY_TYPE_COMMENT,comment.getId(),0,Integer.MAX_VALUE);
                //回复VO列表
                List<Map<String, Object>> replyVOList = new ArrayList<>();
                if(replyList != null){
                    for(Comment reply : replyList){
                        Map<String,Object> replyVO = new HashMap<>();
                        //回复
                        replyVO.put("reply",reply);
                        //用户
                        replyVO.put("user",userService.findUserById(reply.getUserId()));

                        //点赞
                        likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, reply.getId());
                        replyVO.put("likeCount",likeCount);
                        likeStatus = hostHolder.getUser()== null ? 0 : likeService.findEntityLikeStatus(hostHolder.getUser().getId(),ENTITY_TYPE_COMMENT,reply.getId());
                        replyVO.put("likeStatus",likeStatus);

                        //回复目标
                        User target = reply.getTargetId() == 0 ? null : userService.findUserById(reply.getTargetId());
                        replyVO.put("target",target);

                        replyVOList.add(replyVO);
                    }
                }
                commentVO.put("replys",replyVOList);

                //回复数量
                int replyCount = commentService.findCommentCount(ENTITY_TYPE_COMMENT,comment.getId());
                commentVO.put("replyCount",replyCount);

                commentVOList.add(commentVO);
            }
        }
        model.addAttribute("comments",commentVOList);

        return "/site/discuss-detail";
    }



}
